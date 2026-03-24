package com.PTU.controller;

import com.PTU.result.PageResult;
import com.PTU.result.Result;
import com.PTU.service.OrderService;
import com.PTU.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/order")
@Api(tags = "B端订单接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/page")
    @ApiOperation("商家端订单分页查询")
    public Result<PageResult> page(@RequestParam int page,
                                   @RequestParam int pageSize,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) Integer deliveryWay,
                                   @RequestParam(required = false) String orderNumber,
                                   @RequestParam(required = false) String phone) {
        PageResult pageResult = orderService.pageQuery(page, pageSize, status, deliveryWay, orderNumber, phone);
        return Result.success(pageResult);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("商家端查询订单详情")
    public Result<OrderVO> detail(@PathVariable String id) {
        return Result.success(orderService.detail(id));
    }

    @PostMapping("/confirm/{id}")
    @ApiOperation("商家接单")
    public Result<String> confirm(@PathVariable String id) {
        orderService.confirm(id);
        return Result.success();
    }

    @PostMapping("/reject/{id}")
    @ApiOperation("商家拒单")
    public Result<String> reject(@PathVariable String id,
                                 @RequestParam(required = false) String reason) {
        orderService.reject(id, reason);
        return Result.success();
    }

    @PostMapping("/delivery/{id}")
    @ApiOperation("商家发货/派送")
    public Result<String> delivery(@PathVariable String id) {
        orderService.delivery(id);
        return Result.success();
    }

    @PostMapping("/complete/{id}")
    @ApiOperation("商家完成订单")
    public Result<String> complete(@PathVariable String id) {
        orderService.complete(id);
        return Result.success();
    }

    @PostMapping("/return/approve/{id}")
    @ApiOperation("审批通过退货")
    public Result<String> approveReturn(@PathVariable String id) {
        orderService.approveReturn(id);
        return Result.success();
    }

    @PostMapping("/return/reject/{id}")
    @ApiOperation("驳回退货申请")
    public Result<String> rejectReturn(@PathVariable String id,
                                       @RequestParam(required = false) String reason) {
        orderService.rejectReturn(id, reason);
        return Result.success();
    }
}
