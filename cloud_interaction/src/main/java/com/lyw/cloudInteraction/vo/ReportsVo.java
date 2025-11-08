package com.lyw.cloudInteraction.vo;

import com.lyw.commonUtil.vo.BaseVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.experimental.Accessors;

/**
 * <p>
 * 举报表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
@Accessors(chain = true)
@TableName("reports")
public class ReportsVo extends BaseVo {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("举报目标类型：review-评价,reply-回复")
    @TableField(value = "target_type")
    private String targetType;

    @ApiModelProperty("目标ID")
    @TableField(value = "target_id")
    private Long targetId;

    @ApiModelProperty("举报人ID")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("举报类型：inappropriate-内容不当,false_info-虚假信息,harassment-骚扰,other-其他")
    @TableField(value = "report_type")
    private String reportType;

    @ApiModelProperty("举报原因")
    @TableField(value = "reason")
    private String reason;

    @ApiModelProperty("证据信息")
    @TableField(value = "evidence")
    private String evidence;

    @ApiModelProperty("状态：pending-待处理,resolved-已处理,rejected-已拒绝")
    @TableField(value = "status")
    private String status;

    @ApiModelProperty("管理员处理意见")
    @TableField(value = "admin_notes")
    private String adminNotes;

    @ApiModelProperty("处理管理员ID")
    @TableField(value = "admin_id")
    private Long adminId;

    @ApiModelProperty("处理时间")
    @TableField(value = "resolved_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date resolvedAt;

}
