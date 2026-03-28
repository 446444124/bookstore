package com.PTU.utils;

/**
 * SMTP 异常转用户可读提示
 */
public final class MailSendFailureTips {

    private MailSendFailureTips() {
    }

    public static String userTip(Throwable e) {
        String all = joinThrowableMessages(e);
        if (all == null || all.isEmpty()) {
            return "验证码发送失败，请稍后重试";
        }
        String low = all.toLowerCase();
        if (all.contains("550")
                || (low.contains("non-existent") && low.contains("recipient"))
                || low.contains("invalid recipient")) {
            return "收件邮箱无法投递（服务商判定该地址无效或不存在）。请改用真实可收信的邮箱，并在个人资料中与找回密码时填写保持一致。";
        }
        if (low.contains("535") || low.contains("authentication failed") || low.contains("password not accepted")) {
            return "发信邮箱认证失败，请检查 SMTP 授权码与 spring.mail / application-local.yml 中的发件配置。";
        }
        return "验证码发送失败，请稍后重试";
    }

    private static String joinThrowableMessages(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t.getMessage() != null) {
                sb.append(t.getMessage()).append(' ');
            }
        }
        return sb.toString().trim();
    }
}
