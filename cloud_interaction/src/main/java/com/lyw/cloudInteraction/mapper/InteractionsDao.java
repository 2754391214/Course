package com.lyw.cloudInteraction.mapper;

import com.lyw.cloudInteraction.vo.InteractionsVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 互动表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Mapper
public interface InteractionsDao extends BaseMapper<InteractionsVo> {

    void saveInteraction(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId, @Param("favorite") String favorite, @Param("favoriteName") Map<String, String> favoriteName);

    void removeInteraction(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId, @Param("favorite") String favorite);

    List<InteractionsVo> getReviewLikeCounts();

    List<InteractionsVo> getReviewUsefulCounts();

    List<InteractionsVo> getReplyLikeCounts();
}
