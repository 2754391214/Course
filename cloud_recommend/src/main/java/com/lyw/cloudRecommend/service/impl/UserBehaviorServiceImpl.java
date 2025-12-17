package com.lyw.cloudRecommend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudRecommend.mapper.UserBehaviorDao;
import com.lyw.cloudRecommend.service.UserBehaviorService;
import com.lyw.cloudRecommend.utils.RecommendUtils;
import com.lyw.cloudRecommend.vo.UserBehaviorVo;
import com.lyw.commonUtil.message.UserBehaviorMessage;
import com.lyw.commonUtil.util.DateTimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class UserBehaviorServiceImpl extends ServiceImpl<UserBehaviorDao, UserBehaviorVo> implements UserBehaviorService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserBehavior(UserBehaviorMessage message) {
        Long userId = message.getUserId();
        String currentDateTime = DateTimeUtils.getCurrentDateTime();
        String behaviorType = message.getBehaviorType();
        UserBehaviorVo userBehaviorVo = new UserBehaviorVo();
        userBehaviorVo.setUserId(message.getUserId());
        userBehaviorVo.setCourseId(message.getCourseId());
        userBehaviorVo.setBehaviorTime(message.getBehaviorTime());
        userBehaviorVo.setBehaviorType(behaviorType);
        userBehaviorVo.setBehaviorWeight(RecommendUtils.getBehaviorScore(behaviorType));
        userBehaviorVo.setCrdAndLud(currentDateTime);
        userBehaviorVo.setCruAndLuu(userId.toString());
        baseMapper.insert(userBehaviorVo);
    }
}
