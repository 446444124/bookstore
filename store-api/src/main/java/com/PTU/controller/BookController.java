package com.PTU.controller;

import com.PTU.dto.BookAddDTO;
import com.PTU.dto.BookPageQueryDTO;
import com.PTU.result.PageResult;
import com.PTU.result.Result;
import com.PTU.service.BookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/book")
@Api(tags = "B端图书接口")
@Slf4j
public class BookController {

    @Autowired
    private BookService bookService;

    @PostMapping("/add")
    public Result<String> addBook(@RequestBody BookAddDTO booAddkDTO) {
        log.info("添加图书:{}",booAddkDTO);
        bookService.addBook(booAddkDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("图书分页查询")
    public Result<PageResult> page(BookPageQueryDTO bookPageQueryDTO) {
        log.info("图书分页查询：{}", bookPageQueryDTO);
        PageResult pageResult = bookService.pageQuery(bookPageQueryDTO);
        return Result.success(pageResult);
    }
}
