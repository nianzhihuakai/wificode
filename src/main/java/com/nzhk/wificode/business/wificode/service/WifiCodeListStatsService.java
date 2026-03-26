package com.nzhk.wificode.business.wificode.service;

import com.nzhk.wificode.business.wificode.bean.WifiCodeItemResData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeListResData;
import com.nzhk.wificode.business.wificode.bean.WifiTodayCountRow;
import com.nzhk.wificode.business.wificode.entity.WifiCode;
import com.nzhk.wificode.common.utils.BeanConvertUtil;
import com.nzhk.wificode.mapper.WifiScanLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 列表页：今日有效连接（scan_log）+ 汇总
 */
@Service
public class WifiCodeListStatsService {

    @Resource
    private WifiScanLogMapper wifiScanLogMapper;

    private ZoneId zone() {
        return ZoneId.systemDefault();
    }

    public WifiCodeListResData buildListRes(List<WifiCode> entities) {
        if (entities == null || entities.isEmpty()) {
            return WifiCodeListResData.empty();
        }
        LocalDate today = LocalDate.now(zone());
        List<String> ids = entities.stream().map(WifiCode::getId).collect(Collectors.toList());
        Map<String, Integer> todayMap = new HashMap<>();
        if (!ids.isEmpty()) {
            List<WifiTodayCountRow> rows = wifiScanLogMapper.countTodayByWifiIds(today, ids);
            if (rows != null) {
                for (WifiTodayCountRow r : rows) {
                    if (r.getWifiCodeId() != null && r.getCnt() != null) {
                        todayMap.put(r.getWifiCodeId(), r.getCnt());
                    }
                }
            }
        }
        int sumToday = 0;
        int sumTotal = 0;
        List<WifiCodeItemResData> list = entities.stream().map(e -> {
            WifiCodeItemResData row = BeanConvertUtil.copySingleProperties(e, WifiCodeItemResData::new);
            int t = todayMap.getOrDefault(e.getId(), 0);
            row.setTodayCount(t);
            return row;
        }).collect(Collectors.toList());
        for (WifiCodeItemResData row : list) {
            sumToday += row.getTodayCount() != null ? row.getTodayCount() : 0;
            sumTotal += row.getTotalCount() != null ? row.getTotalCount() : 0;
        }
        return new WifiCodeListResData(list, sumToday, sumTotal);
    }
}
