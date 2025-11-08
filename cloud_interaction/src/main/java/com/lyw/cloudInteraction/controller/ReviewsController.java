package com.lyw.cloudInteraction.controller;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudInteraction.service.ReviewsBo;
import com.lyw.cloudInteraction.dto.ReviewsDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 评价表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 评价表")
@RestController
@RequestMapping("Reviews")
public class ReviewsController extends BaseController<ReviewsDto> {

    private final ReviewsBo service;

    @Override
    public ReviewsBo getBaseService() {
        return service;
    }
    /**
     * 根据课程ID获取评价列表
     */
    @ApiOperation("根据课程ID获取评价列表")
    @GetMapping("/courses/{courseId}")
    public CourseResponseWrapper getReviewsByCourseId(
            @PathVariable Long courseId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return service.getReviewsByCourseId(courseId, page, size, sort);
    }

    /**
     * 根据学生ID获取评价列表
     */
    @ApiOperation("根据学生ID获取评价列表")
    @GetMapping("/students/{studentId}")
    public CourseResponseWrapper getReviewsByStudentId(
            @PathVariable Long studentId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return service.getReviewsByStudentId(studentId, page, size);
    }

    /**
     * 获取课程评价统计
     */
    @ApiOperation("获取课程评价统计")
    @GetMapping("/courses/{courseId}/statistics")
    public CourseResponseWrapper getCourseReviewStatistics(@PathVariable Long courseId) {
        return service.getCourseReviewStatistics(courseId);
    }

    /**
     * 提交评价
     */
    @ApiOperation("提交评价")
    @PostMapping("/submit")
    public CourseResponseWrapper submitReview(@RequestBody ReviewsDto dto) {
        return service.submitReview(dto);
    }

    /**
     * 更新评价
     */
    @ApiOperation("更新评价")
    @PutMapping("/{reviewId}")
    public CourseResponseWrapper updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewsDto dto) {
        dto.setId(reviewId);
        return service.updateReview(dto);
    }

    /**
     * 删除评价
     */
    @ApiOperation("删除评价")
    @DeleteMapping("/{reviewId}")
    public CourseResponseWrapper deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long studentId) {
        return service.deleteReview(reviewId, studentId);
    }

    /**
     * 审核评价
     */
    @ApiOperation("审核评价")
    @PutMapping("/{reviewId}/audit")
    public CourseResponseWrapper auditReview(
            @PathVariable Long reviewId,
            @RequestParam String status,
            @RequestParam(required = false) String auditRemark) {
        return service.auditReview(reviewId, status, auditRemark);
    }

    /**
     * 获取热门评价
     */
    @ApiOperation("获取热门评价")
    @GetMapping("/hot")
    public CourseResponseWrapper getHotReviews(
            @RequestParam(required = false) Integer limit) {
        return service.getHotReviews(limit);
    }

    /**
     * 增加评价浏览量
     */
    @ApiOperation("增加评价浏览量")
    @PostMapping("/{reviewId}/view")
    public CourseResponseWrapper incrementViewCount(@PathVariable Long reviewId) {
        return service.incrementViewCount(reviewId);
    }

    /**
     * 检查学生是否已评价课程
     */
    @ApiOperation("检查学生是否已评价课程")
    @GetMapping("/check")
    public CourseResponseWrapper checkReviewExists(
            @RequestParam Long courseId,
            @RequestParam Long studentId) {
        return service.checkReviewExists(courseId, studentId);
    }

    /**
     * 获取评价详情（包含统计信息）
     */
    @ApiOperation("获取评价详情")
    @GetMapping("/{reviewId}/detail")
    public CourseResponseWrapper getReviewDetail(@PathVariable Long reviewId) {
        return service.getReviewDetail(reviewId);
    }
}