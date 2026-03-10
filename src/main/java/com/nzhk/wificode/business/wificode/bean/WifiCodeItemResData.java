package com.nzhk.wificode.business.wificode.bean;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WifiCodeItemResData {
    private String id;
    private String brandName;
    private String ssid;
    private String password;
    private String authType;
    private Integer yesterdayCount;
    private Integer totalCount;
    private Integer status;
    private LocalDateTime createTime;
}
