package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

// User实体类（学生）
@Data
@TableName("user")
public class User {
    private Long userId;
    private String studentId;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private int gender;
    private Integer status;
    // 省略其他...
}
