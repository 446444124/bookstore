package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.constant.StatusConstant;
import com.PTU.dto.AdminLoginDTO;
import com.PTU.entity.Admin;
import com.PTU.entity.User;
import com.PTU.exception.AccountLockedException;
import com.PTU.exception.AccountNotFoundException;
import com.PTU.exception.PasswordErrorException;
import com.PTU.mapper.AdminMapper;
import com.PTU.service.AdminService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


@Service
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    @Override
    public Admin login(AdminLoginDTO adminLoginDTO) {
        String empNo = adminLoginDTO.getEmpNo();
        String password = adminLoginDTO.getPassword();

        //1、根据工号查询数据库中的数据
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("emp_no", empNo);
        Admin admin = this.getOne(queryWrapper);

        //2、处理各种异常情况（工号不存在、密码不对、账号被锁定）
        if (admin == null) {
            //工号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对密码进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(admin.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (admin.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return admin;
    }
}
