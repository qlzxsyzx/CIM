package com.qlzxsyzx.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.file.entity.UploadSegment;

import java.util.List;

public interface UploadSegmentService extends IService<UploadSegment> {
    List<UploadSegment> findListByRecordId(Long recordId);
}
