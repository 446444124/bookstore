package com.PTU.dto;

import lombok.Data;

/**
 * 忘记密码：验证码 + 新密码
 */
@Data
public class UserForgotPasswordResetDTO {
    private String studentId;
    private String email;
    private String code;
    private String newPassword;
}
