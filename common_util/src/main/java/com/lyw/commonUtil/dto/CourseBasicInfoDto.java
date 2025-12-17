package com.lyw.commonUtil.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CourseBasicInfoDto {
    private Long courseId;
    private String courseName;
    private String teacherName;
    private String department;
}
