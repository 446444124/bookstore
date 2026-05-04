package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.context.BaseContext;
import com.PTU.dto.OrdersSubmitDTO;
import com.PTU.dto.SpecialOfferSubmitDTO;
import com.PTU.dto.SecondHandOrderSubmitDTO;
import com.PTU.entity.*;
import com.PTU.exception.AddressBookBusinessException;
import com.PTU.exception.BaseException;
import com.PTU.exception.ShoppingCartBusinessException;
import com.PTU.exception.StockNotEnoughException;
import com.PTU.mapper.*;
import com.PTU.result.PageResult;
import com.PTU.service.CartService;
import com.PTU.service.OrderService;
import com.PTU.service.SecondHandListingService;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

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
    @Autowired
    private WalletFlowMapper walletFlowMapper;
    @Autowired
    private SecondHandListingService secondHandListingService;
    @Autowired
    private SecondHandListingMapper secondHandListingMapper;
    @Autowired
    private SecondHandOrderMapper secondHandOrderMapper;
    @Autowired
    private SpecialOfferMapper specialOfferMapper;
    @Autowired
    private SpecialOfferItemMapper specialOfferItemMapper;

    private static final int SECOND_HAND_MERGE_CAP = 5000;

    //TODO
    private Orders orders;



    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        log.info("用户{}提交订单，请求参数: {}", userId, ordersSubmitDTO);
        if (ordersSubmitDTO.getPayWay() != 1 && ordersSubmitDTO.getPayWay() != 2) {
            throw new BaseException("不支持的支付方式");
        }
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
        applyDefaultImmediateEstimatedDelivery(
                orders.getDeliveryWay(),
                orders.getDeliveryStatus(),
                orders.getEstimatedDeliveryTime(),
                orders::setEstimatedDeliveryTime,
                now);
        orders.setOrderTime(now);
        orders.setCreateTime(now);
        orders.setUpdateTime(now);
        boolean walletPay = ordersSubmitDTO.getPayWay() == 2;
        if (walletPay) {
            int deducted = userMapper.deductWalletBalance(userId, totalAmount);
            if (deducted == 0) {
                throw new BaseException(MessageConstant.WALLET_BALANCE_NOT_ENOUGH);
            }
            orders.setPayStatus(Orders.PAID);
            orders.setStatus(Orders.TO_BE_CONFIRMED);
            orders.setPayTime(now);
        } else {
            orders.setPayStatus(Orders.UN_PAID);
            orders.setStatus(Orders.PENDING_PAYMENT);
        }
        orders.setId(generateOrderNo(userId));
        orders.setUserId(userId);
        orders.setTotalAmount(totalAmount);
        this.orders = orders;
        log.info("生成订单号{}，总金额{}", orders.getId(), orders.getTotalAmount());
        if (walletPay) {
            walletFlowMapper.insert(WalletFlow.builder()
                    .userId(userId)
                    .flowType(WalletFlow.TYPE_CONSUME)
                    .amount(totalAmount)
                    .bizNo(orders.getId())
                    .remark("钱包支付订单")
                    .createTime(now)
                    .build());
        }
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

    @Override
    @Transactional
    public OrderSubmitVO submitSecondHandOrder(SecondHandOrderSubmitDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        if (dto.getListingId() == null) {
            throw new BaseException("缺少二手书条目");
        }
        if (dto.getPayWay() != 1 && dto.getPayWay() != 2) {
            throw new BaseException("不支持的支付方式");
        }
        SecondHandOrder sh = new SecondHandOrder();
        boolean isDelivery = dto.getDeliveryWay() != null && dto.getDeliveryWay() == 1;
        if (isDelivery) {
            AddressBook addressBook = addressBookmapper.selectById(dto.getAddressBookId());
            if (addressBook == null) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }
            sh.setPhone(addressBook.getPhone());
            sh.setConsignee(addressBook.getConsignee());
            sh.setAddress(buildAddress(addressBook));
        }
        String orderId = generateOrderNo(userId);
        secondHandListingService.lockForOrder(dto.getListingId(), orderId, userId);
        SecondHandListing listing = secondHandListingMapper.selectById(dto.getListingId());
        if (listing == null || listing.getSalePrice() == null) {
            throw new BaseException("二手书价格信息异常，请重试");
        }
        Book book = bookMapper.selectById(listing.getBookId());
        if (book == null) {
            throw new BaseException(MessageConstant.BOOK_OFF_SALE_OR_DELETED);
        }
        BigDecimal totalAmount = listing.getSalePrice();
        BeanUtils.copyProperties(dto, sh);
        LocalDateTime now = LocalDateTime.now();
        applyDefaultImmediateEstimatedDelivery(
                sh.getDeliveryWay(),
                sh.getDeliveryStatus(),
                sh.getEstimatedDeliveryTime(),
                sh::setEstimatedDeliveryTime,
                now);
        sh.setOrderTime(now);
        sh.setCreateTime(now);
        sh.setUpdateTime(now);
        sh.setId(orderId);
        sh.setUserId(userId);
        sh.setListingId(dto.getListingId());
        sh.setSellerUserId(listing.getSellerUserId());
        sh.setTotalAmount(totalAmount);
        sh.setBookId(book.getId());
        sh.setBookTitle("[二手] " + book.getTitle());
        sh.setCoverImage(book.getCoverImage());
        boolean walletPay = dto.getPayWay() == 2;
        if (walletPay) {
            int deducted = userMapper.deductWalletBalance(userId, totalAmount);
            if (deducted == 0) {
                throw new BaseException(MessageConstant.WALLET_BALANCE_NOT_ENOUGH);
            }
            sh.setPayStatus(Orders.PAID);
            sh.setStatus(Orders.TO_BE_CONFIRMED);
            sh.setPayTime(now);
        } else {
            sh.setPayStatus(Orders.UN_PAID);
            sh.setStatus(Orders.PENDING_PAYMENT);
        }
        secondHandOrderMapper.insert(sh);
        if (walletPay) {
            walletFlowMapper.insert(WalletFlow.builder()
                    .userId(userId)
                    .flowType(WalletFlow.TYPE_CONSUME)
                    .amount(totalAmount)
                    .bizNo(sh.getId())
                    .remark("钱包支付二手书订单")
                    .createTime(now)
                    .build());
            secondHandListingService.finalizeSoldForWalletPaidOrder(sh);
        }
        return OrderSubmitVO.builder()
                .id(sh.getId())
                .orderNumber(sh.getId())
                .orderTime(sh.getOrderTime())
                .orderAmount(sh.getTotalAmount())
                .build();
    }

    @Override
    @Transactional
    public OrderSubmitVO submitSpecialOfferOrder(SpecialOfferSubmitDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) throw new BaseException(MessageConstant.LOGIN_FAILED);
        if (dto == null || dto.getOfferId() == null) throw new BaseException("缺少特惠活动");
        int count = dto.getCount() == null ? 1 : dto.getCount();
        if (count < 1) throw new BaseException("购买数量必须 >= 1");
        if (dto.getPayWay() != 1 && dto.getPayWay() != 2) throw new BaseException("不支持的支付方式");

        SpecialOffer offer = specialOfferMapper.selectById(dto.getOfferId());
        if (offer == null) throw new BaseException("特惠活动不存在");
        if (offer.getEnabled() == null || offer.getEnabled() != 1) throw new BaseException("特惠活动未启用");
        LocalDateTime now = LocalDateTime.now();
        if (offer.getStartTime() != null && offer.getStartTime().isAfter(now)) throw new BaseException("特惠活动未开始");
        if (offer.getEndTime() != null && offer.getEndTime().isBefore(now)) throw new BaseException("特惠活动已结束");

        boolean isDelivery = dto.getDeliveryWay() != null && dto.getDeliveryWay() == 1;
        Orders orders = new Orders();
        if (isDelivery) {
            AddressBook addressBook = addressBookmapper.selectById(dto.getAddressBookId());
            if (addressBook == null) throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            orders.setPhone(addressBook.getPhone());
            orders.setConsignee(addressBook.getConsignee());
            orders.setAddress(buildAddress(addressBook));
        }

        List<SpecialOfferItem> items = specialOfferItemMapper.selectList(
                new LambdaQueryWrapper<SpecialOfferItem>().eq(SpecialOfferItem::getOfferId, offer.getId())
        );
        if (items == null || items.isEmpty()) throw new BaseException("特惠活动未配置图书");

        List<OrderDetail> orderDetailList = new ArrayList<>();
        BigDecimal originalAmount = BigDecimal.ZERO;
        for (SpecialOfferItem it : items) {
            int baseQty = it.getQuantity() == null ? 1 : it.getQuantity();
            int qty = offer.getOfferType() != null && offer.getOfferType() == 1 ? count : baseQty * count;
            Book book = bookMapper.selectById(it.getBookId());
            if (book == null || book.getStatus() == null || book.getStatus() != 1) {
                throw new BaseException(MessageConstant.BOOK_OFF_SALE_OR_DELETED);
            }
            int affected = bookMapper.deductStock(book.getId(), qty);
            if (affected == 0) throw new StockNotEnoughException(MessageConstant.STOCK_NOT_ENOUGH);
            BigDecimal line = book.getPrice().multiply(BigDecimal.valueOf(qty));
            originalAmount = originalAmount.add(line);
            orderDetailList.add(OrderDetail.builder()
                    .title(book.getTitle())
                    .bookId(book.getId())
                    .quantity(qty)
                    .price(line)
                    .coverImage(book.getCoverImage())
                    .createTime(now)
                    .build());
        }
        BigDecimal dealAmount = applyOfferDiscount(originalAmount, offer.getDiscountType(), offer.getDiscountValue());
        BigDecimal discountAmount = originalAmount.subtract(dealAmount);
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) discountAmount = BigDecimal.ZERO;

        BeanUtils.copyProperties(dto, orders);
        applyDefaultImmediateEstimatedDelivery(
                orders.getDeliveryWay(),
                orders.getDeliveryStatus(),
                orders.getEstimatedDeliveryTime(),
                orders::setEstimatedDeliveryTime,
                now);
        orders.setId(generateOrderNo(userId));
        orders.setUserId(userId);
        orders.setOrderTime(now);
        orders.setCreateTime(now);
        orders.setUpdateTime(now);
        orders.setTotalAmount(dealAmount);
        orders.setSpecialOfferId(offer.getId());
        orders.setDiscountAmount(discountAmount);
        String remark = dto.getRemark() == null ? "" : dto.getRemark().trim();
        String prefix = "【特惠专区】" + (offer.getName() == null ? "" : offer.getName().trim());
        orders.setRemark(remark.isEmpty() ? prefix : (prefix + "；" + remark));

        boolean walletPay = dto.getPayWay() == 2;
        if (walletPay) {
            int deducted = userMapper.deductWalletBalance(userId, dealAmount);
            if (deducted == 0) throw new BaseException(MessageConstant.WALLET_BALANCE_NOT_ENOUGH);
            orders.setPayStatus(Orders.PAID);
            orders.setStatus(Orders.TO_BE_CONFIRMED);
            orders.setPayTime(now);
            walletFlowMapper.insert(WalletFlow.builder()
                    .userId(userId)
                    .flowType(WalletFlow.TYPE_CONSUME)
                    .amount(dealAmount)
                    .bizNo(orders.getId())
                    .remark("钱包支付特惠订单")
                    .createTime(now)
                    .build());
        } else {
            orders.setPayStatus(Orders.UN_PAID);
            orders.setStatus(Orders.PENDING_PAYMENT);
        }

        orderMapper.insert(orders);
        for (OrderDetail od : orderDetailList) {
            od.setOrderId(orders.getId());
        }
        orderDetailMapper.insertBatch(orderDetailList);
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getTotalAmount())
                .build();
    }

    /**
     * 配送且为「立即送出」（或未传 deliveryStatus，兼容旧客户端）时，若未填写预计送达时间，则默认为下单时间后 1 小时。
     * deliveryStatus=0（自选具体时间）且未传时间时不填充，由前端校验。
     */
    private void applyDefaultImmediateEstimatedDelivery(
            Integer deliveryWay,
            Integer deliveryStatus,
            LocalDateTime estimatedDeliveryTime,
            Consumer<LocalDateTime> setEstimated,
            LocalDateTime orderTime) {
        if (deliveryWay == null || deliveryWay != 1) {
            return;
        }
        if (estimatedDeliveryTime != null) {
            return;
        }
        if (deliveryStatus != null && deliveryStatus == 0) {
            return;
        }
        setEstimated.accept(orderTime.plusHours(1));
    }

    private BigDecimal applyOfferDiscount(BigDecimal original, Integer discountType, BigDecimal discountValue) {
        if (original == null) original = BigDecimal.ZERO;
        if (discountType == null || discountValue == null) return original;
        if (discountType == 1) {
            return original.multiply(discountValue).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        }
        if (discountType == 2) {
            return discountValue.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        if (discountType == 3) {
            BigDecimal v = original.subtract(discountValue);
            return v.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : v.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return original;
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
                .isNull(Orders::getSecondHandListingId)
                .orderByDesc(Orders::getOrderTime);
        if (status != null) {
            qw.eq(Orders::getStatus, status);
        }
        if (deliveryWay != null) {
            qw.eq(Orders::getDeliveryWay, deliveryWay);
        }
        try {
            this.page(p, qw);
        } catch (Exception e) {
            log.warn("orders 普通订单分页失败（请确认 orders 表含 second_hand_listing_id 列）: {}", e.toString());
            return new PageResult(0L, new ArrayList<>());
        }
        return new PageResult(p.getTotal(), p.getRecords());
    }

    @Override
    public PageResult pageSecondHandQuery4User(int page, int pageSize, Integer status, Integer deliveryWay) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(Orders::getUserId, userId)
                .isNotNull(Orders::getSecondHandListingId)
                .orderByDesc(Orders::getOrderTime)
                .last("LIMIT " + SECOND_HAND_MERGE_CAP);
        if (status != null) {
            qw.eq(Orders::getStatus, status);
        }
        if (deliveryWay != null) {
            qw.eq(Orders::getDeliveryWay, deliveryWay);
        }
        List<Orders> legacy;
        try {
            legacy = orderMapper.selectList(qw);
        } catch (Exception e) {
            log.warn("orders 二手历史合并查询失败（请确认 orders 表含 second_hand_listing_id）: {}", e.toString());
            legacy = Collections.emptyList();
        }

        LambdaQueryWrapper<SecondHandOrder> qw2 = new LambdaQueryWrapper<>();
        qw2.eq(SecondHandOrder::getUserId, userId)
                .orderByDesc(SecondHandOrder::getOrderTime)
                .last("LIMIT " + SECOND_HAND_MERGE_CAP);
        if (status != null) {
            qw2.eq(SecondHandOrder::getStatus, status);
        }
        if (deliveryWay != null) {
            qw2.eq(SecondHandOrder::getDeliveryWay, deliveryWay);
        }
        List<SecondHandOrder> shRows = safeListSecondHandOrders(qw2);

        List<OrderVO> merged = new ArrayList<>();
        for (Orders o : legacy) {
            merged.add(toOrderVoListRow(o));
        }
        for (SecondHandOrder s : shRows) {
            merged.add(toOrderVoListRow(s));
        }
        merged.sort((a, b) -> {
            LocalDateTime ta = a.getOrderTime();
            LocalDateTime tb = b.getOrderTime();
            if (tb == null) {
                return ta == null ? 0 : -1;
            }
            if (ta == null) {
                return 1;
            }
            return tb.compareTo(ta);
        });
        long total = merged.size();
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, merged.size());
        List<OrderVO> pageRecords = from >= merged.size() ? new ArrayList<>() : new ArrayList<>(merged.subList(from, to));
        return new PageResult(total, pageRecords);
    }

    private OrderVO toOrderVoListRow(Orders o) {
        return OrderVO.builder()
                .id(o.getId())
                .orderNumber(o.getId())
                .userId(o.getUserId())
                .totalAmount(o.getTotalAmount())
                .status(o.getStatus())
                .payStatus(o.getPayStatus())
                .orderTime(o.getOrderTime())
                .payTime(o.getPayTime())
                .deliveryWay(o.getDeliveryWay())
                .secondHandListingId(o.getSecondHandListingId())
                .build();
    }

    private OrderVO toOrderVoListRow(SecondHandOrder s) {
        return OrderVO.builder()
                .id(s.getId())
                .orderNumber(s.getId())
                .userId(s.getUserId())
                .totalAmount(s.getTotalAmount())
                .status(s.getStatus())
                .payStatus(s.getPayStatus())
                .orderTime(s.getOrderTime())
                .payTime(s.getPayTime())
                .deliveryWay(s.getDeliveryWay())
                .secondHandListingId(s.getListingId())
                .build();
    }

    @Override
    public Map<Integer, Long> statusCount4User() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        Map<Integer, Long> counts = new HashMap<>();
        for (int s = Orders.PENDING_PAYMENT; s <= Orders.REFUNDED; s++) {
            long cnt = safeCountOrdersByListingSegment(userId, s, false);
            counts.put(s, cnt);
        }
        return counts;
    }

    @Override
    public Map<Integer, Long> statusCountSecondHand4User() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        Map<Integer, Long> counts = new HashMap<>();
        for (int s = Orders.PENDING_PAYMENT; s <= Orders.REFUNDED; s++) {
            long c1 = safeCountOrdersByListingSegment(userId, s, true);
            long c2 = safeCountSecondHandOrders(userId, s);
            counts.put(s, c1 + c2);
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
        if (order != null) {
            if (!userId.equals(order.getUserId())) {
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
            return;
        }
        SecondHandOrder sh = safeGetSecondHandById(id);
        if (sh == null || !userId.equals(sh.getUserId())) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.COMPLETED.equals(sh.getStatus()) || !Orders.PAID.equals(sh.getPayStatus())) {
            throw new BaseException("当前订单状态不支持退货");
        }
        LocalDateTime baseTime = sh.getDeliveryTime() != null ? sh.getDeliveryTime() : sh.getOrderTime();
        if (baseTime == null || baseTime.isBefore(LocalDateTime.now().minusDays(7))) {
            throw new BaseException("仅支持近7天内订单申请退货");
        }
        String applyReason = (reason == null || reason.trim().isEmpty()) ? "用户申请退货" : ("用户申请退货：" + reason.trim());
        SecondHandOrder upd = SecondHandOrder.builder()
                .id(sh.getId())
                .status(Orders.RETURN_REQUESTED)
                .cancelReason(applyReason)
                .updateTime(LocalDateTime.now())
                .build();
        secondHandOrderMapper.updateById(upd);
    }

    public OrderVO details(String id) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        Orders order = this.getById(id);
        // 仅当 orders 表存在且属于当前用户时才走普通订单详情；否则继续查 second_hand_order（避免 id 与二手订单冲突或误读他人订单）
        if (order != null && userId.equals(order.getUserId())) {
            List<OrderDetail> items = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                    .eq(OrderDetail::getOrderId, id));
            List<OrderItemVO> itemVOs = new ArrayList<>();
            for (OrderDetail d : items) {
                itemVOs.add(OrderItemVO.builder()
                        .bookId(d.getBookId())
                        .title(d.getTitle())
                        .coverImage(d.getCoverImage())
                        .quantity(d.getQuantity())
                        .price(d.getPrice() != null ? d.getPrice() : BigDecimal.ZERO)
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
                    .secondHandListingId(order.getSecondHandListingId())
                    .items(itemVOs)
                    .build();
        }
        SecondHandOrder sh = safeGetSecondHandById(id);
        if (sh != null && userId.equals(sh.getUserId())) {
            return buildOrderDetailFromSecondHand(sh);
        }
        throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
    }

    private OrderVO buildOrderDetailFromSecondHand(SecondHandOrder sh) {
        OrderItemVO item = OrderItemVO.builder()
                .bookId(sh.getBookId())
                .title(sh.getBookTitle() != null ? sh.getBookTitle() : "二手书")
                .coverImage(sh.getCoverImage())
                .quantity(1)
                .price(sh.getTotalAmount())
                .build();
        String address = sh.getAddress();
        if ((address == null || address.trim().isEmpty()) && sh.getAddressBookId() != null) {
            AddressBook ab = addressBookMapper.selectById(sh.getAddressBookId());
            if (ab != null) {
                address = buildAddress(ab);
            }
        }
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
                .address(address)
                .username(sh.getUsername())
                .cancelReason(sh.getCancelReason())
                .cancelTime(sh.getCancelTime())
                .rejectionReason(sh.getRejectionReason())
                .secondHandListingId(sh.getListingId())
                .items(Collections.singletonList(item))
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

    /**
     * 按订单明细将库存加回（购物车/特惠等下单时已扣减的库存）。
     */
    private void restoreStockFromOrderDetails(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            return;
        }
        List<OrderDetail> lines = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));
        if (lines == null || lines.isEmpty()) {
            return;
        }
        for (OrderDetail d : lines) {
            if (d.getBookId() == null || d.getQuantity() == null || d.getQuantity() <= 0) {
                continue;
            }
            int n = bookMapper.addStock(d.getBookId(), d.getQuantity());
            if (n == 0) {
                log.warn("恢复库存未更新到行 bookId={} qty={} orderId={}", d.getBookId(), d.getQuantity(), orderId);
            }
        }
    }

    @Transactional
    public void userCancelById(String id) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        Orders order = this.getById(id);
        if (order != null) {
            if (!userId.equals(order.getUserId())) {
                throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
            }
            if (Orders.CANCELLED.equals(order.getStatus())) {
                throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
            }
            if (order.getPayStatus() != null && order.getPayStatus() == Orders.PAID) {
                throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
            }
            restoreStockFromOrderDetails(id);
            Orders upd = Orders.builder()
                    .id(order.getId())
                    .status(Orders.CANCELLED)
                    .cancelReason("用户取消")
                    .cancelTime(LocalDateTime.now().toString())
                    .updateTime(LocalDateTime.now())
                    .build();
            this.updateById(upd);
            secondHandListingService.releaseIfOrderCancelled(id);
            return;
        }
        SecondHandOrder sh = safeGetSecondHandById(id);
        if (sh == null || !userId.equals(sh.getUserId())) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (sh.getPayStatus() != null && sh.getPayStatus() == Orders.PAID) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        SecondHandOrder upd = SecondHandOrder.builder()
                .id(sh.getId())
                .status(Orders.CANCELLED)
                .cancelReason("用户取消")
                .cancelTime(LocalDateTime.now().toString())
                .updateTime(LocalDateTime.now())
                .build();
        secondHandOrderMapper.updateById(upd);
        secondHandListingService.releaseIfOrderCancelled(id);
    }

    public void repetition(String id) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MessageConstant.LOGIN_FAILED);
        }
        if (safeGetSecondHandById(id) != null) {
            throw new BaseException("二手书订单不支持再来一单");
        }
        Orders order = this.getById(id);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (order.getSecondHandListingId() != null) {
            throw new BaseException("二手书订单不支持再来一单");
        }
        List<OrderDetail> items = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, id));
        for (OrderDetail d : items) {
            cartService.add(d.getBookId(), d.getQuantity());
        }
    }

    /**
     * 按 second_hand_listing_id 是否为空统计 orders；缺列或 SQL 异常时返回 0，避免普通订单与二手 Tab 整页崩溃。
     *
     * @param legacySecondHandInOrders true：历史写在 orders 且 listing_id 非空；false：普通订单（listing_id 为空）
     */
    private long safeCountOrdersByListingSegment(Long userId, int status, boolean legacySecondHandInOrders) {
        try {
            LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<Orders>()
                    .eq(Orders::getUserId, userId)
                    .eq(Orders::getStatus, status);
            if (legacySecondHandInOrders) {
                qw.isNotNull(Orders::getSecondHandListingId);
            } else {
                qw.isNull(Orders::getSecondHandListingId);
            }
            return orderMapper.selectCount(qw);
        } catch (Exception e) {
            log.warn("orders 条件统计失败（请确认已执行 sql/second_hand_listing.sql 中的 ALTER）: {}", e.toString());
            return 0L;
        }
    }

    /** 未执行 second_hand_order 建表时避免整接口失败，降级为空列表 */
    private List<SecondHandOrder> safeListSecondHandOrders(LambdaQueryWrapper<SecondHandOrder> qw) {
        try {
            return secondHandOrderMapper.selectList(qw);
        } catch (Exception e) {
            log.warn("second_hand_order 列表查询失败（请确认已执行 sql/second_hand_order.sql）: {}", e.toString());
            return Collections.emptyList();
        }
    }

    private long safeCountSecondHandOrders(Long userId, int status) {
        try {
            return secondHandOrderMapper.selectCount(new LambdaQueryWrapper<SecondHandOrder>()
                    .eq(SecondHandOrder::getUserId, userId)
                    .eq(SecondHandOrder::getStatus, status));
        } catch (Exception e) {
            log.warn("second_hand_order 统计失败: {}", e.toString());
            return 0L;
        }
    }

    /** 表不存在或查询异常时返回 null，由上层按「无此单」处理，避免未知错误 */
    private SecondHandOrder safeGetSecondHandById(String id) {
        if (id == null) {
            return null;
        }
        try {
            return secondHandOrderMapper.selectById(id);
        } catch (Exception e) {
            log.warn("second_hand_order 按 id 查询失败 id={}: {}", id, e.toString());
            return null;
        }
    }
}
