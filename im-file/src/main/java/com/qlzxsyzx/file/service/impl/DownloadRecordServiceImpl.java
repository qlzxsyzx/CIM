package com.qlzxsyzx.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.file.entity.DownloadRecord;
import com.qlzxsyzx.file.mapper.DownloadRecordMapper;
import com.qlzxsyzx.file.service.DownloadRecordService;
import org.springframework.stereotype.Service;

@Service
public class DownloadRecordServiceImpl extends ServiceImpl<DownloadRecordMapper, DownloadRecord> implements DownloadRecordService {
}
