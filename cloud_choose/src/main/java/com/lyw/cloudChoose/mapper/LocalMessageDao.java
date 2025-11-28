package com.lyw.cloudChoose.mapper;

import com.lyw.cloudChoose.vo.LocalMessageVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 本地消息表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/11/19
 */
@Mapper
public interface LocalMessageDao extends BaseMapper<LocalMessageVo> {
    void batchInsertMessages(@Param("messages") List<LocalMessageVo> messages);
}
