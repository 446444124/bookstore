package com.PTU.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端订单列表 UNION（orders ∪ second_hand_order）行
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderUnionRow implements Serializable {
    private String id;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;
    private Integer payStatus;
    private String phone;
    private String consignee;
    private LocalDateTime orderTime;
    private Integer deliveryWay;
}
