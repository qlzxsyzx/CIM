package com.qlzxsyzx.file.dto;

import lombok.Data;

@Data
public class BigFilePartDto {
    private Long recordId;
    private Long segmentId;
    private String fileName;
}
