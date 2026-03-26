package com.nzhk.wificode.business.wificode.service;

import com.nzhk.wificode.business.wificode.bean.BindCodeGenerateResData;
import com.nzhk.wificode.business.wificode.bean.ReportConnectionResData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeListResData;

public interface IWifiCodeStatsService {

    ReportConnectionResData reportConnection(String wifiCodeId, String visitorUserId, String ip, String deviceInfo);

    BindCodeGenerateResData generateBindCode(String wifiCodeId);

    void bindStore(String bindCode);

    WifiCodeListResData listByStoreOwner(String keyword);

    void refreshYesterdayCounts();
}
