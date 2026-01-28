package com.PTU.service;

import com.PTU.dto.AdminLoginDTO;
import com.PTU.entity.Admin;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AdminService extends IService<Admin> {
    Admin login(AdminLoginDTO adminLoginDTO);
}
