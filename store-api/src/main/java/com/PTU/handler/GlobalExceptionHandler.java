package com.PTU.handler;

import com.PTU.constant.MessageConstant;
import com.PTU.exception.BaseException;
import com.PTU.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result exceptionHandler(BadSqlGrammarException ex) {
        log.error("SQL 语法/表结构异常：{}", ex.getMessage());
        String m = ex.getMessage() != null ? ex.getMessage() : "";
        if (m.contains("user_condition_images") || m.contains("second_hand_listing")) {
            return Result.error(secondHandDbHint());
        }
        return Result.error("数据查询失败，请确认数据库脚本已执行");
    }

    /**
     * MyBatis 会将 JDBC 异常包装为 MyBatisSystemException，需单独处理，否则会走 Spring 默认 500。
     */
    @ExceptionHandler
    public Result exceptionHandler(MyBatisSystemException ex) {
        log.error("MyBatis 异常：{}", ex.getMessage(), ex);
        Throwable t = ex;
        while (t != null) {
            if (t instanceof BadSqlGrammarException) {
                return exceptionHandler((BadSqlGrammarException) t);
            }
            if (t instanceof SQLException) {
                String m = ((SQLException) t).getMessage();
                if (m != null && (m.contains("user_condition_images") || m.contains("second_hand_listing"))) {
                    return Result.error(secondHandDbHint());
                }
            }
            String msg = t.getMessage();
            if (msg != null && msg.contains("Unknown column") && msg.contains("user_condition_images")) {
                return Result.error(secondHandDbHint());
            }
            if (msg != null && msg.contains("Unknown column") && msg.contains("second_hand_listing")) {
                return Result.error(secondHandDbHint());
            }
            t = t.getCause();
        }
        return Result.error("数据查询失败，请确认 MySQL 已执行 sql 目录下二手书相关脚本（second_hand_listing.sql 与 second_hand_listing_user_images_alter.sql）");
    }

    /**
     * 兜底：避免未捕获异常落到 Spring 默认 /error，前端只看到 “Internal Server Error”。
     */
    @ExceptionHandler
    public Result exceptionHandler(Exception ex) {
        log.error("未捕获异常：{}", ex.toString(), ex);
        String hint = hintFromThrowable(ex);
        return Result.error(hint != null ? hint : MessageConstant.UNKNOWN_ERROR);
    }

    private static String hintFromThrowable(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            String m = t.getMessage();
            if (m == null) {
                continue;
            }
            if (m.contains("user_condition_images")) {
                return "数据库缺少列 user_condition_images：请执行 sql/second_hand_listing_user_images_alter.sql 后重启 store-api";
            }
            if (m.contains("second_hand_order")) {
                return "数据库缺少二手书订单表：请执行 sql/second_hand_order.sql 后重启 store-api";
            }
            if (m.contains("second_hand_listing") || m.contains("second_hand_listing_id")) {
                return secondHandDbHint();
            }
            if ((m.contains("Table") || m.contains("table")) && m.contains("doesn't exist")) {
                return "数据库缺少表：请执行 sql/second_hand_listing.sql 后重启 store-api";
            }
            if (m.contains("Unknown column") && m.contains("second_hand")) {
                return "数据库字段不完整：请执行 sql/second_hand_listing.sql 中的 ALTER（orders.second_hand_listing_id）后重启 store-api";
            }
        }
        return null;
    }

    private static String secondHandDbHint() {
        return "数据库缺少二手书表或字段：在 MySQL 中执行 bookstore/sql/second_hand_listing.sql（建表）"
                + " 与 sql/second_hand_listing_user_images_alter.sql（补列，若已含 user_condition_images 可跳过），然后重启 store-api";
    }

    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error("异常信息：{}", ex.getMessage());
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String username = split[2];
            String  msg = username+ MessageConstant.ALREADY_EXIST;
            return Result.error(msg);
        } else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
