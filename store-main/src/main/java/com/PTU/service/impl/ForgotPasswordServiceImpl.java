package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.constant.StatusConstant;
import com.PTU.dto.UserForgotPasswordResetDTO;
import com.PTU.dto.UserForgotPasswordSendCodeDTO;
import com.PTU.entity.User;
import com.PTU.result.Result;
import com.PTU.service.ForgotPasswordService;
import com.PTU.service.UserService;
import com.PTU.utils.MailSendFailureTips;
import com.PTU.vo.ForgotPasswordSendCodeVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w+.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    private static final String REDIS_CODE_PREFIX = "user:pwd-reset:code:";
    private static final String REDIS_RATE_PREFIX = "user:pwd-reset:rate:";
    private static final int CODE_TTL_SEC = 600;
    private static final int RATE_LIMIT_SEC = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Value("${store.mail.mock:false}")
    private boolean mailMock;
    @Value("${store.mail.from:}")
    private String mailFrom;

    private static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    private static String randomDigits(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    @Override
    public Result sendResetCode(UserForgotPasswordSendCodeDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getStudentId()) || !StringUtils.hasText(dto.getEmail())) {
            return Result.error("请填写学号与邮箱");
        }
        String studentId = dto.getStudentId().trim();
        if (!studentId.matches("^\\d{12}$")) {
            return Result.error(MessageConstant.STUDENT_ID_ERROR);
        }
        String emailInput = dto.getEmail().trim();
        if (!EMAIL_PATTERN.matcher(emailInput).matches()) {
            return Result.error("邮箱格式不正确");
        }
        String rateKey = REDIS_RATE_PREFIX + studentId;
        try {
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(rateKey))) {
                return Result.error("发送过于频繁，请 " + RATE_LIMIT_SEC + " 秒后再试");
            }
        } catch (DataAccessException e) {
            log.error("忘记密码发码：Redis 不可用", e);
            return Result.error("验证码服务不可用，请确认 Redis 已启动（默认 127.0.0.1:6379，逻辑库见 store.redis.database）");
        }

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("student_id", studentId);
        User user = userService.getOne(qw);
        String normalized = normalizeEmail(emailInput);
        boolean ok = user != null
                && StringUtils.hasText(user.getEmail())
                && normalized.equals(normalizeEmail(user.getEmail()))
                && user.getStatus() != StatusConstant.DISABLE;

        if (!ok) {
            // 接口仍返回成功，防枚举；日志默认级别即可看见，便于自查
            String reason;
            if (user == null) {
                reason = "学号不存在";
            } else if (!StringUtils.hasText(user.getEmail())) {
                reason = "该账号未绑定邮箱（请登录后在个人资料中填写与找回时一致的邮箱）";
            } else if (StatusConstant.DISABLE.equals(user.getStatus())) {
                reason = "账号已禁用";
            } else {
                reason = "所填邮箱与账号绑定邮箱不一致（系统会忽略大小写与首尾空格后比对）";
            }
            log.warn("[忘记密码] 未生成验证码：{}。学号={}", reason, studentId);
            return Result.success();
        }

        String code = randomDigits(6);
        String redisVal = code + "|" + normalized;
        try {
            stringRedisTemplate.opsForValue().set(REDIS_CODE_PREFIX + studentId, redisVal, CODE_TTL_SEC, TimeUnit.SECONDS);
            stringRedisTemplate.opsForValue().set(rateKey, "1", RATE_LIMIT_SEC, TimeUnit.SECONDS);
        } catch (DataAccessException e) {
            log.error("忘记密码发码：写入 Redis 失败", e);
            return Result.error("验证码服务不可用，请确认 Redis 已启动（默认 127.0.0.1:6379，逻辑库见 store.redis.database）");
        }

        if (mailMock) {
            log.warn("[忘记密码] 当前为邮件模拟模式（store.mail.mock=true），未发送真实邮件。学号={} 邮箱={} 验证码={}（{}分钟内有效，仅用于本地开发）",
                    studentId, emailInput, code, CODE_TTL_SEC / 60);
            return Result.success(new ForgotPasswordSendCodeVO(true));
        }
        if (javaMailSender == null || !StringUtils.hasText(mailFrom)) {
            log.error("邮件未配置：请配置 spring.mail 与 store.mail.from，或开启 store.mail.mock=true");
            stringRedisTemplate.delete(REDIS_CODE_PREFIX + studentId);
            stringRedisTemplate.delete(rateKey);
            return Result.error("邮件服务未配置，无法发送验证码");
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(emailInput);
            message.setSubject("莆田学院校园书店 - 找回密码验证码");
            message.setText("您的验证码为：" + code + "，" + (CODE_TTL_SEC / 60) + " 分钟内有效。如非本人操作请忽略。");
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("发送找回密码邮件失败，收件人={}", emailInput, e);
            stringRedisTemplate.delete(REDIS_CODE_PREFIX + studentId);
            stringRedisTemplate.delete(rateKey);
            return Result.error(MailSendFailureTips.userTip(e));
        }
        return Result.success();
    }

    @Override
    public Result<Void> resetPasswordByEmail(UserForgotPasswordResetDTO dto) {
        if (dto == null
                || !StringUtils.hasText(dto.getStudentId())
                || !StringUtils.hasText(dto.getEmail())
                || !StringUtils.hasText(dto.getCode())
                || !StringUtils.hasText(dto.getNewPassword())) {
            return Result.error("请完整填写信息");
        }
        String studentId = dto.getStudentId().trim();
        if (!studentId.matches("^\\d{12}$")) {
            return Result.error(MessageConstant.STUDENT_ID_ERROR);
        }
        String emailInput = dto.getEmail().trim();
        if (!EMAIL_PATTERN.matcher(emailInput).matches()) {
            return Result.error("邮箱格式不正确");
        }
        if (dto.getNewPassword().length() < 6) {
            return Result.error("新密码至少 6 位");
        }

        String redisKey = REDIS_CODE_PREFIX + studentId;
        String stored;
        try {
            stored = stringRedisTemplate.opsForValue().get(redisKey);
        } catch (DataAccessException e) {
            log.error("忘记密码重置：读取 Redis 失败", e);
            return Result.error("验证码服务不可用，请确认 Redis 已启动");
        }
        if (!StringUtils.hasText(stored) || !stored.contains("|")) {
            return Result.error("验证码错误或已过期");
        }
        String[] parts = stored.split("\\|", 2);
        if (parts.length != 2) {
            return Result.error("验证码错误或已过期");
        }
        String expectCode = parts[0];
        String expectEmail = parts[1];
        if (!expectCode.equals(dto.getCode().trim()) || !expectEmail.equals(normalizeEmail(emailInput))) {
            return Result.error("验证码错误或已过期");
        }

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("student_id", studentId);
        User user = userService.getOne(qw);
        if (user == null || user.getStatus() == StatusConstant.DISABLE) {
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!normalizeEmail(emailInput).equals(normalizeEmail(user.getEmail()))) {
            return Result.error("验证码错误或已过期");
        }

        String hashed = DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes());
        userService.lambdaUpdate()
                .eq(User::getUserId, user.getUserId())
                .set(User::getPassword, hashed)
                .update();
        try {
            stringRedisTemplate.delete(redisKey);
        } catch (DataAccessException e) {
            log.warn("忘记密码重置：清除 Redis 验证码失败（密码已更新）", e);
        }
        return Result.success();
    }
}
