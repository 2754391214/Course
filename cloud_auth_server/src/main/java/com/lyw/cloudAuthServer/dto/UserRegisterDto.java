package com.lyw.cloudAuthServer.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;


@Data
public class UserRegisterDto {

    @NotEmpty(message = "用户名不能为空")
    private String userName;
    @NotEmpty(message = "密码必须填写")
    private String passWord;
    private String email;
    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phone;
    private String nickname;
    private Integer userType; // 1-学生 2-教师
    @NotEmpty(message = "验证码不能为空")
    private String verificationCode; // 验证码
}
