package com.PTU.service.impl;

import com.PTU.constant.CateforyPageConstant;
import com.PTU.constant.StatusConstant;
import com.PTU.dto.BookPageQueryDTO;
import com.PTU.entity.Book;
import com.PTU.mapper.BookMapper;
import com.PTU.result.PageResult;
import com.PTU.service.BookService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class BookServiceImpl  extends ServiceImpl<BookMapper, Book> implements BookService {
    //使用MyBatis-Plus内置分页
    public PageResult pageQuery(BookPageQueryDTO bookPageQueryDTO) {
        // 创建MyBatis-Plus分页对象
        Page<Book> page = new Page<>(bookPageQueryDTO.getPage(), bookPageQueryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(bookPageQueryDTO.getTitle() != null, Book::getTitle, bookPageQueryDTO.getTitle())
                .eq(bookPageQueryDTO.getIsbn() != null, Book::getIsbn, bookPageQueryDTO.getIsbn())
                .like(bookPageQueryDTO.getAuthor() != null, Book::getAuthor, bookPageQueryDTO.getAuthor())
                .eq(bookPageQueryDTO.getStatus() != null, Book::getStatus, bookPageQueryDTO.getStatus())
                .eq(bookPageQueryDTO.getCategoryId() != null, Book::getCategoryId, bookPageQueryDTO.getCategoryId())
                //用户端只展示起售的图书
                .eq(Book::getStatus, StatusConstant.ENABLE);

        // 执行分页查询
        this.page(page, queryWrapper);

        // 构建返回结果
        return new PageResult(page.getTotal(), page.getRecords());
    }

}
