package com.PTU.service.impl;

import com.PTU.constant.MessageConstant;
import com.PTU.dto.UserRegisterSendEmailCodeDTO;
import com.PTU.entity.User;
import com.PTU.mapper.UserMapper;
import com.PTU.result.Result;
import com.PTU.service.RegisterEmailCodeService;
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
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@Slf4j
public class RegisterEmailCodeServiceImpl implements RegisterEmailCodeService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w+.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    private static final String REDIS_CODE_PREFIX = "user:reg-email:code:";
    private static final String REDIS_RATE_PREFIX = "user:reg-email:rate:";
    private static final int CODE_TTL_SEC = 600;
    private static final int RATE_LIMIT_SEC = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired(required = false)
    private JavaMailSender javaMailSender;
    @Autowired
    private UserMapper userMapper;

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

    private boolean emailAlreadyRegistered(String normalizedEmail) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.apply("LOWER(TRIM(email)) = {0}", normalizedEmail);
        return userMapper.selectCount(qw) > 0;
    }

    @Override
    public Result sendRegisterEmailCode(UserRegisterSendEmailCodeDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getEmail())) {
            return Result.error("请填写邮箱");
        }
        String emailInput = dto.getEmail().trim();
        if (!EMAIL_PATTERN.matcher(emailInput).matches()) {
            return Result.error("邮箱格式不正确");
        }
        String normalized = normalizeEmail(emailInput);
        if (emailAlreadyRegistered(normalized)) {
            return Result.error(MessageConstant.EMAIL_EXIST);
        }

        String rateKey = REDIS_RATE_PREFIX + normalized;
        try {
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(rateKey))) {
                return Result.error("发送过于频繁，请 " + RATE_LIMIT_SEC + " 秒后再试");
            }
        } catch (DataAccessException e) {
            log.error("注册发码：Redis 不可用", e);
            return Result.error("验证码服务不可用，请确认 Redis 已启动");
        }

        String code = randomDigits(6);
        String redisVal = code + "|" + normalized;
        try {
            stringRedisTemplate.opsForValue().set(REDIS_CODE_PREFIX + normalized, redisVal, CODE_TTL_SEC, TimeUnit.SECONDS);
            stringRedisTemplate.opsForValue().set(rateKey, "1", RATE_LIMIT_SEC, TimeUnit.SECONDS);
        } catch (DataAccessException e) {
            log.error("注册发码：写入 Redis 失败", e);
            return Result.error("验证码服务不可用，请确认 Redis 已启动");
        }

        if (mailMock) {
            log.warn("[注册邮箱] 模拟邮件未真实发送。邮箱={} 验证码={}（{}分钟内有效）",
                    emailInput, code, CODE_TTL_SEC / 60);
            return Result.success(new ForgotPasswordSendCodeVO(true));
        }
        if (javaMailSender == null || !StringUtils.hasText(mailFrom)) {
            log.error("邮件未配置：请配置 spring.mail 与 store.mail.from，或开启 store.mail.mock=true");
            stringRedisTemplate.delete(REDIS_CODE_PREFIX + normalized);
            stringRedisTemplate.delete(rateKey);
            return Result.error("邮件服务未配置，无法发送验证码");
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(emailInput);
            message.setSubject("莆田学院校园书店 - 注册验证码");
            message.setText("您的注册验证码为：" + code + "，" + (CODE_TTL_SEC / 60) + " 分钟内有效。如非本人操作请忽略。");
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("发送注册验证码邮件失败，收件人={}", emailInput, e);
            stringRedisTemplate.delete(REDIS_CODE_PREFIX + normalized);
            stringRedisTemplate.delete(rateKey);
            return Result.error(MailSendFailureTips.userTip(e));
        }
        return Result.success();
    }

    @Override
    public String verifyAndConsumeRegisterEmailCode(String email, String code) {
        if (!StringUtils.hasText(email)) {
            return "请填写邮箱";
        }
        if (!StringUtils.hasText(code)) {
            return "请填写邮箱验证码";
        }
        String emailInput = email.trim();
        if (!EMAIL_PATTERN.matcher(emailInput).matches()) {
            return "邮箱格式不正确";
        }
        String normalized = normalizeEmail(emailInput);
        String redisKey = REDIS_CODE_PREFIX + normalized;
        String stored;
        try {
            stored = stringRedisTemplate.opsForValue().get(redisKey);
        } catch (DataAccessException e) {
            log.error("注册校验：读取 Redis 失败", e);
            return "验证码服务不可用，请确认 Redis 已启动";
        }
        if (!StringUtils.hasText(stored) || !stored.contains("|")) {
            return "邮箱验证码错误或已过期，请重新获取";
        }
        String[] parts = stored.split("\\|", 2);
        if (parts.length != 2) {
            return "邮箱验证码错误或已过期，请重新获取";
        }
        if (!parts[0].equals(code.trim()) || !parts[1].equals(normalized)) {
            return "邮箱验证码错误或已过期，请重新获取";
        }
        try {
            stringRedisTemplate.delete(redisKey);
        } catch (DataAccessException e) {
            log.warn("注册校验：删除 Redis 验证码失败", e);
        }
        return null;
    }
}
