package com.PTU.controller;

import com.PTU.service.BookService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
}
