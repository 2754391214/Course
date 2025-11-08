package com.lyw.cloudAuthServer.dto;

import lombok.Data;


@Data
public class UserLoginDto {

    private String loginacct;

    private String password;
}
