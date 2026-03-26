package com.PTU.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SecondHandGradeSaveDTO implements Serializable {
    private Long id;
    private String name;
    private BigDecimal recyclePercent;
    private Integer enabled;
    private Integer sort;
}

