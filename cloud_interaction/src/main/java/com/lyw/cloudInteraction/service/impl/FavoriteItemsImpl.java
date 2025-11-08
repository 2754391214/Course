package com.lyw.cloudInteraction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lyw.cloudInteraction.dto.FavoriteItemsDto;
import com.lyw.cloudInteraction.mapper.FavoriteItemsDao;
import com.lyw.cloudInteraction.mapper.FavoritesDao;
import com.lyw.cloudInteraction.mapper.InteractionsDao;
import com.lyw.cloudInteraction.service.FavoriteItemsBo;
import com.lyw.cloudInteraction.vo.FavoriteItemsVo;
import com.lyw.cloudInteraction.vo.FavoritesVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseImpl;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.CurUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 收藏项表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Slf4j
@Service
public class FavoriteItemsImpl extends BaseImpl<FavoriteItemsDao, FavoriteItemsVo, FavoriteItemsDto> implements FavoriteItemsBo {
    @Resource
    private FavoriteItemsDao favoriteItemsDao;
    @Resource
    private FavoritesDao favoritesDao;
    @Resource
    private InteractionsDao interactionsDao;
    @Override
    public CourseResponseWrapper getItemsByTarget(String targetType, Long targetId) {
        try {
            LambdaQueryWrapper<FavoriteItemsVo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FavoriteItemsVo::getTargetType, targetType)
                    .eq(FavoriteItemsVo::getTargetId, targetId)
                    .orderByDesc(FavoriteItemsVo::getCrd);

            List<FavoriteItemsVo> items = favoriteItemsDao.selectList(queryWrapper);

            // 转换为DTO
            List<FavoriteItemsDto> result = items.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return CourseResponseWrapper.getSuccess(result);
        } catch (Exception e) {
            log.error("根据目标查询收藏项失败: targetType={}, targetId={}", targetType, targetId, e);
            return CourseResponseWrapper.getFailed("查询失败");
        }
    }

    @Override
    public CourseResponseWrapper getUserFavoriteItems(Long userId) {
        try {
            // 查询用户的所有收藏夹
            LambdaQueryWrapper<FavoritesVo> favoritesQuery = new LambdaQueryWrapper<>();
            favoritesQuery.eq(FavoritesVo::getUserId, userId);
            List<FavoritesVo> favorites = favoritesDao.selectList(favoritesQuery);

            if (CollectionUtils.isEmpty(favorites)) {
                return CourseResponseWrapper.getSuccess(Collections.emptyList());
            }

            // 获取所有收藏夹ID
            List<Long> favoriteIds = favorites.stream()
                    .map(FavoritesVo::getId)
                    .collect(Collectors.toList());

            // 查询这些收藏夹中的所有收藏项
            LambdaQueryWrapper<FavoriteItemsVo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(FavoriteItemsVo::getFavoriteId, favoriteIds)
                    .orderByDesc(FavoriteItemsVo::getCrd);

            List<FavoriteItemsVo> items = favoriteItemsDao.selectList(queryWrapper);

            // 按收藏夹分组
            Map<Long, List<FavoriteItemsDto>> groupedItems = items.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.groupingBy(FavoriteItemsDto::getFavoriteId));

            return CourseResponseWrapper.getSuccess(groupedItems);
        } catch (Exception e) {
            log.error("查询用户收藏项失败: userId={}", userId, e);
            return CourseResponseWrapper.getFailed("查询失败");
        }
    }

    @Override
    public CourseResponseWrapper getUserItemsByTarget(Long userId, String targetType, Long targetId) {
        try {

            // 查询用户的所有收藏夹
            LambdaQueryWrapper<FavoritesVo> favoritesQuery = new LambdaQueryWrapper<>();
            favoritesQuery.eq(FavoritesVo::getUserId, userId);
            List<FavoritesVo> favorites = favoritesDao.selectList(favoritesQuery);

            if (CollectionUtils.isEmpty(favorites)) {
                return CourseResponseWrapper.getSuccess(Collections.emptyList());
            }

            List<Long> favoriteIds = favorites.stream()
                    .map(FavoritesVo::getId)
                    .collect(Collectors.toList());

            // 查询指定目标的收藏项
            LambdaQueryWrapper<FavoriteItemsVo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(FavoriteItemsVo::getFavoriteId, favoriteIds)
                    .eq(FavoriteItemsVo::getTargetType, targetType)
                    .eq(FavoriteItemsVo::getTargetId, targetId)
                    .orderByDesc(FavoriteItemsVo::getCrd);

            List<FavoriteItemsVo> items = favoriteItemsDao.selectList(queryWrapper);
            List<FavoriteItemsDto> result = items.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return CourseResponseWrapper.getSuccess(result);
        } catch (Exception e) {
            log.error("查询用户目标收藏项失败: userId={}, targetType={}, targetId={}",
                    userId, targetType, targetId, e);
            return CourseResponseWrapper.getFailed("查询失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper batchAddFavoriteItems(List<FavoriteItemsDto> dtos) {
        try {
            if (CollectionUtils.isEmpty(dtos)) {
                return CourseResponseWrapper.getFailed("收藏项列表不能为空");
            }

            String currentUser = CurUserUtil.getUserCode();
            String now = DateTimeUtils.getCurrentDateTime();
            List<FavoriteItemsVo> itemsToAdd = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (FavoriteItemsDto dto : dtos) {
                // 验证收藏夹是否存在且属于当前用户
                FavoritesVo favorite = favoritesDao.selectById(dto.getFavoriteId());
                if (favorite == null) {
                    errors.add("收藏夹不存在: " + dto.getFavoriteId());
                    continue;
                }

                if (!favorite.getUserId().equals(CurUserUtil.getUserCode())) {
                    errors.add("无权操作此收藏夹: " + dto.getFavoriteId());
                    continue;
                }

                // 检查是否已收藏
                LambdaQueryWrapper<FavoriteItemsVo> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(FavoriteItemsVo::getFavoriteId, dto.getFavoriteId())
                        .eq(FavoriteItemsVo::getTargetType, dto.getTargetType())
                        .eq(FavoriteItemsVo::getTargetId, dto.getTargetId());

                if (favoriteItemsDao.selectCount(checkWrapper) > 0) {
                    errors.add("目标已收藏: " + dto.getTargetType() + "-" + dto.getTargetId());
                    continue;
                }

                // 创建收藏项
                FavoriteItemsVo item = convertToEntity(dto);
                item.setCru(currentUser);
                item.setLuu(currentUser);
                item.setCrd(now);
                item.setLud(now);
                itemsToAdd.add(item);

                // 记录互动
                interactionsDao.saveInteraction(
                        favorite.getUserId(),
                        dto.getTargetType(),
                        dto.getTargetId(),
                        "favorite",
                        Map.of("favoriteName", favorite.getName())
                );
            }

            if (!itemsToAdd.isEmpty()) {
                // 批量插入
                for (FavoriteItemsVo item : itemsToAdd) {
                    favoriteItemsDao.insert(item);
                }

                // 更新收藏夹计数
                updateFavoriteItemCounts1(itemsToAdd);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("successCount", itemsToAdd.size());
            result.put("errorCount", errors.size());
            result.put("errors", errors);

            return CourseResponseWrapper.getSuccess("批量添加完成", result);
        } catch (Exception e) {
            log.error("批量添加收藏项失败", e);
            throw new IllegalArgumentException("批量添加失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper batchRemoveFavoriteItems(List<FavoriteItemsDto> dtos) {
        try {
            if (CollectionUtils.isEmpty(dtos)) {
                return CourseResponseWrapper.getFailed("收藏项列表不能为空");
            }

            String currentUser = CurUserUtil.getUserCode();
            List<Long> removedFavoriteIds = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (FavoriteItemsDto dto : dtos) {
                // 验证收藏夹是否属于当前用户
                FavoritesVo favorite = favoritesDao.selectById(dto.getFavoriteId());
                if (favorite == null) {
                    errors.add("收藏夹不存在: " + dto.getFavoriteId());
                    continue;
                }

                if (!favorite.getUserId().equals(CurUserUtil.getUserCode())) {
                    errors.add("无权操作此收藏夹: " + dto.getFavoriteId());
                    continue;
                }

                // 删除收藏项
                LambdaQueryWrapper<FavoriteItemsVo> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(FavoriteItemsVo::getFavoriteId, dto.getFavoriteId())
                        .eq(FavoriteItemsVo::getTargetType, dto.getTargetType())
                        .eq(FavoriteItemsVo::getTargetId, dto.getTargetId());

                int deleted = favoriteItemsDao.delete(deleteWrapper);
                if (deleted > 0) {
                    removedFavoriteIds.add(dto.getFavoriteId());

                    // 移除互动记录
                    interactionsDao.removeInteraction(
                            favorite.getUserId(),
                            dto.getTargetType(),
                            dto.getTargetId(),
                            "favorite"
                    );
                } else {
                    errors.add("收藏项不存在: " + dto.getTargetType() + "-" + dto.getTargetId());
                }
            }

            // 更新收藏夹计数
            if (!removedFavoriteIds.isEmpty()) {
                updateFavoriteItemCounts(removedFavoriteIds);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("successCount", removedFavoriteIds.size());
            result.put("errorCount", errors.size());
            result.put("errors", errors);

            return CourseResponseWrapper.getSuccess("批量移除完成", result);
        } catch (Exception e) {
            log.error("批量移除收藏项失败", e);
            throw new IllegalArgumentException("批量移除失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper moveFavoriteItem(Long itemId, Long targetFavoriteId) {
        try {
            // 验证原收藏项
            FavoriteItemsVo item = favoriteItemsDao.selectById(itemId);
            if (item == null) {
                return CourseResponseWrapper.getFailed("收藏项不存在");
            }

            // 验证原收藏夹权限
            FavoritesVo sourceFavorite = favoritesDao.selectById(item.getFavoriteId());
            if (sourceFavorite == null || !sourceFavorite.getUserId().equals(CurUserUtil.getUserCode())) {
                return CourseResponseWrapper.getFailed("无权操作原收藏夹");
            }

            // 验证目标收藏夹
            FavoritesVo targetFavorite = favoritesDao.selectById(targetFavoriteId);
            if (targetFavorite == null) {
                return CourseResponseWrapper.getFailed("目标收藏夹不存在");
            }

            if (!targetFavorite.getUserId().equals(CurUserUtil.getUserCode())) {
                return CourseResponseWrapper.getFailed("无权操作目标收藏夹");
            }

            // 检查目标收藏夹是否已存在相同收藏项
            LambdaQueryWrapper<FavoriteItemsVo> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(FavoriteItemsVo::getFavoriteId, targetFavoriteId)
                    .eq(FavoriteItemsVo::getTargetType, item.getTargetType())
                    .eq(FavoriteItemsVo::getTargetId, item.getTargetId());

            if (favoriteItemsDao.selectCount(checkWrapper) > 0) {
                return CourseResponseWrapper.getFailed("目标收藏夹中已存在相同收藏项");
            }

            // 更新收藏项
            String currentUser = CurUserUtil.getUserCode();
            String now = DateTimeUtils.getCurrentDateTime();

            item.setFavoriteId(targetFavoriteId);
            item.setLuu(currentUser);
            item.setLud(now);
            favoriteItemsDao.updateById(item);

            // 更新两个收藏夹的计数
            updateFavoriteItemCount(item.getFavoriteId()); // 原收藏夹
            updateFavoriteItemCount(targetFavoriteId);     // 目标收藏夹

            return CourseResponseWrapper.getSuccess("移动成功");
        } catch (Exception e) {
            log.error("移动收藏项失败: itemId={}, targetFavoriteId={}", itemId, targetFavoriteId, e);
            throw new IllegalArgumentException("移动失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper copyFavoriteItem(Long itemId, Long targetFavoriteId) {
        try {
            // 验证原收藏项
            FavoriteItemsVo originalItem = favoriteItemsDao.selectById(itemId);
            if (originalItem == null) {
                return CourseResponseWrapper.getFailed("收藏项不存在");
            }

            // 验证原收藏夹权限
            FavoritesVo sourceFavorite = favoritesDao.selectById(originalItem.getFavoriteId());
            if (sourceFavorite == null || !sourceFavorite.getUserId().equals(CurUserUtil.getUserCode())) {
                return CourseResponseWrapper.getFailed("无权操作原收藏夹");
            }

            // 验证目标收藏夹
            FavoritesVo targetFavorite = favoritesDao.selectById(targetFavoriteId);
            if (targetFavorite == null) {
                return CourseResponseWrapper.getFailed("目标收藏夹不存在");
            }

            if (!targetFavorite.getUserId().equals(CurUserUtil.getUserCode())) {
                return CourseResponseWrapper.getFailed("无权操作目标收藏夹");
            }

            // 检查目标收藏夹是否已存在相同收藏项
            LambdaQueryWrapper<FavoriteItemsVo> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(FavoriteItemsVo::getFavoriteId, targetFavoriteId)
                    .eq(FavoriteItemsVo::getTargetType, originalItem.getTargetType())
                    .eq(FavoriteItemsVo::getTargetId, originalItem.getTargetId());

            if (favoriteItemsDao.selectCount(checkWrapper) > 0) {
                return CourseResponseWrapper.getFailed("目标收藏夹中已存在相同收藏项");
            }

            // 创建新收藏项
            String currentUser = CurUserUtil.getUserCode();
            String now = DateTimeUtils.getCurrentDateTime();

            FavoriteItemsVo newItem = new FavoriteItemsVo();
            BeanUtils.copyProperties(originalItem, newItem);
            newItem.setId(null); // 清除ID，让数据库自动生成
            newItem.setFavoriteId(targetFavoriteId);
            newItem.setCru(currentUser);
            newItem.setLuu(currentUser);
            newItem.setCrd(now);
            newItem.setLud(now);

            favoriteItemsDao.insert(newItem);

            // 更新目标收藏夹计数
            updateFavoriteItemCount(targetFavoriteId);

            // 记录新的互动
            interactionsDao.saveInteraction(
                    targetFavorite.getUserId(),
                    newItem.getTargetType(),
                    newItem.getTargetId(),
                    "favorite",
                    Map.of("favoriteName", targetFavorite.getName())
            );

            return CourseResponseWrapper.getSuccess("复制成功");
        } catch (Exception e) {
            log.error("复制收藏项失败: itemId={}, targetFavoriteId={}", itemId, targetFavoriteId, e);
            throw new IllegalArgumentException("复制失败");
        }
    }

    @Override
    public CourseResponseWrapper getFavoriteItemsStatistics(Long userId, String targetType) {
        try {
            Map<String, Object> statistics = new HashMap<>();

            if (userId != null) {
                // 用户统计
                LambdaQueryWrapper<FavoritesVo> favoritesQuery = new LambdaQueryWrapper<>();
                favoritesQuery.eq(FavoritesVo::getUserId, userId);
                List<FavoritesVo> userFavorites = favoritesDao.selectList(favoritesQuery);

                if (!CollectionUtils.isEmpty(userFavorites)) {
                    List<Long> favoriteIds = userFavorites.stream()
                            .map(FavoritesVo::getId)
                            .collect(Collectors.toList());

                    // 按目标类型统计
                    LambdaQueryWrapper<FavoriteItemsVo> countWrapper = new LambdaQueryWrapper<>();
                    countWrapper.in(FavoriteItemsVo::getFavoriteId, favoriteIds);

                    if (StringUtils.hasText(targetType)) {
                        countWrapper.eq(FavoriteItemsVo::getTargetType, targetType);
                    }

                    Integer totalCount = favoriteItemsDao.selectCount(countWrapper);
                    statistics.put("totalCount", totalCount);

                    // 按目标类型分组统计
                    if (!StringUtils.hasText(targetType)) {
                        List<Map<String, Object>> typeStats = favoriteItemsDao.selectFavoriteCountByType(favoriteIds);
                        statistics.put("typeStatistics", typeStats);
                    }
                }
            }

            // 全局热门收藏统计
            if (StringUtils.hasText(targetType)) {
                List<Map<String, Object>> popularItems = favoriteItemsDao.selectPopularTargets(targetType, 10);
                statistics.put("popularTargets", popularItems);
            }

            return CourseResponseWrapper.getSuccess(statistics);
        } catch (Exception e) {
            log.error("获取收藏项统计失败: userId={}, targetType={}", userId, targetType, e);
            return CourseResponseWrapper.getFailed("获取统计失败");
        }
    }

    @Override
    public CourseResponseWrapper searchFavoriteItems(Long userId, String targetType, String keyword) {
        try {
            LambdaQueryWrapper<FavoriteItemsVo> queryWrapper = new LambdaQueryWrapper<>();

            // 用户过滤
            if (userId != null) {
                LambdaQueryWrapper<FavoritesVo> favoritesQuery = new LambdaQueryWrapper<>();
                favoritesQuery.eq(FavoritesVo::getUserId, userId);
                List<FavoritesVo> userFavorites = favoritesDao.selectList(favoritesQuery);

                if (CollectionUtils.isEmpty(userFavorites)) {
                    return CourseResponseWrapper.getSuccess(Collections.emptyList());
                }

                List<Long> favoriteIds = userFavorites.stream()
                        .map(FavoritesVo::getId)
                        .collect(Collectors.toList());
                queryWrapper.in(FavoriteItemsVo::getFavoriteId, favoriteIds);
            }

            // 目标类型过滤
            if (StringUtils.hasText(targetType)) {
                queryWrapper.eq(FavoriteItemsVo::getTargetType, targetType);
            }

            // 关键词搜索（备注字段）
            if (StringUtils.hasText(keyword)) {
                queryWrapper.like(FavoriteItemsVo::getNotes, keyword);
            }

            queryWrapper.orderByDesc(FavoriteItemsVo::getCrd);
            List<FavoriteItemsVo> items = favoriteItemsDao.selectList(queryWrapper);

            List<FavoriteItemsDto> result = items.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return CourseResponseWrapper.getSuccess(result);
        } catch (Exception e) {
            log.error("搜索收藏项失败: userId={}, targetType={}, keyword={}", userId, targetType, keyword, e);
            return CourseResponseWrapper.getFailed("搜索失败");
        }
    }

    @Override
    public CourseResponseWrapper updateItemSort(Long itemId, Integer sortOrder) {
        try {
            FavoriteItemsVo item = favoriteItemsDao.selectById(itemId);
            if (item == null) {
                return CourseResponseWrapper.getFailed("收藏项不存在");
            }

            // 验证收藏夹权限
            FavoritesVo favorite = favoritesDao.selectById(item.getFavoriteId());
            if (favorite == null || !favorite.getUserId().equals(CurUserUtil.getUserCode())) {
                return CourseResponseWrapper.getFailed("无权操作此收藏项");
            }

            // 更新排序
            LambdaUpdateWrapper<FavoriteItemsVo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(FavoriteItemsVo::getId, itemId)
                    .set(FavoriteItemsVo::getLuu, CurUserUtil.getUserCode())
                    .set(FavoriteItemsVo::getLud, DateTimeUtils.getCurrentDateTime());

            boolean success = update(updateWrapper);
            return success ? CourseResponseWrapper.getSuccess("排序更新成功")
                    : CourseResponseWrapper.getFailed("排序更新失败");
        } catch (Exception e) {
            log.error("更新收藏项排序失败: itemId={}, sortOrder={}", itemId, sortOrder, e);
            return CourseResponseWrapper.getFailed("更新排序失败");
        }
    }

    @Override
    public CourseResponseWrapper getPopularFavoriteTargets(String targetType, Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }

            List<Map<String, Object>> popularTargets = favoriteItemsDao.selectPopularTargets(targetType, limit);
            return CourseResponseWrapper.getSuccess(popularTargets);
        } catch (Exception e) {
            log.error("获取热门收藏目标失败: targetType={}, limit={}", targetType, limit, e);
            return CourseResponseWrapper.getFailed("获取热门目标失败");
        }
    }

    // 辅助方法
    private FavoriteItemsDto convertToDto(FavoriteItemsVo entity) {
        if (entity == null) {
            return null;
        }
        FavoriteItemsDto dto = new FavoriteItemsDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private FavoriteItemsVo convertToEntity(FavoriteItemsDto dto) {
        if (dto == null) {
            return null;
        }
        FavoriteItemsVo entity = new FavoriteItemsVo();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    private void updateFavoriteItemCounts1(List<FavoriteItemsVo> items) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        // 按收藏夹分组
        Map<Long, List<FavoriteItemsVo>> groupedByFavorite = items.stream()
                .collect(Collectors.groupingBy(FavoriteItemsVo::getFavoriteId));

        for (Long favoriteId : groupedByFavorite.keySet()) {
            updateFavoriteItemCount(favoriteId);
        }
    }

    private void updateFavoriteItemCounts(List<Long> favoriteIds) {
        if (CollectionUtils.isEmpty(favoriteIds)) {
            return;
        }

        for (Long favoriteId : favoriteIds) {
            updateFavoriteItemCount(favoriteId);
        }
    }

    private void updateFavoriteItemCount(Long favoriteId) {
        // 统计收藏夹中的项目数量
        LambdaQueryWrapper<FavoriteItemsVo> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FavoriteItemsVo::getFavoriteId, favoriteId);
        Integer itemCount = favoriteItemsDao.selectCount(countWrapper);

        // 更新收藏夹的项目数量
        FavoritesVo favorite = new FavoritesVo();
        favorite.setId(favoriteId);
        favorite.setItemCount(itemCount);
        favorite.setLuu(CurUserUtil.getUserCode());
        favorite.setLud(DateTimeUtils.getCurrentDateTime());
        favoritesDao.updateById(favorite);
    }
}