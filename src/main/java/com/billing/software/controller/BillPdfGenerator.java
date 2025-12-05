package com.billing.software.controller;

import com.billing.software.dto.BillingResponseDTO;
import com.billing.software.dto.BillItemResponseDTO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;

public class BillPdfGenerator {

    public static byte[] generatePdf(BillingResponseDTO bill) {
        try {
            Rectangle pageSize = new Rectangle(226, 1000);
            Document doc = new Document(pageSize, 5, 5, 5, 5);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font bold = new Font(Font.FontFamily.COURIER, 9, Font.BOLD);
            Font normal = new Font(Font.FontFamily.COURIER, 8);
            Font small = new Font(Font.FontFamily.COURIER, 7);

            Paragraph title = new Paragraph(" MART\nAVENUE SUPERMARTS LTD\n", bold);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            doc.add(new Paragraph("---------------------------------------", normal));

            doc.add(new Paragraph("Bill No : " + bill.getBillNumber(), normal));
            doc.add(new Paragraph("Date    : " + bill.getBillTime(), normal));
            doc.add(new Paragraph("Payment : " + bill.getPaymentMethod(), normal));

            doc.add(new Paragraph("---------------------------------------", normal));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{40, 10, 20, 20});

            table.addCell(headerCell("Item"));
            table.addCell(headerCell("Qty"));
            table.addCell(headerCell("Rate"));
            table.addCell(headerCell("Amt"));

            for (BillItemResponseDTO item : bill.getItems()) {

                String itemName = item.getItemName();


                if (itemName.length() > 15) {
                    table.addCell(itemCell(itemName.substring(0, 15)));
                    table.addCell(itemCell(""));
                    table.addCell(itemCell(""));
                    table.addCell(itemCell(""));
                    itemName = itemName.substring(15);
                }

                table.addCell(itemCell(itemName));
                table.addCell(itemCell(String.valueOf(item.getQuantity())));
                table.addCell(itemCell(String.format("%.2f", item.getUnitPrice())));
                table.addCell(itemCell(String.format("%.2f", item.getLineTotal())));

                PdfPCell gstCell = new PdfPCell(new Phrase(
                        String.format("HSN: %s  GST@%s%%  GST: %.2f",
                                item.getHsnCode(),
                                item.getGstPercent().intValue(),
                                item.getGstAmount()
                        ),
                        small));
                gstCell.setColspan(4);
                gstCell.setBorder(PdfPCell.NO_BORDER);
                gstCell.setPaddingBottom(4);
                table.addCell(gstCell);
            }

            doc.add(table);

            doc.add(new Paragraph("---------------------------------------", normal));

            doc.add(new Paragraph(String.format("Subtotal : %.2f", bill.getSubtotalAmount()), normal));
            doc.add(new Paragraph(String.format("Discount : %.2f", bill.getTotalDiscount()), normal));
            doc.add(new Paragraph(String.format("GST Total: %.2f", bill.getTotalGst()), normal));

            doc.add(new Paragraph("---------------------------------------", normal));

            Paragraph total = new Paragraph(String.format("Grand Total: %.2f", bill.getGrandTotal()), bold);
            total.setAlignment(Element.ALIGN_CENTER);
            doc.add(total);

            doc.add(new Paragraph("---------------------------------------", normal));

            if (bill.getTotalDiscount().doubleValue() > 0) {
                Paragraph saved = new Paragraph(
                        String.format("* You Saved Rs %.2f *", bill.getTotalDiscount()), bold);
                saved.setAlignment(Element.ALIGN_CENTER);
                doc.add(saved);
            }

            Paragraph thanks = new Paragraph("THANK YOU FOR SHOPPING", bold);
            thanks.setAlignment(Element.ALIGN_CENTER);
            doc.add(thanks);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private static PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.COURIER, 8, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    private static PdfPCell itemCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.COURIER, 8)));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }
}
