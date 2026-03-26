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
public class SystemNoticeVO implements Serializable {

    private Integer id;

    private String title;

    private String content;

    private Integer enabled;

    private LocalDateTime updateTime;
}

