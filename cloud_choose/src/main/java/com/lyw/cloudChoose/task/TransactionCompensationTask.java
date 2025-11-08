package com.lyw.cloudChoose.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudChoose.mapper.EnrollmentTransactionDao;
import com.lyw.cloudChoose.service.EnrollmentTransationBo;
import com.lyw.cloudChoose.vo.EnrollmentTransactionVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
@Slf4j
public class TransactionCompensationTask {

    @Resource
    private EnrollmentTransationBo transactionService;
    @Resource
    private EnrollmentTransactionDao enrollmentTransactionDao;

    /**
     * 定时执行事务补偿（每5分钟执行一次）
     */
    @Scheduled(fixedDelay = 300000)
    public void executeTransactionCompensation() {
        try {
            log.info("开始执行定时事务补偿任务");
            transactionService.batchCompensateFailedTransactions();
            log.info("定时事务补偿任务执行完成");
        } catch (Exception e) {
            log.error("定时事务补偿任务异常", e);
        }
    }

    /**
     * 清理已补偿的事务记录（每天凌晨3点执行）
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupCompensatedTransactions() {
        try {
            log.info("开始清理已补偿的事务记录");

            // 删除已补偿事务记录
            int deletedCount = enrollmentTransactionDao.delete(new LambdaQueryWrapper<EnrollmentTransactionVo>()
                    .eq(EnrollmentTransactionVo::getTransactionStatus,"SUCCESS")
                    .lt(EnrollmentTransactionVo::getLud,new Date()));
            log.info("清理已补偿的事务记录完成: 删除了{}条记录", deletedCount);

        } catch (Exception e) {
            log.error("清理已补偿事务记录异常", e);
        }
    }
}