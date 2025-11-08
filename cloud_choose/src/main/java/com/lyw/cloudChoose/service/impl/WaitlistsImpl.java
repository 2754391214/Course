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
    @Async
    @Override
    public void processWaitlistAfterDrop(Long courseId) {
        String lockKey = "waitlist-process-" + courseId;
        boolean locked = false;

        try {
            // 获取锁，防止并发处理
            locked = redissLockUtil.tryLock(lockKey, 100L, 10000L);
            if (!locked) {
                log.warn("获取等待列表处理锁失败: courseId={}", courseId);
                return;
            }

            // 获取等待列表中的第一个学生
            WaitlistsVo nextWaitlist = waitlistsDao.selectOne(
                    new LambdaQueryWrapper<WaitlistsVo>()
                            .eq(WaitlistsVo::getCourseId, courseId)
                            .orderByAsc(WaitlistsVo::getPosition)
                            .last("LIMIT 1")
            );

            if (ObjectUtil.isNotEmpty(nextWaitlist)) {
                // 尝试为等待列表中的学生选课
                EnrollmentsDto enrollDto = new EnrollmentsDto();
                enrollDto.setStudentId(nextWaitlist.getStudentId());
                enrollDto.setCourseId(courseId);

                CourseResponseWrapper result = enrollmentService.enrollFromWaitlist(enrollDto);

                if (result.isSuccess()) {
                    // 从等待列表中移除
                    waitlistsDao.deleteById(nextWaitlist.getId());
                    log.info("等待列表学生选课成功: waitlistId={}, studentId={}, courseId={}",
                            nextWaitlist.getId(), nextWaitlist.getStudentId(), courseId);
                }
            }

        } catch (Exception e) {
            log.error("处理等待列表异常: courseId={}", courseId, e);
        } finally {
            if (locked) {
                redissLockUtil.unlock(lockKey);
            }
        }
    }
}