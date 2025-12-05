package com.billing.software.dto;

import lombok.Data;
import java.util.List;

@Data
public class BillingRequestDTO {

    private String paymentMethod;

    private List<BillingItemRequest> items;
}
