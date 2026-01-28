package com.PTU.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
@ApiModel(description = "用户登录时传递的数据模型")
public class UserLoginDTO implements Serializable {

    @ApiModelProperty("学号")
    private String studentId;
    @ApiModelProperty("密码")
    private String password;
}
