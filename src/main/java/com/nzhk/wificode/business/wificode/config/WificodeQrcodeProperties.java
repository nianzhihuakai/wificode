package com.nzhk.wificode.business.wificode.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wificode.qrcode")
public class WificodeQrcodeProperties {

    /**
     * 小程序码 png 缓存落盘目录（用于 nginx 静态化/直出）
     * 目录结构：{storageDir}/miniprogram/{id}.png
     */
    private String storageDir = "E:/data/wificode/qrcode";
}

