package com.PTU.service;

import com.PTU.dto.OrdersSubmitDTO;
import com.PTU.dto.SecondHandOrderSubmitDTO;
import com.PTU.entity.Orders;
import com.PTU.result.PageResult;
import com.PTU.vo.OrderVO;
import com.PTU.vo.OrderSubmitVO;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

public interface OrderService extends IService<Orders> {
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    OrderSubmitVO submitSecondHandOrder(SecondHandOrderSubmitDTO dto);
    PageResult pageQuery4User(int page, int pageSize, Integer status, Integer deliveryWay);

    /** 二手书订单分页（含历史 orders 中 second_hand_listing_id 非空的记录 + second_hand_order） */
    PageResult pageSecondHandQuery4User(int page, int pageSize, Integer status, Integer deliveryWay);

    Map<Integer, Long> statusCount4User();

    Map<Integer, Long> statusCountSecondHand4User();
    OrderVO details(String id);
    void userCancelById(String id);
    void applyReturn(String id, String reason);
    void repetition(String id);
}
