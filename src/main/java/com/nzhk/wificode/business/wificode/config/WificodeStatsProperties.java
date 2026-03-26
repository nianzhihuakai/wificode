package com.nzhk.wificode.business.wificode.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wificode.stats")
public class WificodeStatsProperties {

    /** 绑定码有效时长（小时） */
    private int bindCodeTtlHours = 72;
    /** 同一 IP 对同一码每分钟最多上报次数 */
    private int reportRateLimitPerMinute = 30;

    /** 同一用户每天最多尝试绑定次数（含失败） */
    private int bindAttemptsPerDay = 5;
    /** 同一用户每分钟最多生成绑定码次数 */
    private int bindCodePerUserPerMinute = 5;
    /** 同一 WiFi 码每小时最多生成绑定码次数 */
    private int bindCodePerWifiPerHour = 10;
    /** 同一用户每分钟新建+编辑 WiFi 码合计次数 */
    private int wifiCodeWritePerUserPerMinute = 5;
}
