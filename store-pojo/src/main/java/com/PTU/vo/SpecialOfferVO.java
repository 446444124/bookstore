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
public class SpecialOfferVO implements Serializable {

    private Long id;
    private String name;
    private Integer offerType;
    private Integer discountType;
    private BigDecimal discountValue;
    private Integer enabled;
    private Integer sort;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime updateTime;

    private BigDecimal originalAmount;
    private BigDecimal dealAmount;

    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item implements Serializable {
        private Long bookId;
        private String title;
        private String coverImage;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal lineAmount;
    }
}

