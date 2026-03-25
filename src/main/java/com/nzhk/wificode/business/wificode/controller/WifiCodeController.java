package com.nzhk.wificode.business.wificode.controller;

import com.nzhk.wificode.business.wificode.bean.WifiCodeCreateReqData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeDeleteReqData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeItemResData;
import com.nzhk.wificode.business.wificode.bean.WifiCodeUpdateReqData;
import com.nzhk.wificode.business.wificode.config.WificodeQrcodeProperties;
import com.nzhk.wificode.business.wificode.service.IWifiCodeService;
import com.nzhk.wificode.common.info.RequestInfo;
import com.nzhk.wificode.common.info.ResponseInfo;
import com.nzhk.wificode.wechat.service.WechatApiService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
@RestController
public class WifiCodeController {

    @Resource
    private IWifiCodeService wifiCodeService;
    @Resource
    private WechatApiService wechatApiService;

    /**
     * 小程序码缓存落盘目录（用于 nginx 静态化/直出）
     * 目录结构：{storageDir}/miniprogram/{id}.png
     */
    @Resource
    private WificodeQrcodeProperties qrcodeProperties;

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

    /**
     * 生成小程序 URL Link，用于二维码扫码跳转
     * 无需登录，供扫码展示页使用
     * query 含 id、uid，便于扫码后统计与溯源
     */
    @GetMapping("urllink")
    public ResponseInfo<Map<String, String>> getUrlLink(@RequestParam String id) {
        var entity = wifiCodeService.getEntityByIdPublic(id);
        String path = "pages/wifiqrcode/wifiqrcode";
        String query = StringUtils.isNotBlank(entity.getUserId())
                ? "id=" + id + "&uid=" + entity.getUserId()
                : "id=" + id;
        String urlLink = wechatApiService.generateUrlLink(path, query);
        Map<String, String> data = new HashMap<>();
        data.put("urlLink", urlLink);
        return ResponseInfo.success(data);
    }

    /**
     * 生成微信小程序码（官方圆形码样式）
     * 无需登录，供扫码展示页使用
     * 返回 Base64 图片，前端用 data:image/png;base64,xxx 展示
     */
    @GetMapping("miniprogramCode")
    public ResponseInfo<Map<String, String>> getMiniprogramCode(@RequestParam String id) {
        wifiCodeService.getEntityByIdPublic(id);
        String page = "pages/wifiqrcode/wifiqrcode";
        String imageBase64 = wechatApiService.getUnlimitedWxacode(id, page);
        Map<String, String> data = new HashMap<>();
        data.put("imageBase64", imageBase64);
        return ResponseInfo.success(data);
    }

    /**
     * 小程序码 png 直出（同时支持本地缓存）
     * nginx 建议使用 alias/try_files 先查磁盘，不存在再反代到此接口生成。
     */
    @GetMapping("/qrcode/miniprogram/{id}.png")
    public ResponseEntity<org.springframework.core.io.Resource> getMiniprogramCodeImagePng(@PathVariable String id) {
        wifiCodeService.getEntityByIdPublic(id); // 校验存在且有效

        String page = "pages/wifiqrcode/wifiqrcode";
        Path filePath = Paths.get(qrcodeProperties.getStorageDir(), "miniprogram", id + ".png");

        try {
            if (Files.exists(filePath)) {
                return ResponseEntity.ok()
                        .header("Cache-Control", "max-age=2592000")
                        .contentType(MediaType.IMAGE_PNG)
                        .body(new FileSystemResource(filePath));
            }

            // 生成 base64 -> 写入文件
            String imageBase64 = wechatApiService.getUnlimitedWxacode(id, page);
            byte[] bytes = Base64.getDecoder().decode(imageBase64);

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, bytes);

            return ResponseEntity.ok()
                    .header("Cache-Control", "max-age=2592000")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(new FileSystemResource(filePath));
        } catch (Exception e) {
            log.error("getMiniprogramCodeImagePng failed, id:{}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 公开获取 WiFi 码详情，供扫码用户打开小程序后加载数据
     * 无需登录
     */
    @GetMapping("public/get")
    public ResponseInfo<WifiCodeItemResData> getPublic(@RequestParam String id) {
        return ResponseInfo.success(wifiCodeService.getByIdPublic(id));
    }
}
