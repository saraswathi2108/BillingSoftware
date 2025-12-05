package com.billing.software.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GstDTO {
    private String hsnCode;
    private BigDecimal gstPercent;
}
