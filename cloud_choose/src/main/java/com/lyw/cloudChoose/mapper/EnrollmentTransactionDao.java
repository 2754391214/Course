package com.lyw.cloudChoose.mapper;

import com.lyw.cloudChoose.vo.EnrollmentTransactionVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

/**
 * <p>
 * 选课本地事务表，用于保证分布式环境下选课操作的最终一致性。记录选课、退课等操作的事务状态，支持重试机制、事务追踪和数据一致性检查。 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/27
 */
@Mapper
public interface EnrollmentTransactionDao extends BaseMapper<EnrollmentTransactionVo> {

}
