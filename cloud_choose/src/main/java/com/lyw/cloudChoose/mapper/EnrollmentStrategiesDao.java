package com.lyw.cloudChoose.mapper;

import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 选课策略配置表，支持多种选课算法和规则 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Mapper
public interface EnrollmentStrategiesDao extends BaseMapper<EnrollmentStrategiesVo> {

}
