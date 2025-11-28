package com.lyw.cloudRanking.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
public class CourseBasicInfoDto {
    private Long courseId;
    private String courseName;
    private String teacherName;
    private String department;
}
