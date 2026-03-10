package com.nzhk.wificode.business.wificode.controller;

import com.nzhk.wificode.business.wificode.bean.WifiCodeCreateReqData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeDeleteReqData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeItemResData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeUpdateReqData;
import com.nzhk.wificode.business.wificode.service.IWifiCodeService;
import com.nzhk.wificode.common.info.RequestInfo;
import com.nzhk.wificode.common.info.ResponseInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/wificode")
public class WifiCodeController {

    @Resource
    private IWifiCodeService wifiCodeService;

    @PostMapping("create")
    public ResponseInfo<WifiCodeItemResData> create(@RequestBody RequestInfo<WifiCodeCreateReqData> requestInfo) {
        return ResponseInfo.success(wifiCodeService.create(requestInfo.getData()));
    }

    @PostMapping("update")
    public ResponseInfo<WifiCodeItemResData> update(@RequestBody RequestInfo<WifiCodeUpdateReqData> requestInfo) {
        return ResponseInfo.success(wifiCodeService.update(requestInfo.getData()));
    }

    @GetMapping("get")
    public ResponseInfo<WifiCodeItemResData> get(@RequestParam String id) {
        return ResponseInfo.success(wifiCodeService.getById(id));
    }

    @GetMapping("list")
    public ResponseInfo<List<WifiCodeItemResData>> list(@RequestParam(required = false) String keyword) {
        return ResponseInfo.success(wifiCodeService.listByUserId(keyword));
    }

    @PostMapping("delete")
    public ResponseInfo<Void> delete(@RequestBody RequestInfo<WifiCodeDeleteReqData> requestInfo) {
        wifiCodeService.deleteById(requestInfo.getData().getId());
        return ResponseInfo.success(null);
    }
}
