package com.PTU.service;

import com.PTU.dto.BookAddDTO;
import com.PTU.dto.BookPageQueryDTO;
import com.PTU.entity.Book;
import com.PTU.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface BookService extends IService<Book> {
    void addBook(BookAddDTO bookAddDTO);

    PageResult pageQuery(BookPageQueryDTO BookPageQueryDTO);
}
