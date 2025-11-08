package com.lyw.cloudInteraction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyw.cloudInteraction.dto.InteractionsDto;
import com.lyw.cloudInteraction.vo.InteractionsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

import java.util.Map;

/**
 * <p>
 * 互动表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
public interface InteractionsBo extends IService<InteractionsVo> {
    CourseResponseWrapper likeReview(InteractionsDto dto);

    CourseResponseWrapper unlikeReview(InteractionsDto dto);

    CourseResponseWrapper markReviewUseful(InteractionsDto dto);

    CourseResponseWrapper unmarkReviewUseful(InteractionsDto dto);

    CourseResponseWrapper likeReply(InteractionsDto dto);

    CourseResponseWrapper unlikeReply(InteractionsDto dto);

    CourseResponseWrapper getInteractionStatus(String targetType, Long targetId, Long userId, String interactionType);

    CourseResponseWrapper getUserInteractions(Long userId, String targetType, String interactionType);
}