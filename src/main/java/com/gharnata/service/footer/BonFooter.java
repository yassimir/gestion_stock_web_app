package com.gharnata.service.footer;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class BonFooter extends PdfPageEventHelper {
    public void onEndPage(PdfWriter writer, Document document) {
        Font policeGras = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
        Phrase footer = new Phrase(String.format("Page %d", writer.getPageNumber()),policeGras);
        Rectangle pageSize = document.getPageSize();
        PdfPTable table = new PdfPTable(2);
        try {
            table.setWidths(new float[]{70,10});
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        PdfPCell cell1 = new PdfPCell(new Phrase("ROUTE MEDIOUNA KM 11,5 HEFAYA AIN CHOCK CASABLANCA ", policeGras));
        PdfPCell cell2 = new PdfPCell(new Phrase(footer));
        cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell1.setBorder(Rectangle.NO_BORDER);
        cell2.setBorder(Rectangle.NO_BORDER);
        table.setTotalWidth(pageSize.getWidth() - 20);
        table.addCell(cell1);
        table.addCell(cell2);
        table.writeSelectedRows(0, -1, 30, 30, writer.getDirectContent());
    }
}
