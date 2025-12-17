package com.lyw.cloudInteraction.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyw.commonUtil.vo.BaseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 评价表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
@TableName("reviews")
public class ReviewsVo extends BaseVo {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("课程ID，关联课程表")
    @TableField(value = "course_id")
    private Long courseId;

    @ApiModelProperty("学生ID，关联用户表")
    @TableField(value = "student_id")
    private Long studentId;

    @ApiModelProperty("评价标题")
    @TableField(value = "title")
    private String title;

    @ApiModelProperty("总体评分，1-5分")
    @TableField(value = "overall_rating")
    private Integer overallRating;

    @ApiModelProperty("多维度评分，例如：{\"teaching\": 4.5, \"content\": 4.0, \"assessment\": 3.5}")
    @TableField(value = "dimension_ratings")
    private String dimensionRatings;

    @ApiModelProperty("评价内容")
    @TableField(value = "comment")
    private String comment;

    @ApiModelProperty("是否推荐课程")
    @TableField(value = "is_recommended")
    private Boolean recommended;

    @ApiModelProperty("学期信息")
    @TableField(value = "semester")
    private String semester;

    @ApiModelProperty("获得的成绩")
    @TableField(value = "grade_received")
    private String gradeReceived;

    @ApiModelProperty("课程难度 1-5")
    @TableField(value = "difficulty_level")
    private Integer difficultyLevel;

    @ApiModelProperty("作业量 1-5")
    @TableField(value = "workload_level")
    private Integer workloadLevel;

    @ApiModelProperty("是否匿名评价")
    @TableField(value = "is_anonymous")
    private Boolean anonymous;

    @ApiModelProperty("评价状态：pending-待审核,approved-已审核,rejected-已拒绝,hidden-已隐藏")
    @TableField(value = "status")
    private String status;

    @ApiModelProperty("评价标签，例如：[\"老师幽默\", \"作业多\", \"考试难\"]")
    @TableField(value = "review_tags")
    private String reviewTags;

    @ApiModelProperty("扩展字段")
    @TableField(value = "metadata")
    private String metadata;

    @ApiModelProperty("总评分")
    @TableField(exist = false)
    private Integer totalReviews;

    @ApiModelProperty("平均评分")
    @TableField(exist = false)
    private BigDecimal averageRating;

    @ApiModelProperty("推荐率")
    @TableField(exist = false)
    private BigDecimal recommendRate;

    @ApiModelProperty("评分")
    @TableField(exist = false)
    private Double rating;

    @ApiModelProperty("评分人数")
    @TableField(exist = false)
    private Integer count;

    @ApiModelProperty("评分人数百分比")
    @TableField(exist = false)
    private BigDecimal percentage;

    @ApiModelProperty("评分分布")
    @TableField(exist = false)
    private List<ReviewsVo> ratingDistribution;

    @ApiModelProperty("标签统计")
    @TableField(exist = false)
    private List<ReviewsVo> tagStatistics;
}
