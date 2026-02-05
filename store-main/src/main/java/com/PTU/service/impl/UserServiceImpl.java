package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.constant.StatusConstant;
import com.PTU.dto.UserDTO;
import com.PTU.dto.UserLoginDTO;
import com.PTU.entity.User;
import com.PTU.exception.AccountLockedException;
import com.PTU.exception.AccountNotFoundException;
import com.PTU.exception.PasswordErrorException;
import com.PTU.mapper.UserMapper;
import com.PTU.result.Result;
import com.PTU.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String studentId = userLoginDTO.getStudentId();
        String password = userLoginDTO.getPassword();

        //1、根据学号查询数据库中的数据
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        User user = this.getOne(queryWrapper);

        //2、处理各种异常情况（学号不存在、密码不对、账号被锁定）
        if (user == null) {
            //学号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对密码进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (user.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return user;

    }

    @Override
    public Result register(UserDTO tuser) {
        //学号需为12位纯数字
        if (!tuser.getStudentId().matches("^\\d{12}$")) {
            //学号格式错误
            return Result.error(MessageConstant.STUDENT_ID_ERROR);
        }
        //1.判断学号是否已存在
        String studentId = tuser.getStudentId();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        User user = this.getOne(queryWrapper);
        if (user != null) {
            //学号已存在
            return Result.error(MessageConstant.ALREADY_EXIST);
//            throw new AccountNotFoundException(MessageConstant.ALREADY_EXIST);
        }
        //校验手机号合法性(11位纯数字)
        if (!user.getPhone().matches("^\\d{11}$")) {
            return Result.error("手机号格式错误");
        }
        //2.判断手机号是否已存在
        String phone = tuser.getPhone();
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        user = this.getOne(queryWrapper);
        if (user != null) {
            //手机号已存在
//            throw new AccountNotFoundException(MessageConstant.PHONE_EXIST);
            return Result.error(MessageConstant.PHONE_EXIST);
        }
        //3.密码 加密存储
        tuser.setPassword(DigestUtils.md5DigestAsHex(tuser.getPassword().getBytes()));
        //对象属性拷贝
        User newuser = new User();
        BeanUtils.copyProperties(tuser,newuser);
        //4.注册用户
        this.save(newuser);
        return Result.success();
    }
}
