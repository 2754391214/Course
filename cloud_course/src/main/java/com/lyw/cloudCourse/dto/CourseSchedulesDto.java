package com.lyw.cloudCourse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
public class CourseSchedulesDto extends BaseDto {

    @ApiModelProperty("时间安排ID，主键自增")
    private Long id;

    @ApiModelProperty("课程ID，关联courses表")
    private Long courseId;

    @ApiModelProperty("星期几上课")
    private String dayOfWeek;

    @ApiModelProperty("上课开始时间")
    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    private LocalTime startTime;

    @ApiModelProperty("上课结束时间")
    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    private LocalTime endTime;

    @ApiModelProperty("上课地点")
    private String location;

    @ApiModelProperty("日程类型：常规课/补课/考试")
    private String scheduleType;

    @ApiModelProperty("扩展元数据，存储特殊安排信息")
    private String metadata;

}
