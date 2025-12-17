package com.lyw.cloudMember.service;

import com.lyw.cloudMember.vo.TeacherProfileVo;
import com.lyw.cloudMember.dto.TeacherProfileDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseBo;

/**
 * <p>
 * 教师信息表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
public interface TeacherProfileBo extends BaseBo<TeacherProfileVo,TeacherProfileDto> {

    CourseResponseWrapper bindUser(Long userId, Long teacherId);

    CourseResponseWrapper unbindUser(Long userId, Long teacherId);
}