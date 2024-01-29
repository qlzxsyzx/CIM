package com.qlzxsyzx.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.file.entity.UploadSegment;
import com.qlzxsyzx.file.mapper.UploadSegmentMapper;
import com.qlzxsyzx.file.service.UploadSegmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UploadSegmentServiceImpl extends ServiceImpl<UploadSegmentMapper, UploadSegment> implements UploadSegmentService {
    @Override
    public List<UploadSegment> findListByRecordId(Long recordId) {
        return query().eq("record_id", recordId).list();
    }
}
