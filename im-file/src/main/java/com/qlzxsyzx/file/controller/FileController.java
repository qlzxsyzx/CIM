package com.qlzxsyzx.file.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.file.dto.BigFileInfoDto;
import com.qlzxsyzx.file.dto.BigFilePartDto;
import com.qlzxsyzx.file.dto.MergeFileDto;
import com.qlzxsyzx.file.service.FileService;
import com.qlzxsyzx.file.vo.FileDetailsVo;
import com.qlzxsyzx.resource.annotation.AuthenticationDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity uploadFile(@AuthenticationDetails("userId") Long userId, MultipartFile file) {
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

    @GetMapping("/getFileDetails/{recordId}")
    @ResponseBody
    public FileDetailsVo getFileDetails(@PathVariable("recordId") Long recordId) {
        return fileService.getFileDetails(recordId);
    }

    @GetMapping("/downloadSmallFile/{recordId}")
    public void downloadSmallFile(@AuthenticationDetails("userId") Long userId, @PathVariable("recordId") Long recordId, HttpServletResponse response) {
        fileService.downloadSmallFile(userId, recordId, response);
    }

    @PostMapping("/initDownloadBigFile/{recordId}")
    @ResponseBody
    public ResponseEntity initDownloadBigFile(@AuthenticationDetails("userId") Long userId, @PathVariable("recordId") Long recordId) {
        return fileService.initDownloadBigFile(userId, recordId);
    }

    @GetMapping("/downloadBigFilePart/{recordId}/{partNum}/{partSize}")
    public void downloadBigFilePart(@AuthenticationDetails("userId") Long userId, @PathVariable("recordId") Long recordId, @PathVariable("partNum") Integer partNum, @PathVariable("partSize") Long partSize, HttpServletResponse response) {
        fileService.downloadBigFilePart(userId, recordId, partNum, partSize, response);
    }
}
