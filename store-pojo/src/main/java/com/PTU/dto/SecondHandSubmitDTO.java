package com.PTU.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SecondHandSubmitDTO implements Serializable {
    private Long bookId;
    private String userNote;
    /** 成色参考图 URL，最多 5 张 */
    private List<String> userConditionImages;
}
