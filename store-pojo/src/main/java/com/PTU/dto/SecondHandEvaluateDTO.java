package com.PTU.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 店员评估二手书
 */
@Data
public class SecondHandEvaluateDTO implements Serializable {
    private Long id;
    /** true 同意上架并给出成色；false 驳回 */
    private Boolean approve;
    /** 成色 1-4，同意时必填 */
    private Integer conditionGrade;
    private String staffRemark;
}
