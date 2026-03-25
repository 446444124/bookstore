package com.PTU.service.impl;

import com.PTU.context.BaseContext;
import com.PTU.entity.Book;
import com.PTU.entity.Cart;
import com.PTU.exception.BaseException;
import com.PTU.mapper.CartMapper;
import com.PTU.service.BookService;
import com.PTU.service.CartService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {
    @Autowired
    private BookService bookService;
    @Override
    public void add(Long id, Integer num) {
        int n = (num == null || num < 1) ? 1 : num;
        // 多条重复记录时 getOne 会抛 TooManyResultsException → 全局异常「未知错误」；throwEx=false 取一条即可
        Cart cart1 = this.getOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, BaseContext.getCurrentId())
                        .eq(Cart::getBookId, id),
                false);
        if (cart1 == null) {
            Book book = bookService.getById(id);
            if (book == null) {
                throw new BaseException("图书不存在或已下架，无法加入购物车");
            }
            BigDecimal unit = book.getPrice();
            if (unit == null) {
                throw new BaseException("图书价格异常，无法加入购物车");
            }
            Cart cart = Cart.builder()
                    .userId(BaseContext.getCurrentId())
                    .bookId(book.getId())
                    .title(book.getTitle())
                    .quantity(n)
                    .amount(unit.multiply(BigDecimal.valueOf(n)))
                    .coverImage(book.getCoverImage())
                    .createTime(LocalDateTime.now())
                    .build();
            this.save(cart);
            return;
        }
        Book book = bookService.getById(id);
        if (book == null) {
            throw new BaseException("图书不存在或已下架，无法加入购物车");
        }
        BigDecimal unit = book.getPrice();
        if (unit == null) {
            throw new BaseException("图书价格异常，无法加入购物车");
        }
        int base = cart1.getQuantity() != null ? cart1.getQuantity() : 0;
        cart1.setQuantity(base + n);
        cart1.setAmount(unit.multiply(BigDecimal.valueOf(cart1.getQuantity())));
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
