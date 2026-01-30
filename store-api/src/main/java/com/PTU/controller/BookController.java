package com.PTU.controller;

import com.PTU.dto.BookAddDTO;
import com.PTU.result.Result;
import com.PTU.service.BookService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
