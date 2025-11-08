package com.lyw.cloudInteraction.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudInteraction.service.ReportsBo;
import com.lyw.cloudInteraction.dto.ReportsDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 举报表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 举报表")
@RestController
@RequestMapping("Reports")
public class ReportsController extends BaseController<ReportsDto> {

    private final ReportsBo service;

    @Override
    public ReportsBo getBaseService() {
        return service;
    }
}