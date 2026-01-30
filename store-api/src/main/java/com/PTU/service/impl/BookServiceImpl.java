package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.dto.BookAddDTO;
import com.PTU.entity.Book;
import com.PTU.exception.BaseException;
import com.PTU.mapper.BookMapper;
import com.PTU.service.BookService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {
    @Override
    public void addBook(BookAddDTO bookAddDTO) {
        //检查isbn是否存在
        Book book = baseMapper.selectOne(new LambdaQueryWrapper<Book>().eq(Book::getIsbn, bookAddDTO.getIsbn()));
        if(book != null){
            throw new BaseException(MessageConstant.BOOK_EXIST);
        }
        Book newBook = new Book();
        BeanUtils.copyProperties(bookAddDTO,newBook);
        this.save(newBook);
    }
}
