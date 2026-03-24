package com.PTU.service;

import com.PTU.result.PageResult;
import com.PTU.vo.OrderVO;
import java.util.Map;

public interface OrderService {
    PageResult pageQuery(int page, int pageSize, Integer status, Integer deliveryWay, String orderNumber, String phone);

    OrderVO detail(String id);

    void confirm(String id);

    void reject(String id, String reason);

    void delivery(String id);

    void complete(String id);

    void approveReturn(String id);

    void rejectReturn(String id, String reason);

    Map<Integer, Long> statusCount();
}
