package com.billing.software.service;


import com.billing.software.dto.*;
import com.billing.software.entity.Bill;
import com.billing.software.entity.BillItem;
import com.billing.software.entity.Product;
import com.billing.software.repository.BillItemRepository;
import com.billing.software.repository.BillRepository;
import com.billing.software.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {

    private final ProductRepository productRepo;
    private final BillRepository billRepo;
    private final BillItemRepository billItemRepo;

    public com.billing.software.dto.BillingResponseDTO createBill(BillingRequestDTO request) {

        Map<String, Integer> mergedItems = new HashMap<>();
        for (BillingItemRequest item : request.getItems()) {
            mergedItems.merge(item.getBarcode(), item.getQuantity(), Integer::sum);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;

        // Create Bill
        Bill bill = new Bill();
        bill.setBillNumber("BILL-" + System.currentTimeMillis());
        bill.setPaymentMethod(request.getPaymentMethod());
        bill.setBillTime(LocalDateTime.now());
        billRepo.save(bill);

        List<BillItemResponseDTO> responseItems = new ArrayList<>();

        for (var entry : mergedItems.entrySet()) {

            String barcode = entry.getKey();
            int quantity = entry.getValue();

            Product product = productRepo.findByBarcode(barcode)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + barcode));

            if (product.getStockQty() < quantity)
                throw new RuntimeException("Not enough stock for product: " + product.getName());

            BigDecimal unitPrice = product.getPrice();

            BigDecimal discountAmount = BigDecimal.ZERO;

            if (product.getDiscountType() != null && product.getDiscountValue() != null) {

                switch (product.getDiscountType()) {

                    case PERCENTAGE:
                        discountAmount = unitPrice
                                .multiply(product.getDiscountValue())
                                .divide(BigDecimal.valueOf(100));
                        break;

                    case FLAT:
                        discountAmount = product.getDiscountValue();
                        break;

                    case NONE:
                    default:
                        discountAmount = BigDecimal.ZERO;
                }
            }

            BigDecimal priceAfterDiscount = unitPrice.subtract(discountAmount);

            BigDecimal gstAmount = priceAfterDiscount
                    .multiply(product.getGstPercent())
                    .divide(BigDecimal.valueOf(100));

            BigDecimal lineTotal = (priceAfterDiscount.add(gstAmount))
                    .multiply(BigDecimal.valueOf(quantity));

            subtotal = subtotal.add(priceAfterDiscount.multiply(BigDecimal.valueOf(quantity)));
            totalDiscount = totalDiscount.add(discountAmount.multiply(BigDecimal.valueOf(quantity)));
            totalGst = totalGst.add(gstAmount.multiply(BigDecimal.valueOf(quantity)));

            product.setStockQty(product.getStockQty() - quantity);
            if (product.getStockQty() == 0) {
                product.setActive(false);
            }
            productRepo.save(product);

            BillItem billItem = new BillItem();
            billItem.setBill(bill);
            billItem.setProduct(product);
            billItem.setQuantity(quantity);
            billItem.setUnitPrice(unitPrice);
            billItem.setDiscountAmount(discountAmount);
            billItem.setPriceAfterDiscount(priceAfterDiscount);
            billItem.setGstAmount(gstAmount);
            billItem.setLineTotal(lineTotal);

            billItemRepo.save(billItem);

            responseItems.add(
                    BillItemResponseDTO.builder()
                            .itemName(product.getName())
                            .hsnCode(product.getHsnCode())
                            .quantity(quantity)
                            .unitPrice(unitPrice)
                            .discountAmount(discountAmount)
                            .priceAfterDiscount(priceAfterDiscount)
                            .gstPercent(product.getGstPercent())
                            .gstAmount(gstAmount)
                            .lineTotal(lineTotal)
                            .build()
            );
        }

        BigDecimal grandTotal = subtotal.add(totalGst);

        return com.billing.software.dto.BillingResponseDTO.builder()
                .billNumber(bill.getBillNumber())
                .billTime(LocalDateTime.now())
                .paymentMethod(request.getPaymentMethod())
                .subtotalAmount(subtotal)
                .totalDiscount(totalDiscount)
                .totalGst(totalGst)
                .grandTotal(grandTotal)
                .items(responseItems)
                .build();
    }


//    public BillingResponseDTO getByBillNumber(String billNumber) {
//    }
}
