package com.PTU.controller;

import com.PTU.result.Result;
import com.PTU.service.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/statistics")
@Api(tags = "B端数据统计接口")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    @ApiOperation("销售概览")
    public Result<Map<String, Object>> overview(@RequestParam(required = false) Integer days) {
        return Result.success(statisticsService.overview(days));
    }

    @GetMapping("/salesTrend")
    @ApiOperation("销售额趋势")
    public Result<Map<String, Object>> salesTrend(@RequestParam(required = false) Integer days) {
        return Result.success(statisticsService.salesTrend(days));
    }

    @GetMapping("/categoryShare")
    @ApiOperation("分类销量占比")
    public Result<Map<String, Object>> categoryShare(@RequestParam(required = false) Integer days) {
        return Result.success(statisticsService.categoryShare(days));
    }

    @GetMapping("/bookTop")
    @ApiOperation("图书销量排行")
    public Result<Map<String, Object>> bookTop(@RequestParam(required = false) Integer days,
                                               @RequestParam(required = false) Integer limit) {
        return Result.success(statisticsService.bookTop(days, limit));
    }
}
