package com.lyw.cloudInteraction.controller;

import com.lyw.cloudInteraction.dto.ReviewRepliesDto;
import com.lyw.cloudInteraction.service.ReviewRepliesBo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 评价回复表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 评价回复表")
@RestController
@RequestMapping("reviewReplies")
public class ReviewRepliesController {

    private final ReviewRepliesBo service;

    /**
     * 根据评价ID获取回复列表
     */
    @ApiOperation("根据评价ID获取回复列表")
    @GetMapping("/reviews/{reviewId}")
    public CourseResponseWrapper getRepliesByReviewId(@PathVariable Long reviewId, ReviewRepliesDto dto) {
        dto.setReviewId(reviewId);
        return service.getRepliesByReviewId(dto);
    }

    /**
     * 获取回复的树形结构
     */
    @ApiOperation("获取回复的树形结构")
    @GetMapping("/reviews/{reviewId}/tree")
    public CourseResponseWrapper getReplyTreeByReviewId(@PathVariable Long reviewId) {
        return service.getReplyTreeByReviewId(reviewId);
    }

    /**
     * 提交回复
     */
    @ApiOperation("提交回复")
    @PostMapping("/submit")
    public CourseResponseWrapper submitReply(@RequestBody ReviewRepliesDto dto) {
        return service.submitReply(dto);
    }

    /**
     * 回复回复（二级回复）
     */
    @ApiOperation("回复回复")
    @PostMapping("/{parentId}/reply")
    public CourseResponseWrapper replyToReply(@PathVariable Long parentId, @RequestBody ReviewRepliesDto dto) {
        dto.setParentId(parentId);
        return service.submitReply(dto);
    }

    /**
     * 更新回复
     */
    @ApiOperation("更新回复")
    @PutMapping("/{replyId}")
    public CourseResponseWrapper updateReply(@PathVariable Long replyId, @RequestBody ReviewRepliesDto dto) {
        dto.setId(replyId);
        return service.updateReply(dto);
    }

    /**
     * 删除回复
     */
    @ApiOperation("删除回复")
    @DeleteMapping("/{replyId}")
    public CourseResponseWrapper deleteReply(@PathVariable Long replyId, @RequestParam Long userId) {
        return service.deleteReply(replyId, userId);
    }

    /**
     * 审核回复
     */
    @ApiOperation("审核回复")
    @PutMapping("/{replyId}/audit")
    public CourseResponseWrapper auditReply(@PathVariable Long replyId, @RequestParam String status, @RequestParam(required = false) String auditRemark) {
        return service.auditReply(replyId, status, auditRemark);
    }

    /**
     * 获取用户的所有回复
     */
    @ApiOperation("获取用户的所有回复")
    @GetMapping("/users/{userId}")
    public CourseResponseWrapper getRepliesByUserId(@PathVariable Long userId, ReviewRepliesDto dto) {
        dto.setUserId(userId);
        return service.getRepliesByUserId(dto);
    }

    /**
     * 获取回复详情
     */
    @ApiOperation("获取回复详情")
    @GetMapping("/{replyId}/detail")
    public CourseResponseWrapper getReplyDetail(@PathVariable Long replyId) {
        return service.getReplyDetail(replyId);
    }


    /**
     * 获取子回复列表
     */
    @ApiOperation("获取子回复列表")
    @GetMapping("/{parentId}/children")
    public CourseResponseWrapper getChildReplies(@PathVariable Long parentId, ReviewRepliesDto dto) {
        dto.setParentId(parentId);
        return service.getChildReplies(dto);
    }
}