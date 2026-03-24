package com.PTU.service;

import java.util.Map;

public interface StatisticsService {
    Map<String, Object> overview(Integer days);

    Map<String, Object> salesTrend(Integer days);

    Map<String, Object> categoryShare(Integer days);

    Map<String, Object> bookTop(Integer days, Integer limit);
}
