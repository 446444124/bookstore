package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.entity.OrderDetail;
import com.PTU.entity.Orders;
import com.PTU.exception.BaseException;
import com.PTU.mapper.OrderDetailMapper;
import com.PTU.mapper.OrderMapper;
import com.PTU.result.PageResult;
import com.PTU.service.OrderService;
import com.PTU.vo.OrderItemVO;
import com.PTU.vo.OrderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    public PageResult pageQuery(int page, int pageSize, Integer status, Integer deliveryWay, String orderNumber, String phone) {
        Page<Orders> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(Orders::getOrderTime);
        if (status != null) {
            qw.eq(Orders::getStatus, status);
        }
        if (deliveryWay != null) {
            qw.eq(Orders::getDeliveryWay, deliveryWay);
        }
        if (orderNumber != null && !orderNumber.trim().isEmpty()) {
            qw.like(Orders::getId, orderNumber.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            qw.like(Orders::getPhone, phone.trim());
        }
        orderMapper.selectPage(p, qw);
        return new PageResult(p.getTotal(), p.getRecords());
    }

    @Override
    public OrderVO detail(String id) {
        Orders order = orderMapper.selectById(id);
        if (order == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        List<OrderDetail> items = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, id));
        List<OrderItemVO> itemVOs = new ArrayList<>();
        for (OrderDetail d : items) {
            itemVOs.add(OrderItemVO.builder()
                    .bookId(d.getBookId())
                    .title(d.getTitle())
                    .coverImage(d.getCoverImage())
                    .quantity(d.getQuantity())
                    .price(d.getPrice())
                    .build());
        }
        return OrderVO.builder()
                .id(order.getId())
                .orderNumber(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .payStatus(order.getPayStatus())
                .orderTime(order.getOrderTime())
                .payTime(order.getPayTime())
                .addressBookId(order.getAddressBookId())
                .payWay(order.getPayWay())
                .remark(order.getRemark())
                .deliveryStatus(order.getDeliveryStatus())
                .deliveryWay(order.getDeliveryWay())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .deliveryTime(order.getDeliveryTime())
                .consignee(order.getConsignee())
                .phone(order.getPhone())
                .address(order.getAddress())
                .username(order.getUsername())
                .cancelReason(order.getCancelReason())
                .cancelTime(order.getCancelTime())
                .rejectionReason(order.getRejectionReason())
                .items(itemVOs)
                .build();
    }

    @Override
    @Transactional
    public void confirm(String id) {
        Orders order = checkExists(id);
        if (!Orders.TO_BE_CONFIRMED.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders upd = Orders.builder()
                .id(id)
                .status(Orders.CONFIRMED)
                .updateTime(LocalDateTime.now())
                .build();
        orderMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void reject(String id, String reason) {
        Orders order = checkExists(id);
        if (!Orders.TO_BE_CONFIRMED.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        String rejectReason = (reason == null || reason.trim().isEmpty()) ? "商家拒单" : reason.trim();
        Orders upd = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .rejectionReason(rejectReason)
                .cancelTime(LocalDateTime.now().toString())
                .updateTime(LocalDateTime.now())
                .build();
        orderMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void delivery(String id) {
        Orders order = checkExists(id);
        if (!Orders.CONFIRMED.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders upd = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .updateTime(LocalDateTime.now())
                .build();
        orderMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void complete(String id) {
        Orders order = checkExists(id);
        if (!Orders.DELIVERY_IN_PROGRESS.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders upd = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        orderMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void approveReturn(String id) {
        Orders order = checkExists(id);
        if (!Orders.RETURN_REQUESTED.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders upd = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .payStatus(Orders.REFUND)
                .cancelTime(LocalDateTime.now().toString())
                .updateTime(LocalDateTime.now())
                .build();
        orderMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void rejectReturn(String id, String reason) {
        Orders order = checkExists(id);
        if (!Orders.RETURN_REQUESTED.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        String rejectReason = (reason == null || reason.trim().isEmpty()) ? "商家驳回退货申请" : ("商家驳回退货：" + reason.trim());
        Orders upd = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .rejectionReason(rejectReason)
                .updateTime(LocalDateTime.now())
                .build();
        orderMapper.updateById(upd);
    }

    private Orders checkExists(String id) {
        Orders order = orderMapper.selectById(id);
        if (order == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        return order;
    }
}
