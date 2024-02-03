package com.qlzxsyzx.file.service.impl;

import com.qlzxsyzx.file.service.MinioService;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MinioServiceImpl implements MinioService {
    public static final Integer SMALL_FILE_MAX_SIZE = 1024 * 1024 * 10; // 小文件最大10M

    public static final Integer MAX_FILE_SIZE = 1024 * 1024 * 100; // 大文件最大100M

    public static final Integer MIN_SEGMENT_SIZE = 1024 * 1024 * 2; // 分片最小2M

    public static final Integer MAX_SEGMENT_SIZE = 1024 * 1024 * 5; // 分片最大5M

    @Autowired
    private MinioClient minioClient;

    @Override
    public boolean isExist(String bucketName, String objectName) {
        if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(objectName)) {
            throw new RuntimeException("桶名或对象名为空");
        }
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build()) &&
                    minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build()) != null;
        } catch (Exception e) {
            throw new RuntimeException("判断文件是否存在失败", e);
        }
    }

    @Override
    public void uploadFile(MultipartFile file, String bucketName, String objectName) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }
        if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(objectName)) {
            throw new RuntimeException("桶名或对象名为空");
        }
        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType()).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream downloadForStream(String bucketName, String objectName) {
        if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(objectName)) {
            throw new RuntimeException("桶名或对象名为空");
        }
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            throw new RuntimeException("下载文件失败", e);
        }
    }

    @Override
    public void remove(String bucketName, String objectName) {
        if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(objectName)) {
            throw new RuntimeException("桶名或对象名为空");
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            throw new RuntimeException("删除文件失败", e);
        }
    }

    @Override
    public void mergeFile(String bucketName, String objectName, List<String> parts) {
        if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(objectName)) {
            throw new RuntimeException("桶名或对象名为空");
        }
        try {
            //  创建一个合并文件的策略
            List<ComposeSource> sources = new ArrayList<>();
            parts.forEach(part -> sources.add(ComposeSource.builder().bucket(bucketName).object(part).build()));
            minioClient.composeObject(ComposeObjectArgs.builder().bucket(bucketName).object(objectName).sources(sources).build());
            // 删除分片
            for (String part : parts) {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(part).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("合并文件失败", e);
        }
    }
}
