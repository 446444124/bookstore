package com.PTU.dto;

import lombok.Data;

import java.io.Serializable;
@Data
public class BookPageQueryDTO implements Serializable {
    //页码 默认值为1
    private int page = 1;

    //每页记录数 默认值为10
    private int pageSize = 10;

    private String title;

    private String author;

    private String isbn;

    private Integer status;

    private  Integer categoryId;
}
