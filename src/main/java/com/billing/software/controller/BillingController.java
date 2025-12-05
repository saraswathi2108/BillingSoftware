package com.billing.software.controller;



import com.billing.software.dto.BillingRequestDTO;
import com.billing.software.dto.BillingResponseDTO;
import com.billing.software.service.BillPrintFormatter;
import com.billing.software.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/createBill")
    public ResponseEntity<BillingResponseDTO> createBill(
            @RequestBody BillingRequestDTO request) {
        return ResponseEntity.ok(billingService.createBill(request));
    }



    @PostMapping("/create-bill/print")
    public ResponseEntity<String> printBill(@RequestBody BillingRequestDTO request) {

        BillingResponseDTO bill = billingService.createBill(request);

        String printText = BillPrintFormatter.formatBill(bill);

        return ResponseEntity.ok(printText);
    }

    @PostMapping("/bill/pdf")
    public ResponseEntity<byte[]> generateBillPdf(@RequestBody BillingRequestDTO request) {

        BillingResponseDTO bill = billingService.createBill(request);

        byte[] pdf = BillPdfGenerator.generatePdf(bill);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=bill.pdf")
                .body(pdf);
    }




}

