package com.lyw.cloudInteraction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyw.cloudInteraction.vo.ReviewsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

}
