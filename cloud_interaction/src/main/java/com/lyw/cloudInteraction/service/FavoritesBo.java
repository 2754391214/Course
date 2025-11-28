package com.lyw.cloudInteraction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyw.cloudInteraction.dto.FavoritesDto;
import com.lyw.cloudInteraction.vo.FavoritesVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

/**
 * <p>
 * 收藏夹表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
public interface FavoritesBo extends IService<FavoritesVo> {
    CourseResponseWrapper createFavorite(FavoritesDto dto);

    CourseResponseWrapper updateFavorite(FavoritesDto dto);

    CourseResponseWrapper deleteFavorite(Long favoriteId);

    CourseResponseWrapper getUserFavorites(Long userId);

    CourseResponseWrapper getFavoriteDetail(Long favoriteId);
}