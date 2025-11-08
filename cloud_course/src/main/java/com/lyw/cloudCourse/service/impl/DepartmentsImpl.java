package com.lyw.cloudCourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudCourse.mapper.CoursesDao;
import com.lyw.cloudCourse.vo.CoursesVo;
import com.lyw.cloudCourse.vo.DepartmentsVo;
import com.lyw.cloudCourse.dto.DepartmentsDto;
import com.lyw.cloudCourse.mapper.DepartmentsDao;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseImpl;
import org.springframework.stereotype.Service;
import com.lyw.cloudCourse.service.DepartmentsBo;

import javax.annotation.Resource;

/**
 * <p>
 * 院系信息表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Service
public class DepartmentsImpl extends BaseImpl<DepartmentsDao, DepartmentsVo, DepartmentsDto> implements DepartmentsBo {

    @Resource
    private CoursesDao coursesDao;
    @Override
    public CourseResponseWrapper getDepartmentCourses(Long departmentId, String semester) {
        return CourseResponseWrapper.getSuccess(coursesDao.selectList(new LambdaQueryWrapper<CoursesVo>().eq(CoursesVo::getDepartmentId,departmentId).eq(CoursesVo::getSemester,semester)));
    }
}