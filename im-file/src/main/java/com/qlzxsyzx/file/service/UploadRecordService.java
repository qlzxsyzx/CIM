package com.qlzxsyzx.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.file.entity.UploadRecord;

public interface UploadRecordService extends IService<UploadRecord> {
    UploadRecord getRecordById(String recordId);
}
