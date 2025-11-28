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
 * 课程基础信息表
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Data
@Accessors(chain = true)
@TableName("courses")
public class CoursesVo extends BaseVo {

    @ApiModelProperty("课程ID，主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("课程代码，唯一标识，如：CS101")
    @TableField(value = "code")
    private String code;

    @ApiModelProperty("课程名称")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty("课程详细描述")
    @TableField(value = "description")
    private String description;

    @ApiModelProperty("课程学分")
    @TableField(value = "credits")
    private Integer credits;

    @ApiModelProperty("课程容量，最大选课人数")
    @TableField(value = "capacity")
    private Integer capacity;

    @ApiModelProperty("当前已选人数")
    @TableField(value = "enrolled_count")
    private Integer enrolledCount;

    @ApiModelProperty("授课教师ID，关联users表")
    @TableField(value = "teacher_id")
    private Long teacherId;

    @ApiModelProperty("开课院系ID，关联departments表")
    @TableField(value = "department_id")
    private Long departmentId;

    @ApiModelProperty("开课学期，格式：YYYY-S（如2024-1）")
    @TableField(value = "semester")
    private String semester;

    @ApiModelProperty("课程类型：必修/选修/实验课/在线课等")
    @TableField(value = "course_type")
    private String courseType;

    @ApiModelProperty("难度等级：初级/中级/高级")
    @TableField(value = "difficulty_level")
    private String difficultyLevel;

    @ApiModelProperty("课程封面图片URL")
    @TableField(value = "cover_image")
    private String coverImage;

    @ApiModelProperty("考核方式描述")
    @TableField(value = "assessment_method")
    private String assessmentMethod;

    @ApiModelProperty("先修课程要求，JSON格式存储课程ID列表和条件")
    @TableField(value = "prerequisites")
    private String prerequisites;

    @ApiModelProperty("课程标签，用于分类和搜索")
    @TableField(value = "tags")
    private String tags;

    @ApiModelProperty("课程状态：草稿/已发布/已关闭/已归档")
    @TableField(value = "status")
    private String status;

    @ApiModelProperty("扩展元数据，存储课程其他相关信息")
    @TableField(value = "metadata")
    private String metadata;

    @ApiModelProperty("热度分数")
    @TableField(exist = false)
    private BigDecimal hotScore;

    @ApiModelProperty("评价数量")
    @TableField(exist = false)
    private Integer reviewCount;

    @ApiModelProperty("平均评分")
    @TableField(exist = false)
    private BigDecimal avgRating;



    @ApiModelProperty("可用名额")
    @TableField(exist = false)
    private Integer availableSlots;
    @ApiModelProperty("状态描述")
    @TableField(exist = false)
    private String statusDescription;
    @ApiModelProperty("是否能够选择")
    @TableField(exist = false)
    private Boolean canEnroll;
    @ApiModelProperty("选课进度百分比")
    @TableField(exist = false)
    private BigDecimal enrollmentProgress;
    @ApiModelProperty("新的选课人数")
    @TableField(exist = false)
    private Integer newCurrentEnrollment;

    @ApiModelProperty("成功")
    @TableField(exist = false)
    private Boolean success;

    @ApiModelProperty("信息")
    @TableField(exist = false)
    private String message;

    @ApiModelProperty("课程安排")
    @TableField(exist = false)
    private List<CourseSchedulesVo> courseSchedules;
}
