package com.qlzxsyzx.file.vo;

import lombok.Data;

@Data
public class FileVo {
    private Long recordId;

    private String fileName;

    private Long[] segmentIds;
}
