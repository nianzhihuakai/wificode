package com.nzhk.wificode.business.wificode.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("wifi_scan_log")
public class WifiScanLog {

    @TableId
    private String id;
    private String wifiCodeId;
    /** 对应列 user_id：访客 wx_user.id */
    @TableField("user_id")
    private String visitorUserId;
    private LocalDate statDate;
    private LocalDateTime scanTime;
    private String ip;
    private String deviceInfo;
}
