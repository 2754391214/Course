package com.lyw.cloudInteraction.mapper;

import com.lyw.cloudInteraction.vo.ReviewRepliesVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 评价回复表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Mapper
public interface ReviewRepliesDao extends BaseMapper<ReviewRepliesVo> {
    int incrementLikeCount(@Param("replyId") Long replyId);

    int decrementLikeCount(@Param("replyId") Long replyId);

    int updateLikeCount(@Param("replyId") Long replyId, @Param("count") Long count);

    // 在ReviewRepliesDao中添加
    @Select("SELECT like_count FROM review_replies WHERE id = #{replyId}")
    Long getLikeCount(Long replyId);
}
