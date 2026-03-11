package com.nzhk.wificode.business.wificode.service;

import com.nzhk.wificode.business.wificode.bean.WifiCodeCreateReqData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeItemResData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeUpdateReqData;

import java.util.List;

public interface IWifiCodeService {

    WifiCodeItemResData create(WifiCodeCreateReqData data);

    WifiCodeItemResData update(WifiCodeUpdateReqData data);

    WifiCodeItemResData getById(String id);

    List<WifiCodeItemResData> listByUserId(String keyword);

    void deleteById(String id);

    /** 公开获取，供扫码用户使用，不校验登录 */
    WifiCodeItemResData getByIdPublic(String id);
}
