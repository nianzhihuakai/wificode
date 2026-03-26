package com.nzhk.wificode.business.wificode.bean;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BindCodeGenerateResData {
    private String bindCode;
    private LocalDateTime expireAt;
}
