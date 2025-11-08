package com.lyw.cloudRanking.service.impl;

import com.lyw.cloudRanking.vo.RankingConfigVo;
import com.lyw.cloudRanking.dto.RankingConfigDto;
import com.lyw.cloudRanking.mapper.RankingConfigDao;
import com.lyw.commonUtil.service.BaseImpl;
import org.springframework.stereotype.Service;
import com.lyw.cloudRanking.service.RankingConfigBo;

import java.util.List;

/**
 * <p>
 * 排行榜配置表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Service
public class RankingConfigImpl extends BaseImpl<RankingConfigDao, RankingConfigVo, RankingConfigDto> implements RankingConfigBo {
    /**
     * 根据编码获取配置
     */
    public RankingConfigVo getConfigByCode(String code) {
        return lambdaQuery()
                .eq(RankingConfigVo::getCode, code)
                .eq(RankingConfigVo::getStatus, 1)
                .one();
    }

    /**
     * 获取所有启用的排行榜配置
     */
    public List<RankingConfigVo> getActiveConfigs() {
        return lambdaQuery()
                .eq(RankingConfigVo::getStatus, 1)
                .list();
    }
}