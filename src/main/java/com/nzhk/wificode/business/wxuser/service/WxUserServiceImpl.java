package com.nzhk.wificode.business.wxuser.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wificode.business.wxuser.bean.SaveUserInfoReqData;
import com.nzhk.wificode.business.wxuser.bean.UserInfoResData;
import com.nzhk.wificode.business.wxuser.bean.WxUserLoginReqData;
import com.nzhk.wificode.business.wxuser.bean.WxUserLoginResData;
import com.nzhk.wificode.business.wxuser.entity.WxUser;
import com.nzhk.wificode.business.wxuser.vo.WxLoginResVO;
import com.nzhk.wificode.common.cache.ContextCache;
import com.nzhk.wificode.common.cache.UserInfo;
import com.nzhk.wificode.common.exception.BizException;
import com.nzhk.wificode.common.utils.BeanConvertUtil;
import com.nzhk.wificode.common.utils.IdUtil;
import com.nzhk.wificode.common.utils.JwtUtil;
import com.nzhk.wificode.mapper.WxUserMapper;
import com.nzhk.wificode.wechat.config.WechatProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WxUserServiceImpl extends ServiceImpl<WxUserMapper, WxUser> implements IWxUserService {

    private static final String LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    @Resource
    private WechatProperties wechatProperties;
    @Resource
    private RestTemplate restTemplate;

    @Override
    public WxUserLoginResData login(WxUserLoginReqData req) {
        WxLoginResVO loginRes = getOpenid(req.getCode());
        String openId = loginRes.getOpenId();
        String sessionKey = loginRes.getSessionKey();

        WxUser wxUser = baseMapper.selectByOpenId(openId);
        if (wxUser == null) {
            String userId = req.getUserId();
            if (StringUtils.isEmpty(userId)) {
                wxUser = WxUser.builder()
                        .id(IdUtil.getId())
                        .openid(openId)
                        .sessionKey(sessionKey)
                        .nickName(StringUtils.isNotBlank(req.getNickName()) ? req.getNickName() : null)
                        .avatarUrl(StringUtils.isNotBlank(req.getAvatarUrl()) ? req.getAvatarUrl() : null)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .lastLoginTime(LocalDateTime.now())
                        .build();
                baseMapper.insert(wxUser);
            } else {
                WxUser existUser = baseMapper.selectById(userId);
                if (existUser == null) {
                    wxUser = WxUser.builder()
                            .id(IdUtil.getId())
                            .openid(openId)
                            .sessionKey(sessionKey)
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .build();
                    baseMapper.insert(wxUser);
                } else {
                    wxUser = existUser;
                    LambdaUpdateWrapper<WxUser> u = new LambdaUpdateWrapper<>();
                    u.eq(WxUser::getId, userId)
                            .set(WxUser::getOpenid, openId)
                            .set(WxUser::getSessionKey, sessionKey)
                            .set(WxUser::getUpdateTime, LocalDateTime.now())
                            .set(WxUser::getLastLoginTime, LocalDateTime.now());
                    baseMapper.update(null, u);
                }
            }
        } else {
            LambdaUpdateWrapper<WxUser> u = new LambdaUpdateWrapper<>();
            u.eq(WxUser::getId, wxUser.getId())
                    .set(WxUser::getSessionKey, sessionKey)
                    .set(WxUser::getUpdateTime, LocalDateTime.now())
                    .set(WxUser::getLastLoginTime, LocalDateTime.now());
            baseMapper.update(null, u);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", wxUser.getId());
        String token = JwtUtil.generateToken(claims);

        UserInfo userInfo = UserInfo.builder()
                .id(wxUser.getId())
                .avatarUrl(wxUser.getAvatarUrl())
                .nickName(wxUser.getNickName())
                .build();
        return WxUserLoginResData.builder().token(token).userInfo(userInfo).build();
    }

    @Override
    public WxUserLoginResData saveUserInfo(SaveUserInfoReqData data) {
        String userId = ContextCache.getUserId();
        LambdaUpdateWrapper<WxUser> u = new LambdaUpdateWrapper<>();
        u.eq(WxUser::getId, userId)
                .set(WxUser::getNickName, data.getNickName())
                .set(WxUser::getAvatarUrl, data.getAvatarUrl())
                .set(WxUser::getUpdateTime, LocalDateTime.now());
        baseMapper.update(null, u);

        WxUser wxUser = baseMapper.selectById(userId);
        UserInfo userInfo = UserInfo.builder()
                .id(wxUser.getId())
                .avatarUrl(data.getAvatarUrl())
                .nickName(data.getNickName())
                .build();
        return WxUserLoginResData.builder().userInfo(userInfo).build();
    }

    @Override
    public UserInfoResData getUserInfo() {
        String userId = ContextCache.getUserId();
        WxUser wxUser = baseMapper.selectById(userId);
        if (wxUser == null) {
            throw new BizException(40400, "用户不存在");
        }
        return BeanConvertUtil.copySingleProperties(wxUser, UserInfoResData::new);
    }

    private WxLoginResVO getOpenid(String code) {
        String url = LOGIN_URL
                .replace("{appid}", wechatProperties.getAppid())
                .replace("{secret}", wechatProperties.getSecret())
                .replace("{code}", code);
        String json = restTemplate.getForObject(url, String.class);
        JSONObject obj = JSONObject.parseObject(json);
        if (obj == null || obj.getString("openid") == null) {
            throw new BizException(40001, "微信登录失败：" + (obj != null ? obj.getString("errmsg") : "无响应"));
        }
        return WxLoginResVO.builder()
                .openId(obj.getString("openid"))
                .sessionKey(obj.getString("session_key"))
                .build();
    }
}
