package com.PTU.constant;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 二手书回收：状态与成色折价比例（相对店内图书原价百分比）
 */
public final class SecondHandConstants {

    private SecondHandConstants() {
    }

    /** 待店员审核 */
    public static final int STATUS_PENDING = 0;
    /** 店员驳回 */
    public static final int STATUS_REJECTED = 1;
    /** 已上架在售 */
    public static final int STATUS_ON_SALE = 2;
    /** 已售出 */
    public static final int STATUS_SOLD = 3;
    /** 用户撤回 */
    public static final int STATUS_WITHDRAWN = 4;
    /** 买家已下单待付款，条目从货架锁定 */
    public static final int STATUS_LOCKED_PENDING_PAY = 8;

    public static String statusText(Integer s) {
        if (s == null) return "未知";
        switch (s) {
            case STATUS_PENDING:
                return "待审核";
            case STATUS_REJECTED:
                return "已驳回";
            case STATUS_ON_SALE:
                return "在售";
            case STATUS_SOLD:
                return "已售";
            case STATUS_WITHDRAWN:
                return "已撤回";
            case STATUS_LOCKED_PENDING_PAY:
                return "待支付";
            default:
                return "未知";
        }
    }

    public static String gradeText(Integer g) {
        if (g == null) return "-";
        switch (g) {
            case 1:
                return "近新";
            case 2:
                return "良好";
            case 3:
                return "一般";
            case 4:
                return "较差";
            default:
                return "-";
        }
    }

    /**
     * @param grade 1-4
     * @return 折扣百分比，例如 70 表示按原价 70% 定价
     */
    public static int ratioPercentForGrade(int grade) {
        switch (grade) {
            case 1:
                return 78;
            case 2:
                return 62;
            case 3:
                return 48;
            case 4:
                return 32;
            default:
                throw new IllegalArgumentException("成色档位无效");
        }
    }

    public static BigDecimal salePrice(BigDecimal bookOriginalPrice, int ratioPercent) {
        if (bookOriginalPrice == null || bookOriginalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("图书原价异常");
        }
        return bookOriginalPrice
                .multiply(BigDecimal.valueOf(ratioPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
