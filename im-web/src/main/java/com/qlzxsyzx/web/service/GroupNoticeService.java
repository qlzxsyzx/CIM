package com.qlzxsyzx.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qlzxsyzx.web.entity.GroupNotice;
import com.qlzxsyzx.web.vo.GroupNoticeVo;

import java.util.List;

public interface GroupNoticeService extends IService<GroupNotice> {
    GroupNoticeVo getLatestNotice(Long groupId);

    List<GroupNoticeVo> getNoticesByGroupId(Long groupId, Integer pageNum, Integer pageSize);

    GroupNoticeVo convertToVo(GroupNotice groupNotice);

    GroupNotice getNoticeById(Long noticeId);
}
