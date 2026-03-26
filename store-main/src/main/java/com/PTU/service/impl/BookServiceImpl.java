package com.PTU.service.impl;

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
import org.springframework.util.StringUtils;


@Service
@Slf4j
public class BookServiceImpl  extends ServiceImpl<BookMapper, Book> implements BookService {
    //使用MyBatis-Plus内置分页
    public PageResult pageQuery(BookPageQueryDTO bookPageQueryDTO) {
        // 创建MyBatis-Plus分页对象
        Page<Book> page = new Page<>(bookPageQueryDTO.getPage(), bookPageQueryDTO.getPageSize());

        // 构建查询条件（用户端只展示起售）
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Book::getStatus, StatusConstant.ENABLE);

        if (bookPageQueryDTO.getCategoryId() != null) {
            queryWrapper.eq(Book::getCategoryId, bookPageQueryDTO.getCategoryId());
        }
        // 书名关键词：同时匹配书名或 ISBN（书号），便于二手书上架等场景单框搜索
        if (StringUtils.hasText(bookPageQueryDTO.getTitle())) {
            String t = bookPageQueryDTO.getTitle().trim();
            queryWrapper.and(w -> w.like(Book::getTitle, t).or().like(Book::getIsbn, t));
        }
        if (StringUtils.hasText(bookPageQueryDTO.getIsbn())) {
            queryWrapper.like(Book::getIsbn, bookPageQueryDTO.getIsbn().trim());
        }
        if (StringUtils.hasText(bookPageQueryDTO.getAuthor())) {
            queryWrapper.like(Book::getAuthor, bookPageQueryDTO.getAuthor().trim());
        }

        // 执行分页查询
        this.page(page, queryWrapper);

        // 构建返回结果
        return new PageResult(page.getTotal(), page.getRecords());
    }

}
