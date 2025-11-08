package com.lyw.cloudMember.service.impl;

import com.lyw.cloudMember.vo.UserAuthVo;
import com.lyw.cloudMember.dto.UserAuthDto;
import com.lyw.cloudMember.mapper.UserAuthDao;
import com.lyw.commonUtil.service.BaseImpl;
import org.springframework.stereotype.Service;
import com.lyw.cloudMember.service.UserAuthBo;
/**
 * <p>
 * 用户认证表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Service
public class UserAuthImpl extends BaseImpl<UserAuthDao, UserAuthVo, UserAuthDto> implements UserAuthBo {

}