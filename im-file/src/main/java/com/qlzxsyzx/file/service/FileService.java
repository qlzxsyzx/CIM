package com.qlzxsyzx.file.service;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.file.dto.BigFileInfoDto;
import com.qlzxsyzx.file.dto.BigFilePartDto;
import com.qlzxsyzx.file.dto.MergeFileDto;
import com.qlzxsyzx.file.entity.UploadRecord;
import com.qlzxsyzx.file.vo.FileDetailsVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface FileService {
    // 小文件上传
    ResponseEntity  uploadSmallFile(Long userId, MultipartFile file);

    // 记录大文件上传信息，记录分片数据
    ResponseEntity  initUploadBigFile(BigFileInfoDto bigFileInfoDto);

    // 上传大文件分片
    ResponseEntity uploadBigFilePart(BigFilePartDto bigFilePartDto, MultipartFile file);

    // 合并分片
    ResponseEntity mergeBigFile(MergeFileDto mergeFileDto);

    FileDetailsVo getFileDetails(Long recordId);

    FileDetailsVo covertToFileDetailsVo(UploadRecord uploadRecord);

    void downloadSmallFile(Long userId, Long recordId, HttpServletResponse response);

    ResponseEntity initDownloadBigFile(Long userId, Long recordId);

    void downloadBigFilePart(Long userId, Long recordId, Integer partNum, Long partSize, HttpServletResponse response);
}
