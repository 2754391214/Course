package com.lyw.cloudRecommend.mapper;

import com.lyw.cloudRecommend.vo.RecommendResultsVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyw.cloudRecommend.vo.UserCFVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 推荐结果表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Mapper
public interface RecommendResultsDao extends BaseMapper<RecommendResultsVo> {

    void insertOrUpdata(@Param("list") List<UserCFVo> list);
}
