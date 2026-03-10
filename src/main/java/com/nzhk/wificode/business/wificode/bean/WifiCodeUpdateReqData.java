package com.nzhk.wificode.business.wificode.bean;

import lombok.Data;

@Data
public class WifiCodeUpdateReqData {
    private String id;
    private String brandName;
    private String ssid;
    private String password;
    private String authType;
}
