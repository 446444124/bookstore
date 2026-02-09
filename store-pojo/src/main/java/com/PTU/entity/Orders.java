package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("orders")
public class Orders implements Serializable {

    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    public static final Integer PENDING_PAYMENT = 1;
    public static final Integer TO_BE_CONFIRMED = 2;
    public static final Integer CONFIRMED = 3;
    public static final Integer DELIVERY_IN_PROGRESS = 4;
    public static final Integer COMPLETED = 5;
    public static final Integer CANCELLED = 6;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    public static final Integer UN_PAID = 0;
    public static final Integer PAID = 1;
    public static final Integer REFUND = 2;

    private static final long serialVersionUID = 1L;
    /**
     * 订单号，使用UUID生成
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 订单总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 订单状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 支付时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 地址id
     */
    @TableField("address_book_id")
    private Long addressBookId;

    /**
     * 下单时间
     */
    @TableField("order_time")
    private LocalDateTime orderTime;

    /**
     * 支付状态
     */
    @TableField("pay_status")
    private Integer payStatus;

    /**
     * 支付方式
     */
    @TableField("pay_way")
    private Integer payWay;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 地址
     */
    @TableField("address")
    private String address;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 收货人
     */
    @TableField("consignee")
    private String consignee;

    /**
     * 订单取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 订单取消时间
     */
    @TableField("cancel_time")
    private String cancelTime;

    /**
     * 订单拒绝原因
     */
    @TableField("rejection_reason")
    private String rejectionReason;

    /**
     * 预计送达时间
     */
    @TableField("estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 送达时间
     */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;

    /**
     * 配送状态1立即送出0选择具体时间
     */
    @TableField("delivery_status")
    private Integer deliveryStatus;

    /**
     * 配送方式 自提0 配送1
     */
    @TableField("delivery_way")
    private Integer deliveryWay;
}
