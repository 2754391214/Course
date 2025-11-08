package com.lyw.cloudInteraction.mapper;

import com.lyw.cloudInteraction.vo.ReviewsVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 评价表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Mapper
public interface ReviewsDao extends BaseMapper<ReviewsVo> {
    int incrementLikeCount(@Param("reviewId") Long reviewId);

    int decrementLikeCount(@Param("reviewId") Long reviewId);

    int incrementUsefulCount(@Param("reviewId") Long reviewId);

    int decrementUsefulCount(@Param("reviewId") Long reviewId);

    int updateLikeCount(@Param("reviewId") Long reviewId, @Param("count") Long count);

    int updateUsefulCount(@Param("reviewId") Long reviewId, @Param("count") Long count);

    /**
     * 获取课程评价统计
     */
    ReviewsVo selectReviewStatistics(@Param("courseId") Long courseId);

    /**
     * 获取评分分布
     */
    List<ReviewsVo> selectRatingDistribution(@Param("courseId") Long courseId);

    /**
     * 获取标签统计
     */
    List<ReviewsVo> selectTagStatistics(@Param("courseId") Long courseId);

    /**
     * 增加回复计数
     */
    int incrementReplyCount(@Param("reviewId") Long reviewId);

    /**
     * 减少回复计数
     */
    int decrementReplyCount(@Param("reviewId") Long reviewId);

    @Select("SELECT like_count FROM reviews WHERE id = #{reviewId}")
    Long getLikeCount(Long reviewId);

    @Select("SELECT useful_count FROM reviews WHERE id = #{reviewId}")
    Long getUsefulCount(Long reviewId);
}
