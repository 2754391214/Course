package com.lyw.cloudInteraction.service;

import com.lyw.cloudInteraction.vo.ReviewsVo;
import com.lyw.cloudInteraction.dto.ReviewsDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseBo;

/**
 * <p>
 * 评价表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
public interface ReviewsBo extends BaseBo<ReviewsVo,ReviewsDto> {
    CourseResponseWrapper getReviewsByCourseId(Long courseId, Integer page, Integer size, String sort);

    CourseResponseWrapper getReviewsByStudentId(Long studentId, Integer page, Integer size);

    CourseResponseWrapper getCourseReviewStatistics(Long courseId);

    CourseResponseWrapper submitReview(ReviewsDto dto);

    CourseResponseWrapper updateReview(ReviewsDto dto);

    CourseResponseWrapper deleteReview(Long reviewId, Long studentId);

    CourseResponseWrapper auditReview(Long reviewId, String status, String auditRemark);

    CourseResponseWrapper getHotReviews(Integer limit);

    CourseResponseWrapper incrementViewCount(Long reviewId);

    CourseResponseWrapper checkReviewExists(Long courseId, Long studentId);

    CourseResponseWrapper getReviewDetail(Long reviewId);
}