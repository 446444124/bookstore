package com.PTU.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CarouselBannerSaveDTO implements Serializable {

    private String imageUrl;

    /**
     * 可为空：为空则仅展示图片不跳转
     */
    private String linkPath;

    /**
     * 1=启用；0=停用
     */
    private Integer enabled;

    private Integer sort;
}

