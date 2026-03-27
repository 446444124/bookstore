package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("special_offer")
public class SpecialOffer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 1=单品 2=组合
     */
    private Integer offerType;

    /**
     * 1=折扣(%) 2=一口价(元) 3=立减(元)
     */
    private Integer discountType;

    private BigDecimal discountValue;

    /**
     * 1=启用 0=停用
     */
    private Integer enabled;

    private Integer sort;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long updateBy;
}

