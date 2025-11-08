// 选课策略接口
package com.lyw.cloudChoose.strategy.enrollment;

import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

// 选课策略接口
public interface EnrollmentStrategy {

    CourseResponseWrapper enroll(EnrollmentsDto request, CoursesDto course,
                                 EnrollmentStrategiesVo strategy);
}