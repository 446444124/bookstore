package com.PTU.service;

import com.PTU.dto.OrdersSubmitDTO;
import com.PTU.entity.Orders;
import com.PTU.vo.OrderSubmitVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderService extends IService<Orders> {
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
}
