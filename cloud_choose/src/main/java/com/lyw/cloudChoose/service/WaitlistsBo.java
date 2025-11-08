package com.lyw.cloudChoose.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyw.cloudChoose.vo.WaitlistsVo;
import com.lyw.cloudChoose.dto.WaitlistsDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseBo;

/**
 * <p>
 * 选课等待列表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
public interface WaitlistsBo extends IService<WaitlistsVo> {

    CourseResponseWrapper processWaitlist(Long courseId);

    CourseResponseWrapper joinWaitlist(WaitlistsDto dto);

    CourseResponseWrapper leaveWaitlist(Long waitlistId, WaitlistsDto dto);

    CourseResponseWrapper getStudentWaitlists(Long studentId);

    CourseResponseWrapper acceptWaitlistOffer(Long waitlistId);
    /**
     * 退选后处理等待列表
     */
    void processWaitlistAfterDrop(Long courseId);
}