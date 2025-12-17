package com.lyw.cloudRecommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyw.cloudRecommend.vo.UserBehaviorVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户行为表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Mapper
public interface UserBehaviorDao extends BaseMapper<UserBehaviorVo> {
    List<Long> selectCourseIds(@Param("userId") Long userId);
}
