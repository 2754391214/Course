package com.lyw.cloudChoose.strategy.drop.impl;

import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.service.WaitlistsBo;
import com.lyw.cloudChoose.strategy.drop.DropStrategy;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.CurUserUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Date;
// 有等待列表的退选策略
@Component("WAITLIST_DROP")
public class WaitlistDropStrategy implements DropStrategy {

    @Resource
    private WaitlistsBo waitlistService;

    @Override
    public CourseResponseWrapper drop(EnrollmentsVo enrollment, CoursesDto course,
                                      EnrollmentStrategiesVo strategy) {
        EnrollmentsVo updatedEnrollment = new EnrollmentsVo();
        BeanUtils.copyProperties(enrollment, updatedEnrollment);
        updatedEnrollment.setStatus("DROPPED");
        updatedEnrollment.setDroppedAt(new Date());
        updatedEnrollment.setLud(DateTimeUtils.getCurrentDateTime());
        updatedEnrollment.setLuu(CurUserUtil.getUserCode());

        // 异步处理等待列表
        waitlistService.processWaitlistAfterDrop(course.getId());

        return CourseResponseWrapper.getSuccess("退选成功，已触发等待列表处理", updatedEnrollment);
    }
}
