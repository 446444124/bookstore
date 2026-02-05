package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 地址簿
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressBook implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;

    //用户id
    private Long userId;

    //收货人
    private String consignee;

    //手机号
    private String phone;

    //性别 0 女 1 男
    private String sex;

    //省级名称
    private String provinceName;

    //市级名称
    private String cityName;

    //区级名称
    private String districtName;

    //标签
    private String label;

    //是否默认 0否 1是
    private Integer isDefault;

    //学校分区
    private String schoolPartition;
    //宿舍楼
    private String building;
    //宿舍号
    private String houseNumber;
}
