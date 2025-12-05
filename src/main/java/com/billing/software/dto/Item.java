package com.billing.software.dto;


import lombok.Data;
import java.math.BigDecimal;


@Data
public class Item {
    private String name;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount ;
    private BigDecimal priceAfterDiscount;
    private BigDecimal gstPercent ;
    private BigDecimal gstAmount ;
    private BigDecimal lineTotal ;
}

