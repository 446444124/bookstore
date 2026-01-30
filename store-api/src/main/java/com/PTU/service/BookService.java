package com.PTU.service;

import com.PTU.dto.BookAddDTO;
import com.PTU.entity.Book;
import com.baomidou.mybatisplus.extension.service.IService;

public interface BookService extends IService<Book> {
    void addBook(BookAddDTO bookAddDTO);
}
