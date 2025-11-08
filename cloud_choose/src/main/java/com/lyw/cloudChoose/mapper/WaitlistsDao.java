package com.lyw.cloudChoose.mapper;

import com.lyw.cloudChoose.vo.WaitlistsVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 选课等待列表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Mapper
public interface WaitlistsDao extends BaseMapper<WaitlistsVo> {

}
