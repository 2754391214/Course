package com.lyw.cloudChoose.mapper;

import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 选课表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Mapper
public interface EnrollmentsDao extends BaseMapper<EnrollmentsVo> {

    List<Long> selectEnrolledCourseIds(@Param("studentId") Long studentId);
}
