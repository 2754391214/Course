package com.lyw.cloudChoose.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.dto.ValidationResult;
import com.lyw.cloudChoose.factory.DropStrategyFactory;
import com.lyw.cloudChoose.factory.EnrollmentStrategyFactory;
import com.lyw.cloudChoose.feign.CourseFeignService;
import com.lyw.cloudChoose.mapper.EnrollmentStrategiesDao;
import com.lyw.cloudChoose.mapper.EnrollmentsDao;
import com.lyw.cloudChoose.service.EnrollmentValidationService;
import com.lyw.cloudChoose.service.EnrollmentsBo;
import com.lyw.cloudChoose.strategy.drop.DropStrategy;
import com.lyw.cloudChoose.strategy.enrollment.EnrollmentStrategy;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.FeignResponseHelper;
import com.lyw.commonUtil.util.reidsCache.StringCache.CacheConfig;
import com.lyw.commonUtil.util.reidsCache.StringCache.DistributedCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class EnrollmentsImpl extends ServiceImpl<EnrollmentsDao, EnrollmentsVo> implements EnrollmentsBo {

    @Resource
    private EnrollmentsDao enrollmentsDao;

    @Resource
    private EnrollmentStrategiesDao enrollmentStrategiesDao;

    @Resource
    private CourseFeignService courseFeignService;

    @Resource
    private EnrollmentStrategyFactory strategyFactory;
    @Resource
    private EnrollmentValidationService enrollmentValidationService;
    @Resource
    private DropStrategyFactory dropStrategyFactory;
    @Resource
    private Executor enrollmentExecutor;
    @Resource
    private DistributedCacheHelper distributedCacheHelper;

    private EnrollmentStrategiesVo createDefaultStrategy(Long courseId) {
        // 创建默认的先到先得策略
        return new EnrollmentStrategiesVo()
                .setCourseId(courseId)
                .setStrategyType("FIRST_COME")
                .setDropStrategyType("IMMEDIATE_DROP")
                .setAutoWaitlist(true)
                .setAllowAudit(true)
                .setConflictCheck(true)
                .setPrerequisiteCheck(false);
    }

    // 课程信息缓存
    private CoursesDto getCachedCourse(Long courseId) {
        return distributedCacheHelper.getOrLoad(
                courseId.toString(),
                new CacheConfig(
                        RedisKeyConstant.COURSE_INFO,
                        RedisKeyConstant.LOCK_COURSE_INFO,
                        Duration.ofHours(24)
                ),
                () -> FeignResponseHelper.convert(
                        courseFeignService.searchDetail(courseId), CoursesDto.class),
                CoursesDto.class
        );
    }

    // 策略信息缓存
    private EnrollmentStrategiesVo getCachedStrategy(Long courseId) {
        return distributedCacheHelper.getOrLoad(
                courseId.toString(),
                new CacheConfig(
                        RedisKeyConstant.ENROLLMENT_STRATEGY,
                        RedisKeyConstant.LOCK_ENROLLMENT_STRATEGY,
                        Duration.ofHours(24)
                ),
                () -> {
                    EnrollmentStrategiesVo strategy = enrollmentStrategiesDao.selectOne(
                            new LambdaQueryWrapper<EnrollmentStrategiesVo>()
                                    .eq(EnrollmentStrategiesVo::getCourseId, courseId));
                    return ObjectUtil.isNotEmpty(strategy) ? strategy : createDefaultStrategy(courseId);
                },
                EnrollmentStrategiesVo.class
        );
    }

    @Override
    public CourseResponseWrapper enroll(EnrollmentsDto dto) {
        Long courseId = dto.getCourseId();
        Long studentId = dto.getStudentId();
        try {
            log.info("开始选课流程: studentId={}, courseId={}", studentId, courseId);

            // 并行获取课程、策略信息、黑名单信息
            CompletableFuture<CoursesDto> courseFuture = CompletableFuture.supplyAsync(
                    () -> getCachedCourse(courseId), enrollmentExecutor);

            CompletableFuture<EnrollmentStrategiesVo> strategyFuture = CompletableFuture.supplyAsync(
                    () -> getCachedStrategy(courseId), enrollmentExecutor);

            // 等待并行任务完成
            CoursesDto course = courseFuture.get(2, TimeUnit.SECONDS);
            EnrollmentStrategiesVo strategy = strategyFuture.get(2, TimeUnit.SECONDS);

            // 完整验证
            ValidationResult validationResult = enrollmentValidationService.validateEnrollment(dto, course, strategy);

            if (!validationResult.isValid()) {
                String rejectionReasons = String.join("; ", validationResult.getRejectionReasons());
                return CourseResponseWrapper.getFailed(rejectionReasons);
            }

            // 根据策略类型执行选课逻辑
            EnrollmentStrategy enrollmentStrategy = strategyFactory.getStrategy(strategy.getStrategyType());
            return enrollmentStrategy.enroll(dto, course, strategy);

        } catch (TimeoutException e) {
            log.warn("选课操作超时: studentId={}, courseId={}", studentId, courseId);
            return CourseResponseWrapper.getFailed("系统繁忙，请稍后重试");
        } catch (Exception e) {
            return CourseResponseWrapper.getFailed("系统异常");
        }
    }

    @Override
    public CourseResponseWrapper drop(Long enrollmentId, EnrollmentsDto dto) {
        // 获取选课记录
        EnrollmentsVo enrollment = enrollmentsDao.selectById(enrollmentId);
        if (ObjectUtil.isEmpty(enrollment)) {
            return CourseResponseWrapper.getFailed("选课记录不存在");
        }

        // 权限验证
        if (!enrollment.getStudentId().equals(dto.getStudentId())) {
            return CourseResponseWrapper.getFailed("无权操作此选课记录");
        }

        // 获取课程信息和策略
        CompletableFuture<CoursesDto> courseFuture = CompletableFuture.supplyAsync(
                () -> getCachedCourse(enrollment.getCourseId()),enrollmentExecutor);

        CompletableFuture<EnrollmentStrategiesVo> strategyFuture = CompletableFuture.supplyAsync(
                () -> getCachedStrategy(enrollment.getCourseId()),enrollmentExecutor);

        try {

            // 等待并行任务完成
            CoursesDto course = courseFuture.get(2, TimeUnit.SECONDS);
            EnrollmentStrategiesVo strategy = strategyFuture.get(2, TimeUnit.SECONDS);

            // 并行进行完整退选验证
            ValidationResult validationResult = enrollmentValidationService.validateDrop(dto, course, strategy);
            if (!validationResult.isValid()) {
                String rejectionReasons = String.join("; ", validationResult.getRejectionReasons());
                return CourseResponseWrapper.getFailed(rejectionReasons);
            }

            // 根据策略执行退选
            DropStrategy dropStrategy = dropStrategyFactory.getStrategy(strategy.getDropStrategyType());
            return dropStrategy.drop(enrollment, course, strategy);

        } catch (TimeoutException e) {
            log.warn("选课操作超时: studentId={}, courseId={}", dto.getStudentId(), dto.getCourseId());
            return CourseResponseWrapper.getFailed("系统繁忙，请稍后重试");
        } catch (Exception e) {
            log.error("并行处理异常", e);
            return CourseResponseWrapper.getFailed("系统异常");
        }
    }

    @Override
    public CourseResponseWrapper getStudentEnrollments(Long studentId, EnrollmentsDto dto) {
        try {
            List<EnrollmentsVo> enrollments = enrollmentsDao.selectList(
                    new LambdaQueryWrapper<EnrollmentsVo>()
                            .eq(EnrollmentsVo::getStudentId, studentId));

            return CourseResponseWrapper.getSuccess("查询成功", enrollments);
        } catch (Exception e) {
            log.error("查询学生选课列表异常: studentId={}", studentId, e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        }
    }

    @Override
    public CourseResponseWrapper getCourseEnrollments(Long courseId, EnrollmentsDto dto) {
        try {
            List<EnrollmentsVo> enrollments = enrollmentsDao.selectList(
                    new LambdaQueryWrapper<EnrollmentsVo>()
                            .eq(EnrollmentsVo::getCourseId, courseId));

            return CourseResponseWrapper.getSuccess("查询成功", enrollments);
        } catch (Exception e) {
            log.error("查询课程选课列表异常: courseId={}", courseId, e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        }
    }
}