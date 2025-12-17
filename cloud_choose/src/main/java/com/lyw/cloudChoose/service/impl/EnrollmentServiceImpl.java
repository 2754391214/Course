package com.lyw.cloudChoose.service.impl;

import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.mapper.EnrollmentsDao;
import com.lyw.cloudChoose.service.EnrollmentService;
import com.lyw.cloudChoose.service.LocalMessageService;
import com.lyw.cloudChoose.service.MessageAsyncProcessor;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.cloudChoose.vo.LocalMessageVo;
import com.lyw.commonUtil.constant.CommonKeyConstant;
import com.lyw.commonUtil.constant.RabbitmqKeyConstant;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.message.HeatEventMessage;
import com.lyw.commonUtil.message.UserBehaviorMessage;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.CurUserUtil;
import com.lyw.commonUtil.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Resource
    private EnrollmentsDao enrollmentsDao;
    @Resource
    private LocalMessageService localMessageService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private DefaultRedisScript<Long> dropScript;
    @Resource
    private DefaultRedisScript<Long> enrollScript;
    @Resource
    private MessageAsyncProcessor messageAsyncProcessor;

    /**
     * 处理成功选课情况 - 使用本地消息表
     */
    @Override
    public CourseResponseWrapper handleSuccessfulEnrollment(EnrollmentsDto enrollment, CoursesDto course, Long studentId) {
        Long courseId = course.getId();
        return transactionTemplate.execute(status -> {
            try {
                // 1. 创建选课记录
                EnrollmentsVo enrollmentVo = createEnrollmentRecord(enrollment, course, studentId);
                enrollmentsDao.insert(enrollmentVo);

                // 2. 创建本地消息记录（与业务操作在同一事务中）
                List<LocalMessageVo> messages = createEnrollmentMessages(enrollmentVo, course);

                // 3. 提交事务（业务数据和消息数据同时提交）
                log.info("选课事务提交成功: enrollmentId={}, studentId={}, courseId={}",
                        enrollment.getId(), studentId, courseId);

                // 4. 事务提交后发送消息（异步）
                sendMessagesAsync(messages);
                return CourseResponseWrapper.getSuccess(enrollment);

            } catch (Exception e) {
                status.setRollbackOnly();
                handleEnrollmentFailure(course, studentId, e);
                log.error("选课事务失败: studentId={}, courseId={}", studentId, courseId, e);
                return CourseResponseWrapper.getFailed("选课失败，请稍后重试");
            }
        });
    }

    @Override
    public CourseResponseWrapper handleSuccessfulDrop(EnrollmentsVo enrollment, CoursesDto course, Long studentId) {
        Long enrollmentId = enrollment.getId();
        Long courseId = course.getId();
        return transactionTemplate.execute(status -> {
            try {
                // 1. 删除选课记录
                enrollmentsDao.deleteById(enrollmentId);

                // 2. 创建退课相关的本地消息记录（与业务操作在同一事务中）
                List<LocalMessageVo> messages = createDropMessages(enrollment, course, studentId);

                // 3. 提交事务（业务数据和消息数据同时提交）
                log.info("退课事务提交成功: enrollmentId={}, studentId={}, courseId={}",
                        enrollmentId, studentId, courseId);

                // 4. 事务提交后发送消息（异步）
                sendMessagesAsync(messages);

                log.info("退选成功: enrollmentId={}, studentId={}, courseId={}",
                        enrollmentId, studentId, courseId);
                return CourseResponseWrapper.getSuccess("退选成功", enrollment);

            } catch (Exception e) {
                status.setRollbackOnly();
                handleDropFailure(enrollment, course, studentId, e);
                log.error("退课事务失败: enrollmentId={}, studentId={}, courseId={}",
                        enrollmentId, studentId, courseId, e);
                return CourseResponseWrapper.getFailed("退选失败，请稍后重试");
            }
        });
    }

    /**
     * 创建退课相关的本地消息
     */
    private List<LocalMessageVo> createDropMessages(EnrollmentsVo enrollment, CoursesDto course, Long studentId) {
        log.info("创建选课相关的本地消息");
        long totalStartTime = System.currentTimeMillis();

        String currentDateTime = DateTimeUtils.getCurrentDateTime();
        String userId = CurUserUtil.getUserId();

        // 使用 CompletableFuture 并行创建三个消息
        CompletableFuture<LocalMessageVo> incrementFuture = CompletableFuture.supplyAsync(() ->
                createCourseIncrementMessage(course, currentDateTime, userId, CommonKeyConstant.DECREMENT_ENROLLMENT)
        );

        CompletableFuture<LocalMessageVo> behaviorFuture = CompletableFuture.supplyAsync(() ->
                createBehaviorMessage(course, currentDateTime, userId,CommonKeyConstant.DROP)
        );

        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(incrementFuture, behaviorFuture);

        try {
            allFutures.get(3, TimeUnit.SECONDS); // 设置超时时间

            // 收集结果
            List<LocalMessageVo> messages = Arrays.asList(
                    incrementFuture.get(),
                    behaviorFuture.get()
            );

            // 批量保存消息
            localMessageService.batchInsertMessages(messages);

            log.info("创建选课相关消息完成: enrollmentId={}, 消息数量={}, 总耗时: {}ms",
                    enrollment.getId(), messages.size(), System.currentTimeMillis() - totalStartTime);

            return messages;

        } catch (TimeoutException e) {
            log.error("创建消息超时: enrollmentId={}", enrollment.getId(), e);
            return handleTimeoutFallback(course, currentDateTime, userId, CommonKeyConstant.DECREMENT_ENROLLMENT);
        } catch (Exception e) {
            log.error("创建消息失败: enrollmentId={}", enrollment.getId(), e);
            return handleDeleteFailure(enrollment, course, studentId, currentDateTime, userId);
        }
    }
    /**
     * 处理退课失败的情况
     */
    private void handleDropFailure(EnrollmentsVo enrollment, CoursesDto course, Long studentId, Exception e) {
        try {
            Long courseId = course.getId();
            // 使用Lua脚本恢复Redis计数（重新选课）
            List<String> keys = Arrays.asList(
                    RedisKeyConstant.COURSE_CURRENT + courseId,
                    RedisKeyConstant.STUDENT_COURSES + studentId
            );

            List<String> recoveryArgs = Arrays.asList(
                    course.getCapacity().toString(),
                    courseId.toString(),
                    String.valueOf(RedisKeyConstant.DEFAULT_EXPIRE_SECONDS)
            );

            stringRedisTemplate.execute(enrollScript, keys, recoveryArgs.toArray());

            log.info("退课失败，已恢复Redis计数: studentId={}, courseId={}", studentId, courseId);
        } catch (Exception redisEx) {
            log.error("恢复Redis计数失败: studentId={}, courseId={}", studentId, course.getId(), redisEx);
        }
    }
    /**
     * 创建失败降级处理
     */
    private List<LocalMessageVo> handleDeleteFailure(EnrollmentsVo enrollment, CoursesDto course,
                                                       Long studentId, String currentDateTime, String userId) {
        log.error("消息创建失败，记录错误日志但不影响主流程");

        // 返回空列表，主选课流程继续
        return Collections.emptyList();
    }

    private List<LocalMessageVo> createEnrollmentMessages(EnrollmentsVo enrollment, CoursesDto course) {
        log.info("创建选课相关的本地消息");
        long totalStartTime = System.currentTimeMillis();

        String currentDateTime = DateTimeUtils.getCurrentDateTime();
        String userId = CurUserUtil.getUserId();

        // 使用 CompletableFuture 并行创建三个消息
        CompletableFuture<LocalMessageVo> incrementFuture = CompletableFuture.supplyAsync(() ->
                createCourseIncrementMessage(course, currentDateTime, userId, CommonKeyConstant.INCREMENT_ENROLLMENT)
        );

        CompletableFuture<LocalMessageVo> heatFuture = CompletableFuture.supplyAsync(() ->
                createHeatMessage(course, currentDateTime, userId)
        );

        CompletableFuture<LocalMessageVo> behaviorFuture = CompletableFuture.supplyAsync(() ->
                createBehaviorMessage(course, currentDateTime, userId,CommonKeyConstant.ENROLLMENT)
        );

        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(incrementFuture, heatFuture, behaviorFuture);

        try {
            allFutures.get(3, TimeUnit.SECONDS); // 设置超时时间

            // 收集结果
            List<LocalMessageVo> messages = Arrays.asList(
                    incrementFuture.get(),
                    heatFuture.get(),
                    behaviorFuture.get()
            );

            // 批量保存消息
            localMessageService.batchInsertMessages(messages);

            log.info("创建选课相关消息完成: enrollmentId={}, 消息数量={}, 总耗时: {}ms",
                    enrollment.getId(), messages.size(), System.currentTimeMillis() - totalStartTime);

            return messages;

        } catch (TimeoutException e) {
            log.error("创建消息超时: enrollmentId={}", enrollment.getId(), e);
            return handleTimeoutFallback(course, currentDateTime, userId, CommonKeyConstant.INCREMENT_ENROLLMENT);
        } catch (Exception e) {
            log.error("创建消息失败: enrollmentId={}", enrollment.getId(), e);
            return handleCreationFailure(enrollment, course, currentDateTime, userId);
        }
    }

    /**
     * 创建课程人数增加消息
     */
    private LocalMessageVo createCourseIncrementMessage(CoursesDto course, String currentDateTime, String userId,String operation) {
        Map<String, Object> incrementBody = new HashMap<>();
        String messageId = generateBusinessKey("enrollment");
        incrementBody.put("courseId", course.getId());
        incrementBody.put("messageId", messageId);
        incrementBody.put("operation", operation);

        return localMessageService.createMessage(
                LocalMessageVo.TYPE_COURSE_INCREMENT,
                RabbitmqKeyConstant.ENROLLMENT_ROUTING_KEY,
                incrementBody,
                RabbitmqKeyConstant.ENROLLMENT_EXCHANGE,
                currentDateTime,
                userId,
                messageId
        );
    }

    /**
     * 创建热度事件消息
     */
    private LocalMessageVo createHeatMessage(CoursesDto course,String currentDateTime, String userId) {
        String messageId = generateBusinessKey("heat");
        HeatEventMessage heatBody = new HeatEventMessage();
        heatBody.setMessageId(messageId)
                .setTargetType(CommonKeyConstant.COURSE)
                .setEventType(CommonKeyConstant.ENROLLMENT)
                .setCourseId(course.getId())
                .setEventTime(new Date());

        return localMessageService.createMessage(
                LocalMessageVo.TYPE_COURSE_INCREMENT,
                RabbitmqKeyConstant.COURSE_HEAT_ROUTING_KEY+CommonKeyConstant.ENROLLMENT,
                heatBody,
                RabbitmqKeyConstant.COURSE_HEAT_EXCHANGE,
                currentDateTime,
                userId,
                messageId
        );
    }

    /**
     * 创建用户行为消息
     */
    private LocalMessageVo createBehaviorMessage(CoursesDto course, String currentDateTime, String userId, String behaviorType) {
        String messageId = generateBusinessKey("behavior");
        UserBehaviorMessage behaviorBody = new UserBehaviorMessage();
        behaviorBody.setMessageId(messageId);
        behaviorBody.setCourseId(course.getId());
        behaviorBody.setUserId(Long.valueOf(userId));
        behaviorBody.setBehaviorType(behaviorType);
        behaviorBody.setBehaviorTime(new Date());
        return localMessageService.createMessage(
                LocalMessageVo.TYPE_COURSE_INCREMENT,
                RabbitmqKeyConstant.USER_BEHAVIOR_ROUTING_KEY,
                behaviorBody,
                RabbitmqKeyConstant.USER_BEHAVIOR_EXCHANGE,
                currentDateTime,
                userId,
                messageId
        );
    }

    /**
     * 超时降级处理
     */
    private List<LocalMessageVo> handleTimeoutFallback(CoursesDto course, String currentDateTime, String userId, String operation) {
        log.warn("消息创建超时，使用同步方式创建关键消息");

        // 只创建最重要的课程人数消息
        LocalMessageVo incrementMessage = createCourseIncrementMessage(course, currentDateTime, userId, operation);
        List<LocalMessageVo> messages = Collections.singletonList(incrementMessage);

        localMessageService.batchInsertMessages(messages);
        return messages;
    }

    /**
     * 创建失败降级处理
     */
    private List<LocalMessageVo> handleCreationFailure(EnrollmentsVo enrollment, CoursesDto course,
                                                        String currentDateTime, String userId) {
        log.error("消息创建失败，记录错误日志但不影响主流程");

        // 返回空列表，主选课流程继续
        return Collections.emptyList();
    }

    /**
     * 异步发送消息 - 使用批量队列
     */
    private void sendMessagesAsync(List<LocalMessageVo> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            // 提交到批量处理器
            boolean success = messageAsyncProcessor.submitMessages(messages);

            log.info("消息提交到异步处理器完成: 数量={}, 结果={}, 耗时={}ms",
                    messages.size(), success, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("提交消息到异步处理器失败: 数量={}", messages.size(), e);

            // 降级为同步发送
            fallbackToSyncSend(messages);
        }
    }

    /**
     * 降级为同步发送
     */
    private void fallbackToSyncSend(List<LocalMessageVo> messages) {
        log.warn("异步发送失败，降级为同步发送: 数量={}", messages.size());

        for (LocalMessageVo message : messages) {
            try {
                localMessageService.sendMessage(message);
            } catch (Exception e) {
                log.error("同步发送消息也失败: businessKey={}", message.getBusinessKey(), e);
            }
        }
    }

    /**
     * 处理选课失败的情况
     */
    private void handleEnrollmentFailure(CoursesDto course, Long studentId, Exception e) {
        try {
            Long courseId = course.getId();
            // 使用Lua脚本进行原子性退课操作
            List<String> keys = Arrays.asList(
                    RedisKeyConstant.COURSE_CURRENT + courseId,
                    RedisKeyConstant.STUDENT_COURSES + studentId
            );

            stringRedisTemplate.execute(dropScript, keys, courseId.toString(),
                    String.valueOf(RedisKeyConstant.DEFAULT_EXPIRE_SECONDS));

            log.info("选课失败，已回滚Redis计数: studentId={}, courseId={}", studentId, courseId);
        } catch (Exception redisEx) {
            log.error("回滚Redis计数失败: studentId={}, courseId={}", studentId, course.getId(), redisEx);
        }
    }

    /**
     * 创建选课记录
     */
    private EnrollmentsVo createEnrollmentRecord(EnrollmentsDto request, CoursesDto course, Long studentId) {
        EnrollmentsVo enrollmentsVo = new EnrollmentsVo();
        enrollmentsVo.setStudentId(studentId);
        enrollmentsVo.setCourseId(course.getId());
        enrollmentsVo.setEnrollmentType(request.getEnrollmentType());
        enrollmentsVo.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
        enrollmentsVo.setCruAndLuu(CurUserUtil.getUserId());
        return enrollmentsVo;
    }

    private String generateBusinessKey(String messageType) {
        return messageType + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}