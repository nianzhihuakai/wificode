package com.nzhk.wificode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nzhk.wificode.business.file.entity.UploadedFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UploadedFileMapper extends BaseMapper<UploadedFile> {
}
