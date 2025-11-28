package com.lyw.cloudChoose.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

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
public class CoursesDto extends BaseDto {

    @ApiModelProperty("课程ID，主键自增")
    private Long id;

    @ApiModelProperty("课程代码，唯一标识，如：CS101")
    private String code;

    @ApiModelProperty("课程名称")
    private String name;

    @ApiModelProperty("课程详细描述")
    private String description;

    @ApiModelProperty("课程学分")
    private Integer credits;

    @ApiModelProperty("课程容量，最大选课人数")
    private Integer capacity;

    @ApiModelProperty("当前已选人数")
    private Integer enrolledCount;

    @ApiModelProperty("授课教师ID，关联users表")
    private Long teacherId;

    @ApiModelProperty("开课院系ID，关联departments表")
    private Long departmentId;

    @ApiModelProperty("开课学期，格式：YYYY-S（如2024-1）")
    private String semester;

    @ApiModelProperty("课程类型：必修/选修/实验课/在线课等")
    private String courseType;

    @ApiModelProperty("难度等级：初级/中级/高级")
    private String difficultyLevel;

    @ApiModelProperty("课程封面图片URL")
    private String coverImage;

    @ApiModelProperty("考核方式描述")
    private String assessmentMethod;

    @ApiModelProperty("先修课程要求，JSON格式存储课程ID列表和条件")
    private String prerequisites;

    @ApiModelProperty("课程标签，用于分类和搜索")
    private String tags;

    @ApiModelProperty("课程状态：草稿/已发布/已关闭/已归档")
    private String status;

    @ApiModelProperty("扩展元数据，存储课程其他相关信息")
    private String metadata;

    //不属于表结构字段
    @ApiModelProperty("课程时间安排")
    private List<CourseSchedulesDto> courseSchedules;
}
