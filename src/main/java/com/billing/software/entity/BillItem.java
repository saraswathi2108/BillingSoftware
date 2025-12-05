package com.billing.software.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="bill_id", nullable=false)
    @JsonIgnore
    private Bill bill;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="product_id", nullable=false)
    private Product product;

    @Column(nullable=false)
    private Integer quantity;

    @Column(name="unit_price", nullable=false, precision = 10, scale = 2)
    private BigDecimal unitPrice=BigDecimal.ZERO;

    @Column(name="discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount=BigDecimal.ZERO;

    @Column(name="price_after_discount", precision = 10, scale = 2)
    private BigDecimal priceAfterDiscount=BigDecimal.ZERO;

    @Column(name="gst_percent", precision = 5, scale = 2)
    private BigDecimal gstPercent=BigDecimal.ZERO;

    @Column(name="gst_amount", precision = 10, scale = 2)
    private BigDecimal gstAmount=BigDecimal.ZERO;

    @Column(name="line_total", precision = 10, scale = 2)
    private BigDecimal lineTotal=BigDecimal.ZERO;

}



