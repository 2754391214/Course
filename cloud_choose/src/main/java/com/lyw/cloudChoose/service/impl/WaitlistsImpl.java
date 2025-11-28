package com.lyw.cloudChoose.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.dto.WaitlistsDto;
import com.lyw.cloudChoose.mapper.WaitlistsDao;
import com.lyw.cloudChoose.service.EnrollmentsBo;
import com.lyw.cloudChoose.service.WaitlistsBo;
import com.lyw.cloudChoose.vo.WaitlistsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.RedissLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 选课等待列表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Slf4j
@Service
public class WaitlistsImpl extends ServiceImpl<WaitlistsDao, WaitlistsVo> implements WaitlistsBo {
    @Resource
    private WaitlistsDao waitlistsDao;

    @Resource
    private EnrollmentsBo enrollmentService;
    @Resource
    private RedissLockUtil redissLockUtil;
    @Override
    public CourseResponseWrapper processWaitlist(Long courseId) {
        return null;
    }

    @Override
    public CourseResponseWrapper joinWaitlist(WaitlistsDto dto) {
        return null;
    }

    @Override
    public CourseResponseWrapper leaveWaitlist(Long waitlistId, WaitlistsDto dto) {
        return null;
    }

    @Override
    public CourseResponseWrapper getStudentWaitlists(Long studentId) {
        return null;
    }

    @Override
    public CourseResponseWrapper acceptWaitlistOffer(Long waitlistId) {
        return null;
    }
}