package com.lyw.cloudInteraction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyw.cloudInteraction.dto.ReviewsDto;
import com.lyw.cloudInteraction.vo.ReviewsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

/**
 * <p>
 * 评价表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
public interface ReviewsBo extends IService<ReviewsVo> {
    CourseResponseWrapper getReviewsByCourseId(ReviewsDto dto);

    CourseResponseWrapper getReviewsByStudentId(ReviewsDto dto);

    CourseResponseWrapper getCourseReviewStatistics(Long courseId);

    CourseResponseWrapper submitReview(ReviewsDto dto);

    CourseResponseWrapper updateReview(ReviewsDto dto);

    CourseResponseWrapper deleteReview(Long reviewId, Long studentId);

    CourseResponseWrapper auditReview(Long reviewId, String status, String auditRemark);

    CourseResponseWrapper checkReviewExists(Long courseId, Long studentId);

    CourseResponseWrapper getReviewDetail(Long reviewId);
}