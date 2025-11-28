package com.lyw.cloudChoose.service;

import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

public interface EnrollmentService {
    CourseResponseWrapper handleSuccessfulEnrollment(EnrollmentsDto request, CoursesDto course, Long studentId);

    CourseResponseWrapper handleSuccessfulDrop(EnrollmentsVo request, CoursesDto course, Long studentId);
}
