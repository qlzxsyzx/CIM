package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.web.entity.GroupNotice;
import com.qlzxsyzx.web.mapper.GroupNoticeMapper;
import com.qlzxsyzx.web.service.GroupNoticeService;
import com.qlzxsyzx.web.vo.GroupNoticeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class GroupNoticeServiceImpl extends ServiceImpl<GroupNoticeMapper, GroupNotice> implements GroupNoticeService {

    @Override
    public GroupNoticeVo getLatestNotice(Long groupId) {
        GroupNotice notice = query().eq("group_id", groupId).eq("is_delete", 0)
                .orderByDesc("update_time").last("limit 1").one();
        if (notice == null) {
            return null;
        }
        return convertToVo(notice);
    }


    private GroupNoticeVo convertToVo(GroupNotice notice) {
        GroupNoticeVo groupNoticeVo = new GroupNoticeVo();
        BeanUtils.copyProperties(notice, groupNoticeVo);
        return groupNoticeVo;
    }
}
