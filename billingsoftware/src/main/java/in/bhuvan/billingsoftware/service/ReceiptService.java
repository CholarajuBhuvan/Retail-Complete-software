package in.bhuvan.billingsoftware.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import in.bhuvan.billingsoftware.domain.CustomerOrder;
import in.bhuvan.billingsoftware.domain.OrderItem;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ReceiptService {
    public byte[] generateReceipt(CustomerOrder order) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            doc.add(new Paragraph("Billing Software Receipt", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            doc.add(new Paragraph("Order ID: " + order.getId()));
            doc.add(new Paragraph("Customer: " + order.getCustomer().getName() + " (" + order.getCustomer().getPhone() + ")"));
            doc.add(new Paragraph("Date: " + order.getCreatedAt()));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Item");
            table.addCell("Qty");
            table.addCell("Unit Price");
            table.addCell("Line Total");

            for (OrderItem it : order.getItems()) {
                table.addCell(it.getProduct().getName());
                table.addCell(String.valueOf(it.getQuantity()));
                table.addCell(String.valueOf(it.getUnitPrice()));
                table.addCell(String.valueOf(it.getLineTotal()));
            }

            doc.add(table);
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Subtotal: " + order.getSubtotal()));
            doc.add(new Paragraph("Tax: " + order.getTax()));
            doc.add(new Paragraph("Total: " + order.getTotal(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate receipt", e);
        }
    }
}


