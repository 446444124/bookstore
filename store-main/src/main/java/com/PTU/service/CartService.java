package com.PTU.service;

import com.PTU.entity.Cart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CartService extends IService<Cart> {

    void add(Long id, Integer num);

    List<Cart> showShoppingCart();

    void cleanShoppingCart();
}
