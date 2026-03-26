package com.PTU.vo;

import com.PTU.entity.SecondHandGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecondHandConfigVO implements Serializable {
    private BigDecimal serviceFeePercent;
    private List<SecondHandGrade> grades;
}

