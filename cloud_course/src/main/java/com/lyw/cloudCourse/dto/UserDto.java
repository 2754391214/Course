package com.lyw.cloudCourse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.vo.BaseVo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserDto extends BaseVo {

    private Long id;

    private String userName;

    private String email;

    private String phone;

    private String nickname;

    private String avatar;

    private Integer gender;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "GMT+8")
    private Date birthday;

    private String bio;

    private Integer userType;

    private Integer status;

    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private TeacherProfileDto teacherProfile;
}
