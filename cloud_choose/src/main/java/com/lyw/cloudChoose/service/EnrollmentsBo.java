package com.lyw.cloudChoose.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

/**
 * <p>
 * 选课表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
public interface EnrollmentsBo extends IService<EnrollmentsVo> {

    CourseResponseWrapper enroll(EnrollmentsDto dto);

    CourseResponseWrapper drop(Long enrollmentId, EnrollmentsDto dto);

    CourseResponseWrapper getStudentEnrollments(Long studentId, EnrollmentsDto dto);

    CourseResponseWrapper getStudentTimetable(Long studentId, EnrollmentsDto dto);

    CourseResponseWrapper getCourseEnrollments(Long courseId, EnrollmentsDto dto);

    /**
     * 为等待列表中的学生执行选课
     * 这个方法由系统自动调用，不经过学生主动操作
     */
    CourseResponseWrapper enrollFromWaitlist(EnrollmentsDto enrollDto);
}