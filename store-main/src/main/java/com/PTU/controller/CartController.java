package com.PTU.controller;


import com.PTU.entity.Cart;
import com.PTU.result.Result;
import com.PTU.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/cart")
@Api(tags = "C端-购物车相关接口")
@Slf4j
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add/{id}")
    @ApiOperation("添加购物车")
    public Result add(@PathVariable Long id, Integer num) {
        log.info("添加购物车：{}", id);
        cartService.add(id, num);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<Cart>> list() {
        List<Cart> list = cartService.showShoppingCart();
        return Result.success(list);

    }

    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean() {
        cartService.cleanShoppingCart();
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除购物车中的图书")
    public Result delete(@PathVariable Long id) {
        cartService.removeById(id);
        return Result.success();
    }
}
