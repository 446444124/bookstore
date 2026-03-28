package com.PTU.dto;

import lombok.Data;

/**
 * 登录用户修改密码（校验原密码，无需邮箱验证码）
 */
@Data
public class UserChangePasswordDTO {
    private String oldPassword;
    private String newPassword;
}
