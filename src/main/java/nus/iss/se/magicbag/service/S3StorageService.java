package nus.iss.se.magicbag.service;

import nus.iss.se.magicbag.common.properties.S3Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucketName;

    @Autowired
    public S3StorageService(S3Client s3Client, S3Properties properties) {
        this.s3Client = s3Client;
        this.bucketName = properties.getBucketName();
    }

    /**
     * 上传文件到 S3
     *
     * @param key      S3 对象键（如 "folder/file.xlsx"）
     * @param filePath 本地文件路径
     */
    public void upload(String key, Path filePath) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromFile(filePath));
    }

    /**
     * 上传字节数组（适用于小文件或内存数据）
     */
    public void upload(String key, byte[] data) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(data));
    }

    /**
     * 下载文件到本地路径
     *
     * @param key      S3 对象键
     * @param filePath 本地保存路径
     */
    public void download(String key, Path filePath) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.getObject(getRequest, filePath);
    }

    /**
     * 下载为字节数组（适用于小文件）
     */
    public byte[] downloadAsBytes(String key) throws IOException {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getRequest)) {
            return response.readAllBytes();
        }
    }

    /**
     * 删除 S3 对象
     */
    public void delete(String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);
    }


    /**
     * 判断对象是否存在
     */
    public boolean exists(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * 获取对象元数据（可选）
     */
    public Optional<HeadObjectResponse> getMetadata(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            return Optional.of(s3Client.headObject(headRequest));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }
}