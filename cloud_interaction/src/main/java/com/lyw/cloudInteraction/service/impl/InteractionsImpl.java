package com.lyw.cloudInteraction.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.lyw.cloudInteraction.service.FavoriteItemsBo;
import com.lyw.cloudInteraction.service.InteractionsBo;
import com.lyw.cloudInteraction.service.LikesBo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.FeignResponseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class InteractionsImpl implements InteractionsBo {
    @Resource
    private FavoriteItemsBo favoriteItemsBo;
    @Resource
    private LikesBo likesBo;

    @Override
    public CourseResponseWrapper getAllStatus(String targetType, Long targetId, Long userId) {
        Map<String,String> favorite = FeignResponseHelper.convert(favoriteItemsBo.getFavoriteStatus(targetType, targetId, userId), Map.class);
        Map<String,String> like = FeignResponseHelper.convert(likesBo.getLikeStatus(targetType,targetId,userId), Map.class);
        Map<String,String> result = new HashMap<>();
        if (CollectionUtil.isNotEmpty(favorite)) result.putAll(favorite);
        if (CollectionUtil.isNotEmpty(like)) result.putAll(like);
        return CourseResponseWrapper.getSuccess(result);
    }
}