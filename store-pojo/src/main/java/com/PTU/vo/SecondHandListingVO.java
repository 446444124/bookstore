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
public class SecondHandListingVO implements Serializable {

    private Long id;
    private Long bookId;
    private Long sellerUserId;
    private String userNote;
    /** 用户上传的成色参考图 */
    private List<String> userConditionImages;
    private Integer conditionGrade;
    private String conditionGradeText;
    private Integer priceRatio;
    private BigDecimal refBookPrice;
    private BigDecimal salePrice;
    private Integer status;
    private String statusText;
    private String staffRemark;
    private Long buyerUserId;
    private String orderId;
    private LocalDateTime soldTime;
    private LocalDateTime createTime;

    private String bookTitle;
    private String bookAuthor;
    private String coverImage;
    private BigDecimal bookOriginalPrice;
}
