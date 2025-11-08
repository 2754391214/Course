package com.lyw.cloudInteraction.mapper;

import com.lyw.cloudInteraction.vo.FavoriteItemsVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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
    /**
     * 按目标类型统计收藏数量
     */
    @Select("SELECT target_type as targetType, COUNT(*) as count " +
            "FROM favorite_items " +
            "WHERE favorite_id IN (#{favoriteIds}) " +
            "GROUP BY target_type")
    List<Map<String, Object>> selectFavoriteCountByType(@Param("favoriteIds") List<Long> favoriteIds);

    /**
     * 查询热门收藏目标
     */
    @Select("SELECT target_id as targetId, COUNT(*) as favoriteCount " +
            "FROM favorite_items " +
            "WHERE target_type = #{targetType} " +
            "GROUP BY target_id " +
            "ORDER BY favoriteCount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectPopularTargets(@Param("targetType") String targetType,
                                                   @Param("limit") Integer limit);

    @Select("SELECT favorite_id, COUNT(*) as itemCount FROM favorite_items GROUP BY favorite_id")
    List<Map<String, Object>> getFavoriteItemCounts();
}
