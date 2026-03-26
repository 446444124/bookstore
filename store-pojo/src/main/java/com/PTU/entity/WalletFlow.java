package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("wallet_flow")
public class WalletFlow {
    public static final Integer TYPE_RECHARGE = 1;
    public static final Integer TYPE_CONSUME = 2;
    public static final Integer TYPE_REFUND = 3;
    /** 二手书成交：买家已付款后结算给卖家 */
    public static final Integer TYPE_SELLER_INCOME = 4;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("flow_type")
    private Integer flowType;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("biz_no")
    private String bizNo;

    @TableField("remark")
    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;
}

