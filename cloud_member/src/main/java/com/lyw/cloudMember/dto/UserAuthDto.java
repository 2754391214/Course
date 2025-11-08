package com.lyw.cloudMember.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户认证表
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Data
@Accessors(chain = true)
public class UserAuthDto extends BaseDto {

    private Long id;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("认证类型:password,gitee,weibo")
    private String identityType;
    
    @ApiModelProperty("唯一标识:用户名/第三方ID")
    private String identifier;
    
    @ApiModelProperty("密码凭证/access_token")
    private String credential;
    
    @ApiModelProperty("是否验证")
    private Integer verified;
    
    @ApiModelProperty("凭证过期时间")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date expiresAt;
                    
}
