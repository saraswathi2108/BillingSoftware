package com.billing.software.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingResponseDTO {

    private String billNumber;
    private LocalDateTime billTime;
    private String paymentMethod;

    private BigDecimal subtotalAmount;
    private BigDecimal totalDiscount;
    private BigDecimal totalGst;
    private BigDecimal grandTotal;

    private List<BillItemResponseDTO> items;
}
