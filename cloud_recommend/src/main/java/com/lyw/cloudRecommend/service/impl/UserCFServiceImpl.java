package com.lyw.cloudRecommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudRecommend.mapper.UserCFDao;
import com.lyw.cloudRecommend.service.UserCFService;
import com.lyw.cloudRecommend.utils.RecommendUtils;
import com.lyw.cloudRecommend.vo.UserCFVo;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.message.UserBehaviorMessage;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.RedissLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
@Slf4j
@Service
public class UserCFServiceImpl extends ServiceImpl<UserCFDao, UserCFVo>  implements UserCFService {
    @Resource
    private RedissLockUtil redissLockUtil;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserService(UserBehaviorMessage message) {
        Long userId = message.getUserId();
        Long courseId = message.getCourseId();
        String lockKey = String.format(RedisKeyConstant.LOCK_RECOMMEND_CF,userId,courseId);
        try {
            // 尝试加锁，最多等待5秒，锁持有时间30秒
            boolean locked = redissLockUtil.tryLock(lockKey, 5, 30);
            if (locked) {
                doSaveUserService(message);
            } else {
                log.warn("获取锁失败，userId: {}, courseId: {}", userId, courseId);
            }
        } finally {
            redissLockUtil.unlock(lockKey);
            log.debug("释放分布式锁 [key:{}]", lockKey);
        }
    }
    private void doSaveUserService(UserBehaviorMessage message) {
        Long userId = message.getUserId();
        String currentDateTime = DateTimeUtils.getCurrentDateTime();
        String behaviorType = message.getBehaviorType();
        int behaviorScore = RecommendUtils.getBehaviorScore(behaviorType);

        // 直接使用原子更新，不需要先查询
        boolean updated = baseMapper.update(null,
                new LambdaUpdateWrapper<UserCFVo>()
                        .eq(UserCFVo::getUserId, userId)
                        .eq(UserCFVo::getCourseId, message.getCourseId())
                        .setSql("score = score + " + behaviorScore)
                        .set(UserCFVo::getLud, currentDateTime)
                        .set(UserCFVo::getLuu, userId.toString())
        ) > 0;

        if (!updated) {
            // 记录不存在，尝试插入
            UserCFVo newRecord = new UserCFVo();
            newRecord.setCourseId(message.getCourseId());
            newRecord.setUserId(message.getUserId());
            newRecord.setScore(behaviorScore);
            newRecord.setCrdAndLud(currentDateTime);
            newRecord.setCruAndLuu(userId.toString());
            baseMapper.insert(newRecord);
        }
    }
}
