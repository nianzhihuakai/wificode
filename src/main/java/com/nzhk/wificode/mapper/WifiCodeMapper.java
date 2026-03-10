package com.nzhk.wificode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nzhk.wificode.business.wificode.entity.WifiCode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WifiCodeMapper extends BaseMapper<WifiCode> {
}
