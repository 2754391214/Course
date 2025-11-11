package com.lyw.cloudChoose.strategy.drop.impl;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.service.WaitlistsBo;
import com.lyw.cloudChoose.strategy.drop.DropStrategy;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.CurUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

// 有等待列表的退选策略
@Slf4j
@Component("WAITLIST_DROP")
public class WaitlistDropStrategy implements DropStrategy {

    @Resource
    private WaitlistsBo waitlistService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private DefaultRedisScript<Long> dropScript;
    @Override
    public CourseResponseWrapper drop(EnrollmentsVo enrollment, CoursesDto course,
                                      EnrollmentStrategiesVo strategy) {
        // 使用Lua脚本进行原子性选课检查
        List<String> keys = Arrays.asList(
                "course:current:"+enrollment.getCourseId(),
                "student:enrollment:"+enrollment.getStudentId()+":"+enrollment.getCourseId()
        );
        Long enrollResult = stringRedisTemplate.execute(dropScript, keys);
        if (ObjectUtil.isEmpty(enrollResult)||enrollResult == 2L) {
            return CourseResponseWrapper.getFailed("系统繁忙，请稍后重试");
        } else {
            if (enrollResult == 1L) {
                log.info("Redis中未找到选课记录（需要检查数据库）");
            }
        }

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
