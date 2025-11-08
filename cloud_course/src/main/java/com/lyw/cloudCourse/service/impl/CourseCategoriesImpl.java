package com.lyw.cloudCourse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyw.cloudCourse.dto.CourseCategoriesDto;
import com.lyw.cloudCourse.mapper.CourseCategoriesDao;
import com.lyw.cloudCourse.mapper.CoursesDao;
import com.lyw.cloudCourse.service.CourseCategoriesBo;
import com.lyw.cloudCourse.vo.CourseCategoriesVo;
import com.lyw.cloudCourse.vo.CoursesVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 课程分类表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Service
public class CourseCategoriesImpl extends BaseImpl<CourseCategoriesDao, CourseCategoriesVo, CourseCategoriesDto> implements CourseCategoriesBo {

    @Resource
    private CoursesDao coursesDao;
    @Override
    public CourseResponseWrapper getCategoryCourses(Long categoryId, CourseCategoriesDto dto) {
        return CourseResponseWrapper.getSuccess(coursesDao.selectCategoryCourses(new Page<>(dto.getPageNo(),dto.getPageSize()),new QueryWrapper<CoursesVo>().eq("cc.id",categoryId)));
    }
}