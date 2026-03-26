package com.nzhk.wificode.business.wxuser.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nzhk.wificode.common.mybatis.PostgresStringArrayTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * autoResultMap：查询结果映射 roles（varchar[]）时必须开启，否则 TypeHandler 不生效，会按字符串解析导致反射构造失败。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "wx_user", autoResultMap = true)
public class WxUser {

    @TableId
    private String id;
    private String openid;
    private String unionid;
    private String sessionKey;
    private String nickName;
    private String avatarUrl;
    private Integer gender;
    private String country;
    private String province;
    private String city;
    private String language;
    private String phone;
    private Integer status;
    /** 多角色：SALES / STORE / ADMIN 等，PostgreSQL varchar[] */
    @TableField(typeHandler = PostgresStringArrayTypeHandler.class)
    private String[] roles;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime lastLoginTime;
}
