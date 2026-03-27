package com.PTU.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SpecialOfferSubmitDTO implements Serializable {

    private Long offerId;

    /**
     * 购买份数：单品=数量；组合=套餐数量
     */
    private Integer count;

    //地址簿id
    private Long addressBookId;
    //付款方式
    private int payWay;
    //备注
    private String remark;
    //预计送达时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;
    //配送状态  1立即送出  0选择具体时间
    private Integer deliveryStatus;
    //配送方式 0自提  1配送
    private Integer deliveryWay;
}

