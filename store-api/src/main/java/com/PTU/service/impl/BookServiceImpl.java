package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.constant.StatusConstant;
import com.PTU.dto.BookAddDTO;
import com.PTU.dto.BookPageQueryDTO;
import com.PTU.entity.Book;
import com.PTU.exception.BaseException;
import com.PTU.exception.DeletionNotAllowedException;
import com.PTU.mapper.BookMapper;
import com.PTU.result.PageResult;
import com.PTU.service.BookService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

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
    //使用MyBatis-Plus内置分页
    public PageResult pageQuery(BookPageQueryDTO BookPageQueryDTO) {
        // 创建MyBatis-Plus分页对象
        Page<Book> page = new Page<>(BookPageQueryDTO.getPage(), BookPageQueryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(BookPageQueryDTO.getTitle() != null, Book::getTitle, BookPageQueryDTO.getTitle())
                .eq(BookPageQueryDTO.getIsbn() != null, Book::getIsbn, BookPageQueryDTO.getIsbn())
                .like(BookPageQueryDTO.getAuthor() != null, Book::getAuthor, BookPageQueryDTO.getAuthor());

        // 执行分页查询
        this.page(page, queryWrapper);

        // 构建返回结果
        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        //判断图书状态是否为起售状态
        for (Long id : ids) {
            Book book = this.getById(id);
            if(book.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.BOOK_ON_SALE);
            }
        }
        this.removeByIds(ids);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Book book = new Book();
        book.setStatus(status);
        book.setId(id);
        this.updateById(book);
    }

}
