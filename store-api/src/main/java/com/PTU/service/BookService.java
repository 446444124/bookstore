package com.PTU.service;

import com.PTU.dto.BookAddDTO;
import com.PTU.dto.BookPageQueryDTO;
import com.PTU.entity.Book;
import com.PTU.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BookService extends IService<Book> {
    void addBook(BookAddDTO bookAddDTO);

    PageResult pageQuery(BookPageQueryDTO BookPageQueryDTO);

    void deleteBatch(List<Long> ids);

    void startOrStop(Integer status, Long id);
}
