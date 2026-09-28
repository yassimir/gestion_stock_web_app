package com.gharnata.service.footer;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfFooterProdsEvent extends PdfPageEventHelper {
    public void onEndPage(PdfWriter writer, Document document) {
        Font policeGras = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
        Phrase footer = new Phrase(String.format("Page %d", writer.getPageNumber()),policeGras);
        Rectangle pageSize = document.getPageSize();
        PdfPTable table = new PdfPTable(1);
        PdfPCell cell = new PdfPCell(new Phrase(footer));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.NO_BORDER);
        table.setTotalWidth(pageSize.getWidth() - 40);
        table.addCell(cell);
        table.writeSelectedRows(0, -1, 30, 30, writer.getDirectContent());
    }
}
