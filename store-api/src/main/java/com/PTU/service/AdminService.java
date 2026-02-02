package com.PTU.service;

import com.PTU.dto.AdminDTO;
import com.PTU.dto.AdminLoginDTO;
import com.PTU.dto.AdminPageQueryDTO;
import com.PTU.entity.Admin;
import com.PTU.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AdminService extends IService<Admin> {
    Admin login(AdminLoginDTO adminLoginDTO);

    /**
     * 新增员工
     * @param
     */
    void save(AdminDTO adminDTO);
    /**
     * 管理员分页查询
     * @param adminPageQueryDTO
     * @return
     */
    PageResult pageQuery(AdminPageQueryDTO adminPageQueryDTO);

    void startOrStop(Integer status, Long id);
}
