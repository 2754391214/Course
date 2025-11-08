package com.lyw.cloudCourse.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyw.commonUtil.vo.BaseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 院系信息表
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Data
@Accessors(chain = true)
@TableName("departments")
public class DepartmentsVo extends BaseVo {

    @ApiModelProperty("院系ID，主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("院系代码，唯一标识")
    @TableField(value = "code")
    private String code;

    @ApiModelProperty("院系名称")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty("院系描述")
    @TableField(value = "description")
    private String description;

    @ApiModelProperty("父院系ID，支持院系层级结构")
    @TableField(value = "parent_id")
    private Long parentId;

    @ApiModelProperty("排序字段，用于控制显示顺序")
    @TableField(value = "sort_order")
    private Integer sortOrder;

    @ApiModelProperty("院系状态：活跃/非活跃")
    @TableField(value = "status")
    private String status = "N";



    @ApiModelProperty("学期")
    @TableField(exist = false)
    private String semester;

    @ApiModelProperty("课程总数")
    @TableField(exist = false)
    private BigDecimal totalCourses;

    @ApiModelProperty("课程总容量")
    @TableField(exist = false)
    private BigDecimal totalCapacity;

    @ApiModelProperty("已选课总人数")
    @TableField(exist = false)
    private BigDecimal totalEnrollment;

    @ApiModelProperty("选课/容量 百分比")
    @TableField(exist = false)
    private BigDecimal enrollmentRate;

    @ApiModelProperty("该院系下热门课程")
    @TableField(exist = false)
    private List<CoursesVo> PopularCourses;

    @ApiModelProperty("课程数量趋势")
    @TableField(exist = false)
    private BigDecimal courseCountTrend;

    @ApiModelProperty("选课率趋势")
    @TableField(exist = false)
    private BigDecimal enrollmentRateTrend;

    @ApiModelProperty("选课人数趋势")
    @TableField(exist = false)
    private BigDecimal enrollmentTrend;

    @ApiModelProperty("分析摘要")
    @TableField(exist = false)
    private String analysisSummary;

    @ApiModelProperty("不同学期的统计")
    @TableField(exist = false)
    private List<DepartmentsVo> semesterStats;
}
