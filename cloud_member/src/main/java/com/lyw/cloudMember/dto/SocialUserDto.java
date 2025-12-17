package com.lyw.cloudMember.dto;

import lombok.Data;

import java.io.Serializable;


@Data
public class SocialUserDto implements Serializable {

    private String access_token;
    private String token_type;
    private String refresh_token;
    private String scope;
    private Long create_at;
    private String remind_in;
    private Long expires_in;
    private String uid;
    private String isRealName;
    private String id;
    private String name;
    private String avatarUrl;
    private String bio;
    private String email;
    private String login;

    private String socialType;
    private String nickname;
    private String avatar;
    private String socialUid;
}
