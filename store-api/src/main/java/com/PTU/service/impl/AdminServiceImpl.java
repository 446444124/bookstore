package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.constant.PasswordConstant;
import com.PTU.constant.StatusConstant;
import com.PTU.dto.AdminDTO;
import com.PTU.dto.AdminLoginDTO;
import com.PTU.dto.AdminPageQueryDTO;
import com.PTU.dto.CategoryPageQueryDTO;
import com.PTU.entity.Admin;
import com.PTU.entity.Category;
import com.PTU.exception.AccountLockedException;
import com.PTU.exception.AccountNotFoundException;
import com.PTU.exception.PasswordErrorException;
import com.PTU.mapper.AdminMapper;
import com.PTU.result.PageResult;
import com.PTU.service.AdminService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.time.LocalDateTime;


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

    /**
     * 新增员工
     * @param
     */
    public void save(AdminDTO adminDTO) {
        Admin admin = new Admin();
        //对象属性拷贝
        BeanUtils.copyProperties(adminDTO,admin);
        //设置账号状态，默认正常状态
        admin.setStatus(StatusConstant.ENABLE);
        //设置密码，默认密码123456
        admin.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置当前记录的创建时间和修改时间为当前时间
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        //设置创建人id和修改人id
        //employee.setCreateUser(BaseContext.getCurrentId());
        //employee.setUpdateUser(BaseContext.getCurrentId());
        this.save(admin);
    }

    //使用MyBatis-Plus内置分页
    public PageResult pageQuery(AdminPageQueryDTO adminPageQueryDTO) {
        // 创建MyBatis-Plus分页对象
        Page<Admin> page = new Page<>(adminPageQueryDTO.getPage(), adminPageQueryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(adminPageQueryDTO.getName() != null, Admin::getRealName, adminPageQueryDTO.getName());

        // 执行分页查询
        this.page(page, queryWrapper);

        // 构建返回结果
        return new PageResult(page.getTotal(), page.getRecords());
    }
}
