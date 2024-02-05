package com.qlzxsyzx.file.vo;

import lombok.Data;

@Data
public class FileDetailsVo {
    private Long recordId;

    private String realName;

    private String ext;

    private Long fileSize;
}
