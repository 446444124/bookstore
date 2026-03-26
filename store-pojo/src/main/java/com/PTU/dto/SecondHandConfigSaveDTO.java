package com.PTU.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SecondHandConfigSaveDTO implements Serializable {
    private BigDecimal serviceFeePercent;
}

