package com.lyw.cloudMember.dto;

import lombok.Data;


@Data
public class UserLoginDto {

    private String password;

    private String phone;

    private String userName;
}
