package com.PTU.controller;


import com.PTU.entity.Orders;
import com.PTU.utils.AlipayUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.easysdk.factory.Factory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
/**
 * 支付宝接口
 */
@RestController
@RequestMapping("/api/alipay")
public class AliPayController {

    @Resource
    AlipayUtil alipayUtil;

    @GetMapping(value = "/pay")
    @ResponseBody
    public String pay(@RequestParam long  id) throws AlipayApiException {
        Orders order = new Orders();
        order.setId("120948748372234L");
        order.setUserId(129904058947L);
        order.setTotalAmount(BigDecimal.valueOf(10.00));
        return alipayUtil.pay(order);
    }

    @PostMapping("/notify")  // 注意这里必须是POST接口
    public String payNotify(HttpServletRequest request) throws Exception {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            System.out.println("=========支付宝异步回调========");
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
                // System.out.println(name + " = " + request.getParameter(name));
            }
            String tradeNo = params.get("out_trade_no");
            String gmtPayment = params.get("gmt_payment");
            String alipayTradeNo = params.get("trade_no");
            // 支付宝验签
            if (Factory.Payment.Common().verifyNotify(params)) {
                // 验签通过
                System.out.println("交易名称: " + params.get("subject"));
                System.out.println("交易状态: " + params.get("trade_status"));
                System.out.println("支付宝交易凭证号: " + params.get("trade_no"));
                System.out.println("商户订单号: " + params.get("out_trade_no"));
                System.out.println("交易金额: " + params.get("total_amount"));
                System.out.println("买家在支付宝唯一id: " + params.get("buyer_id"));
                System.out.println("买家付款时间: " + params.get("gmt_payment"));
                System.out.println("买家付款金额: " + params.get("buyer_pay_amount"));
                // 更新订单状态
            }
        }
        return "success";
    }
}

