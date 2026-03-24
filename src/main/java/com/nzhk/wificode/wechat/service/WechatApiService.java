package com.nzhk.wificode.wechat.service;

import com.alibaba.fastjson.JSONObject;
import com.nzhk.wificode.common.exception.BizException;
import com.nzhk.wificode.wechat.config.WechatProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

/**
 * 微信开放接口：access_token、URL Link 等
 */
@Slf4j
@Service
public class WechatApiService {

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";
    private static final String GENERATE_URLLINK_URL = "https://api.weixin.qq.com/wxa/generate_urllink?access_token={access_token}";
    private static final String GET_WXACODE_UNLIMIT_URL = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token={access_token}";

    /** access_token 提前 5 分钟过期 */
    private static final int EXPIRE_BUFFER_SECONDS = 300;

    @Resource
    private WechatProperties wechatProperties;
    @Resource
    private RestTemplate restTemplate;

    private String cachedAccessToken;
    private long tokenExpireAt;

    /**
     * 获取 access_token（带缓存）
     */
    public String getAccessToken() {
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireAt) {
            return cachedAccessToken;
        }
        String url = TOKEN_URL
                .replace("{appid}", wechatProperties.getAppid())
                .replace("{secret}", wechatProperties.getSecret());
        String json = restTemplate.getForObject(url, String.class);
        JSONObject obj = JSONObject.parseObject(json);
        if (obj == null || obj.getString("access_token") == null) {
            throw new BizException(50001, "获取 access_token 失败：" + (obj != null ? obj.getString("errmsg") : "无响应"));
        }
        cachedAccessToken = obj.getString("access_token");
        int expiresIn = obj.getIntValue("expires_in");
        tokenExpireAt = System.currentTimeMillis() + (expiresIn - EXPIRE_BUFFER_SECONDS) * 1000L;
        return cachedAccessToken;
    }

    /**
     * 生成 URL Link，扫码后打开小程序
     *
     * @param path  页面路径，如 pages/wifiqrcode/wifiqrcode
     * @param query query 参数，如 id=xxx
     * @return 如 https://wxaurl.cn/xxx
     */
    public String generateUrlLink(String path, String query) {
        String accessToken = getAccessToken();
        String url = GENERATE_URLLINK_URL.replace("{access_token}", accessToken);

        JSONObject body = new JSONObject();
        body.put("path", path);
        body.put("query", query != null ? query : "");
        body.put("expire_type", 1); // 1=永久有效

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);
        String json = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
        JSONObject res = JSONObject.parseObject(json);
        if (res == null || res.getIntValue("errcode") != 0) {
            int errcode = res != null ? res.getIntValue("errcode") : -1;
            String errmsg = res != null ? res.getString("errmsg") : "无响应";
            if (errcode == 40001) {
                cachedAccessToken = null; // token 失效，下次重新获取
            }
            throw new BizException(50002, "生成 URL Link 失败：" + errmsg);
        }
        return res.getString("url_link");
    }

    /**
     * 生成小程序码（微信官方样式，圆形码）
     * scene 最大 32 字符，此处仅传 id
     *
     * @param scene 场景值，如 wifi_code 的 id（32 位）
     * @param page  页面路径，如 pages/wifiqrcode/wifiqrcode
     * @return Base64 编码的 PNG 图片，可直接用于 img src
     */
    public String getUnlimitedWxacode(String scene, String page) {
        if (scene == null || scene.length() > 32) {
            throw new BizException(40003, "scene 不能为空且不超过 32 字符");
        }
        String accessToken = getAccessToken();
        String url = GET_WXACODE_UNLIMIT_URL.replace("{access_token}", accessToken);

        JSONObject body = new JSONObject();
        body.put("scene", scene);
        body.put("page", page != null ? page : "pages/wifiqrcode/wifiqrcode");
        body.put("check_path", !Boolean.FALSE.equals(wechatProperties.getCheckPath()));
        body.put("width", 280);
        body.put("auto_color", false);
        body.put("is_hyaline", false);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);
        byte[] bytes = response.getBody();
        if (bytes == null || bytes.length == 0) {
            throw new BizException(50003, "生成小程序码失败：无响应");
        }
        if (bytes[0] == '{') {
            JSONObject err = JSONObject.parseObject(new String(bytes));
            int errcode = err.getIntValue("errcode");
            if (errcode == 40001) {
                cachedAccessToken = null;
            }
            throw new BizException(50003, "生成小程序码失败：" + err.getString("errmsg"));
        }
        return Base64.getEncoder().encodeToString(bytes);
    }
}
