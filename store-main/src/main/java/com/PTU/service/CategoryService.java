package com.PTU.service;

import com.PTU.dto.CategoryPageQueryDTO;
import com.PTU.entity.Category;
import com.PTU.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CategoryService extends IService<Category> {

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

}
