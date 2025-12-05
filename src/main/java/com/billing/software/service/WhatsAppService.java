package com.billing.software.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WhatsAppService {

    @Value("${whatsapp.access-token}")
    private String token;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    private final RestTemplate restTemplate;

    public WhatsAppService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Method 1: Send text message with clickable link
    public void sendInvoiceLink(String toPhoneNumber, String pdfUrl, String invoiceId, String customerName) {
        String cleanPhone = toPhoneNumber.replaceAll("[^0-9]", "");

        String url = "https://graph.facebook.com/v20.0/" + phoneNumberId + "/messages";

        String message = String.format(
                "📄 *Invoice Ready*\n\n" +
                        "Hello %s,\n\n" +
                        "Your invoice *#%s* is ready!\n\n" +
                        "Click to view/download:\n" +
                        "%s\n\n" +
                        "Thank you for shopping with us!\n" +
                        "DMart Superstore",
                customerName, invoiceId, pdfUrl
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", cleanPhone);
        payload.put("type", "text");

        Map<String, Object> text = new HashMap<>();
        text.put("preview_url", true);
        text.put("body", message);

        payload.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("Invoice link sent: {}", response.getBody());
        } catch (Exception e) {
            log.error("Failed to send link: {}", e.getMessage());
            throw new RuntimeException("WhatsApp link send failed: " + e.getMessage());
        }
    }


    public void sendInvoiceTemplate(String toPhoneNumber, String pdfUrl, String invoiceId) {
        String cleanPhone = toPhoneNumber.replaceAll("[^0-9]", "");

        String url = "https://graph.facebook.com/v20.0/" + phoneNumberId + "/messages";

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", cleanPhone);
        payload.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", "invoice_ready");


        List<Map<String, Object>> components = new ArrayList<>();


        Map<String, Object> body = new HashMap<>();
        body.put("type", "body");

        List<Map<String, String>> parameters = new ArrayList<>();
        parameters.add(Map.of("type", "text", "text", invoiceId));
        parameters.add(Map.of("type", "text", "text", pdfUrl));

        body.put("parameters", parameters);
        components.add(body);

        template.put("components", components);
        template.put("language", Map.of("code", "en_US"));

        payload.put("template", template);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("Template sent: {}", response.getBody());
        } catch (Exception e) {
            log.error("Template failed: {}", e.getMessage());
            throw new RuntimeException("WhatsApp template failed: " + e.getMessage());
        }
    }
}
