package com.PTU.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "store.alipay")
@Data
public class AlipayProperties {
    @Value("${store.alipay.appid}")
    private String appid; //小程序的appid
    @Value("${store.alipay.merchantPrivateKey}")
    private String merchantPrivateKey; //商户应用私钥
    @Value("${store.alipay.alipayPublicKey}")
    private String alipayPublicKey; //支付宝应用公钥
    @Value("${store.alipay.gatewayUrl}")
    private String notifyUrl; //支付成功的回调地址
    @Value("${store.alipay.returnUrl}")
    private String returnUrl; //支付成功后的返回地址
    @Value("${store.alipay.signType}")
    private String signType; //签名类型
    @Value("${store.alipay.charset}")
    private String charset; //字符编码
    @Value("${store.alipay.gatewayUrl}")
    private String gatewayUrl; //支付宝网关地址

}
