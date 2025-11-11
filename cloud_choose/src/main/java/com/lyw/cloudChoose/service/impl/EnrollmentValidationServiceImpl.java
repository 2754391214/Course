package com.lyw.cloudChoose.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudChoose.dto.ValidationResult;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.feign.CourseFeignService;
import com.lyw.cloudChoose.mapper.EnrollmentBlacklistDao;
import com.lyw.cloudChoose.mapper.EnrollmentsDao;
import com.lyw.cloudChoose.service.EnrollmentValidationService;
import com.lyw.cloudChoose.vo.EnrollmentBlacklistVo;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class EnrollmentValidationServiceImpl implements EnrollmentValidationService {

    @Resource
    private EnrollmentBlacklistDao enrollmentBlacklistDao;
    @Resource
    private EnrollmentsDao enrollmentsDao;
    @Resource
    private CourseFeignService courseFeignService;
    @Resource
    private Executor validationExecutor;
    @Override
    public ValidationResult validateEnrollment(EnrollmentsDto request, CoursesDto course,
                                               EnrollmentStrategiesVo strategy) {
        ValidationResult result = ValidationResult.success();

        // 1. 基础验证（必须最先执行）
        if (!validateBasicInfo(request, course, strategy, result)) {
            return result;
        }

        // 2. 并行执行其他验证
        CompletableFuture<Boolean> blacklistFuture = CompletableFuture.supplyAsync(
                () -> validateBlacklist(request, result),validationExecutor);

        CompletableFuture<Boolean> duplicateFuture = CompletableFuture.supplyAsync(
                () -> validateDuplicateEnrollment(request, result),validationExecutor);

        CompletableFuture<Boolean> capacityFuture = CompletableFuture.supplyAsync(
                () -> validateCapacity(course, strategy, request, result),validationExecutor);

        CompletableFuture<Boolean> timeConflictFuture = CompletableFuture.supplyAsync(
                () -> validateTimeConflict(request, course, strategy, result),validationExecutor);

        // 等待所有验证完成
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    blacklistFuture, duplicateFuture, capacityFuture, timeConflictFuture);
            allFutures.get(1500, TimeUnit.MILLISECONDS);

            // 如果有任何一个验证失败，直接返回
            if (!result.isValid()) {
                return result;
            }

        } catch (TimeoutException e) {
            log.warn("验证操作超时");
            result.addRejectionReason("系统繁忙，请稍后重试");
            return result;
        } catch (Exception e) {
            log.error("并行验证异常", e);
            result.addRejectionReason("系统异常");
            return result;
        }

        return result;
    }

    @Override
    public ValidationResult validateDrop(EnrollmentsDto request, CoursesDto course, EnrollmentStrategiesVo strategy) {
        ValidationResult result = ValidationResult.success();
        // 1. 基础验证
        if (!validateBasicInfoDrop(request, course, strategy, result)&&!validateCheckTime(request, result)) {
            return result;
        }

        return result;
    }

    //退课校验
    private boolean validateBasicInfoDrop(EnrollmentsDto request, CoursesDto course,
                                      EnrollmentStrategiesVo strategy, ValidationResult result) {
        log.info("执行基础信息验证");

        if (ObjectUtil.isEmpty(course)) {
            result.addRejectionReason("课程不存在");
            return false;
        }

        if (!"PUBLISHED".equals(course.getStatus())) {
            result.addRejectionReason("课程未发布或已关闭");
            return false;
        }

        if ("DROPPED".equals(request.getStatus())) {
            result.addRejectionReason("该选课记录已退选");
        }

        return true;
    }
    private boolean validateBasicInfo(EnrollmentsDto request, CoursesDto course,
                                      EnrollmentStrategiesVo strategy, ValidationResult result) {
        log.info("执行基础信息验证");

        if (ObjectUtil.isEmpty(course)) {
            result.addRejectionReason("课程不存在");
            return false;
        }

        if (!"PUBLISHED".equals(course.getStatus())) {
            result.addRejectionReason("课程未发布或已关闭");
            return false;
        }

        if (!Arrays.asList("NORMAL", "AUDIT").contains(request.getEnrollmentType())) {
            result.addRejectionReason("无效的选课类型");
            return false;
        }

        if ("AUDIT".equals(request.getEnrollmentType()) &&
                !Boolean.TRUE.equals(strategy.getAllowAudit())) {
            result.addRejectionReason("该课程不允许旁听");
            return false;
        }

        return true;
    }

    //TODO
    private boolean validateCheckTime(EnrollmentsDto request, ValidationResult result) {
        log.info("检查课程是否开始");
        return true;
    }

    //选课校验
    private boolean validateBlacklist(EnrollmentsDto request, ValidationResult result) {
        log.info("检查黑名单");

        EnrollmentBlacklistVo blacklist = enrollmentBlacklistDao.selectByStudentId(request.getStudentId());
        if (ObjectUtil.isNotEmpty(blacklist)) {
            String reason = ObjectUtil.isEmpty(blacklist.getCourseId()) ?
                    String.format("学生处于黑名单中，禁止选课。原因: %s", blacklist.getReason()) :
                    String.format("该课程对您受限，禁止选课。原因: %s", blacklist.getReason());

            result.addRejectionReason(reason);
            return false;
        }

        return true;
    }

    private boolean validateDuplicateEnrollment(EnrollmentsDto request, ValidationResult result) {
        log.info("检查重复选课");

        long count = enrollmentsDao.selectCount(
                new LambdaQueryWrapper<EnrollmentsVo>()
                        .eq(EnrollmentsVo::getStudentId, request.getStudentId())
                        .eq(EnrollmentsVo::getCourseId, request.getCourseId())
                        .in(EnrollmentsVo::getStatus, "SUCCESS", "PENDING", "WAITING")
        );

        if (count > 0) {
            result.addRejectionReason("您已选过该课程");
            return false;
        }

        return true;
    }

    private boolean validateCapacity(CoursesDto course, EnrollmentStrategiesVo strategy,
                                     EnrollmentsDto request, ValidationResult result) {
        log.info("检查课程容量");

        if ("WAITLIST".equals(request.getEnrollmentType())) {
            return true;
        }
        //TODO 获取容量 redis
        if (course.getEnrolledCount() >= course.getCapacity()) {
            if (Boolean.TRUE.equals(strategy.getAutoWaitlist())) {
                course.setFullEnrolledCount(true);
            } else {
                result.addRejectionReason("课程容量已满");
                return false;
            }
        }

        return true;
    }

    private boolean validateTimeConflict(EnrollmentsDto request, CoursesDto course,
                                         EnrollmentStrategiesVo strategy, ValidationResult result) {
        log.info("检查时间冲突");

        if (!Boolean.TRUE.equals(strategy.getConflictCheck())) {
            return true;
        }

        try {
            List<Long> enrolledCourseIds = enrollmentsDao.selectEnrolledCourseIds(request.getStudentId());
            CourseResponseWrapper response = courseFeignService.checkTimeConflict(
                    course.getId(), request.getStudentId(), enrolledCourseIds);

            if (!response.isSuccess()) {
                result.addRejectionReason(response.getErrorMessage());
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("时间冲突检查异常", e);
            result.addRejectionReason("时间冲突检查服务异常，请稍后重试");
            return false;
        }
    }
}
