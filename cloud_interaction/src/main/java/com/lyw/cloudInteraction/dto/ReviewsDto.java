package com.lyw.cloudInteraction.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 评价表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
public class ReviewsDto extends BaseDto {

    @ApiModelProperty("主键ID")
    private Long id;
    
    @ApiModelProperty("课程ID，关联课程表")
    private Long courseId;
    
    @ApiModelProperty("学生ID，关联用户表")
    private Long studentId;
    
    @ApiModelProperty("评价标题")
    private String title;
    
    @ApiModelProperty("总体评分，1-5分")
    private BigDecimal overallRating;
    
    @ApiModelProperty("多维度评分，例如：{\"teaching\": 4.5, \"content\": 4.0, \"assessment\": 3.5}")
    private String dimensionRatings;
    
    @ApiModelProperty("评价内容")
    private String comment;
    
    @ApiModelProperty("是否推荐课程")
    private Boolean recommended;
    
    @ApiModelProperty("学期信息")
    private String semester;
    
    @ApiModelProperty("获得的成绩")
    private String gradeReceived;
    
    @ApiModelProperty("课程难度 1-5")
    private Integer difficultyLevel;
    
    @ApiModelProperty("作业量 1-5")
    private Integer workloadLevel;
    
    @ApiModelProperty("是否匿名评价")
    private Boolean anonymous;
    
    @ApiModelProperty("评价状态：pending-待审核,approved-已审核,rejected-已拒绝,hidden-已隐藏")
    private String status;
    
    @ApiModelProperty("评价标签，例如：[\"老师幽默\", \"作业多\", \"考试难\"]")
    private String reviewTags;
    
    @ApiModelProperty("扩展字段")
    private String metadata;
                    
}
