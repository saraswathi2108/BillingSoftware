package com.billing.software.dto;

import lombok.Data;

@Data
public class BillingItemRequest {
    private String barcode;
    private Integer quantity;
}
