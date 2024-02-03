package com.qlzxsyzx.file.vo;

import lombok.Data;

@Data
public class FileVo {
    private String recordId;

    private String fileName;

    private String[] segmentIds;
}
