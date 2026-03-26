package com.PTU.vo;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class BookVO {
    private Long id; // 主键ID（与 Book.id 一致，避免 Long→Integer 拷贝异常）

    private String title; // 书名
    private String author; // 作者
    private String publisher; // 出版社
    private String isbn; // ISBN号
    private BigDecimal price; // 价格
    private Integer stock; // 库存
    private Integer categoryId; // 分类ID
    private String coverImage; // 封面图片URL
    private String description; // 图书描述
    private Integer status; // 状态（0：禁用，1：起售）
}
