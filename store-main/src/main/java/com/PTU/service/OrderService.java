package com.PTU.service;

import com.PTU.dto.OrdersSubmitDTO;
import com.PTU.entity.Orders;
import com.PTU.result.PageResult;
import com.PTU.vo.OrderVO;
import com.PTU.vo.OrderSubmitVO;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

public interface OrderService extends IService<Orders> {
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
    PageResult pageQuery4User(int page, int pageSize, Integer status, Integer deliveryWay);
    Map<Integer, Long> statusCount4User();
    OrderVO details(String id);
    void userCancelById(String id);
    void applyReturn(String id, String reason);
    void repetition(String id);
}
