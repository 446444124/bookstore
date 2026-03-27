package com.PTU.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("carousel_banner")
public class CarouselBanner implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图片 URL
     */
    private String imageUrl;

    /**
     * 点击跳转路径（为空则不跳转）
     */
    private String linkPath;

    /**
     * 1=启用；0=停用
     */
    private Integer enabled;

    /**
     * 越小越靠前
     */
    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long updateBy;
}

