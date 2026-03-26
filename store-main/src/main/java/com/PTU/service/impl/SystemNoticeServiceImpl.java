package com.PTU.service.impl;

import com.PTU.entity.SystemNotice;
import com.PTU.mapper.SystemNoticeMapper;
import com.PTU.service.SystemNoticeService;
import com.PTU.vo.SystemNoticeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemNoticeServiceImpl implements SystemNoticeService {

    private static final int CONFIG_ID = 1;

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Override
    public SystemNoticeVO getActive() {
        SystemNotice row = systemNoticeMapper.selectById(CONFIG_ID);
        if (row == null) return null;
        if (row.getEnabled() == null || row.getEnabled() != 1) return null;
        SystemNoticeVO vo = new SystemNoticeVO();
        BeanUtils.copyProperties(row, vo);
        return vo;
    }
}

