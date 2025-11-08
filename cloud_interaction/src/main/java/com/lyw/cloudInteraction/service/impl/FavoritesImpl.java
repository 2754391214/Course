package com.lyw.cloudInteraction.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudInteraction.constant.RedisConstant;
import com.lyw.cloudInteraction.dto.FavoriteItemsDto;
import com.lyw.cloudInteraction.dto.FavoritesDto;
import com.lyw.cloudInteraction.mapper.FavoriteItemsDao;
import com.lyw.cloudInteraction.mapper.FavoritesDao;
import com.lyw.cloudInteraction.service.FavoritesBo;
import com.lyw.cloudInteraction.utils.RedisUtil;
import com.lyw.cloudInteraction.vo.FavoriteItemsVo;
import com.lyw.cloudInteraction.vo.FavoritesVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.CurUserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 收藏夹表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Service
public class FavoritesImpl extends ServiceImpl<FavoritesDao, FavoritesVo> implements FavoritesBo {

    @Resource
    private FavoriteItemsDao favoriteItemsDao;
    @Resource
    private RedisUtil redisUtil;
    @Override
    public CourseResponseWrapper createFavorite(FavoritesDto dto) {
        // 检查收藏夹名称是否重复
        LambdaQueryWrapper<FavoritesVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoritesVo::getUserId, dto.getUserId())
                .eq(FavoritesVo::getName, dto.getName());

        long existingCount = getBaseMapper().selectCount(queryWrapper);

        if (existingCount > 0) {
            return CourseResponseWrapper.getFailed("收藏夹名称已存在");
        }

        // 存储到Redis
        String field = RedisConstant.generateFavoriteField(System.currentTimeMillis()); // 使用时间戳作为临时ID
        Map<String, Object> favoriteData = new HashMap<>();
        favoriteData.put("userId", dto.getUserId());
        favoriteData.put("name", dto.getName());
        favoriteData.put("description", dto.getDescription());
        favoriteData.put("isPublic", dto.getIsPublic());
        favoriteData.put("coverImage", dto.getCoverImage());
        favoriteData.put("itemCount", 0);
        favoriteData.put("cru", CurUserUtil.getUserCode());
        favoriteData.put("luu", CurUserUtil.getUserCode());
        favoriteData.put("crd", DateTimeUtils.getCurrentDateTime());
        favoriteData.put("lud", DateTimeUtils.getCurrentDateTime());
        favoriteData.put("syncStatus", 0);

        redisUtil.hSet(RedisConstant.FAVORITE_HASH_KEY, field, favoriteData);

        return CourseResponseWrapper.getSuccess("收藏夹创建成功，数据同步中");
    }

    @Override
    public CourseResponseWrapper addFavoriteItem(FavoriteItemsDto dto) {
        // 验证收藏夹是否存在（从Redis或数据库检查）
        FavoritesVo favorite = getBaseMapper().selectById(dto.getFavoriteId());
        if (ObjectUtil.isEmpty(favorite)) {
            return CourseResponseWrapper.getFailed("收藏夹不存在");
        }

        String field = RedisConstant.generateFavoriteItemField(dto.getFavoriteId(), dto.getTargetType(), dto.getTargetId());

        // 检查是否已经收藏
        if (redisUtil.hExists(RedisConstant.FAVORITE_ITEM_HASH_KEY, field)) {
            return CourseResponseWrapper.getFailed("该内容已在收藏夹中");
        }

        // 存储到Redis
        Map<String, Object> favoriteItemData = new HashMap<>();
        favoriteItemData.put("favoriteId", dto.getFavoriteId());
        favoriteItemData.put("targetType", dto.getTargetType());
        favoriteItemData.put("targetId", dto.getTargetId());
        favoriteItemData.put("notes", dto.getNotes());
        favoriteItemData.put("cru", CurUserUtil.getUserCode());
        favoriteItemData.put("luu", CurUserUtil.getUserCode());
        favoriteItemData.put("crd", DateTimeUtils.getCurrentDateTime());
        favoriteItemData.put("lud", DateTimeUtils.getCurrentDateTime());
        favoriteItemData.put("syncStatus", 0);

        redisUtil.hSet(RedisConstant.FAVORITE_ITEM_HASH_KEY, field, favoriteItemData);

        // 更新Redis中的收藏夹项目计数
        redisUtil.hIncrement(RedisConstant.FAVORITE_ITEM_COUNT_KEY, dto.getFavoriteId().toString(), 1);

        return CourseResponseWrapper.getSuccess("收藏成功，数据同步中");
    }

    @Override
    public CourseResponseWrapper removeFavoriteItem(FavoriteItemsDto dto) {
        String field = RedisConstant.generateFavoriteItemField(dto.getFavoriteId(), dto.getTargetType(), dto.getTargetId());

        if (!redisUtil.hExists(RedisConstant.FAVORITE_ITEM_HASH_KEY, field)) {
            // 检查数据库
            LambdaQueryWrapper<FavoriteItemsVo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FavoriteItemsVo::getFavoriteId, dto.getFavoriteId())
                    .eq(FavoriteItemsVo::getTargetType, dto.getTargetType())
                    .eq(FavoriteItemsVo::getTargetId, dto.getTargetId());

            long dbCount = favoriteItemsDao.selectCount(queryWrapper);
            if (dbCount == 0) {
                return CourseResponseWrapper.getFailed("收藏项不存在");
            }
        }

        // 从Redis中删除
        redisUtil.hDelete(RedisConstant.FAVORITE_ITEM_HASH_KEY, field);

        // 更新Redis中的计数
        redisUtil.hIncrement(RedisConstant.FAVORITE_ITEM_COUNT_KEY, dto.getFavoriteId().toString(), -1);

        return CourseResponseWrapper.getSuccess("取消收藏成功，数据同步中");
    }

    @Override
    public CourseResponseWrapper updateFavorite(FavoritesDto dto) {
        // 直接更新数据库，因为收藏夹信息需要实时性
        FavoritesVo existingFavorite = getBaseMapper().selectById(dto.getId());
        if (ObjectUtil.isEmpty(existingFavorite)) {
            return CourseResponseWrapper.getFailed("收藏夹不存在");
        }

        if (!existingFavorite.getUserId().equals(dto.getUserId())) {
            return CourseResponseWrapper.getFailed("无权修改此收藏夹");
        }

        // 检查名称是否重复
        LambdaQueryWrapper<FavoritesVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoritesVo::getUserId, dto.getUserId())
                .eq(FavoritesVo::getName, dto.getName())
                .ne(FavoritesVo::getId, dto.getId());

        long existingCount = getBaseMapper().selectCount(queryWrapper);
        if (existingCount > 0) {
            return CourseResponseWrapper.getFailed("收藏夹名称已存在");
        }

        // 更新数据库
        FavoritesVo updateVo = new FavoritesVo();
        updateVo.setId(dto.getId())
                .setName(dto.getName())
                .setDescription(dto.getDescription())
                .setIsPublic(dto.getIsPublic())
                .setCoverImage(dto.getCoverImage())
                .setLuu(CurUserUtil.getUserCode())
                .setLud(DateTimeUtils.getCurrentDateTime());

        getBaseMapper().updateById(updateVo);

        return CourseResponseWrapper.getSuccess("收藏夹更新成功");
    }

    @Override
    public CourseResponseWrapper deleteFavorite(Long favoriteId) {
        // 直接删除数据库记录
        FavoritesVo favorite = getBaseMapper().selectById(favoriteId);
        if (ObjectUtil.isEmpty(favorite)) {
            return CourseResponseWrapper.getFailed("收藏夹不存在");
        }

        // 删除收藏夹下的所有收藏项
        LambdaQueryWrapper<FavoriteItemsVo> itemsWrapper = new LambdaQueryWrapper<>();
        itemsWrapper.eq(FavoriteItemsVo::getFavoriteId, favoriteId);
        favoriteItemsDao.delete(itemsWrapper);

        // 删除收藏夹
        getBaseMapper().deleteById(favoriteId);

        return CourseResponseWrapper.getSuccess("收藏夹删除成功");
    }

    @Override
    public CourseResponseWrapper getFavoriteStatus(String targetType, Long targetId, Long userId) {
        // 查询用户的所有收藏夹
        List<FavoritesVo> userFavorites = getBaseMapper().selectList(
                new LambdaQueryWrapper<FavoritesVo>().eq(FavoritesVo::getUserId, userId));

        if (userFavorites.isEmpty()) {
            return CourseResponseWrapper.getSuccess(false);
        }

        // 检查Redis中的收藏状态
        for (FavoritesVo favorite : userFavorites) {
            String field = RedisConstant.generateFavoriteItemField(favorite.getId(), targetType, targetId);
            if (redisUtil.hExists(RedisConstant.FAVORITE_ITEM_HASH_KEY, field)) {
                return CourseResponseWrapper.getSuccess(true);
            }
        }

        // 检查数据库中的收藏状态
        List<Long> favoriteIds = userFavorites.stream()
                .map(FavoritesVo::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<FavoriteItemsVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(FavoriteItemsVo::getFavoriteId, favoriteIds)
                .eq(FavoriteItemsVo::getTargetType, targetType)
                .eq(FavoriteItemsVo::getTargetId, targetId);

        long count = favoriteItemsDao.selectCount(queryWrapper);
        boolean isFavorited = count > 0;

        return CourseResponseWrapper.getSuccess(isFavorited);
    }

    @Override
    public CourseResponseWrapper getUserFavorites(Long userId) {
        LambdaQueryWrapper<FavoritesVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoritesVo::getUserId, userId)
                .orderByDesc(FavoritesVo::getCrd);

        List<FavoritesVo> favorites = getBaseMapper().selectList(queryWrapper);
        return CourseResponseWrapper.getSuccess(favorites);
    }

    @Override
    public CourseResponseWrapper getFavoriteDetail(Long favoriteId) {
        FavoritesVo favorite = getBaseMapper().selectById(favoriteId);
        if (ObjectUtil.isEmpty(favorite)) {
            return CourseResponseWrapper.getFailed("收藏夹不存在");
        }
        return CourseResponseWrapper.getSuccess(favorite);
    }

    @Override
    public CourseResponseWrapper getFavoriteItems(Long favoriteId) {
        LambdaQueryWrapper<FavoriteItemsVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoriteItemsVo::getFavoriteId, favoriteId)
                .orderByDesc(FavoriteItemsVo::getCrd);

        List<FavoriteItemsVo> favoriteItems = favoriteItemsDao.selectList(queryWrapper);
        return CourseResponseWrapper.getSuccess(favoriteItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper updateFavoriteItem(FavoriteItemsDto dto) {
        // 更新收藏项备注
        FavoriteItemsVo updateItem = new FavoriteItemsVo();
        updateItem.setId(dto.getId())
                .setNotes(dto.getNotes());
        updateItem.setLud(DateTimeUtils.getCurrentDateTime());
        updateItem.setLuu(CurUserUtil.getUserCode());

        int updateCount = favoriteItemsDao.updateById(updateItem);

        if (updateCount > 0) {
            return CourseResponseWrapper.getSuccess("收藏项更新成功");
        } else {
            return CourseResponseWrapper.getFailed("收藏项更新失败");
        }
    }


    /**
     * 更新收藏夹项目数量
     */
    private void updateFavoriteItemCount(Long favoriteId) {
        LambdaQueryWrapper<FavoriteItemsVo> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FavoriteItemsVo::getFavoriteId, favoriteId);

        long itemCount = favoriteItemsDao.selectCount(countWrapper);

        FavoritesVo updateVo = new FavoritesVo();
        updateVo.setId(favoriteId)
                .setItemCount((int) itemCount);
        updateVo.setLud(DateTimeUtils.getCurrentDateTime());
        updateVo.setLuu(CurUserUtil.getUserCode());
        getBaseMapper().updateById(updateVo);
    }
}