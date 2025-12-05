package com.billing.software.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "invoice", schema = "billing")
public class BillingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_id")
    private Long billingId;

    private String invoiceId;


    private String customerName;

    private String phoneNumber;

    private LocalDateTime billingDate;


    private BigDecimal subtotal;


    private BigDecimal totalDiscount ;


    private BigDecimal totalGst;


    private BigDecimal grandTotal ;


    private BigDecimal amountSaved ;

    // Store items as JSON string or remove from Entity

    private String itemsJson;

    // This will be called automatically before persisting
    @PrePersist
    public void onCreate() {
        if (billingDate == null) {
            billingDate = LocalDateTime.now();
        }
        if (invoiceId == null || invoiceId.isEmpty()) {
            invoiceId = "INV" + System.currentTimeMillis();
        }
    }
}