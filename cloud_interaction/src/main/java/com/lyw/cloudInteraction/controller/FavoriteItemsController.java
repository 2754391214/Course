package com.lyw.cloudInteraction.controller;

import com.lyw.cloudInteraction.dto.FavoriteItemsDto;
import com.lyw.cloudInteraction.service.FavoriteItemsBo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
@RequestMapping("favoriteItems")
public class FavoriteItemsController {

    private final FavoriteItemsBo service;

    /**
     * 添加收藏项
     */
    @ApiOperation("添加收藏项")
    @PostMapping("/{favoriteId}/items")
    public CourseResponseWrapper addFavoriteItem(@PathVariable Long favoriteId, @RequestBody FavoriteItemsDto dto) {
        dto.setFavoriteId(favoriteId);
        return service.addFavoriteItem(dto);
    }

    /**
     * 移除收藏项
     */
    @ApiOperation("移除收藏项")
    @DeleteMapping("/{favoriteId}/items")
    public CourseResponseWrapper removeFavoriteItem(@PathVariable Long favoriteId, @RequestBody FavoriteItemsDto dto) {
        dto.setFavoriteId(favoriteId);
        return service.removeFavoriteItem(dto);
    }
    /**
     * 检查收藏状态
     */
    @ApiOperation("检查收藏状态")
    @GetMapping("/status")
    public CourseResponseWrapper getFavoriteStatus(@RequestParam String targetType, @RequestParam Long targetId, @RequestParam Long userId) {
        return service.getFavoriteStatus(targetType, targetId, userId);
    }

    /**
     * 根据目标类型和目标ID查询收藏项
     */
    @ApiOperation("根据目标类型和目标ID查询收藏项")
    @GetMapping("/target")
    public CourseResponseWrapper getItemsByTarget(@RequestParam String targetType, @RequestParam Long targetId) {
        return service.getItemsByTarget(targetType, targetId);
    }

    /**
     * 批量移除收藏项
     */
    @ApiOperation("批量移除收藏项")
    @DeleteMapping("/batch")
    public CourseResponseWrapper batchRemoveFavoriteItems(@RequestBody List<FavoriteItemsDto> dtos) {
        return service.batchRemoveFavoriteItems(dtos);
    }

    /**
     * 移动收藏项到其他收藏夹
     */
    @ApiOperation("移动收藏项到其他收藏夹")
    @PutMapping("/move")
    public CourseResponseWrapper moveFavoriteItem(@RequestBody FavoriteItemsDto dto) {
        return service.moveFavoriteItem(dto);
    }
}