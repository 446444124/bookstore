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
 * 用户二手书回收条目
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("second_hand_listing")
public class SecondHandListing implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("book_id")
    private Long bookId;

    @TableField("seller_user_id")
    private Long sellerUserId;

    @TableField("user_note")
    private String userNote;

    /** 用户上传成色参考图，JSON 数组 URL */
    @TableField("user_condition_images")
    private String userConditionImages;

    /** 成色 1-4 */
    @TableField("condition_grade")
    private Integer conditionGrade;

    @TableField("price_ratio")
    private Integer priceRatio;

    @TableField("ref_book_price")
    private BigDecimal refBookPrice;

    @TableField("sale_price")
    private BigDecimal salePrice;

    private Integer status;

    @TableField("staff_remark")
    private String staffRemark;

    @TableField("buyer_user_id")
    private Long buyerUserId;

    @TableField("order_id")
    private String orderId;

    @TableField("pending_order_id")
    private String pendingOrderId;

    @TableField("sold_time")
    private LocalDateTime soldTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
