package com.PTU.service;

import com.PTU.dto.SystemNoticeSaveDTO;
import com.PTU.vo.SystemNoticeVO;

public interface SystemNoticeService {

    SystemNoticeVO getConfig();

    void save(SystemNoticeSaveDTO dto);
}

