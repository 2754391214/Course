package com.lyw.cloudMember.mapper;

import com.lyw.cloudMember.vo.TeacherProfileVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 教师信息表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Mapper
public interface TeacherProfileDao extends BaseMapper<TeacherProfileVo> {

    Integer unbindUser(@Param("userId") Long userId, @Param("teacherId") Long teacherId);
}
