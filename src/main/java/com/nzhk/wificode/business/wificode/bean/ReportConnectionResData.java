package com.nzhk.wificode.business.wificode.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportConnectionResData {
    /** 本次请求是否计入一次有效连接 */
    private boolean counted;
    private Integer totalCount;
}
