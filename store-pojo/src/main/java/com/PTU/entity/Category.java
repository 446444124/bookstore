package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("category")
public class Category implements Serializable {
    @TableId(type = IdType.AUTO)
    //主键
    private Long id;

    //分类名称
    private String name;

    //排序
    private Integer sort;

    //状态 0:禁用 1:正常
    private Integer status;

}
