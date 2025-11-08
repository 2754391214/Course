package com.lyw.cloudMember.service.impl;

import com.lyw.cloudMember.vo.StudentProfileVo;
import com.lyw.cloudMember.dto.StudentProfileDto;
import com.lyw.cloudMember.mapper.StudentProfileDao;
import com.lyw.commonUtil.service.BaseImpl;
import org.springframework.stereotype.Service;
import com.lyw.cloudMember.service.StudentProfileBo;
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

}