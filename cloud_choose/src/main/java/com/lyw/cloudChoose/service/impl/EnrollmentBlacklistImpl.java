package com.lyw.cloudChoose.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudChoose.dto.EnrollmentBlacklistDto;
import com.lyw.cloudChoose.mapper.EnrollmentBlacklistDao;
import com.lyw.cloudChoose.service.EnrollmentBlacklistBo;
import com.lyw.cloudChoose.vo.EnrollmentBlacklistVo;
import com.lyw.commonUtil.service.BaseImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 选课黑名单表，用于管理学生选课限制，支持全局黑名单和课程特定黑名单，保障选课系统的公平性和规范性 服务实现类
 * </p>
 *
 * @author lyw
 * @since 2025/10/24
 */
@Service
@Slf4j
public class EnrollmentBlacklistImpl extends BaseImpl<EnrollmentBlacklistDao, EnrollmentBlacklistVo, EnrollmentBlacklistDto> implements EnrollmentBlacklistBo {

    @Resource
    private EnrollmentBlacklistDao enrollmentBlacklistDao;

    /**
     * 检查学生是否在黑名单中（包括全局和课程特定）
     */
    public EnrollmentBlacklistVo checkStudentInBlacklist(Long studentId, Long courseId) {
        LambdaQueryWrapper<EnrollmentBlacklistVo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnrollmentBlacklistVo::getStudentId, studentId)
                .eq(EnrollmentBlacklistVo::getStatus, "ACTIVE")
                .le(EnrollmentBlacklistVo::getStartTime, new Date())
                .and(w -> w.isNull(EnrollmentBlacklistVo::getEndTime).or().ge(EnrollmentBlacklistVo::getEndTime, new Date()))
                .and(w -> w.isNull(EnrollmentBlacklistVo::getCourseId).or().eq(EnrollmentBlacklistVo::getCourseId, courseId))
                .orderByDesc(EnrollmentBlacklistVo::getBlacklistType) // 优先返回全局黑名单
                .last("LIMIT 1");

        return enrollmentBlacklistDao.selectOne(wrapper);
    }

    /**
     * 获取学生的所有有效黑名单
     */
    public List<EnrollmentBlacklistVo> getActiveBlacklistsByStudent(Long studentId) {
        LambdaQueryWrapper<EnrollmentBlacklistVo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnrollmentBlacklistVo::getStudentId, studentId)
                .eq(EnrollmentBlacklistVo::getStatus, "ACTIVE")
                .le(EnrollmentBlacklistVo::getStartTime, new Date())
                .and(w -> w.isNull(EnrollmentBlacklistVo::getEndTime).or().ge(EnrollmentBlacklistVo::getEndTime, new Date()))
                .orderByDesc(EnrollmentBlacklistVo::getCrd);

        return enrollmentBlacklistDao.selectList(wrapper);
    }

    /**
     * 添加黑名单记录
     */
    public boolean addToBlacklist(EnrollmentBlacklistVo blacklist) {
        // 检查是否已存在相同的黑名单记录
        LambdaQueryWrapper<EnrollmentBlacklistVo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnrollmentBlacklistVo::getStudentId, blacklist.getStudentId())
                .eq(EnrollmentBlacklistVo::getCourseId, blacklist.getCourseId())
                .eq(EnrollmentBlacklistVo::getStatus, "ACTIVE");

        EnrollmentBlacklistVo existing = enrollmentBlacklistDao.selectOne(wrapper);
        if (existing != null) {
            log.warn("黑名单记录已存在: studentId={}, courseId={}",
                    blacklist.getStudentId(), blacklist.getCourseId());
            return false;
        }

        int result = enrollmentBlacklistDao.insert(blacklist);
        if (result > 0) {
            log.info("成功添加黑名单记录: id={}, studentId={}, courseId={}",
                    blacklist.getId(), blacklist.getStudentId(), blacklist.getCourseId());
            return true;
        }
        return false;
    }

    /**
     * 释放黑名单（设置为失效）
     */
    public boolean releaseBlacklist(Long blacklistId, Long operatorId, String releaseReason) {
        EnrollmentBlacklistVo blacklist = enrollmentBlacklistDao.selectById(blacklistId);
        if (blacklist == null) {
            log.warn("黑名单记录不存在: id={}", blacklistId);
            return false;
        }

        blacklist.setStatus("INACTIVE");
        blacklist.setLud(String.valueOf(operatorId));
        blacklist.setRemarks((blacklist.getRemarks() == null ? "" : blacklist.getRemarks() + "; ") +
                "释放原因: " + releaseReason);

        int result = enrollmentBlacklistDao.updateById(blacklist);
        if (result > 0) {
            log.info("成功释放黑名单: id={}, studentId={}", blacklistId, blacklist.getStudentId());
            return true;
        }
        return false;
    }

    /**
     * 自动清理过期的黑名单
     */
    public int cleanupExpiredBlacklists() {
        LambdaQueryWrapper<EnrollmentBlacklistVo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnrollmentBlacklistVo::getStatus, "ACTIVE")
                .eq(EnrollmentBlacklistVo::getAutoRelease, true)
                .le(EnrollmentBlacklistVo::getEndTime, new Date());

        List<EnrollmentBlacklistVo> expiredBlacklists = enrollmentBlacklistDao.selectList(wrapper);

        for (EnrollmentBlacklistVo blacklist : expiredBlacklists) {
            blacklist.setStatus("EXPIRED");
            blacklist.setRemarks((blacklist.getRemarks() == null ? "" : blacklist.getRemarks() + "; ") +
                    "系统自动过期释放");
            enrollmentBlacklistDao.updateById(blacklist);
        }

        log.info("自动清理过期黑名单完成: 共清理{}条记录", expiredBlacklists.size());
        return expiredBlacklists.size();
    }
}
