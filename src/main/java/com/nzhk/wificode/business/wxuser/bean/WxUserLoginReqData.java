package com.nzhk.wificode.business.wxuser.bean;

import lombok.Data;

@Data
public class WxUserLoginReqData {
    private String code;
    private String userId;
    private String nickName;
    private String avatarUrl;
}
