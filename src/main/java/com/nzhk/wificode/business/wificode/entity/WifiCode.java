package com.nzhk.wificode.business.wificode.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wifi_code")
public class WifiCode {

    @TableId
    private String id;
    /** 推销员/创建者 */
    private String userId;
    /** 店主，绑定后非空 */
    private String storeOwnerId;
    private String brandName;
    private String ssid;
    private String password;
    private String authType;
    private Integer yesterdayCount;
    private Integer totalCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
