package com.lyw.cloudMember.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudMember.vo.StudentProfileVo;
import com.lyw.cloudMember.dto.StudentProfileDto;
import com.lyw.cloudMember.mapper.StudentProfileDao;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseImpl;
import com.lyw.commonUtil.util.CurUserUtil;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.RedissLockUtil;
import org.springframework.stereotype.Service;
import com.lyw.cloudMember.service.StudentProfileBo;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 学生信息表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Service
public class StudentProfileImpl extends BaseImpl<StudentProfileDao, StudentProfileVo, StudentProfileDto> implements StudentProfileBo {
    @Resource
    private RedissLockUtil redissLockUtil;
    @Override
    public CourseResponseWrapper bindUser(Long userId, Long studentId) {
        String lockKey = String.format(RedisKeyConstant.LOCK_RECOMMEND_STUDENT_PROFILE,studentId);
        try {
            // 尝试加锁，最多等待5秒，锁持有时间30秒
            boolean locked = redissLockUtil.tryLock(lockKey, 5, 30);
            if (locked) {

                StudentProfileVo studentProfileVo = baseMapper.selectOne(new LambdaQueryWrapper<StudentProfileVo>().eq(StudentProfileVo::getStudentId, studentId));
                if (ObjectUtil.isEmpty(studentProfileVo)) {
                    return CourseResponseWrapper.getFailed("请输入正确的学生号！");
                }
                StudentProfileVo profileVo = baseMapper.selectOne(new LambdaQueryWrapper<StudentProfileVo>().eq(StudentProfileVo::getUserId, userId));
                if (ObjectUtil.isNotEmpty(profileVo)) {
                    return CourseResponseWrapper.getFailed("你已经绑定了 学生号:"+profileVo.getStudentId()+" 请先解除绑定！");
                }
                Long studentProfileVoUserId = studentProfileVo.getUserId();
                if (ObjectUtil.isNotEmpty(studentProfileVoUserId)) {
                    if (ObjectUtil.equals(studentProfileVoUserId,studentId)) {
                        return CourseResponseWrapper.getFailed("你已经绑定了！");
                    }else {
                        return CourseResponseWrapper.getFailed("该学生号已被绑定！");
                    }
                }
                studentProfileVo.setUserId(userId);
                studentProfileVo.setLuu(CurUserUtil.getUserId());
                studentProfileVo.setLud(DateTimeUtils.getCurrentDateTime());
                baseMapper.updateById(studentProfileVo);
                return CourseResponseWrapper.getSuccess();
            } else {
                return CourseResponseWrapper.getFailed("绑定失败，请重试！");
            }
        } finally {
            redissLockUtil.unlock(lockKey);
        }
    }

    @Override
    public CourseResponseWrapper unbindUser(Long userId, Long studentId) {
        Integer count = baseMapper.unbindUser(userId,studentId);
        if (count == 0) {
            return CourseResponseWrapper.getFailed("解绑失败！");
        }
        return CourseResponseWrapper.getSuccess();
    }

    @Override
    public CourseResponseWrapper searchBatchByIds(List<Long> userIds) {
        return CourseResponseWrapper.getSuccess(baseMapper.selectList(new LambdaQueryWrapper<StudentProfileVo>().in(StudentProfileVo::getUserId,userIds)));
    }
}