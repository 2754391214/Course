package com.lyw.cloudCourse.dto;

import com.lyw.commonUtil.vo.BaseVo;
import lombok.Data;

@Data
public class TeacherProfileDto extends BaseVo {

    private Long id;

    private Long userId;

    private String teacherId;

    private String name;

    private String avatar;

    private String title;

    private String department;

    private String office;

    private String researchField;

    private Integer teachingYears;

    private Integer isTutor;

    private Integer employmentStatus;

}
