package com.nzhk.wificode.wechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wificode.wechat")
public class WechatProperties {

    private String appid;
    private String secret;
    /** 生成小程序码时是否校验 page 存在，开发环境可设为 false 便于未发布时测试 */
    private Boolean checkPath = true;
}
