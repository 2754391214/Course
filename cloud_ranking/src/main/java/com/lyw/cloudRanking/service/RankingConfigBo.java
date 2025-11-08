package com.lyw.cloudRanking.service;

import com.lyw.cloudRanking.vo.RankingConfigVo;
import com.lyw.cloudRanking.dto.RankingConfigDto;
import com.lyw.commonUtil.service.BaseBo;

import java.util.List;

/**
 * <p>
 * 排行榜配置表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
public interface RankingConfigBo extends BaseBo<RankingConfigVo,RankingConfigDto> {
    RankingConfigVo getConfigByCode(String code);
    List<RankingConfigVo> getActiveConfigs();
}