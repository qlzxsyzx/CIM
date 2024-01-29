package com.qlzxsyzx.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
@Data
public class MinioProperties {
    private String url;

    private String accessKey;

    private String secretKey;

    private String privateBucketName;

    private String publicBucketName;

    private String imagePath;

    private String filePath;
}
