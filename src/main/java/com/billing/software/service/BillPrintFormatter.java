package com.billing.software.service;



import com.billing.software.dto.BillingResponseDTO;
import com.billing.software.dto.BillItemResponseDTO;

public class BillPrintFormatter {

    public static String formatBill(BillingResponseDTO bill) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Bill No : %-20s\n", bill.getBillNumber()));
        sb.append(String.format("Date    : %-20s\n", bill.getBillTime()));
        sb.append(String.format("Payment : %-20s\n", bill.getPaymentMethod()));
        sb.append("------------------------------------------------\n");

        sb.append(String.format("%-20s %5s %8s %10s\n", "Item", "Qty", "Rate", "Amount"));
        sb.append("------------------------------------------------\n");

        for (BillItemResponseDTO item : bill.getItems()) {

            String name = item.getItemName();

            if (name.length() > 20) {
                sb.append(name.substring(0, 20)).append("\n");
                name = name.substring(20);
            }

            sb.append(String.format(
                    "%-20s %5d %8.2f %10.2f\n",
                    name,
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineTotal()
            ));

            sb.append(String.format(
                    "HSN: %-10s GST @ %-3s%%  GST: %.2f\n",
                    item.getHsnCode(),
                    item.getGstPercent().intValue(),
                    item.getGstAmount()
            ));
        }

        sb.append("------------------------------------------------\n");

        sb.append(String.format("%-25s %15.2f\n", "Subtotal", bill.getSubtotalAmount()));
        sb.append(String.format("%-25s %15.2f\n", "Discount", bill.getTotalDiscount()));
        sb.append(String.format("%-25s %15.2f\n", "GST Total", bill.getTotalGst()));
        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-25s %15.2f\n", "Grand Total", bill.getGrandTotal()));
        sb.append("------------------------------------------------\n");

        // ⭐ SHOW SAVINGS SECTION IF DISCOUNT > 0
        if (bill.getTotalDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append(String.format(
                    "* You Saved Rs. %.2f on MRP *\n",
                    bill.getTotalDiscount()
            ));
        }

        return sb.toString();
    }
}
