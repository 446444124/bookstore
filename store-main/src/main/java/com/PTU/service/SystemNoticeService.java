package com.PTU.service;

import com.PTU.vo.SystemNoticeVO;

public interface SystemNoticeService {

    /**
     * 用户端获取当前启用的公告；若未启用则返回 null
     */
    SystemNoticeVO getActive();
}

