package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("employee")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    @TableId(type = IdType.AUTO)
    private Long employeeId;
    private String empNo;
    private String password;
    private String realName;
    private String position;
    private String email;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
