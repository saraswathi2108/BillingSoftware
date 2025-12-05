package com.billing.software.controller;


import com.billing.software.entity.BillingRequest;
import com.billing.software.Invoice.InvoiceHtmlBuilder;
import com.billing.software.service.FileStorageService;
import com.billing.software.service.PdfGeneratorService;
import com.billing.software.service.WhatsAppService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@Slf4j
public class BillController {

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private WhatsAppService whatsAppService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/generate")
    public Map<String, Object> generateInvoice(@RequestBody Map<String, Object> requestData) throws Exception {

        Map<String, Object> response = new HashMap<>();

        try {
            // Extract data from request
            String customerName = (String) requestData.get("customerName");
            String phoneNumber = (String) requestData.get("phoneNumber");
            Object itemsObj = requestData.get("items");

            // Auto-generate invoice ID
            String invoiceId = "INV" + System.currentTimeMillis();

            log.info("Generating invoice for: {}", customerName);
            log.info("Invoice ID: {}", invoiceId);

            // Create BillingRequest object
            BillingRequest request = new BillingRequest();
            request.setInvoiceId(invoiceId);
            request.setCustomerName(customerName);
            request.setPhoneNumber(phoneNumber);
            request.onCreate(); // Set billing date

            // Convert items to JSON string
            String itemsJson = objectMapper.writeValueAsString(itemsObj);
            request.setItemsJson(itemsJson);

            // 1. Generate HTML with all calculations
            // We need to pass items separately
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("customerName", customerName);
            templateData.put("phoneNumber", phoneNumber);
            templateData.put("invoiceId", invoiceId);
            templateData.put("items", itemsObj);
            templateData.put("billingDate", request.getBillingDate());

            String html = InvoiceHtmlBuilder.build(templateData);

            // 2. Generate PDF
            byte[] pdf = pdfGeneratorService.generatePdfFromHtml(html);

            // 3. Save file
            String fileName = "invoice_" + invoiceId + ".pdf";
            fileStorageService.saveInvoice(pdf, fileName);

            // 4. Create public URL
            String publicUrl;
            if (publicBaseUrl != null && !publicBaseUrl.isEmpty() && !publicBaseUrl.contains("localhost")) {
                publicUrl = publicBaseUrl.trim() + "/api/billing/" + fileName;
                log.info("Invoice Link: {}", publicUrl);
            } else {
                // For local testing - you'll need ngrok for WhatsApp to work
                publicUrl = "http://localhost:8080/api/billing/" + fileName;
                log.warn("Using localhost URL - WhatsApp won't be able to access this!");
            }

            // 5. Send WhatsApp with CLICKABLE LINK
            whatsAppService.sendInvoiceLink(
                    phoneNumber,
                    publicUrl,
                    invoiceId,
                    customerName
            );

            // Calculate totals from the HTML builder (you might need to return these)
            response.put("billingId", request.getBillingId());
            response.put("invoiceId", invoiceId);
            response.put("billingDate", request.getBillingDate());
            response.put("customerName", customerName);
            response.put("phoneNumber", phoneNumber);
            response.put("status", "success");
            response.put("message", "Invoice generated and download link sent via WhatsApp");
            response.put("downloadLink", publicUrl);

        } catch (Exception e) {
            log.error("Error generating invoice: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to generate invoice: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/debug-template")
    public Map<String, Object> debugTemplate() {
        Map<String, Object> response = new HashMap<>();

        try {
            InputStream inputStream = InvoiceHtmlBuilder.class
                    .getClassLoader()
                    .getResourceAsStream("templates/invoice.html");

            if (inputStream == null) {
                response.put("status", "error");
                response.put("message", "Template file not found");
                return response;
            }

            byte[] bytes = inputStream.readAllBytes();
            String template = new String(bytes, StandardCharsets.UTF_8);

            // Analyze first 10 characters
            StringBuilder analysis = new StringBuilder();
            for (int i = 0; i < Math.min(10, template.length()); i++) {
                char c = template.charAt(i);
                analysis.append("Position ").append(i).append(": '");

                if (c == '\n') analysis.append("\\n");
                else if (c == '\r') analysis.append("\\r");
                else if (c == '\t') analysis.append("\\t");
                else if (c == ' ') analysis.append("[space]");
                else if (c == '\uFEFF') analysis.append("[BOM]");
                else if (c == '\u0000') analysis.append("[NULL]");
                else analysis.append(c);

                analysis.append("' (Unicode: ").append((int)c).append(")\n");
            }

            response.put("status", "success");
            response.put("template_length", template.length());
            response.put("first_200_chars", template.substring(0, Math.min(200, template.length())));
            response.put("character_analysis", analysis.toString());
            response.put("has_bom", template.startsWith("\uFEFF"));
            response.put("starts_with_doctype", template.trim().startsWith("<!DOCTYPE"));

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> getInvoiceFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("invoices").resolve(fileName);

            if (!Files.exists(filePath)) {
                log.warn("File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            headers.set("Access-Control-Allow-Origin", "*");
            headers.set("Cache-Control", "no-cache, no-store, must-revalidate");

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error serving file {}: {}", fileName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}