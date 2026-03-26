package com.nzhk.wificode.common.cache;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserInfo {

    private String id;
    private String nickName;
    private String avatarUrl;
    private String token;
    /** 角色列表 */
    private List<String> roles;
    /** 是否管理员（roles 含 ADMIN） */
    private Boolean admin;
    /** 是否已绑定至少一家门店（store_owner_id 为本人） */
    private Boolean hasStoreBind;
}
