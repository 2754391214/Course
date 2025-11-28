package com.lyw.cloudInteraction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * 举报表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
public class ReportsDto extends BaseDto {

    @ApiModelProperty("主键ID")
    private Long id;
    
    @ApiModelProperty("举报目标类型：review-评价,reply-回复")
    private String targetType;
    
    @ApiModelProperty("目标ID")
    private Long targetId;
    
    @ApiModelProperty("举报人ID")
    private Long userId;
    
    @ApiModelProperty("举报类型：inappropriate-内容不当,false_info-虚假信息,harassment-骚扰,other-其他")
    private String reportType;
    
    @ApiModelProperty("举报原因")
    private String reason;
    
    @ApiModelProperty("证据信息")
    private String evidence;
    
    @ApiModelProperty("状态：pending-待处理,resolved-已处理,rejected-已拒绝")
    private String status;
    
    @ApiModelProperty("管理员处理意见")
    private String adminNotes;
    
    @ApiModelProperty("处理管理员ID")
    private Long adminId;
    
    @ApiModelProperty("处理时间")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date resolvedAt;
                    
}
