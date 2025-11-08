package com.lyw.cloudInteraction.controller;

import com.lyw.cloudInteraction.dto.FavoriteItemsDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudInteraction.service.FavoritesBo;
import com.lyw.cloudInteraction.dto.FavoritesDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 收藏夹表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 收藏夹表")
@RestController
@RequestMapping("Favorites")
public class FavoritesController {

    private final FavoritesBo service;

    /**
     * 创建收藏夹
     */
    @ApiOperation("创建收藏夹")
    @PostMapping
    public CourseResponseWrapper createFavorite(@RequestBody FavoritesDto dto) {
        return service.createFavorite(dto);
    }

    /**
     * 更新收藏夹
     */
    @ApiOperation("更新收藏夹")
    @PutMapping("/{favoriteId}")
    public CourseResponseWrapper updateFavorite(
            @PathVariable Long favoriteId,
            @RequestBody FavoritesDto dto) {
        dto.setId(favoriteId);
        return service.updateFavorite(dto);
    }

    /**
     * 删除收藏夹
     */
    @ApiOperation("删除收藏夹")
    @DeleteMapping("/{favoriteId}")
    public CourseResponseWrapper deleteFavorite(
            @PathVariable Long favoriteId) {
        return service.deleteFavorite(favoriteId);
    }

    /**
     * 获取用户的收藏夹列表
     */
    @ApiOperation("获取用户的收藏夹列表")
    @GetMapping("/users/{userId}")
    public CourseResponseWrapper getUserFavorites(@PathVariable Long userId) {
        return service.getUserFavorites(userId);
    }

    /**
     * 获取收藏夹详情
     */
    @ApiOperation("获取收藏夹详情")
    @GetMapping("/{favoriteId}")
    public CourseResponseWrapper getFavoriteDetail(@PathVariable Long favoriteId) {
        return service.getFavoriteDetail(favoriteId);
    }

    /**
     * 添加收藏项
     */
    @ApiOperation("添加收藏项")
    @PostMapping("/{favoriteId}/items")
    public CourseResponseWrapper addFavoriteItem(
            @PathVariable Long favoriteId,
            @RequestBody FavoriteItemsDto dto) {
        dto.setFavoriteId(favoriteId);
        return service.addFavoriteItem(dto);
    }

    /**
     * 移除收藏项
     */
    @ApiOperation("移除收藏项")
    @DeleteMapping("/{favoriteId}/items")
    public CourseResponseWrapper removeFavoriteItem(
            @PathVariable Long favoriteId,
            @RequestBody FavoriteItemsDto dto) {
        dto.setFavoriteId(favoriteId);
        return service.removeFavoriteItem(dto);
    }

    /**
     * 获取收藏夹内的项目列表
     */
    @ApiOperation("获取收藏夹内的项目列表")
    @GetMapping("/{favoriteId}/items")
    public CourseResponseWrapper getFavoriteItems(@PathVariable Long favoriteId) {
        return service.getFavoriteItems(favoriteId);
    }

    /**
     * 更新收藏项备注
     */
    @ApiOperation("更新收藏项备注")
    @PutMapping("/{favoriteId}/items/{itemId}")
    public CourseResponseWrapper updateFavoriteItem(
            @PathVariable Long favoriteId,
            @PathVariable Long itemId,
            @RequestBody FavoriteItemsDto dto) {
        dto.setFavoriteId(favoriteId)
                .setId(itemId);
        return service.updateFavoriteItem(dto);
    }

    /**
     * 检查用户收藏状态
     */
    @ApiOperation("检查用户收藏状态")
    @GetMapping("/status")
    public CourseResponseWrapper getFavoriteStatus(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam Long userId) {
        return service.getFavoriteStatus(targetType, targetId, userId);
    }
}