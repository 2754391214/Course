package com.lyw.cloudInteraction.service;

import com.lyw.cloudInteraction.vo.ReviewRepliesVo;
import com.lyw.cloudInteraction.dto.ReviewRepliesDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseBo;

/**
 * <p>
 * 评价回复表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
public interface ReviewRepliesBo extends BaseBo<ReviewRepliesVo,ReviewRepliesDto> {
    CourseResponseWrapper getRepliesByReviewId(Long reviewId, Integer page, Integer size, String sort);

    CourseResponseWrapper getReplyTreeByReviewId(Long reviewId);

    CourseResponseWrapper submitReply(ReviewRepliesDto dto);

    CourseResponseWrapper updateReply(ReviewRepliesDto dto);

    CourseResponseWrapper deleteReply(Long replyId, Long userId);

    CourseResponseWrapper auditReply(Long replyId, String status, String auditRemark);

    CourseResponseWrapper getRepliesByUserId(Long userId, Integer page, Integer size);

    CourseResponseWrapper getReplyDetail(Long replyId);

    CourseResponseWrapper incrementLikeCount(Long replyId);

    CourseResponseWrapper getChildReplies(Long parentId, Integer page, Integer size);
}