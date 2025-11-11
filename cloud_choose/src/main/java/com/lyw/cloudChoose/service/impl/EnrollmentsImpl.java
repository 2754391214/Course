package com.lyw.cloudChoose.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudChoose.dto.ValidationResult;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.factory.DropStrategyFactory;
import com.lyw.cloudChoose.factory.EnrollmentStrategyFactory;
import com.lyw.cloudChoose.feign.CourseFeignService;
import com.lyw.cloudChoose.mapper.EnrollmentStrategiesDao;
import com.lyw.cloudChoose.mapper.EnrollmentsDao;
import com.lyw.cloudChoose.mapper.WaitlistsDao;
import com.lyw.cloudChoose.service.*;
import com.lyw.cloudChoose.strategy.drop.DropStrategy;
import com.lyw.cloudChoose.strategy.enrollment.EnrollmentStrategy;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.cloudChoose.vo.WaitlistsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.FeignResponseHelper;
import com.lyw.commonUtil.util.CurUserUtil;
import com.lyw.commonUtil.util.RedissLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnrollmentsImpl extends ServiceImpl<EnrollmentsDao, EnrollmentsVo> implements EnrollmentsBo {

    @Resource
    private EnrollmentsDao enrollmentsDao;

    @Resource
    private EnrollmentStrategiesDao enrollmentStrategiesDao;

    @Resource
    private WaitlistsDao waitlistsDao;

    @Resource
    private CourseFeignService courseFeignService;

    @Resource
    private EnrollmentStrategyFactory strategyFactory;
    @Resource
    private EnrollmentValidationService enrollmentValidationService;
    @Resource
    private RedissLockUtil redissLockUtil;
    @Resource
    private EnrollmentTransationBo enrollmentTransationBo;
    @Resource
    private DropStrategyFactory dropStrategyFactory;
    @Resource
    private HeatEventPublisher heatEventPublisher;
    @Resource
    private UserBehaviorProducerService userBehaviorProducerService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private Executor enrollmentExecutor;
    private void validateEnrollmentRequest(EnrollmentsDto enrollments) {
        if (ObjectUtil.isEmpty(enrollments.getStudentId())) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        if (ObjectUtil.isEmpty(enrollments.getCourseId())) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
    }

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
        String cacheKey = "course:" + courseId;
        try {
            CoursesDto course = (CoursesDto) redisTemplate.opsForValue().get(cacheKey);
            if (ObjectUtil.isEmpty(course)) {
                course = FeignResponseHelper.convert(
                        courseFeignService.searchDetail(courseId), CoursesDto.class);
                if (ObjectUtil.isNotEmpty(course)) {
                    redisTemplate.opsForValue().set(cacheKey, course, Duration.ofMinutes(10));
                }
            }
            return course;
        } catch (Exception e) {
            log.warn("课程缓存获取失败，降级到直接查询: courseId={}", courseId, e);
            return FeignResponseHelper.convert(
                    courseFeignService.searchDetail(courseId), CoursesDto.class);
        }
    }
    // 策略信息缓存
    private EnrollmentStrategiesVo getCachedStrategy(Long courseId) {
        String cacheKey = "enrollment_strategy:" + courseId;
        try {
            EnrollmentStrategiesVo strategy = (EnrollmentStrategiesVo) redisTemplate.opsForValue().get(cacheKey);
            if (ObjectUtil.isEmpty(strategy)) {
                strategy = enrollmentStrategiesDao.selectOne(
                        new LambdaQueryWrapper<EnrollmentStrategiesVo>()
                                .eq(EnrollmentStrategiesVo::getCourseId, courseId));
                if (ObjectUtil.isEmpty(strategy)) {
                    strategy = createDefaultStrategy(courseId);
                }
                redisTemplate.opsForValue().set(cacheKey, strategy, Duration.ofMinutes(30));
            }
            return strategy;
        } catch (Exception e) {
            log.warn("策略缓存获取失败，降级到直接查询: courseId={}", courseId, e);
            return enrollmentStrategiesDao.selectOne(
                    new LambdaQueryWrapper<EnrollmentStrategiesVo>()
                            .eq(EnrollmentStrategiesVo::getCourseId, courseId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper enroll(EnrollmentsDto dto) {
        // 基础验证
        validateEnrollmentRequest(dto);

        // 并行获取课程和策略信息
        CompletableFuture<CoursesDto> courseFuture = CompletableFuture.supplyAsync(
                () -> getCachedCourse(dto.getCourseId()),enrollmentExecutor);

        CompletableFuture<EnrollmentStrategiesVo> strategyFuture = CompletableFuture.supplyAsync(
                () -> getCachedStrategy(dto.getCourseId()),enrollmentExecutor);

        try {

            // 等待并行任务完成
            CoursesDto course = courseFuture.get(2, TimeUnit.SECONDS);
            EnrollmentStrategiesVo strategy = strategyFuture.get(1, TimeUnit.SECONDS);
            if (ObjectUtil.isEmpty(course)) {
                return CourseResponseWrapper.getFailed("课程不存在");
            }

            // 并行进行完整验证
            ValidationResult validationResult = enrollmentValidationService.validateEnrollment(dto, course, strategy);
            if (!validationResult.isValid()) {
                String rejectionReasons = String.join("; ", validationResult.getRejectionReasons());
                return CourseResponseWrapper.getFailed(rejectionReasons);
            }

            // 根据策略类型执行选课逻辑
            EnrollmentStrategy enrollmentStrategy = strategyFactory.getStrategy(strategy.getStrategyType());
            CourseResponseWrapper result = enrollmentStrategy.enroll(dto, course, strategy);

            // 保存选课记录
            if (result.isSuccess() && result.getData() instanceof EnrollmentsVo) {
                EnrollmentsVo enrollment = (EnrollmentsVo) result.getData();
                enrollment.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
                enrollment.setCruAndLuu(CurUserUtil.getUserCode());
                enrollmentsDao.insert(enrollment);

                // 创建本地事务记录
                String transactionId = enrollmentTransationBo.createEnrollmentTransaction(dto, enrollment);

                // 异步调用课程服务增加选课人数（避免长事务）
                enrollmentTransationBo.asyncIncrementCourseEnrollment(course.getId(), transactionId);

                // 发送选课热度事件到排行榜模块
                sendEnrollmentHeatEvent(course.getId(), dto.getStudentId(), course.getTeacherId());

                // 发送用户行为消息到推荐模块
                log.info("选课成功: enrollmentId={}, studentId={}, courseId={}",
                        enrollment.getId(), enrollment.getStudentId(), enrollment.getCourseId());

            } else if (result.isSuccess() && result.getData() instanceof WaitlistsVo) {
                WaitlistsVo waitlist = (WaitlistsVo) result.getData();
                waitlist.setPosition(1);
                waitlist.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
                waitlist.setCruAndLuu(CurUserUtil.getUserCode());
                waitlistsDao.insert(waitlist);

                log.info("加入等待列表成功: waitlistId={}, studentId={}, courseId={}",
                        waitlist.getId(), waitlist.getStudentId(), waitlist.getCourseId());
            }

            log.info("选课处理完成: studentId={}, courseId={}, success={}",
                    dto.getStudentId(), dto.getCourseId(), result.isSuccess());

            return result;

        } catch (TimeoutException e) {
            log.warn("选课操作超时: studentId={}, courseId={}", dto.getStudentId(), dto.getCourseId());
            return CourseResponseWrapper.getFailed("系统繁忙，请稍后重试");
        } catch (Exception e) {
            log.error("并行处理异常", e);
            return CourseResponseWrapper.getFailed("系统异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
            EnrollmentStrategiesVo strategy = strategyFuture.get(1, TimeUnit.SECONDS);
            if (ObjectUtil.isEmpty(course)) {
                return CourseResponseWrapper.getFailed("课程不存在");
            }

            // 并行进行完整退选验证
            ValidationResult validationResult = enrollmentValidationService.validateDrop(dto, course, strategy);
            if (!validationResult.isValid()) {
                String rejectionReasons = String.join("; ", validationResult.getRejectionReasons());
                return CourseResponseWrapper.getFailed(rejectionReasons);
            }

            // 根据策略执行退选
            DropStrategy dropStrategy = dropStrategyFactory.getStrategy(strategy.getDropStrategyType());
            CourseResponseWrapper result = dropStrategy.drop(enrollment, course, strategy);

            // 更新选课记录
            if (result.isSuccess() && result.getData() instanceof EnrollmentsVo) {
                EnrollmentsVo updatedEnrollment = (EnrollmentsVo) result.getData();
                enrollmentsDao.deleteById(updatedEnrollment.getId());

                // 创建退选事务记录
                String transactionId = enrollmentTransationBo.createDropTransaction(dto, updatedEnrollment);

                // 异步调用课程服务减少选课人数
                enrollmentTransationBo.asyncDecrementCourseEnrollment(course.getId(), transactionId);

                // 发送退课热度事件到排行榜模块
                sendWithdrawalHeatEvent(course.getId(), dto.getStudentId(), course.getTeacherId());

                // 发送退课行为消息到推荐模块
                sendDropBehaviorMessage(dto.getStudentId(), course.getId());

                log.info("退选成功: enrollmentId={}, studentId={}, courseId={}",
                        enrollmentId, dto.getStudentId(), course.getId());
            }

            return result;
        } catch (TimeoutException e) {
            log.warn("选课操作超时: studentId={}, courseId={}", dto.getStudentId(), dto.getCourseId());
            return CourseResponseWrapper.getFailed("系统繁忙，请稍后重试");
        } catch (Exception e) {
            log.error("并行处理异常", e);
            return CourseResponseWrapper.getFailed("系统异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper enrollFromWaitlist(EnrollmentsDto dto) {
        String lockKey = "waitlist-enroll-lock-" + dto.getCourseId() + ":" + dto.getStudentId();
        boolean locked = false;

        try {
            // 1. 获取分布式锁（锁时间可以短一些，因为这是系统自动操作）
            locked = redissLockUtil.tryLock(lockKey, 50L, 10000L);
            if (!locked) {
                return CourseResponseWrapper.getFailed("系统繁忙，请稍后重试");
            }

            // 2. 基础验证（简化版，因为等待列表学生已经通过初步验证）
            if (ObjectUtil.hasEmpty(dto.getStudentId(), dto.getCourseId())) {
                return CourseResponseWrapper.getFailed("参数不完整");
            }

            // 3. 检查学生是否已经在等待列表中
            WaitlistsVo waitlist = waitlistsDao.selectOne(
                    new LambdaQueryWrapper<WaitlistsVo>()
                            .eq(WaitlistsVo::getStudentId, dto.getStudentId())
                            .eq(WaitlistsVo::getCourseId, dto.getCourseId()));

            if (ObjectUtil.isEmpty(waitlist)) {
                return CourseResponseWrapper.getFailed("学生不在等待列表中");
            }

            // 4. 获取课程信息和策略
            CoursesDto course = FeignResponseHelper.convert(
                    courseFeignService.searchDetail(dto.getCourseId()), CoursesDto.class);
            if (ObjectUtil.isEmpty(course)) {
                return CourseResponseWrapper.getFailed("课程不存在");
            }

            EnrollmentStrategiesVo strategy = enrollmentStrategiesDao.selectOne(
                    new LambdaQueryWrapper<EnrollmentStrategiesVo>()
                            .eq(EnrollmentStrategiesVo::getCourseId, dto.getCourseId()));
            if (ObjectUtil.isEmpty(strategy)) {
                strategy = createDefaultStrategy(dto.getCourseId());
            }

            // 5. 等待列表选课的特殊验证（跳过容量检查等）
            ValidationResult validationResult = enrollmentValidationService.validateEnrollment(dto, course, strategy);
            if (!validationResult.isValid()) {
                String rejectionReasons = String.join("; ", validationResult.getRejectionReasons());
                log.warn("等待列表选课验证失败: studentId={}, courseId={}, reasons={}",
                        dto.getStudentId(), dto.getCourseId(), rejectionReasons);
                return CourseResponseWrapper.getFailed(rejectionReasons);
            }

            // 6. 执行选课逻辑（直接选课，不涉及等待列表逻辑）
            CourseResponseWrapper result = executeWaitlistEnrollment(dto, course, strategy);

            // 7. 如果选课成功，从等待列表中移除学生
            if (result.isSuccess()) {
                waitlistsDao.deleteById(waitlist.getId());

                //发送等待列表选课行为消息到推荐模块
                sendWaitlistEnrollmentBehaviorMessage(dto.getStudentId(), course.getId());

                log.info("等待列表学生选课成功并移除等待记录: studentId={}, courseId={}, waitlistId={}",
                        dto.getStudentId(), dto.getCourseId(), waitlist.getId());
            }

            log.info("等待列表选课处理完成: studentId={}, courseId={}, success={}",
                    dto.getStudentId(), dto.getCourseId(), result.isSuccess());
            return result;

        } catch (Exception e) {
            log.error("等待列表选课系统异常: studentId={}, courseId={}", dto.getStudentId(), dto.getCourseId(), e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        } finally {
            // 确保锁一定会被释放
            if (locked) {
                try {
                    redissLockUtil.unlock(lockKey);
                } catch (Exception e) {
                    log.error("释放分布式锁异常: lockKey={}", lockKey, e);
                }
            }
        }
    }

    /**
     * 执行等待列表选课
     * 直接创建选课记录，不涉及等待列表逻辑
     */
    private CourseResponseWrapper executeWaitlistEnrollment(EnrollmentsDto dto, CoursesDto course,
                                                            EnrollmentStrategiesVo strategy) {
        try {
            // 创建选课记录
            EnrollmentsVo enrollment = new EnrollmentsVo();
            enrollment.setStudentId(dto.getStudentId());
            enrollment.setCourseId(dto.getCourseId());
            enrollment.setStatus("ENROLLED");
            enrollment.setEnrolledAt(new Date());
            enrollment.setEnrollmentType("WAITLIST"); // 标记为等待列表自动选课
            enrollment.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
            enrollment.setCruAndLuu("SYSTEM"); // 系统操作

            enrollmentsDao.insert(enrollment);

            // 创建本地事务记录
            String transactionId = enrollmentTransationBo.createWaitlistEnrollmentTransaction(dto, enrollment);

            // 异步调用课程服务增加选课人数
            enrollmentTransationBo.asyncIncrementCourseEnrollment(course.getId(), transactionId);

            // 发送等待列表选课热度事件到排行榜模块
            sendWaitlistEnrollmentHeatEvent(course.getId(), dto.getStudentId(), course.getTeacherId());

            log.info("等待列表选课执行成功: enrollmentId={}, studentId={}, courseId={}",
                    enrollment.getId(), enrollment.getStudentId(), enrollment.getCourseId());

            return CourseResponseWrapper.getSuccess("选课成功", enrollment);

        } catch (Exception e) {
            log.error("执行等待列表选课失败: studentId={}, courseId={}", dto.getStudentId(), dto.getCourseId(), e);
            return CourseResponseWrapper.getFailed("选课失败: " + e.getMessage());
        }
    }

    /**
     * 发送选课行为消息
     */
    private void sendEnrollmentBehaviorMessage(Long studentId, Long courseId) {
        try {
            userBehaviorProducerService.sendEnrollmentBehavior(studentId, courseId);
            log.debug("选课行为消息发送成功: studentId={}, courseId={}", studentId, courseId);
        } catch (Exception e) {
            log.error("发送选课行为消息失败: studentId={}, courseId={}", studentId, courseId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 发送退课行为消息
     */
    private void sendDropBehaviorMessage(Long studentId, Long courseId) {
        try {
            userBehaviorProducerService.sendDropBehavior(studentId, courseId);
            log.debug("退课行为消息发送成功: studentId={}, courseId={}", studentId, courseId);
        } catch (Exception e) {
            log.error("发送退课行为消息失败: studentId={}, courseId={}", studentId, courseId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 发送等待列表选课行为消息
     */
    private void sendWaitlistEnrollmentBehaviorMessage(Long studentId, Long courseId) {
        try {
            userBehaviorProducerService.sendWaitlistEnrollmentBehavior(studentId, courseId);
            log.debug("等待列表选课行为消息发送成功: studentId={}, courseId={}", studentId, courseId);
        } catch (Exception e) {
            log.error("发送等待列表选课行为消息失败: studentId={}, courseId={}", studentId, courseId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    /**
     * 发送选课热度事件
     */
    private void sendEnrollmentHeatEvent(Long courseId, Long studentId, Long teacherId) {
        try {
            heatEventPublisher.publishEnrollmentEvent(courseId, studentId, teacherId);
            log.debug("选课热度事件发送成功: courseId={}, studentId={}", courseId, studentId);
        } catch (Exception e) {
            log.error("发送选课热度事件失败: courseId={}, studentId={}", courseId, studentId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 发送退课热度事件
     */
    private void sendWithdrawalHeatEvent(Long courseId, Long studentId, Long teacherId) {
        try {
            heatEventPublisher.publishWithdrawalEvent(courseId, studentId, teacherId);
            log.debug("退课热度事件发送成功: courseId={}, studentId={}", courseId, studentId);
        } catch (Exception e) {
            log.error("发送退课热度事件失败: courseId={}, studentId={}", courseId, studentId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 发送等待列表选课热度事件
     */
    private void sendWaitlistEnrollmentHeatEvent(Long courseId, Long studentId, Long teacherId) {
        try {
            heatEventPublisher.publishWaitlistEnrollmentEvent(courseId, studentId, teacherId);
            log.debug("等待列表选课热度事件发送成功: courseId={}, studentId={}", courseId, studentId);
        } catch (Exception e) {
            log.error("发送等待列表选课热度事件失败: courseId={}, studentId={}", courseId, studentId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    public CourseResponseWrapper getStudentEnrollments(Long studentId, EnrollmentsDto dto) {
        try {
            List<EnrollmentsVo> enrollments = enrollmentsDao.selectList(
                    new LambdaQueryWrapper<EnrollmentsVo>()
                            .eq(EnrollmentsVo::getStudentId, studentId)
                            .in(EnrollmentsVo::getStatus, Arrays.asList("SUCCESS", "PENDING")));

            return CourseResponseWrapper.getSuccess("查询成功", enrollments);
        } catch (Exception e) {
            log.error("查询学生选课列表异常: studentId={}", studentId, e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        }
    }

    @Override
    public CourseResponseWrapper getStudentTimetable(Long studentId, EnrollmentsDto dto) {
        try {
            // 获取学生成功的选课记录
            List<EnrollmentsVo> successfulEnrollments = enrollmentsDao.selectList(
                    new LambdaQueryWrapper<EnrollmentsVo>()
                            .eq(EnrollmentsVo::getStudentId, studentId)
                            .eq(EnrollmentsVo::getStatus, "SUCCESS"));

            // 获取课程详细信息并构建课表
            List<CoursesDto> timetable = successfulEnrollments.stream()
                    .map(enrollment -> FeignResponseHelper.convert(
                            courseFeignService.searchDetail(enrollment.getCourseId()), CoursesDto.class))
                    .collect(Collectors.toList());

            return CourseResponseWrapper.getSuccess("查询成功", timetable);
        } catch (Exception e) {
            log.error("查询学生课表异常: studentId={}", studentId, e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        }
    }

    @Override
    public CourseResponseWrapper getCourseEnrollments(Long courseId, EnrollmentsDto dto) {
        try {
            List<EnrollmentsVo> enrollments = enrollmentsDao.selectList(
                    new LambdaQueryWrapper<EnrollmentsVo>()
                            .eq(EnrollmentsVo::getCourseId, courseId)
                            .eq(EnrollmentsVo::getStatus, "SUCCESS"));

            return CourseResponseWrapper.getSuccess("查询成功", enrollments);
        } catch (Exception e) {
            log.error("查询课程选课列表异常: courseId={}", courseId, e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        }
    }
}