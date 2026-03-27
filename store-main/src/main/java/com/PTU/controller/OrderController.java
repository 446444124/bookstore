package com.PTU.controller;

import com.PTU.dto.OrdersSubmitDTO;
import com.PTU.dto.SpecialOfferSubmitDTO;
import com.PTU.dto.SecondHandOrderSubmitDTO;
import com.PTU.result.Result;
import com.PTU.service.OrderService;
import com.PTU.result.PageResult;
import com.PTU.vo.OrderVO;
import com.PTU.vo.OrderSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "C端订单接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;


    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
       log.info("订单数据：{}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO =orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @PostMapping("/submitSecondHand")
    @ApiOperation("二手书下单（单件）")
    public Result<OrderSubmitVO> submitSecondHand(@RequestBody SecondHandOrderSubmitDTO dto) {
        log.info("二手书订单：{}", dto);
        return Result.success(orderService.submitSecondHandOrder(dto));
    }

    @PostMapping("/submitSpecialOffer")
    @ApiOperation("特惠专区下单（专区内下单方可享受优惠）")
    public Result<OrderSubmitVO> submitSpecialOffer(@RequestBody SpecialOfferSubmitDTO dto) {
        log.info("特惠订单：{}", dto);
        return Result.success(orderService.submitSpecialOfferOrder(dto));
    }
    @GetMapping("/page")
    @ApiOperation("个人订单分页查询")
    public Result<PageResult> page(@RequestParam int page,
                                   @RequestParam int pageSize,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) Integer deliveryWay) {
        PageResult pageResult = orderService.pageQuery4User(page, pageSize, status, deliveryWay);
        return Result.success(pageResult);
    }

    @GetMapping("/statusCount")
    @ApiOperation("个人订单状态数量统计")
    public Result<Map<Integer, Long>> statusCount() {
        return Result.success(orderService.statusCount4User());
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable("id") String id) {
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable("id") String id) {
        orderService.userCancelById(id);
        return Result.success();
    }

    @PostMapping("/return/{id}")
    @ApiOperation("申请退货（7天内）")
    public Result applyReturn(@PathVariable("id") String id,
                              @RequestParam(required = false) String reason) {
        orderService.applyReturn(id, reason);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable String id) {
        orderService.repetition(id);
        return Result.success();
    }


}
