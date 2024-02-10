package com.qlzxsyzx.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.web.entity.GroupNotice;
import com.qlzxsyzx.web.vo.GroupNoticeVo;

public interface GroupNoticeService extends IService<GroupNotice> {
    GroupNoticeVo getLatestNotice(Long groupId);
}
