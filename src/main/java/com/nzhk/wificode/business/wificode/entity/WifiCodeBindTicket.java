package com.nzhk.wificode.business.wificode.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wifi_code_bind_ticket")
public class WifiCodeBindTicket {

    @TableId
    private String id;
    private String wifiCodeId;
    private String code;
    private LocalDateTime expireAt;
    private LocalDateTime usedAt;
    private String usedByUserId;
    private LocalDateTime createTime;
}
