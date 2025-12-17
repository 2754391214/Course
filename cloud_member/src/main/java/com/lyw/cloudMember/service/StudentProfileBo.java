package com.lyw.cloudMember.service;

import com.lyw.cloudMember.vo.StudentProfileVo;
import com.lyw.cloudMember.dto.StudentProfileDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseBo;

import java.util.List;

/**
 * <p>
 * 学生信息表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
public interface StudentProfileBo extends BaseBo<StudentProfileVo,StudentProfileDto> {

    CourseResponseWrapper bindUser(Long userId, Long studentId);

    CourseResponseWrapper unbindUser(Long userId, Long studentId);

    CourseResponseWrapper searchBatchByIds(List<Long> userIds);
}