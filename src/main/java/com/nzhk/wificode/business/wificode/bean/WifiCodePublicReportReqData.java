package com.nzhk.wificode.business.wificode.bean;

import lombok.Data;

@Data
public class WifiCodePublicReportReqData {
    /** WiFi 码主键 */
    private String wifiCodeId;
    /** 可选，客户端设备简述 */
    private String deviceInfo;
}
