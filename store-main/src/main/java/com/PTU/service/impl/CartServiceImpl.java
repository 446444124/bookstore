package com.PTU.service.impl;

import com.PTU.context.BaseContext;
import com.PTU.entity.Book;
import com.PTU.entity.Cart;
import com.PTU.mapper.CartMapper;
import com.PTU.service.BookService;
import com.PTU.service.CartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {
    @Autowired
    private BookService bookService;
    @Override
    public void add(Long id, Integer num) {
        //检查购物车中该图书是否在
        Cart cart1 = this.getOne(new QueryWrapper<Cart>().eq("user_id", BaseContext.getCurrentId()).eq("book_id", id));
        if(cart1 == null) {
            Book book = bookService.getById(id);
            Cart cart = Cart.builder()
                    .userId(BaseContext.getCurrentId())
                    .bookId(book.getId())
                    .title(book.getTitle())
                    .quantity(num)
                    .amount(book.getPrice().multiply(BigDecimal.valueOf(num)))
                    .coverImage(book.getCoverImage())
                    .build();
            this.save(cart);
            return;
        }
        //如果购物车中已存在该图书，更新数量
        cart1.setQuantity(cart1.getQuantity() + num);
        this.updateById(cart1);
    }

    @Override
    public List<Cart> showShoppingCart() {
        Long currentId = BaseContext.getCurrentId();

        // 查询当前用户的购物车数据
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, currentId);
        return this.list(queryWrapper);
    }
    @Override
    public void cleanShoppingCart() {
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, currentId);
        this.remove(queryWrapper);
    }
}
