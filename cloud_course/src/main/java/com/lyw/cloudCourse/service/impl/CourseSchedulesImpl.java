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

            if (ObjectUtil.isEmpty(studentId) || ObjectUtil.isEmpty(courseId)) {
                return CourseResponseWrapper.getFailed("学生ID和课程ID不能为空");
            }

            if (CollectionUtil.isEmpty(enrolledCourseIds)) {
                return CourseResponseWrapper.getSuccess("无时间冲突", false);
            }

            // 直接在数据库层面检查冲突
            Integer conflictCount = courseSchedulesDao.checkTimeConflictInDB(courseId, enrolledCourseIds);

            if (conflictCount != null && conflictCount > 0) {
                log.warn("发现课程时间冲突: studentId={}, courseId={}, conflictCount={}",
                        studentId, courseId, conflictCount);
                return CourseResponseWrapper.getFailed("该课程与其他已选课程存在时间冲突");
            }

            log.info("课程时间检查通过，无冲突: studentId={}, courseId={}", studentId, courseId);
            return CourseResponseWrapper.getSuccess("无时间冲突", false);

        } catch (Exception e) {
            log.error("检查课程时间冲突异常: studentId={}, courseId={}", studentId, courseId, e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        }
    }
}