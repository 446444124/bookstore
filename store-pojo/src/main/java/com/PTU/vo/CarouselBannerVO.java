package com.PTU.vo;

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
public class CarouselBannerVO implements Serializable {

    private Long id;

    private String imageUrl;

    private String linkPath;

    private Integer enabled;

    private Integer sort;

    private LocalDateTime updateTime;
}

