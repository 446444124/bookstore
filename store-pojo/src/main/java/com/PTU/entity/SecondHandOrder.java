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
 * 二手书订单（独立于 {@link Orders}）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("second_hand_order")
public class SecondHandOrder implements Serializable {

    public static final Integer PENDING_PAYMENT = 1;
    public static final Integer TO_BE_CONFIRMED = 2;
    public static final Integer CONFIRMED = 3;
    public static final Integer DELIVERY_IN_PROGRESS = 4;
    public static final Integer COMPLETED = 5;
    public static final Integer CANCELLED = 6;
    public static final Integer RETURN_REQUESTED = 7;
    public static final Integer REFUNDED = 8;

    public static final Integer UN_PAID = 0;
    public static final Integer PAID = 1;
    public static final Integer REFUND = 2;

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("user_id")
    private Long userId;

    @TableField("listing_id")
    private Long listingId;

    @TableField("seller_user_id")
    private Long sellerUserId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("status")
    private Integer status;

    @TableField("pay_time")
    private LocalDateTime payTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("address_book_id")
    private Long addressBookId;

    @TableField("order_time")
    private LocalDateTime orderTime;

    @TableField("pay_status")
    private Integer payStatus;

    @TableField("pay_way")
    private Integer payWay;

    @TableField("remark")
    private String remark;

    @TableField("phone")
    private String phone;

    @TableField("address")
    private String address;

    @TableField("username")
    private String username;

    @TableField("consignee")
    private String consignee;

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField("cancel_time")
    private String cancelTime;

    @TableField("rejection_reason")
    private String rejectionReason;

    @TableField("estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    @TableField("delivery_time")
    private LocalDateTime deliveryTime;

    @TableField("delivery_status")
    private Integer deliveryStatus;

    @TableField("delivery_way")
    private Integer deliveryWay;

    @TableField("book_id")
    private Long bookId;

    @TableField("book_title")
    private String bookTitle;

    @TableField("cover_image")
    private String coverImage;
}
