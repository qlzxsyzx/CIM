package com.qlzxsyzx.file.service.impl;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.common.type.ImageExtEnum;
import com.qlzxsyzx.common.utils.IdUtil;
import com.qlzxsyzx.file.config.MinioProperties;
import com.qlzxsyzx.file.entity.UploadRecord;
import com.qlzxsyzx.file.feign.IdGeneratorClient;
import com.qlzxsyzx.file.service.ImageService;
import com.qlzxsyzx.file.service.MinioService;
import com.qlzxsyzx.file.service.UploadRecordService;
import com.qlzxsyzx.file.vo.FileVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

@Slf4j
@Service
public class ImageServiceImpl implements ImageService {
    public static final Integer IMAGE_MAX_SIZE = 1024 * 1024 * 2; //2M

    @Autowired
    private MinioProperties minioProperties;

    @Autowired
    private MinioService minioService;

    @Autowired
    private UploadRecordService uploadRecordService;

    @Autowired
    private IdGeneratorClient idGeneratorClient;

    @Override
    public void getImageByName(HttpServletResponse response, String name) throws IOException {
        String extension = name.substring(name.lastIndexOf(".") + 1);
        if (!ImageExtEnum.isImageExt(extension)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("图片格式不支持");
            return;
        }
        response.setContentType("image/jpeg");
        response.setHeader("Content-Disposition", "inline; filename=" + name);
        try (InputStream inputStream = minioService.downloadForStream(minioProperties.getPrivateBucketName(), minioProperties.getImagePath() + "/" + name);
             ServletOutputStream outputStream = response.getOutputStream()) {
            // 读取文件并写入响应
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getImageByRecordId(HttpServletResponse response, String recordId) throws IOException {
        UploadRecord imageRecord = uploadRecordService.getRecordById(recordId);
        if (imageRecord == null || imageRecord.getUploadStatus() != 2) {
            throw new RuntimeException("图片不存在");
        }
        getImageByName(response, imageRecord.getFileName());
    }

    @Override
    public ResponseEntity uploadImage(Long userId, MultipartFile file) {
        Assert.notNull(file, "文件不能为空");
        // 获取文件信息
        String originalFilename = file.getOriginalFilename();
        long size = file.getSize();
        if (size > IMAGE_MAX_SIZE) {
            return ResponseEntity.fail( "图片大小超过2M");
        }
        // 获取文件扩展名
        Assert.notNull(originalFilename, "文件名不能为空");
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if (!ImageExtEnum.isImageExt(extension)) {
            return ResponseEntity.fail( "图片格式不支持");
        }
        // 生成文件名
        String fileName = IdUtil.getUUID() + "." + extension;
        String objectName = minioProperties.getImagePath() + "/" + fileName;
        // 上传文件
        minioService.uploadFile(file, minioProperties.getPrivateBucketName(), objectName);
        // 保存文件信息到数据库
        UploadRecord uploadRecord;
        try {
            uploadRecord = new UploadRecord();
            uploadRecord.setRecordId(idGeneratorClient.generate());
            uploadRecord.setUserId(userId);
            uploadRecord.setFileName(fileName);
            uploadRecord.setRealName(originalFilename);
            uploadRecord.setExt(extension);
            uploadRecord.setFileSize(size);
            uploadRecord.setUploadTime(LocalDateTime.now());
            uploadRecord.setBucketName(minioProperties.getPrivateBucketName());
            uploadRecord.setObjectName(objectName);
            uploadRecord.setUploadStatus(2);
            uploadRecord.setIsDelete(0);
            uploadRecordService.save(uploadRecord);
        } catch (Exception e) {
            log.error("保存文件信息到数据库失败", e);
            minioService.remove(minioProperties.getPrivateBucketName(), objectName);
            return ResponseEntity.fail( "上传图片失败");
        }
        FileVo fileVo = new FileVo();
        fileVo.setRecordId(uploadRecord.getRecordId());
        fileVo.setFileName(fileName);
        return ResponseEntity.success(fileVo);
    }
}
