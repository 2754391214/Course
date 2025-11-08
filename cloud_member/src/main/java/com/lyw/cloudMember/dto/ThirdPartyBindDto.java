package com.lyw.cloudMember.dto;

import lombok.Data;

@Data
public class ThirdPartyBindDto {
    private String identityType; // gitee, weibo
    private String code;
    private String state;
}
