package com.PTU.dto;

import lombok.Data;

/**
 * 忘记密码：请求发送邮箱验证码
 */
@Data
public class UserForgotPasswordSendCodeDTO {
    private String studentId;
    private String email;
}
