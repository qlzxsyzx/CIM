package com.qlzxsyzx.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.file.entity.UploadRecord;
import com.qlzxsyzx.file.mapper.UploadRecordMapper;
import com.qlzxsyzx.file.service.UploadRecordService;
import org.springframework.stereotype.Service;

@Service
public class UploadRecordServiceImpl extends ServiceImpl<UploadRecordMapper, UploadRecord> implements UploadRecordService {
    @Override
    public UploadRecord getRecordById(String recordId) {
        return query().eq("record_id", recordId).one();
    }
}
