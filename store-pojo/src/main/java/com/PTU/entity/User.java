package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// User实体类（学生）
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;
    private String studentId;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private int gender;
    private Integer status;
    private String avatar;

}
