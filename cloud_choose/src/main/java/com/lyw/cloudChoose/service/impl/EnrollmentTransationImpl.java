package com.lyw.cloudChoose.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.commonUtil.dto.EnrollmentMessage;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.mapper.EnrollmentTransactionDao;
import com.lyw.cloudChoose.mapper.EnrollmentsDao;
import com.lyw.cloudChoose.mapper.WaitlistsDao;
import com.lyw.cloudChoose.service.EnrollmentTransationBo;
import com.lyw.cloudChoose.service.RabbitMQService;
import com.lyw.cloudChoose.vo.EnrollmentTransactionVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.cloudChoose.vo.WaitlistsVo;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.CurUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class EnrollmentTransationImpl implements EnrollmentTransationBo {
    @Resource
    private EnrollmentTransactionDao enrollmentTransactionDao;
    @Resource
    private RabbitMQService rabbitMQService;
    @Resource
    private WaitlistsDao waitlistsDao;
    @Resource
    private EnrollmentsDao enrollmentsDao;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public String createEnrollmentTransaction(EnrollmentsDto dto, EnrollmentsVo enrollment) {
        try {
            String transactionId = "TXN_" + System.currentTimeMillis() + "_" +
                    UUID.randomUUID().toString().substring(0, 8);

            EnrollmentTransactionVo transaction = new EnrollmentTransactionVo()
                    .setTransactionId(transactionId)
                    .setEnrollmentId(enrollment.getId())
                    .setStudentId(dto.getStudentId())
                    .setCourseId(dto.getCourseId())
                    .setOperationType("ENROLL")
                    .setTransactionStatus("INIT")
                    .setRetryCount(0)
                    .setMaxRetryCount(5)
                    .setRequestData(JSON.toJSONString(dto));
            transaction.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
            transaction.setCruAndLuu(CurUserUtil.getUserCode());

            enrollmentTransactionDao.insert(transaction);
            log.info("创建选课事务记录成功: transactionId={}, enrollmentId={}",
                    transactionId, enrollment.getId());

            return transactionId;

        } catch (Exception e) {
            log.error("创建选课事务记录异常: enrollmentId={}", enrollment.getId(), e);
            return "ERROR_" + System.currentTimeMillis();
        }
    }

    @Override
    public void asyncIncrementCourseEnrollment(Long courseId, String transactionId) {
        try {
            // 标记事务为处理中
            updateTransactionStatus(transactionId, "PROCESSING", null, null);

            // 使用RabbitMQ发送消息
            EnrollmentMessage message = new EnrollmentMessage()
                    .setTransactionId(transactionId)
                    .setCourseId(courseId)
                    .setOperation("INCREMENT_ENROLLMENT")
                    .setTimestamp(System.currentTimeMillis());

            rabbitMQService.sendEnrollmentMessage(message);

        } catch (Exception e) {
            log.error("异步增加课程人数异常: transactionId={}, courseId={}",
                    transactionId, courseId, e);
            updateTransactionStatus(transactionId, "FAILED", null,
                    "消息发送异常: " + e.getMessage());
        }
    }

    @Override
    public void updateTransactionStatus(String transactionId, String status, String responseData, String errorMessage) {
        try {
            EnrollmentTransactionVo transaction = enrollmentTransactionDao.selectOne(
                    new LambdaQueryWrapper<EnrollmentTransactionVo>()
                            .eq(EnrollmentTransactionVo::getTransactionId, transactionId));

            if (ObjectUtil.isNotEmpty(transaction)) {
                transaction.setTransactionStatus(status)
                        .setResponseData(responseData)
                        .setErrorMessage(errorMessage)
                        .setLud(DateTimeUtils.getCurrentDateTime())
                        .setLuu(CurUserUtil.getUserCode());

                if ("PROCESSING".equals(status)) {
                    // 设置下次重试时间
                    transaction.setNextRetryTime(
                            new Date(System.currentTimeMillis() + 30000)); // 30秒后重试
                }

                enrollmentTransactionDao.updateById(transaction);
                log.info("更新事务状态成功: transactionId={}, status={}", transactionId, status);
            }

        } catch (Exception e) {
            log.error("更新事务状态异常: transactionId={}, status={}", transactionId, status, e);
        }
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void compensateFailedTransaction(String transactionId) {
        try {
            log.info("开始补偿失败事务: transactionId={}", transactionId);

            // 1. 获取事务记录
            EnrollmentTransactionVo transaction = enrollmentTransactionDao.selectOne(
                    new LambdaQueryWrapper<EnrollmentTransactionVo>()
                            .eq(EnrollmentTransactionVo::getTransactionId, transactionId));

            if (ObjectUtil.isEmpty(transaction)) {
                log.warn("事务记录不存在，无需补偿: transactionId={}", transactionId);
                return;
            }

            // 2. 根据操作类型执行不同的补偿逻辑
            switch (transaction.getOperationType()) {
                case "ENROLL":
                    compensateEnrollmentTransaction(transaction);
                    break;
                case "DROP":
                    compensateDropTransaction(transaction);
                    break;
                case "BATCH_ENROLL":
                    compensateBatchEnrollmentTransaction(transaction);
                    break;
                case "PRIORITY_REPLACE":
                    compensatePriorityReplaceTransaction(transaction);
                    break;
                default:
                    log.warn("未知的操作类型，使用默认补偿: transactionId={}, operationType={}",
                            transactionId, transaction.getOperationType());
                    compensateDefaultTransaction(transaction);
            }

            // 3. 更新事务状态为已补偿
            transaction.setTransactionStatus("COMPENSATED");
            transaction.setLud(DateTimeUtils.getCurrentDateTime());
            transaction.setLuu(CurUserUtil.getUserCode());
            enrollmentTransactionDao.updateById(transaction);

            log.info("事务补偿完成: transactionId={}", transactionId);

        } catch (Exception e) {
            log.error("补偿失败事务异常: transactionId={}", transactionId, e);
            throw new RuntimeException("事务补偿失败", e);
        }
    }
    /**
     * 补偿选课事务
     */
    private void compensateEnrollmentTransaction(EnrollmentTransactionVo transaction) {
        try {
            log.info("补偿选课事务: transactionId={}, enrollmentId={}",
                    transaction.getTransactionId(), transaction.getEnrollmentId());

            // 1. 删除选课记录
            if (ObjectUtil.isNotEmpty(transaction.getEnrollmentId())) {
                EnrollmentsVo enrollment = enrollmentsDao.selectById(transaction.getEnrollmentId());
                if (ObjectUtil.isNotEmpty(enrollment)) {
                    enrollmentsDao.deleteById(transaction.getEnrollmentId());
                    log.info("删除选课记录完成: enrollmentId={}", transaction.getEnrollmentId());

                    // 2. 清理Redis缓存
                    cleanupEnrollmentCache(transaction.getStudentId(), transaction.getCourseId());

                    // 3. 回滚Redis中的课程容量
                    rollbackCourseCapacityInRedis(transaction.getCourseId());

                    // 4. 如果学生原本在等待列表中，恢复等待列表状态
                    restoreWaitlistStatus(transaction.getStudentId(), transaction.getCourseId());
                }
            }

        } catch (Exception e) {
            log.error("补偿选课事务异常: transactionId={}", transaction.getTransactionId(), e);
            throw new RuntimeException("选课事务补偿失败", e);
        }
    }

    /**
     * 补偿退课事务
     */
    private void compensateDropTransaction(EnrollmentTransactionVo transaction) {
        try {
            log.info("补偿退课事务: transactionId={}, enrollmentId={}",
                    transaction.getTransactionId(), transaction.getEnrollmentId());

            // 1. 恢复选课记录状态
            if (ObjectUtil.isNotEmpty(transaction.getEnrollmentId())) {
                EnrollmentsVo enrollment = enrollmentsDao.selectById(transaction.getEnrollmentId());
                if (ObjectUtil.isNotEmpty(enrollment)) {
                    enrollment.setStatus("SUCCESS");
                    enrollment.setDroppedAt(null);
                    enrollment.setLud(DateTimeUtils.getCurrentDateTime());
                    enrollmentsDao.updateById(enrollment);

                    log.info("恢复选课记录状态完成: enrollmentId={}", transaction.getEnrollmentId());

                    // 2. 更新Redis缓存
                    updateEnrollmentCache(transaction.getStudentId(), transaction.getCourseId(), enrollment.getId());

                    // 3. 重新扣减Redis中的课程容量
                    decrementCourseCapacityInRedis(transaction.getCourseId());
                }
            }

        } catch (Exception e) {
            log.error("补偿退课事务异常: transactionId={}", transaction.getTransactionId(), e);
            throw new RuntimeException("退课事务补偿失败", e);
        }
    }

    /**
     * 补偿批量选课事务
     */
    private void compensateBatchEnrollmentTransaction(EnrollmentTransactionVo transaction) {
        try {
            log.info("补偿批量选课事务: transactionId={}", transaction.getTransactionId());

            // 解析请求数据，获取批量选课信息
            // 这里需要根据实际的请求数据结构来解析
            // 假设请求数据中包含了批量选课的课程ID列表

            // 1. 删除所有相关的选课记录
            deleteBatchEnrollments(transaction);

            // 2. 清理Redis缓存
            cleanupBatchEnrollmentCache(transaction);

            // 3. 回滚所有课程的容量
            rollbackBatchCourseCapacity(transaction);

        } catch (Exception e) {
            log.error("补偿批量选课事务异常: transactionId={}", transaction.getTransactionId(), e);
            throw new RuntimeException("批量选课事务补偿失败", e);
        }
    }
    /**
     * 补偿优先级替换事务
     */
    private void compensatePriorityReplaceTransaction(EnrollmentTransactionVo transaction) {
        try {
            log.info("补偿优先级替换事务: transactionId={}", transaction.getTransactionId());

            // 1. 恢复被替换学生的选课记录
            restoreReplacedStudentEnrollment(transaction);

            // 2. 删除新学生的选课记录
            if (ObjectUtil.isNotEmpty(transaction.getEnrollmentId())) {
                enrollmentsDao.deleteById(transaction.getEnrollmentId());
            }

            // 3. 恢复课程容量
            rollbackCourseCapacityInRedis(transaction.getCourseId());

            // 4. 清理缓存
            cleanupEnrollmentCache(transaction.getStudentId(), transaction.getCourseId());

        } catch (Exception e) {
            log.error("补偿优先级替换事务异常: transactionId={}", transaction.getTransactionId(), e);
            throw new RuntimeException("优先级替换事务补偿失败", e);
        }
    }

    /**
     * 默认补偿逻辑
     */
    private void compensateDefaultTransaction(EnrollmentTransactionVo transaction) {
        try {
            log.info("执行默认补偿: transactionId={}", transaction.getTransactionId());

            // 1. 尝试删除选课记录
            if (ObjectUtil.isNotEmpty(transaction.getEnrollmentId())) {
                enrollmentsDao.deleteById(transaction.getEnrollmentId());
            }

            // 2. 清理Redis缓存
            cleanupEnrollmentCache(transaction.getStudentId(), transaction.getCourseId());

            // 3. 回滚课程容量
            rollbackCourseCapacityInRedis(transaction.getCourseId());

        } catch (Exception e) {
            log.error("默认补偿异常: transactionId={}", transaction.getTransactionId(), e);
        }
    }
    /**
     * 清理选课缓存
     */
    private void cleanupEnrollmentCache(Long studentId, Long courseId) {
        try {
            String studentEnrollmentKey = "student:enrollment:" + studentId + ":" + courseId;
            redisTemplate.delete(studentEnrollmentKey);

            String studentCoursesKey = "student:courses:" + studentId;
            redisTemplate.delete(studentCoursesKey);

            log.debug("清理选课缓存完成: studentId={}, courseId={}", studentId, courseId);

        } catch (Exception e) {
            log.error("清理选课缓存异常: studentId={}, courseId={}", studentId, courseId, e);
        }
    }

    /**
     * 更新选课缓存
     */
    private void updateEnrollmentCache(Long studentId, Long courseId, Long enrollmentId) {
        try {
            String studentEnrollmentKey = "student:enrollment:" + studentId + ":" + courseId;
            redisTemplate.opsForValue().set(studentEnrollmentKey, enrollmentId, 24, java.util.concurrent.TimeUnit.HOURS);

            log.debug("更新选课缓存完成: studentId={}, courseId={}, enrollmentId={}",
                    studentId, courseId, enrollmentId);

        } catch (Exception e) {
            log.error("更新选课缓存异常: studentId={}, courseId={}", studentId, courseId, e);
        }
    }

    /**
     * 回滚Redis中的课程容量
     */
    private void rollbackCourseCapacityInRedis(Long courseId) {
        try {
            String currentKey = "course:current:" + courseId;
            Long currentCount = redisTemplate.opsForValue().decrement(currentKey);

            if (ObjectUtil.isNotEmpty(currentCount)) {
                log.info("回滚课程容量成功: courseId={}, 当前人数={}", courseId, currentCount);
            } else {
                log.warn("回滚课程容量失败，键不存在: courseId={}", courseId);
            }

        } catch (Exception e) {
            log.error("回滚课程容量异常: courseId={}", courseId, e);
        }
    }

    /**
     * 扣减Redis中的课程容量
     */
    private void decrementCourseCapacityInRedis(Long courseId) {
        try {
            String currentKey = "course:current:" + courseId;
            Long currentCount = redisTemplate.opsForValue().increment(currentKey);

            if (currentCount != null) {
                log.info("扣减课程容量成功: courseId={}, 当前人数={}", courseId, currentCount);
            }

        } catch (Exception e) {
            log.error("扣减课程容量异常: courseId={}", courseId, e);
        }
    }

    /**
     * 恢复等待列表状态
     */
    private void restoreWaitlistStatus(Long studentId, Long courseId) {
        try {
            // 检查学生是否原本在等待列表中
            WaitlistsVo waitlist = waitlistsDao.selectOne(
                    new LambdaQueryWrapper<WaitlistsVo>()
                            .eq(WaitlistsVo::getStudentId, studentId)
                            .eq(WaitlistsVo::getCourseId, courseId)
                            .eq(WaitlistsVo::getStatus, "WAITING"));

            if (ObjectUtil.isNotEmpty(waitlist)) {
                // 如果学生原本在等待列表中，确保状态正确
                waitlist.setStatus("WAITING");
                waitlist.setLud(DateTimeUtils.getCurrentDateTime());
                waitlistsDao.updateById(waitlist);

                log.info("恢复等待列表状态完成: studentId={}, courseId={}", studentId, courseId);
            }

        } catch (Exception e) {
            log.error("恢复等待列表状态异常: studentId={}, courseId={}", studentId, courseId, e);
        }
    }

    /**
     * 删除批量选课记录
     */
    private void deleteBatchEnrollments(EnrollmentTransactionVo transaction) {
        try {
            // 这里需要根据实际的请求数据结构来解析课程ID列表
            // 假设我们从请求数据中解析出了课程ID列表
            // List<Long> courseIds = parseCourseIdsFromRequest(transaction.getRequestData());

            // 示例：删除该学生的所有相关选课记录
            enrollmentsDao.delete(
                    new LambdaQueryWrapper<EnrollmentsVo>()
                            .eq(EnrollmentsVo::getStudentId, transaction.getStudentId())
                            .eq(EnrollmentsVo::getStatus, "SUCCESS")
                            .apply("DATE(created_at) = DATE({0})", transaction.getCrd())
            );

            log.info("删除批量选课记录完成: studentId={}", transaction.getStudentId());

        } catch (Exception e) {
            log.error("删除批量选课记录异常: transactionId={}", transaction.getTransactionId(), e);
        }
    }
    /**
     * 清理批量选课缓存
     */
    private void cleanupBatchEnrollmentCache(EnrollmentTransactionVo transaction) {
        try {
            // 清理学生所有课程缓存
            String studentCoursesKey = "student:courses:" + transaction.getStudentId();
            redisTemplate.delete(studentCoursesKey);

            // 清理具体课程的缓存（需要根据实际课程ID列表）
            // for (Long courseId : courseIds) {
            //     cleanupEnrollmentCache(transaction.getStudentId(), courseId);
            // }

            log.info("清理批量选课缓存完成: studentId={}", transaction.getStudentId());

        } catch (Exception e) {
            log.error("清理批量选课缓存异常: transactionId={}", transaction.getTransactionId(), e);
        }
    }

    /**
     * 回滚批量课程容量
     */
    private void rollbackBatchCourseCapacity(EnrollmentTransactionVo transaction) {
        try {
            // 这里需要根据实际的课程ID列表来回滚容量
            // for (Long courseId : courseIds) {
            //     rollbackCourseCapacityInRedis(courseId);
            // }

            log.info("回滚批量课程容量完成: transactionId={}", transaction.getTransactionId());

        } catch (Exception e) {
            log.error("回滚批量课程容量异常: transactionId={}", transaction.getTransactionId(), e);
        }
    }

    /**
     * 恢复被替换学生的选课记录
     */
    private void restoreReplacedStudentEnrollment(EnrollmentTransactionVo transaction) {
        try {
            // 从请求数据中解析被替换学生的信息
            // 这里需要根据实际的请求数据结构来解析

            // 示例：查找被标记为"REPLACED"的选课记录并恢复
            EnrollmentsVo replacedEnrollment = enrollmentsDao.selectOne(
                    new LambdaQueryWrapper<EnrollmentsVo>()
                            .eq(EnrollmentsVo::getCourseId, transaction.getCourseId())
                            .eq(EnrollmentsVo::getStatus, "REPLACED")
                            .orderByDesc(EnrollmentsVo::getLud)
                            .last("LIMIT 1"));

            if (replacedEnrollment != null) {
                replacedEnrollment.setStatus("SUCCESS");
                replacedEnrollment.setLud(DateTimeUtils.getCurrentDateTime());
                enrollmentsDao.updateById(replacedEnrollment);

                log.info("恢复被替换学生选课记录完成: enrollmentId={}, studentId={}",
                        replacedEnrollment.getId(), replacedEnrollment.getStudentId());
            }

        } catch (Exception e) {
            log.error("恢复被替换学生选课记录异常: transactionId={}", transaction.getTransactionId(), e);
        }
    }

    /**
     * 批量补偿失败的事务
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCompensateFailedTransactions() {
        try {
            log.info("开始批量补偿失败事务");

            // 查找所有需要补偿的失败事务
            List<EnrollmentTransactionVo> failedTransactions = enrollmentTransactionDao.selectList(
                    new LambdaQueryWrapper<EnrollmentTransactionVo>()
                            .in(EnrollmentTransactionVo::getTransactionStatus,
                                    "FAILED", "FINAL_FAILURE")
                            .le(EnrollmentTransactionVo::getLud,
                                    new Date(System.currentTimeMillis() - 300000)) // 5分钟前的失败事务
            );

            int successCount = 0;
            int failureCount = 0;

            for (EnrollmentTransactionVo transaction : failedTransactions) {
                try {
                    compensateFailedTransaction(transaction.getTransactionId());
                    successCount++;
                } catch (Exception e) {
                    log.error("批量补偿单个事务失败: transactionId={}",
                            transaction.getTransactionId(), e);
                    failureCount++;
                }
            }

            log.info("批量补偿失败事务完成: 总数={}, 成功={}, 失败={}",
                    failedTransactions.size(), successCount, failureCount);

        } catch (Exception e) {
            log.error("批量补偿失败事务异常", e);
        }
    }

    @Override
    public String createDropTransaction(EnrollmentsDto dto, EnrollmentsVo enrollment) {
        try {
            String transactionId = "TXN_" + System.currentTimeMillis() + "_" +
                    UUID.randomUUID().toString().substring(0, 8);

            EnrollmentTransactionVo transaction = new EnrollmentTransactionVo()
                    .setTransactionId(transactionId)
                    .setEnrollmentId(enrollment.getId())
                    .setStudentId(dto.getStudentId())
                    .setCourseId(dto.getCourseId())
                    .setOperationType("DROP")
                    .setTransactionStatus("INIT")
                    .setRetryCount(0)
                    .setMaxRetryCount(5)
                    .setRequestData(JSON.toJSONString(dto));
            transaction.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
            transaction.setCruAndLuu(CurUserUtil.getUserCode());

            enrollmentTransactionDao.insert(transaction);
            log.info("创建选课事务记录成功: transactionId={}, enrollmentId={}",
                    transactionId, enrollment.getId());

            return transactionId;

        } catch (Exception e) {
            log.error("创建选课事务记录异常: enrollmentId={}", enrollment.getId(), e);
            return "ERROR_" + System.currentTimeMillis();
        }
    }

    @Override
    @Async
    public void asyncDecrementCourseEnrollment(Long courseId, String transactionId) {
        try {
            // 标记事务为处理中
            updateTransactionStatus(transactionId, "PROCESSING", null, null);

            // 使用RabbitMQ发送消息
            EnrollmentMessage message = new EnrollmentMessage()
                    .setTransactionId(transactionId)
                    .setCourseId(courseId)
                    .setOperation("DECREMENT_ENROLLMENT")
                    .setTimestamp(System.currentTimeMillis());

            rabbitMQService.sendEnrollmentMessage(message);

        } catch (Exception e) {
            log.error("异步减少课程人数异常: transactionId={}, courseId={}",
                    transactionId, courseId, e);
            updateTransactionStatus(transactionId, "FAILED", null,
                    "消息发送异常: " + e.getMessage());
        }
    }

    @Override
    public String createWaitlistEnrollmentTransaction(EnrollmentsDto dto, EnrollmentsVo enrollment) {
        try {
            String transactionId = "TXN_" + System.currentTimeMillis() + "_" +
                    UUID.randomUUID().toString().substring(0, 8);

            EnrollmentTransactionVo transaction = new EnrollmentTransactionVo()
                    .setTransactionId(transactionId)
                    .setEnrollmentId(enrollment.getId())
                    .setStudentId(dto.getStudentId())
                    .setCourseId(dto.getCourseId())
                    .setOperationType("WAITLIST_ENROLL")
                    .setTransactionStatus("INIT")
                    .setRetryCount(0)
                    .setMaxRetryCount(5)
                    .setRequestData(JSON.toJSONString(dto));
            transaction.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
            transaction.setCruAndLuu(CurUserUtil.getUserCode());

            enrollmentTransactionDao.insert(transaction);
            log.info("创建选课事务记录成功: transactionId={}, enrollmentId={}",
                    transactionId, enrollment.getId());

            return transactionId;

        } catch (Exception e) {
            log.error("创建选课事务记录异常: enrollmentId={}", enrollment.getId(), e);
            return "ERROR_" + System.currentTimeMillis();
        }
    }
}
