package com.lyw.cloudInteraction.service.impl;

import com.lyw.cloudInteraction.dto.ReportsDto;
import com.lyw.cloudInteraction.mapper.ReportsDao;
import com.lyw.cloudInteraction.service.ReportsBo;
import com.lyw.cloudInteraction.vo.ReportsVo;
import com.lyw.commonUtil.service.BaseImpl;
import org.springframework.stereotype.Service;
/**
 * <p>
 * 举报表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Service
public class ReportsImpl extends BaseImpl<ReportsDao, ReportsVo, ReportsDto> implements ReportsBo {

}