package com.PTU.dto;

import lombok.Data;

import java.io.Serializable;
@Data
public class BookPageQueryDTO implements Serializable {
    //页码
    private int page;

    //每页记录数
    private int pageSize;

    private String title;

    private String author;

    private String isbn;
}
