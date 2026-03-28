package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.constant.StatusConstant;
import com.PTU.dto.UserChangePasswordDTO;
import com.PTU.entity.User;
import com.PTU.mapper.UserMapper;
import com.PTU.result.Result;
import com.PTU.service.UserPasswordChangeService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class UserPasswordChangeServiceImpl implements UserPasswordChangeService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result changePassword(Long userId, UserChangePasswordDTO dto) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        if (dto == null || !StringUtils.hasText(dto.getOldPassword()) || !StringUtils.hasText(dto.getNewPassword())) {
            return Result.error("请填写原密码与新密码");
        }
        if (dto.getNewPassword().length() < 6) {
            return Result.error("新密码至少 6 位");
        }
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            return Result.error("新密码不能与原密码相同");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (user.getStatus() != null && StatusConstant.DISABLE.equals(user.getStatus())) {
            return Result.error(MessageConstant.ACCOUNT_LOCKED);
        }

        String oldHash = DigestUtils.md5DigestAsHex(dto.getOldPassword().getBytes());
        if (!oldHash.equals(user.getPassword())) {
            return Result.error(MessageConstant.PASSWORD_ERROR);
        }

        String newHash = DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes());
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getUserId, userId)
                .set(User::getPassword, newHash));
        return Result.success();
    }
}
