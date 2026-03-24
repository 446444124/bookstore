package com.PTU.service;

import com.PTU.vo.HomeRecommendVO;

public interface HomeRecommendService {
    HomeRecommendVO recommendForHome(Long userId, int categoryLimit, int bookLimit);
}
