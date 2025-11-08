package com.lyw.cloudMember.vo;

import com.lyw.commonUtil.vo.BaseVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.time.LocalDateTime;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户基础表
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Data
@Accessors(chain = true)
@TableName("user")
public class UserVo extends BaseVo {

    @ApiModelProperty("用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户名")
    @TableField(value = "username")
    private String username;

    @ApiModelProperty("邮箱")
    @TableField(value = "email")
    private String email;

    @ApiModelProperty("手机号")
    @TableField(value = "phone")
    private String phone;

    @ApiModelProperty("昵称")
    @TableField(value = "nickname")
    private String nickname;

    @ApiModelProperty("头像URL")
    @TableField(value = "avatar")
    private String avatar;

    @ApiModelProperty("性别:0-未知 1-男 2-女")
    @TableField(value = "gender")
    private Integer gender;

    @ApiModelProperty("生日")
    @TableField(value = "birthday")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "GMT+8")
    private Date birthday;

    @ApiModelProperty("个人简介")
    @TableField(value = "bio")
    private String bio;

    @ApiModelProperty("用户类型:1-学生 2-教师 3-管理员")
    @TableField(value = "user_type")
    private Integer userType;

    @ApiModelProperty("状态:0-禁用 1-正常 2-未激活")
    @TableField(value = "status")
    private Integer status;

    @ApiModelProperty("最后登录时间")
    @TableField(value = "last_login_time")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty("最后登录IP")
    @TableField(value = "last_login_ip")
    private String lastLoginIp;

}
