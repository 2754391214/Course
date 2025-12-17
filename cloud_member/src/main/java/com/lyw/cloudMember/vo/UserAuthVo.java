package com.lyw.cloudMember.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.vo.BaseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * 用户认证表
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Data
@TableName("user_auth")
public class UserAuthVo extends BaseVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("认证类型:password,gitee,weibo")
    @TableField(value = "identity_type")
    private String identityType;

    @ApiModelProperty("唯一标识:用户名/第三方ID")
    @TableField(value = "identifier")
    private String identifier;

    @ApiModelProperty("密码凭证/access_token")
    @TableField(value = "credential")
    private String credential;

    @ApiModelProperty("是否验证")
    @TableField(value = "verified")
    private Integer verified;

    @ApiModelProperty("凭证过期时间")
    @TableField(value = "expires_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date expiresAt;

}
