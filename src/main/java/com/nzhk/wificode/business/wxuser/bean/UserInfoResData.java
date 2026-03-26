package com.nzhk.wificode.business.wxuser.bean;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserInfoResData {
    private String id;
    private String avatarUrl;
    private String nickName;
    private LocalDateTime createTime;
    /** 多角色 */
    private List<String> roles;
    /** 是否管理员 */
    private Boolean admin;
    /** 是否已绑定门店（用于前端展示「门店 WiFi 码」） */
    private Boolean hasStoreBind;
}
