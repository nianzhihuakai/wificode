package com.nzhk.wificode.business.file.service;

import com.nzhk.wificode.business.file.bean.FileUploadResData;
import com.nzhk.wificode.business.file.entity.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {

    FileUploadResData upload(String userId, MultipartFile file, String bizType, String clientTraceId);

    UploadedFile getByFileId(String fileId);
}
