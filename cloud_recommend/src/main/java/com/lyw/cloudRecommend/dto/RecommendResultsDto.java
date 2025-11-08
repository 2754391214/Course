package com.lyw.cloudRecommend.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 推荐结果表
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Data
@Accessors(chain = true)
public class RecommendResultsDto extends BaseDto {

    private Long id;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("推荐类型: HOME_PAGE, COURSE_DETAIL, PERSONALIZED")
    private String recommendType;
    
    @ApiModelProperty("推荐课程ID列表")
    private String courseIds;
    
    @ApiModelProperty("推荐分数")
    private String scores;
    
    @ApiModelProperty("使用的策略")
    private String strategyUsed;
    
    @ApiModelProperty("过期时间")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;
                    
}
