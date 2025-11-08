package com.lyw.cloudChoose.strategy.drop.impl;

import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.strategy.drop.DropStrategy;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.CurUserUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

// 立即退选策略
@Component("IMMEDIATE_DROP")
public class ImmediateDropStrategy implements DropStrategy {

    @Override
    public CourseResponseWrapper drop(EnrollmentsVo enrollment, CoursesDto course,
                                      EnrollmentStrategiesVo strategy) {
        // 立即退选，不涉及等待列表处理
        EnrollmentsVo updatedEnrollment = new EnrollmentsVo();
        BeanUtils.copyProperties(enrollment, updatedEnrollment);
        updatedEnrollment.setStatus("DROPPED");
        updatedEnrollment.setDroppedAt(new Date());
        updatedEnrollment.setLud(DateTimeUtils.getCurrentDateTime());
        updatedEnrollment.setLuu(CurUserUtil.getUserCode());

        return CourseResponseWrapper.getSuccess("退选成功", updatedEnrollment);
    }
}
