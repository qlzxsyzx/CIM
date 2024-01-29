package com.qlzxsyzx.file.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_upload_segments")
public class UploadSegment {
    @TableId
    private Long segmentId;

    private Long recordId;

    private Integer segmentNumber;

    // 0 未上传，1 已上传
    private Integer uploadStatus;
}
