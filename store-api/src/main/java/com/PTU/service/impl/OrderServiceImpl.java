package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.entity.OrderDetail;
import com.PTU.entity.Orders;
import com.PTU.entity.SecondHandOrder;
import com.PTU.entity.WalletFlow;
import com.PTU.exception.BaseException;
import com.PTU.mapper.AdminOrderUnionMapper;
import com.PTU.mapper.OrderDetailMapper;
import com.PTU.mapper.OrderMapper;
import com.PTU.mapper.SecondHandOrderMapper;
import com.PTU.mapper.UserMapper;
import com.PTU.mapper.WalletFlowMapper;
import com.PTU.result.PageResult;
import com.PTU.service.OrderService;
import com.PTU.vo.OrderItemVO;
import com.PTU.vo.OrderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private SecondHandOrderMapper secondHandOrderMapper;
    @Autowired
    private AdminOrderUnionMapper adminOrderUnionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WalletFlowMapper walletFlowMapper;

    @Override
    public PageResult pageQuery(int page, int pageSize, Integer status, Integer deliveryWay, String orderNumber, String phone) {
        long offset = (long) (page - 1) * pageSize;
        long total = adminOrderUnionMapper.countUnion(status, deliveryWay, orderNumber, phone);
        List<?> records = adminOrderUnionMapper.selectUnionPage(status, deliveryWay, orderNumber, phone, offset, pageSize);
        return new PageResult(total, records);
    }

    @Override
    public OrderVO detail(String id) {
        Orders order = orderMapper.selectById(id);
        if (order != null) {
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
                    .secondHandListingId(order.getSecondHandListingId())
                    .items(itemVOs)
                    .build();
        }
        SecondHandOrder sh = safeGetSecondHandById(id);
        if (sh != null) {
            return buildDetailFromSecondHand(sh);
        }
        throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
    }

    private OrderVO buildDetailFromSecondHand(SecondHandOrder sh) {
        OrderItemVO item = OrderItemVO.builder()
                .bookId(sh.getBookId())
                .title(sh.getBookTitle())
                .coverImage(sh.getCoverImage())
                .quantity(1)
                .price(sh.getTotalAmount())
                .build();
        return OrderVO.builder()
                .id(sh.getId())
                .orderNumber(sh.getId())
                .userId(sh.getUserId())
                .totalAmount(sh.getTotalAmount())
                .status(sh.getStatus())
                .payStatus(sh.getPayStatus())
                .orderTime(sh.getOrderTime())
                .payTime(sh.getPayTime())
                .addressBookId(sh.getAddressBookId())
                .payWay(sh.getPayWay())
                .remark(sh.getRemark())
                .deliveryStatus(sh.getDeliveryStatus())
                .deliveryWay(sh.getDeliveryWay())
                .estimatedDeliveryTime(sh.getEstimatedDeliveryTime())
                .deliveryTime(sh.getDeliveryTime())
                .consignee(sh.getConsignee())
                .phone(sh.getPhone())
                .address(sh.getAddress())
                .username(sh.getUsername())
                .cancelReason(sh.getCancelReason())
                .cancelTime(sh.getCancelTime())
                .rejectionReason(sh.getRejectionReason())
                .secondHandListingId(sh.getListingId())
                .items(Collections.singletonList(item))
                .build();
    }

    @Override
    @Transactional
    public void confirm(String id) {
        Orders order = orderMapper.selectById(id);
        if (order != null) {
            if (!Orders.TO_BE_CONFIRMED.equals(order.getStatus())) {
                throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
            }
            Orders upd = Orders.builder()
                    .id(id)
                    .status(Orders.CONFIRMED)
                    .updateTime(LocalDateTime.now())
                    .build();
            orderMapper.updateById(upd);
            return;
        }
        SecondHandOrder sh = safeGetSecondHandById(id);
        if (sh == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.TO_BE_CONFIRMED.equals(sh.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        SecondHandOrder upd = SecondHandOrder.builder()
                .id(id)
                .status(Orders.CONFIRMED)
                .updateTime(LocalDateTime.now())
                .build();
        secondHandOrderMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void reject(String id, String reason) {
        Orders order = orderMapper.selectById(id);
        if (order != null) {
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
            return;
        }
        SecondHandOrder sh = safeGetSecondHandById(id);
        if (sh == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.TO_BE_CONFIRMED.equals(sh.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        String rejectReason = (reason == null || reason.trim().isEmpty()) ? "商家拒单" : reason.trim();
        SecondHandOrder upd = SecondHandOrder.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .rejectionReason(rejectReason)
                .cancelTime(LocalDateTime.now().toString())
                .updateTime(LocalDateTime.now())
                .build();
        secondHandOrderMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void delivery(String id) {
        Orders order = orderMapper.selectById(id);
        if (order != null) {
            if (!Orders.CONFIRMED.equals(order.getStatus())) {
                throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
            }
            Orders upd = Orders.builder()
                    .id(id)
                    .status(Orders.DELIVERY_IN_PROGRESS)
                    .updateTime(LocalDateTime.now())
                    .build();
            orderMapper.updateById(upd);
            return;
        }
        SecondHandOrder sh = safeGetSecondHandById(id);
        if (sh == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.CONFIRMED.equals(sh.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        SecondHandOrder upd = SecondHandOrder.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .updateTime(LocalDateTime.now())
                .build();
        secondHandOrderMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void complete(String id) {
        Orders order = orderMapper.selectById(id);
        if (order != null) {
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
            return;
        }
        SecondHandOrder sh = safeGetSecondHandById(id);
        if (sh == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.DELIVERY_IN_PROGRESS.equals(sh.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        SecondHandOrder upd = SecondHandOrder.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        secondHandOrderMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void approveReturn(String id) {
        Orders order = orderMapper.selectById(id);
        if (order != null) {
            if (!Orders.RETURN_REQUESTED.equals(order.getStatus())) {
                throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
            }
            if (order.getUserId() == null || order.getTotalAmount() == null || order.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new BaseException("订单退款信息异常");
            }
            if (walletFlowMapper.countByBizNoAndType(order.getId(), WalletFlow.TYPE_REFUND) > 0) {
                throw new BaseException("该订单已退款入账");
            }
            userMapper.addWalletBalance(order.getUserId(), order.getTotalAmount());
            walletFlowMapper.insert(WalletFlow.builder()
                    .userId(order.getUserId())
                    .flowType(WalletFlow.TYPE_REFUND)
                    .amount(order.getTotalAmount())
                    .bizNo(order.getId())
                    .remark("退货退款入钱包")
                    .createTime(LocalDateTime.now())
                    .build());
            Orders upd = Orders.builder()
                    .id(id)
                    .status(Orders.REFUNDED)
                    .payStatus(Orders.REFUND)
                    .cancelTime(LocalDateTime.now().toString())
                    .updateTime(LocalDateTime.now())
                    .build();
            orderMapper.updateById(upd);
            return;
        }
        SecondHandOrder sh = safeGetSecondHandById(id);
        if (sh == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.RETURN_REQUESTED.equals(sh.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (sh.getUserId() == null || sh.getTotalAmount() == null || sh.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BaseException("订单退款信息异常");
        }
        if (walletFlowMapper.countByBizNoAndType(sh.getId(), WalletFlow.TYPE_REFUND) > 0) {
            throw new BaseException("该订单已退款入账");
        }
        userMapper.addWalletBalance(sh.getUserId(), sh.getTotalAmount());
        walletFlowMapper.insert(WalletFlow.builder()
                .userId(sh.getUserId())
                .flowType(WalletFlow.TYPE_REFUND)
                .amount(sh.getTotalAmount())
                .bizNo(sh.getId())
                .remark("退货退款入钱包")
                .createTime(LocalDateTime.now())
                .build());
        SecondHandOrder upd = SecondHandOrder.builder()
                .id(id)
                .status(Orders.REFUNDED)
                .payStatus(Orders.REFUND)
                .cancelTime(LocalDateTime.now().toString())
                .updateTime(LocalDateTime.now())
                .build();
        secondHandOrderMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void rejectReturn(String id, String reason) {
        Orders order = orderMapper.selectById(id);
        if (order != null) {
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
            return;
        }
        SecondHandOrder sh = safeGetSecondHandById(id);
        if (sh == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.RETURN_REQUESTED.equals(sh.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        String rejectReason = (reason == null || reason.trim().isEmpty()) ? "商家驳回退货申请" : ("商家驳回退货：" + reason.trim());
        SecondHandOrder upd = SecondHandOrder.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .rejectionReason(rejectReason)
                .updateTime(LocalDateTime.now())
                .build();
        secondHandOrderMapper.updateById(upd);
    }

    @Override
    public Map<Integer, Long> statusCount() {
        Map<Integer, Long> m = new HashMap<>();
        m.put(Orders.TO_BE_CONFIRMED, countStatusBoth(Orders.TO_BE_CONFIRMED));
        m.put(Orders.CONFIRMED, countStatusBoth(Orders.CONFIRMED));
        m.put(Orders.DELIVERY_IN_PROGRESS, countStatusBoth(Orders.DELIVERY_IN_PROGRESS));
        m.put(Orders.RETURN_REQUESTED, countStatusBoth(Orders.RETURN_REQUESTED));
        return m;
    }

    private long countStatusBoth(int status) {
        long c1 = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, status));
        long c2 = safeCountSecondHandByStatus(status);
        return c1 + c2;
    }

    private SecondHandOrder safeGetSecondHandById(String id) {
        if (id == null) {
            return null;
        }
        try {
            return secondHandOrderMapper.selectById(id);
        } catch (Exception e) {
            log.warn("second_hand_order 查询失败 id={}: {}", id, e.toString());
            return null;
        }
    }

    private long safeCountSecondHandByStatus(int status) {
        try {
            return secondHandOrderMapper.selectCount(new LambdaQueryWrapper<SecondHandOrder>()
                    .eq(SecondHandOrder::getStatus, status));
        } catch (Exception e) {
            log.warn("second_hand_order 统计失败: {}", e.toString());
            return 0L;
        }
    }
}
