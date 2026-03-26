package com.PTU.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SecondHandOrderSubmitDTO implements Serializable {
    private Long listingId;

    private Long addressBookId;
    private int payWay;
    private String remark;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;
    private Integer deliveryStatus;
    private Integer deliveryWay;
}
