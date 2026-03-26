package com.nzhk.wificode.business.wificode.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WifiCodeListResData {

    private List<WifiCodeItemResData> list;
    /** 本页列表内各码「今日有效连接」之和 */
    private Integer sumTodayCount;
    /** 本页列表内各码「累计有效连接」之和 */
    private Integer sumTotalCount;

    public static WifiCodeListResData empty() {
        return new WifiCodeListResData(Collections.emptyList(), 0, 0);
    }
}
