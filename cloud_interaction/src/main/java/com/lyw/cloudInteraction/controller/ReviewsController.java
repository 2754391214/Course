package com.lyw.cloudInteraction.controller;

import com.lyw.cloudInteraction.dto.ReviewsDto;
import com.lyw.cloudInteraction.service.ReviewsBo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("reviews")
public class ReviewsController {

    private final ReviewsBo service;
    /**
     * 根据课程ID获取评价列表
     */
    @ApiOperation("根据课程ID获取评价列表")
    @GetMapping("/courses/{courseId}")
    public CourseResponseWrapper getReviewsByCourseId(@PathVariable Long courseId,ReviewsDto dto) {
        dto.setCourseId(courseId);
        return service.getReviewsByCourseId(dto);
    }

    /**
     * 根据学生ID获取评价列表
     */
    @ApiOperation("根据学生ID获取评价列表")
    @GetMapping("/students/{studentId}")
    public CourseResponseWrapper getReviewsByStudentId(@PathVariable Long studentId,ReviewsDto dto) {
        dto.setStudentId(studentId);
        return service.getReviewsByStudentId(dto);
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
    public CourseResponseWrapper updateReview(@PathVariable Long reviewId, @RequestBody ReviewsDto dto) {
        dto.setId(reviewId);
        return service.updateReview(dto);
    }

    /**
     * 删除评价
     */
    @ApiOperation("删除评价")
    @DeleteMapping("/{reviewId}")
    public CourseResponseWrapper deleteReview(@PathVariable Long reviewId, @RequestParam Long studentId) {
        return service.deleteReview(reviewId, studentId);
    }

    /**
     * 审核评价
     */
    @ApiOperation("审核评价")
    @PutMapping("/{reviewId}/audit")
    public CourseResponseWrapper auditReview(@PathVariable Long reviewId, @RequestParam String status, @RequestParam(required = false) String auditRemark) {
        return service.auditReview(reviewId, status, auditRemark);
    }


    /**
     * 检查学生是否已评价课程
     */
    @ApiOperation("检查学生是否已评价课程")
    @GetMapping("/check")
    public CourseResponseWrapper checkReviewExists(@RequestParam Long courseId, @RequestParam Long studentId) {
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