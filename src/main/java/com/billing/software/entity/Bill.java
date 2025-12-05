package com.billing.software.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="bill_number", unique = true, nullable=false)
    private String billNumber;

    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private LocalDateTime billTime;

    @Column(precision = 10, scale = 2, nullable=false)
    private BigDecimal subtotalAmount=BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable=false)
    private BigDecimal totalDiscount=BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable=false)
    private BigDecimal totalGst=BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable=false)
    private BigDecimal grandTotal=BigDecimal.ZERO;

    @Column(nullable=false)
    private String paymentMethod;



    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<BillItem> items;

}
