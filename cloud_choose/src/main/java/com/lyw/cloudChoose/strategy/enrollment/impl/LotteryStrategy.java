package com.lyw.cloudChoose.strategy.enrollment.impl;

import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.strategy.enrollment.EnrollmentStrategy;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.WaitlistsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 抽签策略
 */
@Component("LOTTERY")
public class LotteryStrategy implements EnrollmentStrategy {

    @Override
    public CourseResponseWrapper enroll(EnrollmentsDto request, CoursesDto course, EnrollmentStrategiesVo strategy) {
        // 在抽签时间之前，所有申请都进入等待列表
        if (new Date().before(strategy.getLotteryTime())) {
            WaitlistsVo waitlist = new WaitlistsVo();
            waitlist.setStudentId(request.getStudentId());
            waitlist.setCourseId(request.getCourseId());
            waitlist.setStatus("WAITING");
            waitlist.setJoinedAt(new Date());
            waitlist.setPriority(calculatePriority(request, strategy));

            return CourseResponseWrapper.getSuccess("已加入抽签等待列表", waitlist);
        }

        return CourseResponseWrapper.getFailed("抽签已结束");
    }

    private Integer calculatePriority(EnrollmentsDto request, EnrollmentStrategiesVo strategy) {
        // 根据优先级规则计算优先级
        // 这里可以扩展为更复杂的计算逻辑
        return 1;
    }
}
