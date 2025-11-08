package com.lyw.cloudCourse.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudCourse.vo.CourseSchedulesVo;
import com.lyw.cloudCourse.dto.CourseSchedulesDto;
import com.lyw.cloudCourse.mapper.CourseSchedulesDao;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.lyw.cloudCourse.service.CourseSchedulesBo;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程时间安排表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Service
@Slf4j
public class CourseSchedulesImpl extends BaseImpl<CourseSchedulesDao, CourseSchedulesVo, CourseSchedulesDto> implements CourseSchedulesBo {

    @Resource
    private CourseSchedulesDao courseSchedulesDao;
    @Override
    public CourseResponseWrapper checkTimeConflict(Long studentId, Long courseId, List<Long> enrolledCourseIds) {
        try {
            log.info("开始检查课程时间冲突: studentId={}, courseId={}", studentId, courseId);

            // 1. 验证参数
            if (ObjectUtil.isEmpty(studentId) || ObjectUtil.isEmpty(courseId)) {
                return CourseResponseWrapper.getFailed("学生ID和课程ID不能为空");
            }

            // 2. 获取目标课程的时间安排
            List<CourseSchedulesVo> targetSchedules = courseSchedulesDao.selectList(
                    new LambdaQueryWrapper<CourseSchedulesVo>()
                            .eq(CourseSchedulesVo::getCourseId,courseId));
            if (ObjectUtil.isEmpty(targetSchedules) || targetSchedules.isEmpty()) {
                log.info("目标课程没有时间安排，无需检查冲突: courseId={}", courseId);
                return CourseResponseWrapper.getSuccess("无时间冲突", false);
            }

            // 3. 获取学生已选课程的时间安排
            if (CollectionUtil.isEmpty(enrolledCourseIds)) {
                return CourseResponseWrapper.getSuccess("无时间冲突", false);
            }
            List<CourseSchedulesVo> studentSchedules = courseSchedulesDao.selectList(
                    new LambdaQueryWrapper<CourseSchedulesVo>()
                            .in(CourseSchedulesVo::getCourseId,enrolledCourseIds));
            if (ObjectUtil.isEmpty(studentSchedules)) {
                log.info("学生没有已选课程，无需检查冲突: studentId={}", studentId);
                return CourseResponseWrapper.getSuccess("无时间冲突", false);
            }

            // 4. 检查时间冲突
            if (checkScheduleConflicts(targetSchedules, studentSchedules)) {
                log.warn("发现课程时间冲突: studentId={}, courseId={}",
                        studentId, courseId);
                return CourseResponseWrapper.getFailed("该课程与其他已选课程存在时间冲突");
            }

            log.info("课程时间检查通过，无冲突: studentId={}, courseId={}", studentId, courseId);
            return CourseResponseWrapper.getSuccess("无时间冲突", false);

        } catch (Exception e) {
            log.error("检查课程时间冲突异常: studentId={}, courseId={}", studentId, courseId, e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        }
    }
    /**
     * 检查时间安排冲突
     */
    private Boolean checkScheduleConflicts(List<CourseSchedulesVo> targetSchedules,
                                           List<CourseSchedulesVo> studentSchedules) {
        // 按星期几分组，提高比较效率
        Map<String, List<CourseSchedulesVo>> targetSchedulesByDay = targetSchedules.stream()
                .collect(Collectors.groupingBy(CourseSchedulesVo::getDayOfWeek));

        Map<String, List<CourseSchedulesVo>> studentSchedulesByDay = studentSchedules.stream()
                .collect(Collectors.groupingBy(CourseSchedulesVo::getDayOfWeek));

        // 只检查有相同星期几的课程
        for (String dayOfWeek : targetSchedulesByDay.keySet()) {
            if (!studentSchedulesByDay.containsKey(dayOfWeek)) {
                continue;
            }

            List<CourseSchedulesVo> targetDaySchedules = targetSchedulesByDay.get(dayOfWeek);
            List<CourseSchedulesVo> studentDaySchedules = studentSchedulesByDay.get(dayOfWeek);

            // 检查同一天的时间冲突
            for (CourseSchedulesVo targetSchedule : targetDaySchedules) {
                for (CourseSchedulesVo studentSchedule : studentDaySchedules) {
                    if (isTimeConflict(targetSchedule, studentSchedule)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 检查两个时间安排是否冲突
     */
    private boolean isTimeConflict(CourseSchedulesVo schedule1, CourseSchedulesVo schedule2) {
        if (ObjectUtil.isEmpty(schedule1.getStartTime()) || ObjectUtil.isEmpty(schedule1.getEndTime()) ||
                ObjectUtil.isEmpty(schedule2.getStartTime()) || ObjectUtil.isEmpty(schedule2.getEndTime())) {
            return false;
        }

        // 时间冲突判断：两个时间段有重叠
        return schedule1.getStartTime().before(schedule2.getEndTime()) &&
                schedule1.getEndTime().after(schedule2.getStartTime());
    }
}