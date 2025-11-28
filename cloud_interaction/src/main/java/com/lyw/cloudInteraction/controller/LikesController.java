package com.lyw.cloudInteraction.controller;

import com.lyw.cloudInteraction.dto.LikesDto;
import com.lyw.cloudInteraction.service.LikesBo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 点赞表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 点赞表")
@RestController
@RequestMapping("like")
public class LikesController {

    private final LikesBo service;

    /**
     * 点赞
     */
    @ApiOperation("点赞")
    @PostMapping
    public CourseResponseWrapper addLike(@RequestBody LikesDto dto) {
        return service.addLike(dto);
    }

    /**
     * 取消点赞
     */
    @ApiOperation("取消点赞")
    @DeleteMapping
    public CourseResponseWrapper removeLike(@RequestBody LikesDto dto) {
        return service.removeLike(dto);
    }
    /**
     * 检查点赞状态
     */
    @ApiOperation("检查点赞状态")
    @GetMapping("/status")
    public CourseResponseWrapper getLikeStatus(@RequestParam String targetType, @RequestParam Long targetId, @RequestParam Long userId) {
        return service.getLikeStatus(targetType, targetId, userId);
    }

    /**
     * 查看用户点赞列表
     */
    @ApiOperation("查看用户点赞列表")
    @GetMapping("/user")
    public CourseResponseWrapper getLikesByUser() {
        return service.getLikesByUser();
    }

}