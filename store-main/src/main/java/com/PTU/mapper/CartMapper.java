package com.PTU.mapper;

import com.PTU.entity.Cart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
    @Delete("delete from cart where user_id = #{currentId}")
    void deleteByUserId(Long currentId);
}
