package com.PTU.mapper;

import com.PTU.entity.Book;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
    /**
     * 根据分类ID统计品书数量
     *
     * @param categoryId 分类ID
     * @return 品书数量
     */
    @Select("SELECT COUNT(*) FROM book WHERE category_id = #{categoryId}")
    int countByCategoryId(Long categoryId);

    @Update("UPDATE book SET stock = stock + #{quantity} WHERE id = #{bookId}")
    int addStock(@Param("bookId") Long bookId, @Param("quantity") Integer quantity);
}
