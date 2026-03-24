package com.PTU.utils;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PayUtil {
    //appid
    private final String APP_ID = "9021000161635489";
    //应用私钥
    //private final String APP_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCA/pAnYcUr8AaABhNdw60sEAwnR6IafQHKwJwjqH64wwLPnO0zzhdk3Pb238TUdVMdUEArDeUwziE5wrTxuMeDwp3lQsO6UOYVDLDQ+nuTb92a6OhMQ57dWOPCRDHlTwIK1uQtvcDDhu/QkvGI18tgcprP2+Iaq0H4ILX46uz/2NF62Xr6gTsN97ZDEUCoai5N4nl7gZfhMy1DZ3rnp1cx0neUdx3Q59SO04+s7IJSn0Rr3SBt/jvavUCYKld+kzQDReBAPIwCktX6TzL+nBhGRcbh1/meXW559xG1b+SSAb86fDlS0L6+BNRkxGU6onDBqKotlijiV4bMPY41RhK5AgMBAAECggEAKkpLHH4zVFpW5zYWt1Dlv2JreZ1Eigadxckr54zgLtbQXxyFr8xLYygGV525PCZS33Nl9jeSAlQyFGu/QthfoNT3Syh+XuES7afBeGIGjanJL3ZYYlhxBwSTdydAbHtgogkdudlSD0h36xuh0ElfRfmMw8TE9hVbhWvD/R2YIbCOnEDQp9K6U8b/VpqNC36cECjpkLcUW2J4sLsxBuvZdqddxAenWtSlApnO7Y2ob3hZUCubO6DSznx1g09ZAgGizh3hCHbxQp8fkvEXod/lqZINXwZxRVMBml3CB3DzKHJaC63EOLa20tM9SyZMwOsmf7vLNMJEZZ6o21G83REaYQKBgQC6O+PqS4pJ1vbdx07Wr3DXhqmlAthA8GHz70AgWe9d5rrl3ekmgZCDq+2xHumH5A18FsI/K409kXTQnO6qrTbB6NGBAObkc9BQIZwXI47SsZc13khnM/gIkDH7v5N107t+0sNS3uri1cCoBiwvtGm4TRG/u0QFH2PsxCaE3j5rTQKBgQCxUVCVOi8I94F81w0r0yMLuuP5+71eD2eKEo13REojod5+G9AZpHW/gv+WQ4ODuVArsfUeCKd8cpyss52D3NbZGOXDR4g/bZWsZtu7MRsM/fzb346FGQPgecFQ5nSf0Llp3jp8bgA4TLTu1lGUZhOmGKDLOS8e1IZu5ofwNtAXHQKBgFW9zyAGqWN3me7fnTeQTHaeJCq5lZDIW4CwbSi331GvMes08OvuiPdmR6fdYI1U/LRd0g69YWiwOET3i5Z+6w/5vcMUBDm37HeIEdJBZ7cvVjvR3CnPrmla4fhS6nQ8CD3KkWzs/t1ypiF1vhA0ktvsd7h7YkStgBb48/cGT1ydAoGBAKlYzsxmsEsxDwMq5BJp6LuoN1klmzr9HVbZ1zAEEYasbrEkYQBMeIR6WR2qxy0DzFSe9kpD26nYUFgEhzhLrwnSZIW3iKGDSIJKDitef8ZNwLRBc30cYf5+aimYn+OOVbaNnm/+IsmiQN8l4u4G+1cTiGXu7FjWgrniBAxgXSblAoGAJlSLwTQx4bHrb1BZJKRvRlOjrBx/6SWqPsBQQpTpQExlmKOQurdb24Wr2E+hvfGz08vil8+WhHCzRUsxHbMq4yPkxNa0/EfNjGElhYrMgEcl95nE9Kt+cQKk7UCWRUe0QRJ3W8m7cqj8Wn7M5ymYj7BcdZ/XR2LKKzLTodia1P4=";
    private final String APP_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCqFQUakC7/o4wwebpY9PN17qLJ4c6swd5eQM3ViFXStKNh7+FD8t5QnTpiLp7bCT4TI3JKRpSdP/vjp74wte3UUEtcX8ga4SnT0S7gWB7urELk44KjKwfZ4N9BpXYlZ0spVjnAS1fPXXEZ1eUjnx2o9ILxZKlYJ3jwgKkquewy+iIJbXCt7HD+7y9db9RrlfpunH6ILGujf5dM5lIXfd5LuLie+3gb2uNC8dG3tru/YrFTd1d0Y7nC2zbPYhGt2IodJBQqlcvuyXiFhrB2XUbLADsrhI4AY7xc7+yBT2cWG3PMTeRny8AgVqaq6VF4wwGPcoU6i0E/0yiuz+KiXkyFAgMBAAECggEBAJody+8jAYNhtjFizCm5ZDbT0yyt8XH3hkDP5fP8ww/X4Uhobi39D9fqcLHw3r9Jo7cB4plNVXMcPGowYJWQ9P3y+CavIArDRvyJyzk8gGLzMvZm9cRWxJl4O7UncmQ4MhI18MS/QMVFPawUdrqNfvnO/iwwc4bO1bbQTvnJWS9ioG8sinmgS3xY/HqHnVsLhnt7D0lDfaPhLhNAcpukjAU90Ct2mSlahrIMpL9SnrVpqAj57Fe46dIdRW3ycUymuO9ghJ9KCerdAMS1b/nxzV0TKweIhCQPELaBqvfJeGR5eNO3qKBw0yS+hgToIw7f9T+eblz3Zkcbbg/eQ76vKi0CgYEA0yPwILr9LvLpCVu/JyDPMxKj+IPhvvOfACpuv9yC5+/ZM8lREUunEDKrl3WDa+Wy+Y+5K6CsfNXbYZj2VlkVY8apskSGDIZuGhxhzThxshUDXoBlqLjOMteMxJsm/jfSm7wqyaAhggtJI/O9Mz5c/M33nb1ckr52qzuMnSAO8F8CgYEAzjflV+OKojKhRncQzhyxqFjSUCfTt4x8aSCaP5HEB9To3yVTroqyEmeejs2du9qzwm1MKSN5XKqdunrkAf+eWYvGhmdgKVR9EGKxtRRhvo6O8p3Egul/tlsTHTyBtDErfpGkROsImsR2exv8Op1y66EwWjmVu0a0bA3+wI7oHZsCgYATCkyfRv2J/FHTjDiCuMy2mFm9EXeMFsAh7bGrD6GmdNSrfN4Ypy/boBYlRG2jj0VCiBl0u7qST17HQFoGk2YAZ2vqLY/BFWZcaG7RaOOn4Yk6Pm3sdy3xG8sP98Xeav5NGPzqzcv3xDjGSzamplsQWSqZSPhOK2rcKjSV/swaFQKBgFhsJT3DRl14AikZwq3uJcs+kqdl/GUWqv3Uye76DKkjBNsxwSj1NZeosJcg2UKEa1Gx1ddJ667tRYaBTSUCOx7ATVayGsx9+sY821/+pk60B91GAs3zWxyQ/qgpmp/t+W1i+6HFecIK/ZaaEFP90Rnx2q7FREux7Ijsk1BztlmDAoGAOggSTHJPe5XwRIBOrfjT3c6b/VkwWJ8I8OblHWc3+Q4bhOkdvjPnc91WbNfAmfULfURPD/p86CKvPky+LAIPifVMgWAyXTaf8EtIA+Mnb1VmURReJ/Xhiq1ahy5L/Lt65kYAu5s/TdbZo7//YLEfb/ID+hsqWex+LFwdt3DHS40=";

    private final String CHARSET = "UTF-8";
    // 支付宝公钥
    private final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhrT6UnFHCFHDG3c3i+Xebvxd1SWw0ovG06hi6aDk4V0YyC75mtoufzD9vYwTt6yPFaoAeKYfPL8At8igd+RA7sYQmxxxms2bzzgLtXZ2kCC9fo4X97tONqzJ6ieUEGk3d3iMIL3Pg5CbM5fAf68UMjJcqY9Xp4Rrbhmv4YJFvUaZO2bSnWCt1mGcCY9HPRp6N3Kr+Vu6BrO2WGXbyeCJyr7OQ+dUvpDtWyRAL5rolfourkeMgX7zVO8qTwpGN9mvqFfvFBWa56dfziOfJ38WPvnF0EBsm+8q45qzI1b0l/VSGP5muy0eqHwO88p+7bd5BIFSIXsDzsmwDgxx/83H8wIDAQAB";
    //这是沙箱接口路径,正式路径为https://openapi.alipay.com/gateway.do
    private final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private final String FORMAT = "JSON";
    //签名方式
    private final String SIGN_TYPE = "RSA2";
    //支付宝异步通知路径,付款完毕后会异步调用本项目的方法,必须为公网地址
    private final String NOTIFY_URL = "https://h6q9j1cdjz.fy.takin.cc/api/alipay/notify";
    //支付宝同步通知路径,也就是当付款完毕后跳转本项目的页面,可以不是公网地址
    //private final String RETURN_URL = "http://localhost:5173/#/";
    private final String RETURN_URL = "http://localhost:8080/api/alipay/toSuccess";
    private AlipayClient alipayClient = null;
    //支付宝官方提供的接口
    public String sendRequestToAlipay(String outTradeNo, BigDecimal totalAmount, String subject) throws AlipayApiException {
        //获得初始化的AlipayClient
        alipayClient = new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(RETURN_URL);
        alipayRequest.setNotifyUrl(NOTIFY_URL);

        // 组装业务参数（务必使用正确字段 out_trade_no）
        String safeSubject = (subject == null || subject.trim().isEmpty())
                ? ("校园书店订单-" + outTradeNo)
                : subject;
        String amountStr = totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
        String body = "";
        String bizContent = "{"
                + "\"out_trade_no\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + amountStr + "\","
                + "\"subject\":\"" + safeSubject + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                + "}";
        alipayRequest.setBizContent(bizContent);

        //请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        System.out.println("返回的结果是："+result );
        return result;
    }

    public String getAlipayPublicKey() {
        return ALIPAY_PUBLIC_KEY;
    }
    public String getCharset() {
        return CHARSET;
    }
    public String getSignType() {
        return SIGN_TYPE;
    }

    //    通过订单编号查询
    public String query(String id){
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", id);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        String body=null;
        try {
            response = alipayClient.execute(request);
            body = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response != null && response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        return body;
    }
}
