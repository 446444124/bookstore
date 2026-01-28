package com.PTU.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
@ApiModel(description = "管理员登录时传递的数据模型")
public class AdminLoginDTO implements Serializable {

    @ApiModelProperty("工号")
    private String empNo;
    @ApiModelProperty("密码")
    private String password;
}
