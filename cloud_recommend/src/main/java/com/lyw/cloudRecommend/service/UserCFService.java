package com.lyw.cloudRecommend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyw.cloudRecommend.vo.UserCFVo;
import com.lyw.commonUtil.message.UserBehaviorMessage;

public interface UserCFService extends IService<UserCFVo> {
    void saveUserService(UserBehaviorMessage message);
}
