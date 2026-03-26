package com.nzhk.wificode.business.wificode.bean;

import lombok.Data;

/**
 * wifi_scan_log 按码按日聚合查询行
 */
@Data
public class WifiTodayCountRow {
    private String wifiCodeId;
    private Integer cnt;
}
