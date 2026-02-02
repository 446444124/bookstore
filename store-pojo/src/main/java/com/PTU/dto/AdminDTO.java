package com.PTU.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
@ApiModel(description = "管理员新增时传递的数据模型")
public class AdminDTO implements Serializable {

    private Long employeeId;
    private String empNo;
    private String password;
    private String realName;
    private String position;
    private String email;
    private String phone;
    private Integer status;
}
