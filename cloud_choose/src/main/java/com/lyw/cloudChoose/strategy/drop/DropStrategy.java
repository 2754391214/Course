package com.lyw.cloudChoose.strategy.drop;

import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

// 退选策略接口
public interface DropStrategy {
    CourseResponseWrapper drop(EnrollmentsVo enrollment, CoursesDto course,
                               EnrollmentStrategiesVo strategy);
}
