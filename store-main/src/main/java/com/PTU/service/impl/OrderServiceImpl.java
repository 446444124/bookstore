package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.context.BaseContext;
import com.PTU.dto.OrdersSubmitDTO;
import com.PTU.entity.AddressBook;
import com.PTU.entity.Cart;
import com.PTU.entity.OrderDetail;
import com.PTU.entity.Orders;
import com.PTU.exception.AddressBookBusinessException;
import com.PTU.exception.ShoppingCartBusinessException;
import com.PTU.mapper.*;
import com.PTU.service.OrderService;
import com.PTU.vo.OrderSubmitVO;
import com.alipay.api.domain.KbPosOrderDishDetail;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookmapper;
    @Autowired
    private CartMapper shoppingCartmapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    //TODO
    private Orders orders;



    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理各种异常情况（如库存、支付方式、地址等）
        Orders orders = new Orders();
        boolean isDelivery = ordersSubmitDTO.getDeliveryWay() == 1;
        //1:配送
        if(isDelivery){
            AddressBook addressBook = addressBookmapper.selectById(ordersSubmitDTO.getAddressBookId());
            if (addressBook == null) {
                // 如果地址为空，抛出异常
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }
            orders.setPhone(addressBook.getPhone());
            orders.setConsignee(addressBook.getConsignee());
        }
        //查询购物车数据
        Long userId = BaseContext.getCurrentId();
        List<Cart> ShoppingCartlist = shoppingCartmapper.selectList(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
        );
        if (ShoppingCartlist == null || ShoppingCartlist.size() == 0) {
            // 如果购物车为空，抛出异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //TODO查询库存,判断库存是否充足(乐观锁)
        for (Cart cart : ShoppingCartlist) {

        }
        // 向订单表插入数据
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        // 订单号：时间戳 + 随机数
        orders.setId((String.valueOf(System.currentTimeMillis())));
        orders.setUserId(userId);
        //TODO 给全局变量赋值
        this.orders = orders;
        orderMapper.insert(orders);
        // 向订单明细表插入数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (Cart cart : ShoppingCartlist) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetail.setPrice(cart.getAmount());
            orderDetailList.add(orderDetail);

        }
        orderDetailMapper.insertBatch(orderDetailList);
        //清理购物车数据
        shoppingCartmapper.deleteByUserId(userId);
        // 返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getTotalAmount())
                .build();
        return orderSubmitVO;
    }
}
