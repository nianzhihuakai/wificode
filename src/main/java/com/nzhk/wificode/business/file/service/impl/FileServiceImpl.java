package com.nzhk.wificode.business.file.service.impl;

import com.nzhk.wificode.business.file.bean.FileUploadResData;
import com.nzhk.wificode.business.file.entity.UploadedFile;
import com.nzhk.wificode.business.file.service.IFileService;
import com.nzhk.wificode.common.exception.BizException;
import com.nzhk.wificode.common.utils.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nzhk.wificode.mapper.UploadedFileMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements IFileService {

    private static final long MAX_SIZE = 2L * 1024 * 1024;
    private static final Set<String> ALLOW_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOW_BIZ_TYPE = Set.of("avatar");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Value("${wificode.file.storage-dir:uploads}")
    private String storageDir;

    @Value("${wificode.file.base-url:/file/access}")
    private String baseUrl;

    @Resource
    private UploadedFileMapper uploadedFileMapper;

    @Override
    public FileUploadResData upload(String userId, MultipartFile file, String bizType, String clientTraceId) {
        if (file == null || file.isEmpty()) {
            throw new BizException(40000, "文件不能为空");
        }
        if (StringUtils.isBlank(bizType) || !ALLOW_BIZ_TYPE.contains(bizType)) {
            throw new BizException(40000, "bizType 参数错误，仅支持 avatar");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException(40002, "文件大小不能超过 2MB");
        }

        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        if (StringUtils.isBlank(ext) || !ALLOW_EXT.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new BizException(40001, "文件类型不支持，仅支持 jpg/png/webp");
        }

        String fileId = "f_" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String relativeDir = bizType + "/" + LocalDate.now().format(DATE_FORMATTER);
        String storedName = fileId + "." + ext.toLowerCase(Locale.ROOT);
        Path targetDir = Paths.get(storageDir, relativeDir);
        Path targetPath = targetDir.resolve(storedName);
        try {
            Files.createDirectories(targetDir);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("upload save file error, userId:{}, clientTraceId:{}", userId, clientTraceId, e);
            throw new BizException(50000, "文件保存失败");
        }

        Integer width = null;
        Integer height = null;
        try {
            BufferedImage image = ImageIO.read(targetPath.toFile());
            if (image != null) {
                width = image.getWidth();
                height = image.getHeight();
            }
        } catch (Exception e) {
            log.warn("read image metadata failed, fileId:{}", fileId, e);
        }

        String accessPath = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/") + fileId;

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setId(IdUtil.getId());
        uploadedFile.setFileId(fileId);
        uploadedFile.setUserId(userId);
        uploadedFile.setBizType(bizType);
        uploadedFile.setOriginalName(originalName);
        uploadedFile.setStoragePath(targetPath.toAbsolutePath().toString());
        uploadedFile.setUrl(accessPath);
        uploadedFile.setMimeType(file.getContentType());
        uploadedFile.setFileSize(file.getSize());
        uploadedFile.setWidth(width);
        uploadedFile.setHeight(height);
        uploadedFile.setStatus(1);
        uploadedFile.setCreateTime(LocalDateTime.now());
        uploadedFileMapper.insert(uploadedFile);

        FileUploadResData resData = new FileUploadResData();
        resData.setFileId(fileId);
        resData.setUrl(accessPath);
        resData.setWidth(width);
        resData.setHeight(height);
        resData.setSize(file.getSize());
        resData.setMimeType(file.getContentType());
        return resData;
    }

    @Override
    public UploadedFile getByFileId(String fileId) {
        return uploadedFileMapper.selectOne(
                new LambdaQueryWrapper<UploadedFile>()
                        .eq(UploadedFile::getFileId, fileId)
                        .eq(UploadedFile::getStatus, 1)
        );
    }

    private String getExtension(String fileName) {
        if (StringUtils.isBlank(fileName) || !fileName.contains(".")) {
            return null;
        }
        return StringUtils.substringAfterLast(fileName, ".");
    }
}
