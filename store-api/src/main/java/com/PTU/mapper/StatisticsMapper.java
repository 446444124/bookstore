package com.PTU.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    @Select("SELECT IFNULL(SUM(total_amount),0) FROM orders WHERE status = 5 AND order_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)")
    BigDecimal sumSalesAmount(@Param("days") Integer days);

    @Select("SELECT COUNT(*) FROM orders WHERE status = 5 AND order_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)")
    Long countOrders(@Param("days") Integer days);

    @Select("SELECT COUNT(*) FROM orders WHERE status = 5 AND pay_status = 1 AND order_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)")
    Long countPaidOrders(@Param("days") Integer days);

    @Select("SELECT COUNT(*) FROM orders WHERE status = 5 AND order_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)")
    Long countCompletedOrders(@Param("days") Integer days);

    @Select("SELECT DATE_FORMAT(order_time, '%Y-%m-%d') AS d, IFNULL(SUM(total_amount),0) AS amt " +
            "FROM orders " +
            "WHERE status = 5 AND order_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE_FORMAT(order_time, '%Y-%m-%d') " +
            "ORDER BY d")
    List<Map<String, Object>> salesTrend(@Param("days") Integer days);

    @Select("SELECT IFNULL(c.name, CONCAT('分类', b.category_id)) AS name, IFNULL(SUM(oi.quantity),0) AS qty " +
            "FROM order_item oi " +
            "JOIN orders o ON o.id = oi.order_id " +
            "JOIN book b ON b.id = oi.book_id " +
            "LEFT JOIN category c ON c.id = b.category_id " +
            "WHERE o.status = 5 AND o.order_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY b.category_id, c.name " +
            "ORDER BY qty DESC")
    List<Map<String, Object>> categorySalesShare(@Param("days") Integer days);

    @Select("SELECT b.id AS bookId, b.title AS title, IFNULL(SUM(oi.quantity),0) AS qty, IFNULL(SUM(oi.price),0) AS amount " +
            "FROM order_item oi " +
            "JOIN orders o ON o.id = oi.order_id " +
            "JOIN book b ON b.id = oi.book_id " +
            "WHERE o.status = 5 AND o.order_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY b.id, b.title " +
            "ORDER BY qty DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> topSellingBooks(@Param("days") Integer days, @Param("limit") Integer limit);
}
