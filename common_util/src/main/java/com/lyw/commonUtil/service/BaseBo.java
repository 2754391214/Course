package com.lyw.commonUtil.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

public interface BaseBo<T,D> extends IService<T> {
    /**
     * 分页查询,Dto必须继承BaseDto
     * @param dto
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    CourseResponseWrapper searchList(D dto) throws NoSuchFieldException, IllegalAccessException;

    /**
     * 根据ID查询具体数据
     * @param id
     * @return
     */
    CourseResponseWrapper findById(Long id);

    CourseResponseWrapper addDetail(D dto) throws NoSuchFieldException, IllegalAccessException;

    CourseResponseWrapper updateDetail(Long id, D dto) throws NoSuchFieldException, IllegalAccessException;

    CourseResponseWrapper deleteDetail(Long id);
}
