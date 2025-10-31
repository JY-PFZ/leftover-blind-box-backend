package nus.iss.se.magicbag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.magicbag.common.constant.ResultStatus;
import nus.iss.se.magicbag.common.exception.BusinessException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService{
    private final S3StorageService storageService;

    public String uploadFile(String dir, String fileName, MultipartFile file) {
        // 验证文件
        if (file.isEmpty()) {
            throw new BusinessException(ResultStatus.FAIL,"File could not be empty");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ResultStatus.FAIL,"Only image files are supported");
        }

        // 验证文件大小 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(ResultStatus.FAIL,"The file size cannot exceed 5MB");
        }

        String key = dir + "/" + fileName + "." + getFileExtension(file.getOriginalFilename());
        try {
            Path tempFile = Files.write(Files.createTempFile("upload-", ""), file.getBytes());
            // 上传文件
            return storageService.upload(key, tempFile);
        } catch (IOException e) {
            log.error("upload file failed,file-{}: {}",file.getOriginalFilename(), ExceptionUtils.getStackTrace(e));
            throw new BusinessException(ResultStatus.FAIL,"Upload file failed");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        return filename == null || !filename.contains(".") ? "" : filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
