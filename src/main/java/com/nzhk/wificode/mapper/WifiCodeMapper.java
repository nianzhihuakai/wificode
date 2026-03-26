package com.nzhk.wificode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nzhk.wificode.business.wificode.entity.WifiCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

@Mapper
public interface WifiCodeMapper extends BaseMapper<WifiCode> {

    @Select("SELECT COUNT(*) FROM wifi_code WHERE store_owner_id = #{userId} AND status = 1")
    long countActiveByStoreOwner(@Param("userId") String userId);

    /**
     * 按 wifi_scan_log 汇总回写各码昨日有效连接数
     */
    @Update("""
            UPDATE wifi_code w SET yesterday_count = COALESCE((
                SELECT CAST(COUNT(*) AS INTEGER) FROM wifi_scan_log s
                WHERE s.wifi_code_id = w.id AND s.stat_date = #{yesterday}
            ), 0)
            """)
    void refreshYesterdayCountsForAll(@Param("yesterday") LocalDate yesterday);
}
