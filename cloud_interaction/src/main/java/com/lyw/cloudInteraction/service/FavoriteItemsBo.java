package com.lyw.cloudInteraction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyw.cloudInteraction.dto.FavoriteItemsDto;
import com.lyw.cloudInteraction.vo.FavoriteItemsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

import java.util.List;

/**
 * <p>
 * 收藏项表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
public interface FavoriteItemsBo extends IService<FavoriteItemsVo> {
    CourseResponseWrapper addFavoriteItem(FavoriteItemsDto dto);

    CourseResponseWrapper removeFavoriteItem(FavoriteItemsDto dto);
    CourseResponseWrapper getFavoriteStatus(String targetType, Long targetId, Long userId);

    CourseResponseWrapper getItemsByTarget(String targetType, Long targetId);

    CourseResponseWrapper getFavoriteItems(Long favoriteId);

    CourseResponseWrapper batchRemoveFavoriteItems(List<FavoriteItemsDto> dtos);

    CourseResponseWrapper moveFavoriteItem(FavoriteItemsDto dto);
}