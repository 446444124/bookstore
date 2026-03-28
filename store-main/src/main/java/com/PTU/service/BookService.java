package com.PTU.service;

import com.PTU.dto.BookPageQueryDTO;
import com.PTU.entity.Book;
import com.PTU.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BookService extends IService<Book> {
    PageResult pageQuery(BookPageQueryDTO BookPageQueryDTO);

    long countOnSale();

    List<Book> listCheapestOnSale(int limit);

    List<Book> listPriciestOnSale(int limit);

    List<Book> searchOnSaleByKeyword(String keyword, int limit);
}
