package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.context.BaseContext;
import com.PTU.dto.OrdersSubmitDTO;
import com.PTU.entity.*;
import com.PTU.exception.AddressBookBusinessException;
import com.PTU.exception.BaseException;
import com.PTU.exception.ShoppingCartBusinessException;
import com.PTU.exception.StockNotEnoughException;
import com.PTU.mapper.*;
import com.PTU.result.PageResult;
import com.PTU.service.CartService;
import com.PTU.service.OrderService;
import com.PTU.vo.OrderItemVO;
import com.PTU.vo.OrderSubmitVO;
import com.PTU.vo.OrderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookmapper;
    @Autowired
    private CartMapper shoppingCartmapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private CartService cartService;
    //TODO
    private Orders orders;



    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        log.info("用户{}提交订单，请求参数: {}", userId, ordersSubmitDTO);
        Orders orders = new Orders();
        boolean isDelivery = ordersSubmitDTO.getDeliveryWay() == 1;
        if(isDelivery){
            AddressBook addressBook = addressBookmapper.selectById(ordersSubmitDTO.getAddressBookId());
            if (addressBook == null) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }
            orders.setPhone(addressBook.getPhone());
            orders.setConsignee(addressBook.getConsignee());
            orders.setAddress(buildAddress(addressBook));
        }
        List<Cart> shoppingCartList = shoppingCartmapper.selectList(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
        );
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        List<OrderDetail> orderDetailList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : shoppingCartList) {
            Book book = bookMapper.selectById(cart.getBookId());
            if (book == null || book.getStatus() == null || book.getStatus() != 1) {
                log.warn("图书{}已下架或不存在", cart.getBookId());
                throw new BaseException(MessageConstant.BOOK_OFF_SALE_OR_DELETED);
            }
            int affected = bookMapper.deductStock(cart.getBookId(), cart.getQuantity());
            if (affected == 0) {
                log.warn("图书{}库存不足，请求数量{}", cart.getBookId(), cart.getQuantity());
                throw new StockNotEnoughException(MessageConstant.STOCK_NOT_ENOUGH);
            }
            BigDecimal lineAmount = book.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(lineAmount);
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(null);
            orderDetail.setPrice(lineAmount);
            orderDetailList.add(orderDetail);
        }

        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        LocalDateTime now = LocalDateTime.now();
        orders.setOrderTime(now);
        orders.setCreateTime(now);
        orders.setUpdateTime(now);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setId(generateOrderNo(userId));
        orders.setUserId(userId);
        orders.setTotalAmount(totalAmount);
        this.orders = orders;
        log.info("生成订单号{}，总金额{}", orders.getId(), orders.getTotalAmount());
        orderMapper.insert(orders);
        for (OrderDetail od : orderDetailList) {
            od.setOrderId(orders.getId());
        }
        orderDetailMapper.insertBatch(orderDetailList);
        shoppingCartmapper.deleteByUserId(userId);
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getTotalAmount())
                .build();
        return orderSubmitVO;
    }

    private String generateOrderNo(Long userId) {
        long ts = System.currentTimeMillis();
        int rnd = ThreadLocalRandom.current().nextInt(1000, 10000);
        return ts + String.valueOf(userId == null ? 0 : userId) + rnd;
    }

    public PageResult pageQuery4User(int page, int pageSize, Integer status, Integer deliveryWay) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        Page<Orders> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getOrderTime);
        if (status != null) {
            qw.eq(Orders::getStatus, status);
        }
        if (deliveryWay != null) {
            qw.eq(Orders::getDeliveryWay, deliveryWay);
        }
        this.page(p, qw);
        return new PageResult(p.getTotal(), p.getRecords());
    }

    @Override
    public Map<Integer, Long> statusCount4User() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        Map<Integer, Long> counts = new HashMap<>();
        for (int s = Orders.PENDING_PAYMENT; s <= Orders.RETURN_REQUESTED; s++) {
            long cnt = this.count(new LambdaQueryWrapper<Orders>()
                    .eq(Orders::getUserId, userId)
                    .eq(Orders::getStatus, s));
            counts.put(s, cnt);
        }
        return counts;
    }

    @Override
    @Transactional
    public void applyReturn(String id, String reason) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        Orders order = this.getById(id);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.COMPLETED.equals(order.getStatus()) || !Orders.PAID.equals(order.getPayStatus())) {
            throw new BaseException("当前订单状态不支持退货");
        }
        LocalDateTime baseTime = order.getDeliveryTime() != null ? order.getDeliveryTime() : order.getOrderTime();
        if (baseTime == null || baseTime.isBefore(LocalDateTime.now().minusDays(7))) {
            throw new BaseException("仅支持近7天内订单申请退货");
        }
        String applyReason = (reason == null || reason.trim().isEmpty()) ? "用户申请退货" : ("用户申请退货：" + reason.trim());
        Orders upd = Orders.builder()
                .id(order.getId())
                .status(Orders.RETURN_REQUESTED)
                .cancelReason(applyReason)
                .updateTime(LocalDateTime.now())
                .build();
        this.updateById(upd);
    }

    public OrderVO details(String id) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        Orders order = this.getById(id);
        if (order == null || !userId.equals(order.getUserId())) {
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
        String address = order.getAddress();
        if ((address == null || address.trim().isEmpty()) && order.getAddressBookId() != null) {
            AddressBook ab = addressBookMapper.selectById(order.getAddressBookId());
            if (ab != null) {
                address = buildAddress(ab);
            }
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
                .address(address)
                .username(order.getUsername())
                .cancelReason(order.getCancelReason())
                .cancelTime(order.getCancelTime())
                .rejectionReason(order.getRejectionReason())
                .items(itemVOs)
                .build();
    }

    private String buildAddress(AddressBook addressBook) {
        if (addressBook == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        appendPart(sb, addressBook.getProvinceName());
        appendPart(sb, addressBook.getCityName());
        appendPart(sb, addressBook.getDistrictName());
        appendPart(sb, addressBook.getSchoolPartition());
        appendPart(sb, addressBook.getBuilding());
        appendPart(sb, addressBook.getHouseNumber());
        String addr = sb.toString().trim();
        return addr.isEmpty() ? null : addr;
    }

    private void appendPart(StringBuilder sb, String part) {
        if (part == null || part.trim().isEmpty()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(part.trim());
    }

    public void userCancelById(String id) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        Orders order = this.getById(id);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (order.getPayStatus() != null && order.getPayStatus() == Orders.PAID) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders upd = Orders.builder()
                .id(order.getId())
                .status(Orders.CANCELLED)
                .cancelReason("用户取消")
                .cancelTime(LocalDateTime.now().toString())
                .updateTime(LocalDateTime.now())
                .build();
        this.updateById(upd);
    }

    public void repetition(String id) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        Orders order = this.getById(id);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        List<OrderDetail> items = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, id));
        for (OrderDetail d : items) {
            cartService.add(d.getBookId(), d.getQuantity());
        }
    }
}
