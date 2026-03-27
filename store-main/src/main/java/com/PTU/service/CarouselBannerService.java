package com.PTU.service;

import com.PTU.vo.CarouselBannerVO;

import java.util.List;

public interface CarouselBannerService {

    /**
     * 用户端：获取启用的轮播图（按 sort 升序）
     */
    List<CarouselBannerVO> listEnabled();
}

