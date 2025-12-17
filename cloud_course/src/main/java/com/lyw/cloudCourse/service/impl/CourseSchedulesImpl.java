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
    public CourseResponseWrapper batchGetCourseSchedules(List<Long> courseIds) {
        return CourseResponseWrapper.getSuccess(courseSchedulesDao.selectList(
                new LambdaQueryWrapper<CourseSchedulesVo>()
                        .in(CourseSchedulesVo::getCourseId, courseIds)));
    }
}