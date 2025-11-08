package com.lyw.cloudMember.dto;

import lombok.Data;


@Data
public class UserRegisterDto {

    private String username;
    private String password;
    private String email;
    private String phone;
    private String nickname;
    private Integer userType; // 1-学生 2-教师
    private String verificationCode; // 验证码
    private String avatar;
}
