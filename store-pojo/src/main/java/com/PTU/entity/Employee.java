package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("employee")
public class Employee {
    private Long employeeId;
    private String empNo;
    private String username;
    private String password;
    private String realName;
    private String department;
    private String position;
    private String email;
    private String phone;
    private Integer status;
    // 省略其他...
}
