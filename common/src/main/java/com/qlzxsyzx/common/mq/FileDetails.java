package com.qlzxsyzx.common.mq;

import lombok.Data;

@Data
public class FileDetails {
    private Long recordId;

    private String realName;

    private String ext;

    private Long fileSize;
}
