package com.PTU.service;

import com.PTU.dto.CategoryDTO;
import com.PTU.dto.CategoryPageQueryDTO;
import com.PTU.entity.Category;
import com.PTU.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CategoryService extends IService<Category> {
    /**
     * 新增分类
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);
    /**
     * 根据id删除分类
     * @param id
     */
    void deleteById(Long id);

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);
}
