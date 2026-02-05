package com.PTU.service.impl;

import com.PTU.dto.MajorPageQueryDTO;
import com.PTU.entity.Major;
import com.PTU.mapper.MajorMapper;
import com.PTU.result.PageResult;
import com.PTU.service.MajorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {

    //使用MyBatis-Plus内置分页
    public PageResult pageQuery(MajorPageQueryDTO majorPageQueryDTO) {
        // 创建MyBatis-Plus分页对象
        Page<Major> page = new Page<>(majorPageQueryDTO.getPage(), majorPageQueryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Major> queryWrapper = new LambdaQueryWrapper<>();

        // 执行分页查询
        this.page(page, queryWrapper);

        // 构建返回结果
        return new PageResult(page.getTotal(), page.getRecords());
    }
}
