package com.lyw.cloudMember.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudMember.vo.UserPreferenceVo;
import com.lyw.cloudMember.dto.UserPreferenceDto;
import com.lyw.cloudMember.mapper.UserPreferenceDao;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseImpl;
import org.springframework.stereotype.Service;
import com.lyw.cloudMember.service.UserPreferenceBo;
/**
 * <p>
 * 用户偏好表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Service
public class UserPreferenceImpl extends BaseImpl<UserPreferenceDao, UserPreferenceVo, UserPreferenceDto> implements UserPreferenceBo {

    @Override
    public CourseResponseWrapper findDetailByUser(Long userId) {
        return CourseResponseWrapper.getSuccess(
                baseMapper.selectOne(
                        new LambdaQueryWrapper<UserPreferenceVo>()
                                .eq(UserPreferenceVo::getUserId,userId)
                ));
    }
}