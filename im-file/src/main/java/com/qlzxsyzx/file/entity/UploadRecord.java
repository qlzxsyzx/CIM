package com.qlzxsyzx.file.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("t_upload_records")
@Data
public class UploadRecord {
    @TableId
    private Long recordId;

    private Long userId;

    private String fileName;

    private String realName;

    private String ext;

    private Long fileSize;

    private LocalDateTime uploadTime;

    private String bucketName;

    private String objectName;

    // 0准备，1上传中，2成功，3失败，4暂停
    private Integer uploadStatus;

    // 0未删除，1已删除
    private Integer isDelete;
}
