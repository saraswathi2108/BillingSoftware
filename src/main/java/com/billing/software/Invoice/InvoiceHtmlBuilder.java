package com.billing.software.Invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class InvoiceHtmlBuilder {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static String build(Map<String, Object> requestData) throws IOException {
        InputStream inputStream = InvoiceHtmlBuilder.class
                .getClassLoader()
                .getResourceAsStream("templates/invoice.html");

        if (inputStream == null) {
            throw new IOException("invoice.html NOT FOUND in resources/templates/");
        }


        byte[] bytes = inputStream.readAllBytes();
        String template = new String(bytes, StandardCharsets.UTF_8);


        template = cleanTemplate(template);


        System.out.println(template.substring(0, Math.min(100, template.length())));


        // Extract data
        String customerName = (String) requestData.get("customerName");
        String phoneNumber = (String) requestData.get("phoneNumber");
        String invoiceId = (String) requestData.get("invoiceId");
        LocalDateTime billingDate = (LocalDateTime) requestData.get("billingDate");

        List<Map<String, Object>> items = objectMapper.convertValue(
                requestData.get("items"),
                new TypeReference<List<Map<String, Object>>>() {}
        );


        BigDecimal subtotal = BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        BigDecimal totalDiscount = BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        BigDecimal totalGst = BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        BigDecimal grandTotal = BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);

        StringBuilder rows = new StringBuilder();

        for (Map<String, Object> item : items) {

            String name = (String) item.get("name");
            int quantity = ((Number) item.get("quantity")).intValue();


            BigDecimal unitPrice = toBigDecimal(item.get("unitPrice"));
            BigDecimal discountAmount = toBigDecimal(item.get("discountAmount"));
            BigDecimal gstPercent = toBigDecimal(item.get("gstPercent"));


            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity))
                    .setScale(SCALE, ROUNDING_MODE);


            BigDecimal priceAfterDiscount = itemTotal.subtract(discountAmount)
                    .setScale(SCALE, ROUNDING_MODE);


            BigDecimal gstAmount = priceAfterDiscount.multiply(gstPercent)
                    .divide(BigDecimal.valueOf(100), SCALE, ROUNDING_MODE);

            BigDecimal lineTotal = priceAfterDiscount.add(gstAmount)
                    .setScale(SCALE, ROUNDING_MODE);


            subtotal = subtotal.add(itemTotal);
            totalDiscount = totalDiscount.add(discountAmount);
            totalGst = totalGst.add(gstAmount);
            grandTotal = grandTotal.add(lineTotal);


            rows.append("<tr>")
                    .append("<td>").append(escapeXml(name)).append("</td>")
                    .append("<td>").append(quantity).append("</td>")
                    .append("<td>₹").append(formatCurrency(unitPrice)).append("</td>")
                    .append("<td>₹").append(formatCurrency(discountAmount)).append("</td>")
                    .append("<td>₹").append(formatCurrency(priceAfterDiscount)).append("</td>")
                    .append("<td>").append(formatPercent(gstPercent)).append("</td>")
                    .append("<td>₹").append(formatCurrency(gstAmount)).append("</td>")
                    .append("<td>₹").append(formatCurrency(lineTotal)).append("</td>")
                    .append("</tr>");
        }

        // Calculate amount saved (total discount)
        BigDecimal amountSaved = totalDiscount.setScale(SCALE, ROUNDING_MODE);

        // Format date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDate = billingDate.format(formatter);

        // Replace all placeholders
        String html = template
                .replace("{{customerName}}", escapeXml(customerName))
                .replace("{{phone}}", escapeXml(phoneNumber))
                .replace("{{invoiceId}}", escapeXml(invoiceId))
                .replace("{{billingDate}}", escapeXml(formattedDate))
                .replace("{{items}}", rows.toString())
                .replace("{{subtotal}}", "₹" + formatCurrency(subtotal))
                .replace("{{totalDiscount}}", "₹" + formatCurrency(totalDiscount))
                .replace("{{totalGst}}", "₹" + formatCurrency(totalGst))
                .replace("{{grandTotal}}", "₹" + formatCurrency(grandTotal))
                .replace("{{amountSaved}}", "₹" + formatCurrency(amountSaved))
                .replace("{{shopName}}", "DMart")
                .replace("{{shopAddress}}", "123 DSL Virtual Mall, Uppal Hyderabad, PIN: 500039")
                .replace("{{shopPhone}}", "+91-80-12345678")
                .replace("{{shopGST}}", "29AABCM1234M1Z5");

        return html;
    }

    private static String cleanTemplate(String template) {
        // Remove UTF-8 BOM if present
        if (template.startsWith("\uFEFF")) {
            template = template.substring(1);
        }

        // Remove any other invisible characters
        template = template.replace("\u0000", "");
        template = template.replace("\u200B", "");

        // Ensure it starts with proper DOCTYPE
        template = template.trim();

        // If it doesn't start with DOCTYPE or XML declaration, add default
        if (!template.startsWith("<!DOCTYPE") && !template.startsWith("<?xml")) {
            // Add XHTML DOCTYPE
            template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" +
                    template;
        }

        return template;
    }

    private static String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(SCALE, ROUNDING_MODE);
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue())
                    .setScale(SCALE, ROUNDING_MODE);
        }
        return new BigDecimal(value.toString())
                .setScale(SCALE, ROUNDING_MODE);
    }

    private static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(SCALE, ROUNDING_MODE).toPlainString();
    }

    private static String formatPercent(BigDecimal percent) {
        if (percent == null) {
            return "0.00";
        }
        return percent.setScale(SCALE, ROUNDING_MODE).toPlainString();
    }
}