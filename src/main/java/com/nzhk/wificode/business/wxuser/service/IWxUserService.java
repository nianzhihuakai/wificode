package com.nzhk.wificode.business.wxuser.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nzhk.wificode.business.wxuser.bean.SaveUserInfoReqData;
import com.nzhk.wificode.business.wxuser.bean.UserInfoResData;
import com.nzhk.wificode.business.wxuser.bean.WxUserLoginReqData;
import com.nzhk.wificode.business.wxuser.bean.WxUserLoginResData;
import com.nzhk.wificode.business.wxuser.entity.WxUser;

public interface IWxUserService extends IService<WxUser> {

    WxUserLoginResData login(WxUserLoginReqData req);

    WxUserLoginResData saveUserInfo(SaveUserInfoReqData data);

    UserInfoResData getUserInfo();
}
