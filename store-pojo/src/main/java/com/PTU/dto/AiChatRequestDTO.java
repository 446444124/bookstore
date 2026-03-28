package com.PTU.dto;

import lombok.Data;

@Data
public class AiChatRequestDTO {
    /** 用户输入，建议后端限制长度 */
    private String message;
}
