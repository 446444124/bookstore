package com.PTU.service;

import com.PTU.dto.MajorPageQueryDTO;
import com.PTU.entity.Major;
import com.PTU.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface MajorService extends IService<Major> {
    PageResult pageQuery(MajorPageQueryDTO majorPageQueryDTO);
}
