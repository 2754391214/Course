package com.lyw.cloudCourse.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.vo.BaseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;

/**
 * <p>
 * 课程时间安排表
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Data
@Accessors(chain = true)
@TableName("course_schedules")
public class CourseSchedulesVo extends BaseVo {

    @ApiModelProperty("时间安排ID，主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("课程ID，关联courses表")
    @TableField(value = "course_id")
    private Long courseId;

    @ApiModelProperty("星期几上课")
    @TableField(value = "day_of_week")
    private String dayOfWeek;

    @ApiModelProperty("上课开始时间")
    @TableField(value = "start_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @ApiModelProperty("上课结束时间")
    @TableField(value = "end_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @ApiModelProperty("上课地点")
    @TableField(value = "location")
    private String location;

    @ApiModelProperty("日程类型：常规课/补课/考试")
    @TableField(value = "schedule_type")
    private String scheduleType;

    @ApiModelProperty("扩展元数据，存储特殊安排信息")
    @TableField(value = "metadata")
    private String metadata;

}
