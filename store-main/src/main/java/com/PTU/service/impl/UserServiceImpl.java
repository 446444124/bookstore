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
import com.PTU.service.RegisterEmailCodeService;
import com.PTU.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Pattern REGISTER_EMAIL_PATTERN =
            Pattern.compile("^[\\w+.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    @Autowired
    private RegisterEmailCodeService registerEmailCodeService;

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
        if (!tuser.getPhone().matches("^\\d{11}$")) {
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
        String emailRaw = tuser.getEmail();
        if (!StringUtils.hasText(emailRaw) || !REGISTER_EMAIL_PATTERN.matcher(emailRaw.trim()).matches()) {
            return Result.error("邮箱格式不正确");
        }
        queryWrapper = new QueryWrapper<>();
        queryWrapper.apply("LOWER(TRIM(email)) = {0}", emailRaw.trim().toLowerCase());
        user = this.getOne(queryWrapper);
        if (user != null) {
            return Result.error(MessageConstant.EMAIL_EXIST);
        }
        String codeErr = registerEmailCodeService.verifyAndConsumeRegisterEmailCode(emailRaw, tuser.getEmailCode());
        if (codeErr != null) {
            return Result.error(codeErr);
        }
        //3.密码 加密存储
        tuser.setPassword(DigestUtils.md5DigestAsHex(tuser.getPassword().getBytes()));
        //对象属性拷贝
        User newuser = new User();
        BeanUtils.copyProperties(tuser,newuser);
        newuser.setWalletBalance(BigDecimal.ZERO);
        //4.注册用户
        this.save(newuser);
        return Result.success();
    }

    @Override
    public BigDecimal getWalletBalance(Long userId) {
        if (userId == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal balance = this.baseMapper.getWalletBalance(userId);
        return balance == null ? BigDecimal.ZERO : balance;
    }

    @Override
    public void addWalletBalance(Long userId, BigDecimal amount) {
        if (userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        this.baseMapper.addWalletBalance(userId, amount);
    }
}
