package com.PTU.controller;

import com.PTU.entity.Orders;
import com.PTU.entity.SecondHandOrder;
import com.PTU.entity.WalletFlow;
import com.PTU.mapper.OrderMapper;
import com.PTU.mapper.SecondHandOrderMapper;
import com.PTU.mapper.UserMapper;
import com.PTU.mapper.WalletFlowMapper;
import com.PTU.service.SecondHandListingService;
import com.PTU.utils.PayUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/alipay")
@Slf4j
public class AliPayController {

    @Value("${store.pay.success-page-url:http://localhost:5173/#/paysuccess}")
    private String paySuccessPageUrl;

    @Value("${store.pay.wallet-recharge-success-page-url:http://localhost:5173/#/wallet-rechargesuccess}")
    private String walletRechargeSuccessPageUrl;

    @Autowired
    private PayUtil alipayUtil;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private SecondHandOrderMapper secondHandOrderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WalletFlowMapper walletFlowMapper;
    @Autowired
    private SecondHandListingService secondHandListingService;

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        try {
            java.util.HashMap<String, String> params = new java.util.HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }
            boolean signVerified = com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                    params,
                    alipayUtil.getAlipayPublicKey(),
                    alipayUtil.getCharset(),
                    alipayUtil.getSignType()
            );
            if (!signVerified) {
                return "failure";
            }
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");
            String totalAmountStr = params.get("total_amount");
            if (outTradeNo == null || outTradeNo.isEmpty()) {
                return "failure";
            }
            // 钱包充值回调：WALLET_userId_amountCent_timestamp
            if (outTradeNo.startsWith("WALLET_")) {
                String[] arr = outTradeNo.split("_");
                if (arr.length < 4) {
                    return "failure";
                }
                if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                    return "success";
                }
                Long userId;
                BigDecimal amount;
                try {
                    userId = Long.valueOf(arr[1]);
                    amount = new BigDecimal(arr[2]).divide(new BigDecimal("100"));
                    BigDecimal paid = new BigDecimal(totalAmountStr);
                    if (paid.compareTo(amount) != 0) {
                        return "failure";
                    }
                } catch (Exception e) {
                    return "failure";
                }
                if (walletFlowMapper.countByBizNoAndType(outTradeNo, WalletFlow.TYPE_RECHARGE) > 0) {
                    return "success";
                }
                userMapper.addWalletBalance(userId, amount);
                walletFlowMapper.insert(WalletFlow.builder()
                        .userId(userId)
                        .flowType(WalletFlow.TYPE_RECHARGE)
                        .amount(amount)
                        .bizNo(outTradeNo)
                        .remark("支付宝钱包充值")
                        .createTime(LocalDateTime.now())
                        .build());
                return "success";
            }
            Orders order = orderMapper.selectById(outTradeNo);
            if (order != null) {
                if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                    return "success";
                }
                try {
                    BigDecimal paid = new BigDecimal(totalAmountStr);
                    if (order.getTotalAmount() != null && order.getTotalAmount().compareTo(paid) != 0) {
                        return "failure";
                    }
                } catch (Exception e) {
                    return "failure";
                }
                if (Orders.isPaid(order.getPayStatus())) {
                    return "success";
                }
                Orders upd = Orders.builder()
                        .id(order.getId())
                        .payStatus(Orders.PAID)
                        .status(Orders.TO_BE_CONFIRMED)
                        .payTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                orderMapper.updateById(upd);
                secondHandListingService.finalizeSoldAfterPaid(outTradeNo, order.getUserId());
                return "success";
            }
            SecondHandOrder shOrder = safeGetSecondHandOrder(outTradeNo);
            if (shOrder == null) {
                return "failure";
            }
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                return "success";
            }
            try {
                BigDecimal paid = new BigDecimal(totalAmountStr);
                if (shOrder.getTotalAmount() != null && shOrder.getTotalAmount().compareTo(paid) != 0) {
                    return "failure";
                }
            } catch (Exception e) {
                return "failure";
            }
            if (Orders.isPaid(shOrder.getPayStatus())) {
                return "success";
            }
            SecondHandOrder shUpd = SecondHandOrder.builder()
                    .id(shOrder.getId())
                    .payStatus(Orders.PAID)
                    .status(Orders.TO_BE_CONFIRMED)
                    .payTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            secondHandOrderMapper.updateById(shUpd);
            secondHandListingService.finalizeSoldAfterPaid(outTradeNo, shOrder.getUserId());
            return "success";
        } catch (Exception e) {
            return "failure";
        }
    }

    @GetMapping("/pay")
    @ResponseBody
    public String pay(@RequestParam(value = "id", required = false) String id,
                      @RequestParam(value = "orderId", required = false) String orderId,
                      @RequestParam(value = "no", required = false) String no,
                      @RequestParam(value = "orderNumber", required = false) String orderNumber) throws AlipayApiException {
        String useId = id;
        if (useId == null || useId.isEmpty()) useId = orderId;
        if (useId == null || useId.isEmpty()) useId = no;
        if (useId == null || useId.isEmpty()) useId = orderNumber;
        if (useId == null || useId.isEmpty()) {
            log.warn("支付下单缺少订单号参数");
            return "<html><body><h3>缺少订单号参数</h3><p>请从下单页面重新发起支付。</p></body></html>";
        }
        Orders order = orderMapper.selectById(useId);
        if (order != null) {
            if (Orders.isPaid(order.getPayStatus())) {
                log.warn("支付下单失败，订单已支付 id={}", useId);
                return "<html><body><h3>订单已支付</h3></body></html>";
            }
            if (order.getStatus() != null && order.getStatus() != Orders.PENDING_PAYMENT) {
                log.warn("支付下单失败，订单状态非待付款 id={}, status={}", useId, order.getStatus());
                return "<html><body><h3>订单状态异常</h3></body></html>";
            }
            if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return "<html><body><h3>订单金额非法</h3></body></html>";
            }
            return alipayUtil.sendRequestToAlipay(order.getId(), order.getTotalAmount(), "Your books");
        }
        SecondHandOrder sh = safeGetSecondHandOrder(useId);
        if (sh == null) {
            log.warn("支付下单失败，订单不存在 id={}", useId);
            return "<html><body><h3>订单不存在</h3></body></html>";
        }
        if (Orders.isPaid(sh.getPayStatus())) {
            log.warn("支付下单失败，订单已支付 id={}", useId);
            return "<html><body><h3>订单已支付</h3></body></html>";
        }
        if (sh.getStatus() != null && sh.getStatus() != Orders.PENDING_PAYMENT) {
            log.warn("支付下单失败，订单状态非待付款 id={}, status={}", useId, sh.getStatus());
            return "<html><body><h3>订单状态异常</h3></body></html>";
        }
        if (sh.getTotalAmount() == null || sh.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "<html><body><h3>订单金额非法</h3></body></html>";
        }
        return alipayUtil.sendRequestToAlipay(sh.getId(), sh.getTotalAmount(), "Your books");
    }

    //    当我们支付完成之后跳转这个请求并携带参数，我们将里面的订单id接收到，通过订单id查询订单信息，信息包括支付是否成功等
    @GetMapping("/toSuccess")
    public ResponseEntity<Void> returns(@RequestParam(value = "out_trade_no", required = false) String out_trade_no) {
        try {
            if (out_trade_no != null && !out_trade_no.isEmpty() && out_trade_no.startsWith("WALLET_")) {
                HttpHeaders wHeaders = new HttpHeaders();
                wHeaders.setLocation(URI.create(walletRechargeSuccessPageUrl));
                return new ResponseEntity<>(wHeaders, HttpStatus.FOUND);
            }
            if (out_trade_no != null && !out_trade_no.isEmpty()) {
                String query = alipayUtil.query(out_trade_no);
                if (query != null && !query.trim().isEmpty()) {
                    JSONObject jsonObject = JSONObject.parseObject(query);
                    Object o = jsonObject.get("alipay_trade_query_response");
                    if (o instanceof Map) {
                        Map map = (Map) o;
                        Object s = map.get("trade_status");
                        if ("TRADE_SUCCESS".equals(s) || "TRADE_FINISHED".equals(s)) {
                            Orders order = orderMapper.selectById(out_trade_no);
                            if (order != null && !Orders.isPaid(order.getPayStatus())) {
                                Orders upd = Orders.builder()
                                        .id(order.getId())
                                        .payStatus(Orders.PAID)
                                        .status(Orders.TO_BE_CONFIRMED)
                                        .payTime(LocalDateTime.now())
                                        .updateTime(LocalDateTime.now())
                                        .build();
                                orderMapper.updateById(upd);
                                secondHandListingService.finalizeSoldAfterPaid(out_trade_no, order.getUserId());
                            }
                            SecondHandOrder sh = safeGetSecondHandOrder(out_trade_no);
                            if (sh != null && !Orders.isPaid(sh.getPayStatus())) {
                                SecondHandOrder shUpd = SecondHandOrder.builder()
                                        .id(sh.getId())
                                        .payStatus(Orders.PAID)
                                        .status(Orders.TO_BE_CONFIRMED)
                                        .payTime(LocalDateTime.now())
                                        .updateTime(LocalDateTime.now())
                                        .build();
                                secondHandOrderMapper.updateById(shUpd);
                                secondHandListingService.finalizeSoldAfterPaid(out_trade_no, sh.getUserId());
                            }
                        }
                    }
                }
            }
        } catch (Exception ignore) {}
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(paySuccessPageUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private SecondHandOrder safeGetSecondHandOrder(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        try {
            return secondHandOrderMapper.selectById(id);
        } catch (Exception e) {
            log.warn("second_hand_order 查询失败 id={}: {}", id, e.toString());
            return null;
        }
    }

}
