package com.lyw.cloudRanking.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudRanking.service.CourseHeatDailyBo;
import com.lyw.cloudRanking.dto.CourseHeatDailyDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 课程热度日快照表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 课程热度日快照表")
@RestController
@RequestMapping("courseHeatDaily")
public class CourseHeatDailyController extends BaseController<CourseHeatDailyDto> {

    private final CourseHeatDailyBo service;

    @Override
    public CourseHeatDailyBo getBaseService() {
        return service;
    }
}