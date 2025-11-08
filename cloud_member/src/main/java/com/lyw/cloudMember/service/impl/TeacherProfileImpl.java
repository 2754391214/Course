package com.lyw.cloudMember.service.impl;

import com.lyw.cloudMember.vo.TeacherProfileVo;
import com.lyw.cloudMember.dto.TeacherProfileDto;
import com.lyw.cloudMember.mapper.TeacherProfileDao;
import com.lyw.commonUtil.service.BaseImpl;
import org.springframework.stereotype.Service;
import com.lyw.cloudMember.service.TeacherProfileBo;
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

}