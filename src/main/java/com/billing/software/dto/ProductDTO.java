package com.billing.software.dto;



import com.billing.software.entity.DiscountType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private String id;
    private String name;
    private String barcode;
    private BigDecimal price;
    private String hsnCode;
    private DiscountType discountType;
    private BigDecimal discountValue;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal gstPercent;
    private Integer stockQty;
    private Boolean active;
    private Long categoryId;
}
