package com.lyw.cloudInteraction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyw.cloudInteraction.vo.ReviewRepliesVo;
import org.apache.ibatis.annotations.Mapper;

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
}
