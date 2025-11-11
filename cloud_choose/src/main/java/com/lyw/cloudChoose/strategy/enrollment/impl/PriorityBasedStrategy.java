package com.lyw.cloudChoose.strategy.enrollment.impl;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.strategy.enrollment.EnrollmentStrategy;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.cloudChoose.vo.WaitlistsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 优先级策略
 */
@Component("PRIORITY_BASED")
public class PriorityBasedStrategy implements EnrollmentStrategy {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private DefaultRedisScript<Long> enrollScript;
    @Override
    public CourseResponseWrapper enroll(EnrollmentsDto request, CoursesDto course, EnrollmentStrategiesVo strategy) {
        int priority = calculateStudentPriority(request, strategy);

        if (Boolean.TRUE.equals(course.getFullEnrolledCount())) {
            // 根据优先级决定是否替换低优先级学生
            return handlePriorityEnrollment(request, course, strategy, priority);
        } else {
            // 使用Lua脚本进行原子性选课检查
            List<String> keys = Arrays.asList(
                    "course:current:"+request.getCourseId().toString(),
                    "student:enrollment:"+request.getStudentId().toString()+":"+request.getCourseId().toString()
            );
            List<String> args = Arrays.asList(course.getCapacity().toString());
            Long enrollResult = stringRedisTemplate.execute(enrollScript, keys, args.toArray());
            if (ObjectUtil.isEmpty(enrollResult)||enrollResult == 3L) {
                return CourseResponseWrapper.getFailed("系统繁忙，请稍后重试");
            } else {
                if (enrollResult == 1L) {
                    return CourseResponseWrapper.getFailed("您已经选过该课程");
                } else if (enrollResult == 2L) {
                    return handlePriorityEnrollment(request, course, strategy, priority);
                }
            }
            // 直接选课成功
            EnrollmentsVo enrollment = new EnrollmentsVo()
                    .setStudentId(request.getStudentId())
                    .setCourseId(request.getCourseId())
                    .setEnrollmentType(request.getEnrollmentType())
                    .setStatus("SUCCESS")
                    .setPriority(priority)
                    .setEnrolledAt(new Date());
            // 调用课程服务增加选课人数
            return CourseResponseWrapper.getSuccess("选课成功", enrollment);
        }
    }

    private int calculateStudentPriority(EnrollmentsDto request, EnrollmentStrategiesVo strategy) {
        // 实现复杂的优先级计算逻辑
        // 可以考虑专业相关度、学业排名、学分需求等因素
        return 85; // 示例优先级分数
    }

    private CourseResponseWrapper handlePriorityEnrollment(EnrollmentsDto request, CoursesDto course,
                                                           EnrollmentStrategiesVo strategy, int priority) {
        // 实现优先级替换逻辑
        return CourseResponseWrapper.getFailed("课程容量已满，但您的优先级不足以替换现有学生");
    }
}
