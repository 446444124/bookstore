package com.PTU.service.impl;

import com.PTU.mapper.StatisticsMapper;
import com.PTU.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Override
    public Map<String, Object> overview(Integer days) {
        int d = normalizeDays(days, 7);
        Map<String, Object> data = new HashMap<>();
        BigDecimal salesAmount = nz(statisticsMapper.sumSalesAmount(d));
        long totalOrders = nvl(statisticsMapper.countOrders(d));
        long paidOrders = nvl(statisticsMapper.countPaidOrders(d));
        long completedOrders = nvl(statisticsMapper.countCompletedOrders(d));
        BigDecimal paidRate = totalOrders == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(paidOrders * 100.0 / totalOrders).setScale(2, RoundingMode.HALF_UP);
        data.put("days", d);
        data.put("salesAmount", salesAmount);
        data.put("totalOrders", totalOrders);
        data.put("paidOrders", paidOrders);
        data.put("completedOrders", completedOrders);
        data.put("paidRate", paidRate);
        return data;
    }

    @Override
    public Map<String, Object> salesTrend(Integer days) {
        int d = normalizeDays(days, 7);
        List<Map<String, Object>> rows = statisticsMapper.salesTrend(d);
        Map<String, BigDecimal> byDate = new HashMap<>();
        for (Map<String, Object> row : rows) {
            byDate.put(String.valueOf(row.get("d")), toDecimal(row.get("amt")));
        }
        List<String> dates = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.now().minusDays(d - 1L);
        for (int i = 0; i < d; i++) {
            String ds = start.plusDays(i).format(fmt);
            dates.add(ds);
            amounts.add(byDate.getOrDefault(ds, BigDecimal.ZERO));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("days", d);
        data.put("dates", dates);
        data.put("amounts", amounts);
        return data;
    }

    @Override
    public Map<String, Object> categoryShare(Integer days) {
        int d = normalizeDays(days, 30);
        List<Map<String, Object>> rows = statisticsMapper.categorySalesShare(d);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> it = new HashMap<>();
            it.put("name", String.valueOf(row.get("name")));
            it.put("value", nvlObj(row.get("qty")));
            items.add(it);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("days", d);
        data.put("items", items);
        return data;
    }

    @Override
    public Map<String, Object> bookTop(Integer days, Integer limit) {
        int d = normalizeDays(days, 30);
        int lim = (limit == null || limit <= 0) ? 10 : Math.min(limit, 50);
        List<Map<String, Object>> rows = statisticsMapper.topSellingBooks(d, lim);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> it = new HashMap<>();
            it.put("bookId", row.get("bookId"));
            it.put("title", row.get("title"));
            it.put("quantity", nvlObj(row.get("qty")));
            it.put("amount", toDecimal(row.get("amount")));
            items.add(it);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("days", d);
        data.put("limit", lim);
        data.put("items", items);
        return data;
    }

    private int normalizeDays(Integer days, int def) {
        if (days == null || days <= 0) return def;
        return Math.min(days, 365);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private long nvlObj(Object v) {
        if (v == null) return 0L;
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return 0L;
        }
    }

    private BigDecimal toDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(String.valueOf(o));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
