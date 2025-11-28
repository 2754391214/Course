package com.lyw.cloudRanking.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudRanking.service.RankingConfigBo;
import com.lyw.cloudRanking.dto.RankingConfigDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 排行榜配置表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 排行榜配置表")
@RestController
@RequestMapping("rankingConfig")
public class RankingConfigController extends BaseController<RankingConfigDto> {

    private final RankingConfigBo service;

    @Override
    public RankingConfigBo getBaseService() {
        return service;
    }
}