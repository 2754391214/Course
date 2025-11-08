package com.lyw.commonUtil.controller;

import com.lyw.commonUtil.service.BaseBo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.web.bind.annotation.*;

/*
    简化开发流程,将基本的CURD，包装到该方法中
 */
public abstract class BaseController<Dto> {

    public abstract BaseBo getBaseService();

    //查询所有Vo信息
    @GetMapping
    public CourseResponseWrapper searchList(Dto dto) throws NoSuchFieldException, IllegalAccessException {
        return getBaseService().searchList(dto);
    }

    //查询具体Vo信息
    @GetMapping("/{id}")
    public CourseResponseWrapper searchDetail(@PathVariable Long id){
        return getBaseService().findById(id);
    }

    //添加Vo信息
    @PostMapping
    public CourseResponseWrapper addDetail(@RequestBody Dto dto) throws NoSuchFieldException, IllegalAccessException {
        return getBaseService().addDetail(dto);
    }

    //更新Vo信息
    @PutMapping("/{id}")
    public CourseResponseWrapper updateDetail(@PathVariable Long id,@RequestBody Dto dto) throws NoSuchFieldException, IllegalAccessException {
        return getBaseService().updateDetail(id,dto);
    }
    //删除Vo信息
    @DeleteMapping("/{id}")
    public CourseResponseWrapper deleteDetail(@PathVariable Long id){
        return getBaseService().deleteDetail(id);
    }

}
