package com.PTU.service.impl;

import com.PTU.context.BaseContext;
import com.PTU.dto.SystemNoticeSaveDTO;
import com.PTU.entity.SystemNotice;
import com.PTU.exception.BaseException;
import com.PTU.mapper.SystemNoticeMapper;
import com.PTU.service.SystemNoticeService;
import com.PTU.vo.SystemNoticeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SystemNoticeServiceImpl implements SystemNoticeService {

    private static final int CONFIG_ID = 1;

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;

    @Override
    public SystemNoticeVO getConfig() {
        SystemNotice row = ensureRow();
        SystemNoticeVO vo = new SystemNoticeVO();
        BeanUtils.copyProperties(row, vo);
        return vo;
    }

    @Override
    @Transactional
    public void save(SystemNoticeSaveDTO dto) {
        if (dto == null) throw new BaseException("参数错误");
        String title = dto.getTitle() == null ? null : dto.getTitle().trim();
        String content = dto.getContent() == null ? null : dto.getContent().trim();
        Integer enabled = dto.getEnabled();
        if (enabled == null) enabled = 0;
        if (!(enabled == 0 || enabled == 1)) throw new BaseException("enabled 只能为 0 或 1");
        if (title != null && title.length() > 128) throw new BaseException("标题长度不能超过 128");

        ensureRow();
        systemNoticeMapper.updateById(SystemNotice.builder()
                .id(CONFIG_ID)
                .title(title)
                .content(content)
                .enabled(enabled)
                .updateTime(LocalDateTime.now())
                .updateBy(BaseContext.getCurrentId())
                .build());
    }

    private SystemNotice ensureRow() {
        SystemNotice cfg = systemNoticeMapper.selectById(CONFIG_ID);
        if (cfg != null) return cfg;
        SystemNotice init = SystemNotice.builder()
                .id(CONFIG_ID)
                .title("系统公告")
                .content("")
                .enabled(0)
                .updateTime(LocalDateTime.now())
                .updateBy(BaseContext.getCurrentId())
                .build();
        try {
            systemNoticeMapper.insert(init);
        } catch (Exception ignore) {
        }
        SystemNotice again = systemNoticeMapper.selectById(CONFIG_ID);
        return again != null ? again : init;
    }
}

