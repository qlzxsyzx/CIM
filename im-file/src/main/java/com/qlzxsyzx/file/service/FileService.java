package com.qlzxsyzx.file.service;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.file.dto.BigFileInfoDto;
import com.qlzxsyzx.file.dto.BigFilePartDto;
import com.qlzxsyzx.file.dto.MergeFileDto;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    // 小文件上传
    ResponseEntity  uploadSmallFile(Long userId, MultipartFile file);

    // 记录大文件上传信息，记录分片数据
    ResponseEntity  initUploadBigFile(BigFileInfoDto bigFileInfoDto);

    // 上传大文件分片
    ResponseEntity uploadBigFilePart(BigFilePartDto bigFilePartDto, MultipartFile file);

    // 合并分片
    ResponseEntity mergeBigFile(MergeFileDto mergeFileDto);
}
