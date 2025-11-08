package com.lyw.cloudMember.mapper;

import com.lyw.cloudMember.vo.StudentProfileVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 学生信息表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Mapper
public interface StudentProfileDao extends BaseMapper<StudentProfileVo> {

}
