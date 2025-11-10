package com.lyw.cloudAuthServer.dto;

import lombok.Data;


@Data
public class UserLoginDto {

    private String userName;

    private String phone;

    private String password;
}
