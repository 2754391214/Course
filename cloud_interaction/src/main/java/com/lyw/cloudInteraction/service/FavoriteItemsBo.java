package com.lyw.cloudInteraction.service;

import com.lyw.cloudInteraction.vo.FavoriteItemsVo;
import com.lyw.cloudInteraction.dto.FavoriteItemsDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseBo;

import java.util.List;

/**
 * <p>
 * 收藏项表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
public interface FavoriteItemsBo extends BaseBo<FavoriteItemsVo,FavoriteItemsDto> {
    CourseResponseWrapper getItemsByTarget(String targetType, Long targetId);

    CourseResponseWrapper getUserFavoriteItems(Long userId);

    CourseResponseWrapper getUserItemsByTarget(Long userId, String targetType, Long targetId);

    CourseResponseWrapper batchAddFavoriteItems(List<FavoriteItemsDto> dtos);

    CourseResponseWrapper batchRemoveFavoriteItems(List<FavoriteItemsDto> dtos);

    CourseResponseWrapper moveFavoriteItem(Long itemId, Long targetFavoriteId);

    CourseResponseWrapper copyFavoriteItem(Long itemId, Long targetFavoriteId);

    CourseResponseWrapper getFavoriteItemsStatistics(Long userId, String targetType);

    CourseResponseWrapper searchFavoriteItems(Long userId, String targetType, String keyword);

    CourseResponseWrapper updateItemSort(Long itemId, Integer sortOrder);

    CourseResponseWrapper getPopularFavoriteTargets(String targetType, Integer limit);
}