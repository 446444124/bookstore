package com.PTU.utils;

import com.PTU.entity.Orders;
import com.PTU.properties.AlipayProperties;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

import static com.sun.webpane.platform.ConfigManager.log;

@Component
public class AlipayUtil {
    @Autowired
    private AlipayProperties alipayProperties;
    public String pay(Orders order) throws AlipayApiException {

        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new
                DefaultAlipayClient(alipayProperties.getGatewayUrl(), alipayProperties.getAppid(), alipayProperties.getMerchantPrivateKey(),
                "JSON", alipayProperties.getCharset(), alipayProperties.getAlipayPublicKey(), alipayProperties.getSignType());

        log.info(alipayProperties.getMerchantPrivateKey());
        log.info("----------------");
        log.info(alipayProperties.getAlipayPublicKey());

        //2、创建一个支付请求，并设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(alipayProperties.getReturnUrl());
        alipayRequest.setNotifyUrl(alipayProperties.getNotifyUrl());

        String id = order.getId();
        String interfaceInfoId = "id";
        BigDecimal money = order.getTotalAmount();
        String paymentMethod = "alipay";

        //商品描述(可空)
//        alipayRequest.setBizContent(" {\"out_trade_no\":\"" + id + "\","
//                + "\"total_amount\":\"" + "10.00" + "\","
//                + "\"subject\":\"" + interfaceInfoId + "\","
//                + "\"body\":\"" + paymentMethod + "\","
//                + "\"timeout_express\":\"" + timeout + "\","
//                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应：" + result);
        return result;
    }
}
