package com.billing.software.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String barcode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price=BigDecimal.ZERO;

    private String hsnCode;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private BigDecimal discountValue=BigDecimal.ZERO;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal gstPercent=BigDecimal.ZERO;

    private Integer stockQty;

    @Column(nullable = false)
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
