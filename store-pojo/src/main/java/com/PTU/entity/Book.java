package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

// Book实体类（图书）
@Data
@TableName("book")
public class Book {
    private Integer id; // 主键ID
    private String title; // 书名
    private String author; // 作者
    private String publisher; // 出版社
    private String isbn; // ISBN号
    private BigDecimal price; // 价格
    private Integer stock; // 库存
    private Integer categoryId; // 分类ID
    private String coverImage; // 封面图片URL
    private String description; // 图书描述
}
