package com.lyw.cloudChoose.service;

import com.lyw.cloudChoose.dto.EnrollmentBlacklistDto;
import com.lyw.cloudChoose.vo.EnrollmentBlacklistVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyw.commonUtil.service.BaseBo;

/**
 * <p>
 * 选课黑名单表，用于管理学生选课限制，支持全局黑名单和课程特定黑名单，保障选课系统的公平性和规范性 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/24
 */
public interface EnrollmentBlacklistBo extends BaseBo<EnrollmentBlacklistVo, EnrollmentBlacklistDto> {

}
