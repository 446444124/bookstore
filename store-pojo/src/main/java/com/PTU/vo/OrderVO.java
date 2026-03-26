package com.PTU.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO implements Serializable {
    private String id;
    private String orderNumber;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;
    private Integer payStatus;
    private LocalDateTime orderTime;
    private LocalDateTime payTime;
    private Long addressBookId;
    private Integer payWay;
    private String remark;
    private Integer deliveryStatus;
    private Integer deliveryWay;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime deliveryTime;
    private String consignee;
    private String phone;
    private String address;
    private String username;
    private String cancelReason;
    private String cancelTime;
    private String rejectionReason;
    private List<OrderItemVO> items;
    /** 非空表示该订单含二手书条目 */
    private Long secondHandListingId;
}
