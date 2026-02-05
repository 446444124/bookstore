package com.PTU.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

// User实体类（学生）
@Data
@ApiModel(description = "用户注册传递的数据模型")
public class UserDTO {
    private Long userId;
    private String studentId;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private int gender;
    private Long majorId;
}
