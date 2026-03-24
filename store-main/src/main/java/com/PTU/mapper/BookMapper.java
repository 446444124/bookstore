package com.PTU.mapper;

import com.PTU.entity.Book;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
    @Update("UPDATE book SET stock = stock - #{quantity} WHERE id = #{bookId} AND stock >= #{quantity}")
    int deductStock(@Param("bookId") Long bookId, @Param("quantity") Integer quantity);
}
