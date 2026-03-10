package com.nzhk.wificode.common.cache;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfo {

    private String id;
    private String nickName;
    private String avatarUrl;
    private String token;
}
