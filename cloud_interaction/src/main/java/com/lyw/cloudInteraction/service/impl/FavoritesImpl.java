package com.lyw.cloudInteraction.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudInteraction.dto.FavoritesDto;
import com.lyw.cloudInteraction.mapper.FavoriteItemsDao;
import com.lyw.cloudInteraction.mapper.FavoritesDao;
import com.lyw.cloudInteraction.service.FavoritesBo;
import com.lyw.cloudInteraction.vo.FavoriteItemsVo;
import com.lyw.cloudInteraction.vo.FavoritesVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.BeanConverter;
import com.lyw.commonUtil.util.CurUserUtil;
import com.lyw.commonUtil.util.DateTimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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
    @Override
    public CourseResponseWrapper createFavorite(FavoritesDto dto) {
        // 检查收藏夹名称是否重复
        long existingCount = baseMapper.selectCount(
                new LambdaQueryWrapper<FavoritesVo>()
                        .eq(FavoritesVo::getUserId, dto.getUserId())
                        .eq(FavoritesVo::getName, dto.getName())
        );

        if (existingCount > 0) {
            return CourseResponseWrapper.getFailed("收藏夹名称已存在");
        }
        FavoritesVo favoritesVo = BeanConverter.dtoToVo(dto, FavoritesVo.class);
        favoritesVo.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
        favoritesVo.setCruAndLuu(CurUserUtil.getUserId());
        int count = baseMapper.insert(favoritesVo);
        if (count != 1) {
            return CourseResponseWrapper.getFailed("收藏夹创建失败");
        }
        return CourseResponseWrapper.getSuccess("收藏夹创建成功");
    }
    @Override
    public CourseResponseWrapper updateFavorite(FavoritesDto dto) {
        // 检查名称是否重复
        long existingCount = baseMapper.selectCount(
                new LambdaQueryWrapper<FavoritesVo>()
                        .eq(FavoritesVo::getUserId, dto.getUserId())
                        .eq(FavoritesVo::getName, dto.getName())
                        .ne(FavoritesVo::getId, dto.getId())
        );
        if (existingCount > 0) {
            return CourseResponseWrapper.getFailed("收藏夹名称已存在");
        }

        // 更新数据库
        FavoritesVo updateVo = BeanConverter.dtoToVo(dto, FavoritesVo.class);
        updateVo.setLuu(CurUserUtil.getUserId());
        updateVo.setLud(DateTimeUtils.getCurrentDateTime());
        baseMapper.updateById(updateVo);

        return CourseResponseWrapper.getSuccess("收藏夹更新成功");
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper deleteFavorite(Long favoriteId) {
        // 删除收藏夹
        int deleteCount = baseMapper.deleteById(favoriteId);
        if (deleteCount==0) {
            return CourseResponseWrapper.getFailed("收藏夹不存在");
        }
        // 删除收藏夹下的所有收藏项
        favoriteItemsDao.delete(
                new LambdaQueryWrapper<FavoriteItemsVo>()
                        .eq(FavoriteItemsVo::getFavoriteId, favoriteId)
        );
        return CourseResponseWrapper.getSuccess("收藏夹删除成功");
    }

    @Override
    public CourseResponseWrapper getUserFavorites(Long userId) {
        return CourseResponseWrapper.getSuccess(baseMapper.selectList(
                new LambdaQueryWrapper<FavoritesVo>()
                        .eq(FavoritesVo::getUserId, userId)
                        .orderByDesc(FavoritesVo::getCrd)
        ));
    }

    @Override
    public CourseResponseWrapper getFavoriteDetail(Long favoriteId) {
        FavoritesVo favorite = baseMapper.selectById(favoriteId);
        if (ObjectUtil.isEmpty(favorite)) {
            return CourseResponseWrapper.getFailed("收藏夹不存在");
        }
        return CourseResponseWrapper.getSuccess(favorite);
    }
}