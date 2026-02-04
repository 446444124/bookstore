package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.constant.StatusConstant;
import com.PTU.dto.CategoryDTO;
import com.PTU.dto.CategoryPageQueryDTO;
import com.PTU.entity.Category;
import com.PTU.exception.DeletionNotAllowedException;
import com.PTU.mapper.BookMapper;
import com.PTU.mapper.CategoryMapper;
import com.PTU.result.PageResult;
import com.PTU.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private BookMapper bookMapper;

    //使用MyBatis-Plus内置分页
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        // 创建MyBatis-Plus分页对象
        Page<Category> page = new Page<>(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(categoryPageQueryDTO.getName() != null, Category::getName, categoryPageQueryDTO.getName())
                    //仅展示启用的分类
                .eq(Category::getStatus, StatusConstant.ENABLE)

                .orderByAsc(Category::getSort);

        // 执行分页查询
        this.page(page, queryWrapper);

        // 构建返回结果
        return new PageResult(page.getTotal(), page.getRecords());
    }

}
