package com.qlzxsyzx.file.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.file.dto.BigFileInfoDto;
import com.qlzxsyzx.file.dto.BigFilePartDto;
import com.qlzxsyzx.file.dto.MergeFileDto;
import com.qlzxsyzx.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/upload/{userId}")
    @ResponseBody
    public ResponseEntity uploadFile(@PathVariable("userId") Long userId, MultipartFile file) {
        return fileService.uploadSmallFile(userId, file);
    }

    @PostMapping("/initUploadBigFile")
    @ResponseBody
    public ResponseEntity initUploadBigFile(@RequestBody BigFileInfoDto bigFileInfoDto) {
        return fileService.initUploadBigFile(bigFileInfoDto);
    }

    @PostMapping("/uploadBigFilePart")
    @ResponseBody
    public ResponseEntity uploadBigFilePart(MultipartFile file, BigFilePartDto bigFilePartDto) {
        return fileService.uploadBigFilePart(bigFilePartDto, file);
    }

    @PostMapping("/mergeBigFile")
    @ResponseBody
    public ResponseEntity mergeBigFile(@RequestBody MergeFileDto mergeFileDto) {
        return fileService.mergeBigFile(mergeFileDto);
    }
}
