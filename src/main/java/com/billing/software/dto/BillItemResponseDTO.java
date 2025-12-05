package com.billing.software.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItemResponseDTO {

    private String itemName;
    private String hsnCode;

    private Integer quantity;
    private BigDecimal unitPrice;

    private BigDecimal discountAmount;
    private BigDecimal priceAfterDiscount;

    private BigDecimal gstPercent;
    private BigDecimal gstAmount;

    private BigDecimal lineTotal;
}
