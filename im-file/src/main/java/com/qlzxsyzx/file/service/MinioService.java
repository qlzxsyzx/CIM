package com.qlzxsyzx.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface MinioService {
    // 判断文件是否存在
    boolean isExist(String bucketName, String objectName);

    // 上传文件
    void uploadFile(MultipartFile file, String bucketName, String objectName);


    // 下载文件
    InputStream downloadForStream(String bucketName, String objectName);

    // 删除文件
    void remove(String bucketName, String objectName);

    // 合并分片文件
    void mergeFile(String bucketName, String objectName, List<String> parts);
}
