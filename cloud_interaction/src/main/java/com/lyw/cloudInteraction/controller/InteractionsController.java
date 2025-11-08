package com.lyw.cloudInteraction.controller;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudInteraction.service.InteractionsBo;
import com.lyw.cloudInteraction.dto.InteractionsDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 互动表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 互动表")
@RestController
@RequestMapping("Interactions")
public class InteractionsController {

    private final InteractionsBo service;
    /**
     * 点赞评价
     */
    @ApiOperation("点赞评价")
    @PostMapping("/reviews/{reviewId}/like")
    public CourseResponseWrapper likeReview(
            @PathVariable Long reviewId,
            @RequestBody InteractionsDto dto) {
        dto.setTargetType("review")
                .setTargetId(reviewId)
                .setInteractionType("like");
        return service.likeReview(dto);
    }

    /**
     * 取消点赞评价
     */
    @ApiOperation("取消点赞评价")
    @DeleteMapping("/reviews/{reviewId}/like")
    public CourseResponseWrapper unlikeReview(
            @PathVariable Long reviewId,
            @RequestBody InteractionsDto dto) {
        dto.setTargetType("review")
                .setTargetId(reviewId)
                .setInteractionType("like");
        return service.unlikeReview(dto);
    }

    /**
     * 标记评价有用
     */
    @ApiOperation("标记评价有用")
    @PostMapping("/reviews/{reviewId}/useful")
    public CourseResponseWrapper markReviewUseful(
            @PathVariable Long reviewId,
            @RequestBody InteractionsDto dto) {
        dto.setTargetType("review")
                .setTargetId(reviewId)
                .setInteractionType("useful");
        return service.markReviewUseful(dto);
    }

    /**
     * 取消标记有用
     */
    @ApiOperation("取消标记有用")
    @DeleteMapping("/reviews/{reviewId}/useful")
    public CourseResponseWrapper unmarkReviewUseful(
            @PathVariable Long reviewId,
            @RequestBody InteractionsDto dto) {
        dto.setTargetType("review")
                .setTargetId(reviewId)
                .setInteractionType("useful");
        return service.unmarkReviewUseful(dto);
    }

    /**
     * 点赞回复
     */
    @ApiOperation("点赞回复")
    @PostMapping("/replies/{replyId}/like")
    public CourseResponseWrapper likeReply(
            @PathVariable Long replyId,
            @RequestBody InteractionsDto dto) {
        dto.setTargetType("reply")
                .setTargetId(replyId)
                .setInteractionType("like");
        return service.likeReply(dto);
    }

    /**
     * 取消点赞回复
     */
    @ApiOperation("取消点赞回复")
    @DeleteMapping("/replies/{replyId}/like")
    public CourseResponseWrapper unlikeReply(
            @PathVariable Long replyId,
            @RequestBody InteractionsDto dto) {
        dto.setTargetType("reply")
                .setTargetId(replyId)
                .setInteractionType("like");
        return service.unlikeReply(dto);
    }

    /**
     * 获取用户互动状态
     */
    @ApiOperation("获取用户互动状态")
    @GetMapping("/status")
    public CourseResponseWrapper getInteractionStatus(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam Long userId,
            @RequestParam String interactionType) {
        return service.getInteractionStatus(targetType, targetId, userId, interactionType);
    }

    /**
     * 获取用户的互动列表
     */
    @ApiOperation("获取用户的互动列表")
    @GetMapping("/users/{userId}")
    public CourseResponseWrapper getUserInteractions(
            @PathVariable Long userId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String interactionType) {
        return service.getUserInteractions(userId, targetType, interactionType);
    }
}