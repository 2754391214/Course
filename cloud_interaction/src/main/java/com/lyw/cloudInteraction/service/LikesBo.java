package com.lyw.cloudInteraction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyw.cloudInteraction.dto.LikesDto;
import com.lyw.cloudInteraction.vo.LikesVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

/**
 * <p>
 * 点赞表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
public interface LikesBo extends IService<LikesVo> {

    CourseResponseWrapper addLike(LikesDto dto);

    CourseResponseWrapper removeLike(LikesDto dto);

    CourseResponseWrapper getLikeStatus(String targetType, Long targetId, Long userId);

    CourseResponseWrapper getLikesByUser();

}