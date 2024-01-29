package com.qlzxsyzx.file.dto;

import lombok.Data;

@Data
public class BigFileInfoDto {
    private Long userId;
    private String fileName;//文件名
    private Long fileSize;//文件大小
    private Integer segmentCount;//分片数量
    private Integer segmentSize;//分片大小
}
