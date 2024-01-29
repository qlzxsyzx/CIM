package com.qlzxsyzx.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qlzxsyzx.web.entity.*;
import com.qlzxsyzx.web.feign.IdGeneratorClient;
import com.qlzxsyzx.web.feign.UserFeignClient;
import com.qlzxsyzx.web.mapper.RecentChatMapper;
import com.qlzxsyzx.web.service.*;
import com.qlzxsyzx.web.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RecentChatServiceImpl extends ServiceImpl<RecentChatMapper, RecentChat> implements RecentChatService {

    @Override
    public RecentChatVo convertToVo(RecentChat recentChat) {
        RecentChatVo recentChatVo = new RecentChatVo();
        BeanUtils.copyProperties(recentChat, recentChatVo);
        return recentChatVo;
    }
}
