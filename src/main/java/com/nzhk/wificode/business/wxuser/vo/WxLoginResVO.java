package com.nzhk.wificode.business.wxuser.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WxLoginResVO {
    private String sessionKey;
    private String openId;
}
