package com.lyw.cloudInteraction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyw.cloudInteraction.vo.FavoritesVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 收藏夹表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Mapper
public interface FavoritesDao extends BaseMapper<FavoritesVo> {
}
