package com.nzhk.wificode.business.file.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("uploaded_file")
public class UploadedFile {

    @TableId
    private String id;
    private String fileId;
    private String userId;
    private String bizType;
    private String originalName;
    private String storagePath;
    private String url;
    private String mimeType;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private Integer status;
    private LocalDateTime createTime;
}
