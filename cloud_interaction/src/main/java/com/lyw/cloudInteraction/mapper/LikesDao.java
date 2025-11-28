package com.lyw.cloudInteraction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyw.cloudInteraction.vo.LikesVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 点赞表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Mapper
public interface LikesDao extends BaseMapper<LikesVo> {

    List<Long> selectUserId(@Param("targetType") String targetType, @Param("targetId") Long targetId);
}
