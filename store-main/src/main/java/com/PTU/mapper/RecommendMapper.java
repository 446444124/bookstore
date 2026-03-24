package com.PTU.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface RecommendMapper {

    @Select("SELECT b.category_id AS categoryId, IFNULL(SUM(oi.quantity),0) AS qty " +
            "FROM orders o " +
            "JOIN order_item oi ON o.id = oi.order_id " +
            "JOIN book b ON b.id = oi.book_id " +
            "WHERE o.user_id = #{userId} AND o.pay_status = 1 " +
            "GROUP BY b.category_id " +
            "ORDER BY qty DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> userCategoryPref(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Select("SELECT oi.book_id AS bookId, IFNULL(SUM(oi.quantity),0) AS qty " +
            "FROM orders o " +
            "JOIN order_item oi ON o.id = oi.order_id " +
            "WHERE o.user_id = #{userId} AND o.pay_status = 1 " +
            "GROUP BY oi.book_id " +
            "ORDER BY qty DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> userBookPref(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Select("SELECT b.category_id AS categoryId, IFNULL(SUM(oi.quantity),0) AS qty " +
            "FROM orders o " +
            "JOIN order_item oi ON o.id = oi.order_id " +
            "JOIN book b ON b.id = oi.book_id " +
            "WHERE o.pay_status = 1 " +
            "GROUP BY b.category_id " +
            "ORDER BY qty DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> globalCategoryHot(@Param("limit") Integer limit);

    @Select("SELECT oi.book_id AS bookId, IFNULL(SUM(oi.quantity),0) AS qty " +
            "FROM orders o " +
            "JOIN order_item oi ON o.id = oi.order_id " +
            "WHERE o.pay_status = 1 " +
            "GROUP BY oi.book_id " +
            "ORDER BY qty DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> globalBookHot(@Param("limit") Integer limit);
}
