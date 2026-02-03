package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
// Book实体类（图书）
@Data
@TableName("book")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @TableId(type = IdType.AUTO)
    private Long id; // 主键ID

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
    //更新时间
    private LocalDateTime updateTime;
    //创建时间
    private LocalDateTime createTime;
}
