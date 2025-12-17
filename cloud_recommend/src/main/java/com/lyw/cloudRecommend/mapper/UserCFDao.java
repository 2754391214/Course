package com.lyw.cloudRecommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyw.cloudRecommend.vo.UserCFVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户画像表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Mapper
public interface UserCFDao extends BaseMapper<UserCFVo> {

}
