package com.qlzxsyzx.file.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("t_download_records")
@Data
public class DownloadRecord {
    private Long recordId;

    private Long userId;

    private String fileName;

    private String realName;

    private String ext;

    private Long fileSize;

    private LocalDateTime downloadTime;

    private String bucketName;

    private String objectName;

    // 0准备，1上传中，2成功，3失败
    private Integer downloadStatus;
}
