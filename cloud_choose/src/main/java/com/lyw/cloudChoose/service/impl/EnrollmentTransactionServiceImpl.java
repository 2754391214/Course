package com.lyw.cloudChoose.service.impl;

import com.lyw.cloudChoose.vo.EnrollmentTransactionVo;
import com.lyw.cloudChoose.mapper.EnrollmentTransactionDao;
import com.lyw.cloudChoose.service.EnrollmentTransactionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 选课本地事务表，用于保证分布式环境下选课操作的最终一致性。记录选课、退课等操作的事务状态，支持重试机制、事务追踪和数据一致性检查。 服务实现类
 * </p>
 *
 * @author lyw
 * @since 2025/10/27
 */
@Service
public class EnrollmentTransactionServiceImpl extends ServiceImpl<EnrollmentTransactionDao, EnrollmentTransactionVo> implements EnrollmentTransactionService {

}
