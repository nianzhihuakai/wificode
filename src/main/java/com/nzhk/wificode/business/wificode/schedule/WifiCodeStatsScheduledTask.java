package com.nzhk.wificode.business.wificode.schedule;

import com.nzhk.wificode.business.wificode.service.IWifiCodeStatsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每日回写各码昨日有效连接数（依赖 wifi_scan_log.stat_date）
 */
@Slf4j
@Component
public class WifiCodeStatsScheduledTask {

    @Resource
    private IWifiCodeStatsService wifiCodeStatsService;

    @Scheduled(cron = "0 5 0 * * ?")
    public void refreshYesterdayCounts() {
        try {
            wifiCodeStatsService.refreshYesterdayCounts();
        } catch (Exception e) {
            log.error("refreshYesterdayCounts failed", e);
        }
    }
}
