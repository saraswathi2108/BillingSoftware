package com.billing.software.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;  // Grocery, Dairy, Snacks, Personal Care...

    @Column(name = "default_hsn", length = 20)
    private String defaultHsn;

    @Column(name = "default_gst", precision = 5, scale = 2)
    private BigDecimal defaultGst;

    @Column(nullable = false)
    private Boolean active = true;

}
