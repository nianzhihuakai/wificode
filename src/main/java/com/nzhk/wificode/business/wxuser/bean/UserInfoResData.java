package com.nzhk.wificode.business.wxuser.bean;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoResData {
    private String id;
    private String avatarUrl;
    private String nickName;
    private LocalDateTime createTime;
}
