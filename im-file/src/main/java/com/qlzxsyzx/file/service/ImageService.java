package com.qlzxsyzx.file.service;

import com.qlzxsyzx.common.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ImageService {
    void getImageByName(HttpServletResponse response, String name) throws IOException;

    void getImageByRecordId(HttpServletResponse response, String recordId) throws IOException;

    ResponseEntity uploadImage(Long userId, MultipartFile file);
}
