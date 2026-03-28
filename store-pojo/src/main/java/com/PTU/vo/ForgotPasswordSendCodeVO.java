package com.PTU.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 忘记密码发码结果：开发环境下 mock 不发真实邮件时告知前端
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordSendCodeVO {
    /** 为 true 表示未走真实 SMTP，验证码在后端日志中 */
    private boolean devMailMock;
}
