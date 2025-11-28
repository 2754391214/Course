package com.lyw.cloudInteraction.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyw.cloudInteraction.vo.FavoriteItemsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 收藏项表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Mapper
public interface FavoriteItemsDao extends BaseMapper<FavoriteItemsVo> {
    List<Long> selectUserId(@Param("targetType") String targetType, @Param("targetId") Long targetId);
}
