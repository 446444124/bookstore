package com.PTU.controller;

import com.PTU.constant.MessageConstant;
import com.PTU.dto.BookAddDTO;
import com.PTU.dto.BookDTO;
import com.PTU.dto.BookPageQueryDTO;
import com.PTU.entity.Book;
import com.PTU.exception.BaseException;
import com.PTU.result.PageResult;
import com.PTU.result.Result;
import com.PTU.service.BookService;
import com.PTU.vo.BookVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/book")
@Api(tags = "B端图书接口")
@Slf4j
public class BookController {

    @Autowired
    private BookService bookService;
    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate redisTemplate;

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
    @DeleteMapping
    @ApiOperation("批量删除图书")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("根据id删除图书：{}", ids);
        bookService.deleteBatch(ids);
        return Result.success();
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

    @PutMapping
    @ApiOperation("修改图书")
    public Result update(@RequestBody BookDTO bookDTO) {
        log.info("修改图书：{}", bookDTO);
        //isbn不能为空
        if(bookDTO.getIsbn() == null){
            throw new BaseException(MessageConstant.BOOK_ISBN_NULL);
        }
        //校验isbn是否存在
        Book book1 = bookService.getOne(new LambdaQueryWrapper<Book>().eq(Book::getIsbn, bookDTO.getIsbn()));
        if (book1 != null && !book1.getId().equals(bookDTO.getId())) {
            return Result.error(MessageConstant.ISBN_EXIST);
        }
        Book book = new Book();
        BeanUtils.copyProperties(bookDTO, book);
        bookService.updateById(book);

        return Result.success();
    }
    /**
     * 图书起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("图书起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        bookService.startOrStop(status,id);
        return Result.success();
    }

    //清理缓存数据
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
