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
    /** 成色档位ID（second_hand_grade），同意时必填 */
    private Long gradeId;
    /** 兼容旧前端：成色 1-4（将逐步废弃） */
    private Integer conditionGrade;
    private String staffRemark;
}
