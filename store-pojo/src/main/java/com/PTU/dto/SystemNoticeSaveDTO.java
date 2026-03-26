package com.PTU.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SystemNoticeSaveDTO implements Serializable {

    private String title;

    private String content;

    /**
     * 1=启用；0=停用
     */
    private Integer enabled;
}

