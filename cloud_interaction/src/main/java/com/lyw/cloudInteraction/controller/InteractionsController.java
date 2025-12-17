package com.lyw.cloudInteraction.controller;

import com.lyw.cloudInteraction.service.InteractionsBo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 收藏项表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 互动")
@RestController
@RequestMapping("interaction")
public class InteractionsController {

    private final InteractionsBo service;
    /**
     * 检查全部状态
     */
    @ApiOperation("检查全部状态")
    @GetMapping("/status")
    public CourseResponseWrapper getFavoriteStatus(@RequestParam String targetType, @RequestParam Long targetId, @RequestParam Long userId) {
        return service.getAllStatus(targetType, targetId, userId);
    }
}