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
    /** 今日有效连接（由 wifi_scan_log 按 stat_date=今日 聚合） */
    private Integer todayCount;
    private Integer yesterdayCount;
    private Integer totalCount;
    private Integer status;
    private LocalDateTime createTime;
}
