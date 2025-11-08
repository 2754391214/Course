package com.lyw.cloudInteraction.controller;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudInteraction.service.FavoriteItemsBo;
import com.lyw.cloudInteraction.dto.FavoriteItemsDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 收藏项表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 收藏项表")
@RestController
@RequestMapping("FavoriteItems")
public class FavoriteItemsController extends BaseController<FavoriteItemsDto> {

    private final FavoriteItemsBo service;

    @Override
    public FavoriteItemsBo getBaseService() {
        return service;
    }

    /**
     * 根据目标类型和目标ID查询收藏项
     */
    @ApiOperation("根据目标类型和目标ID查询收藏项")
    @GetMapping("/target")
    public CourseResponseWrapper getItemsByTarget(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return service.getItemsByTarget(targetType, targetId);
    }

    /**
     * 根据用户ID查询所有收藏项
     */
    @ApiOperation("根据用户ID查询所有收藏项")
    @GetMapping("/users/{userId}")
    public CourseResponseWrapper getUserFavoriteItems(@PathVariable Long userId) {
        return service.getUserFavoriteItems(userId);
    }

    /**
     * 根据用户ID和目标查询收藏项
     */
    @ApiOperation("根据用户ID和目标查询收藏项")
    @GetMapping("/users/{userId}/target")
    public CourseResponseWrapper getUserItemsByTarget(
            @PathVariable Long userId,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return service.getUserItemsByTarget(userId, targetType, targetId);
    }

    /**
     * 批量添加收藏项
     */
    @ApiOperation("批量添加收藏项")
    @PostMapping("/batch")
    public CourseResponseWrapper batchAddFavoriteItems(@RequestBody java.util.List<FavoriteItemsDto> dtos) {
        return service.batchAddFavoriteItems(dtos);
    }

    /**
     * 批量移除收藏项
     */
    @ApiOperation("批量移除收藏项")
    @DeleteMapping("/batch")
    public CourseResponseWrapper batchRemoveFavoriteItems(@RequestBody java.util.List<FavoriteItemsDto> dtos) {
        return service.batchRemoveFavoriteItems(dtos);
    }

    /**
     * 移动收藏项到其他收藏夹
     */
    @ApiOperation("移动收藏项到其他收藏夹")
    @PutMapping("/{itemId}/move")
    public CourseResponseWrapper moveFavoriteItem(
            @PathVariable Long itemId,
            @RequestParam Long targetFavoriteId) {
        return service.moveFavoriteItem(itemId, targetFavoriteId);
    }

    /**
     * 复制收藏项到其他收藏夹
     */
    @ApiOperation("复制收藏项到其他收藏夹")
    @PostMapping("/{itemId}/copy")
    public CourseResponseWrapper copyFavoriteItem(
            @PathVariable Long itemId,
            @RequestParam Long targetFavoriteId) {
        return service.copyFavoriteItem(itemId, targetFavoriteId);
    }

    /**
     * 获取收藏项的统计信息
     */
    @ApiOperation("获取收藏项的统计信息")
    @GetMapping("/statistics")
    public CourseResponseWrapper getFavoriteItemsStatistics(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String targetType) {
        return service.getFavoriteItemsStatistics(userId, targetType);
    }

    /**
     * 搜索收藏项
     */
    @ApiOperation("搜索收藏项")
    @GetMapping("/search")
    public CourseResponseWrapper searchFavoriteItems(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String keyword) {
        return service.searchFavoriteItems(userId, targetType, keyword);
    }

    /**
     * 更新收藏项排序
     */
    @ApiOperation("更新收藏项排序")
    @PutMapping("/{itemId}/sort")
    public CourseResponseWrapper updateItemSort(
            @PathVariable Long itemId,
            @RequestParam Integer sortOrder) {
        return service.updateItemSort(itemId, sortOrder);
    }

    /**
     * 获取热门收藏目标
     */
    @ApiOperation("获取热门收藏目标")
    @GetMapping("/popular")
    public CourseResponseWrapper getPopularFavoriteTargets(
            @RequestParam String targetType,
            @RequestParam(defaultValue = "10") Integer limit) {
        return service.getPopularFavoriteTargets(targetType, limit);
    }
}