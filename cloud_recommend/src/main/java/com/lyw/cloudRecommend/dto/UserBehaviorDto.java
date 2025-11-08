package com.lyw.cloudRecommend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 用户行为表
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Data
@Accessors(chain = true)
public class UserBehaviorDto extends BaseDto {

    private Long id;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("课程ID")
    private Long courseId;
    
    @ApiModelProperty("行为类型: VIEW, ENROLL, COMPLETE, LIKE, RATE, SEARCH")
    private String behaviorType;
    
    @ApiModelProperty("行为权重")
    private BigDecimal behaviorWeight;
    
    @ApiModelProperty("行为上下文")
    private String context;
    
    @ApiModelProperty("行为时间")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date behaviorTime;
                    
}
