package com.PTU.service.impl;

import com.PTU.entity.Book;
import com.PTU.mapper.BookMapper;
import com.PTU.service.BookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class BookServiceImpl  extends ServiceImpl<BookMapper, Book> implements BookService  {
}
