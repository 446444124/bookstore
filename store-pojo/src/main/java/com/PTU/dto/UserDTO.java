package com.PTU.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// User实体类（学生）
@Data
@TableName("user")
public class UserDTO {
    private Long userId;
    private String studentId;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private int gender;
}
