package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ptu_major")
public class Major{
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String collegeId;
}
