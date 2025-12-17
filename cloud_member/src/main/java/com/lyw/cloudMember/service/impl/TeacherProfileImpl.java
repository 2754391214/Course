package com.lyw.cloudMember.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudMember.dto.TeacherProfileDto;
import com.lyw.cloudMember.mapper.TeacherProfileDao;
import com.lyw.cloudMember.service.TeacherProfileBo;
import com.lyw.cloudMember.vo.TeacherProfileVo;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseImpl;
import com.lyw.commonUtil.util.CurUserUtil;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.RedissLockUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 教师信息表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Service
public class TeacherProfileImpl extends BaseImpl<TeacherProfileDao, TeacherProfileVo, TeacherProfileDto> implements TeacherProfileBo {

    @Resource
    private RedissLockUtil redissLockUtil;
    @Override
    public CourseResponseWrapper bindUser(Long userId, Long teacherId) {
        String lockKey = String.format(RedisKeyConstant.LOCK_RECOMMEND_TEACHER_PROFILE, teacherId);
        try {
            // 尝试加锁，最多等待5秒，锁持有时间30秒
            boolean locked = redissLockUtil.tryLock(lockKey, 5, 30);
            if (locked) {

                TeacherProfileVo teacherProfileVo = baseMapper.selectOne(new LambdaQueryWrapper<TeacherProfileVo>().eq(TeacherProfileVo::getTeacherId, teacherId));
                if (ObjectUtil.isEmpty(teacherProfileVo)) {
                    return CourseResponseWrapper.getFailed("请输入正确的学生号！");
                }
                TeacherProfileVo profileVo = baseMapper.selectOne(new LambdaQueryWrapper<TeacherProfileVo>().eq(TeacherProfileVo::getUserId, userId));
                if (ObjectUtil.isNotEmpty(profileVo)) {
                    return CourseResponseWrapper.getFailed("你已经绑定了 学生号:" + profileVo.getTeacherId() + " 请先解除绑定！");
                }
                Long studentProfileVoUserId = teacherProfileVo.getUserId();
                if (ObjectUtil.isNotEmpty(studentProfileVoUserId)) {
                    if (ObjectUtil.equals(studentProfileVoUserId, teacherId)) {
                        return CourseResponseWrapper.getFailed("你已经绑定了！");
                    } else {
                        return CourseResponseWrapper.getFailed("该学生号已被绑定！");
                    }
                }
                teacherProfileVo.setUserId(userId);
                teacherProfileVo.setLuu(CurUserUtil.getUserId());
                teacherProfileVo.setLud(DateTimeUtils.getCurrentDateTime());
                baseMapper.updateById(teacherProfileVo);
                return CourseResponseWrapper.getSuccess();
            } else {
                return CourseResponseWrapper.getFailed("绑定失败，请重试！");
            }
        } finally {
            redissLockUtil.unlock(lockKey);
        }
    }

    @Override
    public CourseResponseWrapper unbindUser(Long userId, Long teacherId) {
        Integer count = baseMapper.unbindUser(userId,teacherId);
        if (count == 0) {
            return CourseResponseWrapper.getFailed("解绑失败！");
        }
        return CourseResponseWrapper.getSuccess();
    }
}