package com.nzhk.wificode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nzhk.wificode.business.wificode.bean.WifiTodayCountRow;
import com.nzhk.wificode.business.wificode.entity.WifiScanLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface WifiScanLogMapper extends BaseMapper<WifiScanLog> {

    @Select("<script>"
            + "SELECT wifi_code_id AS wifiCodeId, COUNT(*)::int AS cnt FROM wifi_scan_log "
            + "WHERE stat_date = #{statDate} AND wifi_code_id IN "
            + "<foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">#{id}</foreach> "
            + "GROUP BY wifi_code_id"
            + "</script>")
    List<WifiTodayCountRow> countTodayByWifiIds(@Param("statDate") LocalDate statDate, @Param("ids") List<String> ids);
}
