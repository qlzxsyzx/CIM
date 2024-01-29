package com.qlzxsyzx.file.service.impl;

import com.qlzxsyzx.common.ResponseEntity;
import com.qlzxsyzx.common.type.ImageExtEnum;
import com.qlzxsyzx.common.utils.IdUtil;
import com.qlzxsyzx.file.config.MinioProperties;
import com.qlzxsyzx.file.dto.BigFileInfoDto;
import com.qlzxsyzx.file.dto.BigFilePartDto;
import com.qlzxsyzx.file.dto.MergeFileDto;
import com.qlzxsyzx.file.entity.UploadRecord;
import com.qlzxsyzx.file.entity.UploadSegment;
import com.qlzxsyzx.file.feign.IdGeneratorClient;
import com.qlzxsyzx.file.service.*;
import com.qlzxsyzx.file.vo.FileVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.qlzxsyzx.file.service.impl.MinioServiceImpl.*;

@Slf4j
@Service
@Transactional
public class FileServiceImpl implements FileService {
    @Autowired
    private MinioService minioService;

    @Autowired
    private MinioProperties minioProperties;

    @Autowired
    private UploadRecordService uploadRecordService;

    @Autowired
    private DownloadRecordService downloadRecordService;

    @Autowired
    private IdGeneratorClient idGeneratorClient;

    @Autowired
    private UploadSegmentService uploadSegmentService;

    @Override
    public ResponseEntity uploadSmallFile(Long userId, MultipartFile file) {
        Assert.notNull(file, "文件不能为空");
        // 获取文件信息
        String originalFilename = file.getOriginalFilename();
        long size = file.getSize();
        if (size > SMALL_FILE_MAX_SIZE) {
            return ResponseEntity.fail("文件大小超过2M");
        }
        // 获取文件扩展名
        Assert.notNull(originalFilename, "文件名不能为空");
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if (ImageExtEnum.isImageExt(extension)) {
            return ResponseEntity.fail("不允许上传图片文件");
        }
        // 生成文件名
        String fileName = IdUtil.getUUID()
                + "." + extension;
        String objectName = minioProperties.getFilePath() + "/" + fileName;
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
            return ResponseEntity.fail("上传文件失败");
        }
        FileVo fileVo = new FileVo();
        fileVo.setRecordId(uploadRecord.getRecordId());
        fileVo.setFileName(fileName);
        return ResponseEntity.success(fileVo);
    }

    @Override
    public ResponseEntity initUploadBigFile(BigFileInfoDto bigFileInfoDto) {
        if (bigFileInfoDto.getFileSize() <= SMALL_FILE_MAX_SIZE) {
            return ResponseEntity.fail("文件大小不能小于2M");
        }
        if (bigFileInfoDto.getFileSize() > MAX_FILE_SIZE) {
            return ResponseEntity.fail("文件大小不能大于100M");
        }
        if (bigFileInfoDto.getSegmentSize() < MIN_SEGMENT_SIZE && bigFileInfoDto.getSegmentSize() > MAX_SEGMENT_SIZE) {
            return ResponseEntity.fail("分片大小必须在2M到5M之间");
        }
        // 记录大文件信息，返回recordId
        Long recordId = idGeneratorClient.generate();
        String realName = bigFileInfoDto.getFileName();
        String extension = realName
                .substring(realName.lastIndexOf(".") + 1);
        String fileName = IdUtil.getUUID() + "." + extension;
        String objectName = minioProperties.getFilePath() + "/" + fileName;
        UploadRecord uploadRecord = new UploadRecord();
        uploadRecord.setRecordId(recordId);
        uploadRecord.setUserId(bigFileInfoDto.getUserId());
        uploadRecord.setFileName(fileName);
        uploadRecord.setRealName(realName);
        uploadRecord.setExt(extension);
        uploadRecord.setFileSize(bigFileInfoDto.getFileSize());
        uploadRecord.setUploadTime(LocalDateTime.now());
        uploadRecord.setBucketName(minioProperties.getPrivateBucketName());
        uploadRecord.setObjectName(objectName);
        uploadRecord.setUploadStatus(1);
        uploadRecord.setIsDelete(0);
        uploadRecordService.save(uploadRecord);
        // 记录分片信息
        List<UploadSegment> segments = new ArrayList<>();
        Long[] segmentIds = idGeneratorClient.generateIdBatch(bigFileInfoDto.getSegmentCount());
        for (int i = 0; i < segmentIds.length; i++) {
            UploadSegment segment = new UploadSegment();
            segment.setSegmentId(segmentIds[i]);
            segment.setRecordId(recordId);
            segment.setSegmentNumber(i + 1);
            segment.setUploadStatus(0);
            segments.add(segment);
        }
        uploadSegmentService.saveBatch(segments);
        FileVo fileVo = new FileVo();
        fileVo.setRecordId(recordId);
        fileVo.setFileName(fileName);
        fileVo.setSegmentIds(segmentIds);
        return ResponseEntity.success(fileVo);
    }

    @Override
    public ResponseEntity uploadBigFilePart(BigFilePartDto bigFilePartDto, MultipartFile file) {
        Assert.notNull(file, "文件不能为空");
        Long recordId = bigFilePartDto.getRecordId();
        Long segmentId = bigFilePartDto.getSegmentId();
        String fileName = bigFilePartDto.getFileName();
        String fileNameNoExt = fileName.substring(0, fileName.lastIndexOf("."));
        String ext = fileName.substring(fileName.lastIndexOf("."));
        // 检查分片信息
        UploadSegment segment = uploadSegmentService.getById(segmentId);
        if (segment == null) {
            return ResponseEntity.error("分片信息不存在");
        }
        if (!segment.getRecordId().equals(recordId)) {
            return ResponseEntity.error("分片信息不匹配");
        }
        // 检查文件分片是否已上传
        if (segment.getUploadStatus() == 1) {
            return ResponseEntity.success(null);
        }
        // 保存分片文件
        String partName = fileNameNoExt + "_" + segment.getSegmentNumber() + ext;
        String objectName = minioProperties.getFilePath() + "/" + partName;
        minioService.uploadFile(file, minioProperties.getPrivateBucketName(), objectName);
        // 更新分片信息
        try {
            segment.setUploadStatus(1);
            uploadSegmentService.updateById(segment);
        } catch (Exception e) {
            log.error("更新分片信息到数据库失败", e);
            minioService.remove(minioProperties.getPrivateBucketName(), objectName);
            return ResponseEntity.error("上传分片失败");
        }
        return ResponseEntity.success(null);
    }

    @Override
    public ResponseEntity mergeBigFile(MergeFileDto mergeFileDto) {
        Long recordId = mergeFileDto.getRecordId();
        // 查询文件信息
        UploadRecord record = uploadRecordService.getById(recordId);
        // 如果上传记录不存在或者记录不是上传中状态1，就失败
        if (record == null || record.getUploadStatus() != 1) {
            return ResponseEntity.error("上传文件信息错误");
        }
        // 查询分片信息
        List<UploadSegment> segments = uploadSegmentService.findListByRecordId(recordId);
        // 检查分片上传状态
        boolean notMerge = segments.stream().anyMatch(segment -> segment.getUploadStatus() != 1);
        if (notMerge) {
            return ResponseEntity.error("分片没有上传完全");
        }
        String fileNameNoExt = record.getFileName()
                .substring(0, record.getFileName().lastIndexOf("."));
        // 合并文件
        List<String> parts = segments.stream().map(segment -> minioProperties.getFilePath() + "/" + fileNameNoExt + "_" + segment.getSegmentNumber() + "." + record.getExt()).collect(Collectors.toList());
        minioService.mergeFile(record.getBucketName(), record.getObjectName(), parts);
        try {
            // 更新上传记录状态
            record.setUploadStatus(2);
            uploadRecordService.updateById(record);
        } catch (Exception e) {
            log.error("更新上传记录状态失败", e);
            minioService.remove(record.getBucketName(), record.getObjectName());
            return ResponseEntity.fail("上传失败");
        }
        return ResponseEntity.success(null);
    }
}
