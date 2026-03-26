package com.PTU.controller;

import com.PTU.result.PageResult;
import com.PTU.result.Result;
import com.PTU.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user/secondHandOrder")
@Api(tags = "C端二手书订单")
@Slf4j
public class SecondHandOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/page")
    @ApiOperation("二手书订单分页（含历史写入 orders 的记录）")
    public Result<PageResult> page(@RequestParam int page,
                                   @RequestParam int pageSize,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) Integer deliveryWay) {
        return Result.success(orderService.pageSecondHandQuery4User(page, pageSize, status, deliveryWay));
    }

    @GetMapping("/statusCount")
    @ApiOperation("二手书订单各状态数量")
    public Result<Map<Integer, Long>> statusCount() {
        return Result.success(orderService.statusCountSecondHand4User());
    }
}
