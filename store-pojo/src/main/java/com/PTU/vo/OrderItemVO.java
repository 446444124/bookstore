package com.PTU.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO implements Serializable {
    private Long bookId;
    private String title;
    private String coverImage;
    private Integer quantity;
    private BigDecimal price;
}
