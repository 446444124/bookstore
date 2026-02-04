package com.PTU.controller;

import com.PTU.dto.BookPageQueryDTO;
import com.PTU.entity.Book;
import com.PTU.result.PageResult;
import com.PTU.result.Result;
import com.PTU.service.BookService;
import com.PTU.vo.BookVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/book")
@Api(tags = "图书相关接口")
@Slf4j
public class BookController {
    @Autowired
    private BookService bookService;
    @Autowired
    private RedisTemplate redisTemplate;


    @GetMapping("/page")
    @ApiOperation("图书分页查询")
    public Result<PageResult> page(BookPageQueryDTO bookPageQueryDTO) {
        log.info("图书分页查询：{}", bookPageQueryDTO);
        PageResult pageResult = bookService.pageQuery(bookPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询图书")
    public Result<BookVO> getById(@PathVariable Long id) {
        log.info("根据id查询图书：{}", id);
        Book book = bookService.getById(id);
        BookVO bookVO = new BookVO();
        BeanUtils.copyProperties(book, bookVO);
        return Result.success(bookVO);
    }


}
