package com.PTU.service;

import com.PTU.vo.SpecialOfferVO;

import java.util.List;

public interface SpecialOfferService {

    /**
     * 用户端：获取当前有效且启用的特惠活动
     */
    List<SpecialOfferVO> listActive();
}

