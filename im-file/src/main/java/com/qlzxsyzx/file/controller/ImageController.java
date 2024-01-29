package com.qlzxsyzx.file.controller;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.file.service.ImageService;
import com.qlzxsyzx.resource.annotation.AuthenticationDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/image")
@Slf4j
public class ImageController {
    @Autowired
    private ImageService imageService;

    @GetMapping("/getByName/{name}")
    public void getByName(HttpServletResponse response, @PathVariable("name") String name) throws IOException {
        imageService.getImageByName(response, name);
    }

    @GetMapping("/getById/{recordId}")
    public void getById(HttpServletResponse response, @PathVariable("recordId") String recordId) throws IOException {
        imageService.getImageByRecordId(response, recordId);
    }

    @PostMapping("/uploadImage")
    @ResponseBody
    public ResponseEntity upload(@AuthenticationDetails("userId") Long userId, MultipartFile file) {
        return imageService.uploadImage(userId, file);
    }
}
