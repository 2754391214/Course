package com.lyw.cloudMember.mapper;

import com.lyw.cloudMember.vo.UserVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户基础表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Mapper
public interface UserDao extends BaseMapper<UserVo> {

    UserVo selectInfoById(@Param("userId") Long userId);
}
