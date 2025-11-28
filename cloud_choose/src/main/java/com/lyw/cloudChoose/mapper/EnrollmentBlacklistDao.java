package com.lyw.cloudChoose.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyw.cloudChoose.vo.EnrollmentBlacklistVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 选课黑名单表，用于管理学生选课限制，支持全局黑名单和课程特定黑名单，保障选课系统的公平性和规范性 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/24
 */
@Mapper
public interface EnrollmentBlacklistDao extends BaseMapper<EnrollmentBlacklistVo> {

    EnrollmentBlacklistVo selectByStudentId(@Param("studentId") Long studentId,@Param("courseId") Long courseId);
}
