package com.PTU.handler;

import com.PTU.constant.MessageConstant;
import com.PTU.exception.BaseException;
import com.PTU.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler
    public Result exceptionHandler(BadSqlGrammarException ex) {
        log.error("SQL 语法/表结构异常：{}", ex.getMessage());
        return Result.error(secondHandDbHint(ex.getMessage()));
    }

    /**
     * 兜底：未单独处理的异常（如表不存在、列不存在），给出可读提示
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
                return "数据库缺少列 user_condition_images：请执行 sql/second_hand_listing_user_images_alter.sql 后重启 store-main";
            }
            if (m.contains("second_hand_order")) {
                return "数据库缺少二手书订单表：请执行 sql/second_hand_order.sql 后重启 store-main";
            }
            if (m.contains("second_hand_listing") || m.contains("second_hand_listing_id")) {
                return "数据库尚未升级二手书模块：请在 MySQL 中执行项目内 sql/second_hand_listing.sql 后重启 store-main";
            }
            if ((m.contains("Table") || m.contains("table")) && m.contains("doesn't exist")) {
                return "数据库缺少相关表：二手书功能请执行 sql/second_hand_listing.sql";
            }
            if (m.contains("Unknown column") && m.contains("second_hand")) {
                return "数据库 orders 表缺少 second_hand_listing_id 字段：请执行 sql/second_hand_listing.sql 中的 ALTER 语句";
            }
        }
        return null;
    }

    private static String secondHandDbHint(String message) {
        if (message == null) {
            return "数据库执行失败，请检查表结构是否已执行 sql/second_hand_listing.sql";
        }
        String hint = hintFromThrowable(new RuntimeException(message));
        return hint != null ? hint : "数据库查询失败，请确认已执行 sql/second_hand_listing.sql";
    }

}
