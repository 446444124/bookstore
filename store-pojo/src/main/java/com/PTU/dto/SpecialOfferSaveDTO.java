package com.PTU.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SpecialOfferSaveDTO implements Serializable {

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

    private Integer enabled;

    private Integer sort;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 活动包含图书列表
     */
    private List<Item> items;

    @Data
    public static class Item implements Serializable {
        private Long bookId;
        private Integer quantity;
    }
}

