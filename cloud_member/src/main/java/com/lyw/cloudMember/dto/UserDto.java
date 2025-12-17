package com.lyw.cloudMember.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * 用户基础表
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Data
public class UserDto extends BaseDto {

    @ApiModelProperty("用户ID")
    private Long id;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("邮箱")
    private String email;
    
    @ApiModelProperty("手机号")
    private String phone;
    
    @ApiModelProperty("昵称")
    private String nickname;
    
    @ApiModelProperty("头像URL")
    private String avatar;
    
    @ApiModelProperty("性别:0-未知 1-男 2-女")
    private Integer gender;
    
    @ApiModelProperty("生日")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "GMT+8")
    private Date birthday;
    
    @ApiModelProperty("个人简介")
    private String bio;
    
    @ApiModelProperty("用户类型:1-学生 2-教师 3-管理员")
    private Integer userType;
    
    @ApiModelProperty("状态:0-禁用 1-正常 2-未激活")
    private Integer status;
    
    @ApiModelProperty("最后登录时间")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;
    
    @ApiModelProperty("最后登录IP")
    private String lastLoginIp;
                    
}
