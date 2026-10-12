package clinicabalbarrey;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class ActividadClinicaPdfExporter {
    private static final BaseColor VERDE = new BaseColor(0, 105, 92);
    private static final BaseColor GRIS = new BaseColor(242, 245, 247);

    private ActividadClinicaPdfExporter() { }

    public static void exportar(File destino, Date desde, Date hasta, List<String[]> registros,
                                boolean incluirMedico) throws Exception {
        Document documento = new Document(PageSize.A4.rotate(), 24, 24, 36, 32);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(destino));
        writer.setPageEvent(new PiePagina());
        documento.open();

        Font tituloFont = new Font(Font.FontFamily.HELVETICA, 17, Font.BOLD, VERDE);
        Font detalleFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);
        Font cabeceraFont = new Font(Font.FontFamily.HELVETICA, 7.5f, Font.BOLD, BaseColor.WHITE);
        Font cuerpoFont = new Font(Font.FontFamily.HELVETICA, 7.2f, Font.NORMAL, BaseColor.DARK_GRAY);

        Paragraph titulo = new Paragraph("ACTIVIDAD CLINICA RECIENTE", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(5);
        documento.add(titulo);

        SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy");
        Paragraph periodo = new Paragraph("Periodo: " + formatoFecha(desde, fecha) + " al "
                + formatoFecha(hasta, fecha) + "    |    Registros: " + registros.size(), detalleFont);
        periodo.setAlignment(Element.ALIGN_CENTER);
        periodo.setSpacingAfter(10);
        documento.add(periodo);

        int columnas = incluirMedico ? 6 : 5;
        PdfPTable tabla = new PdfPTable(columnas);
        tabla.setWidthPercentage(100);
        tabla.setWidths(incluirMedico
                ? new float[]{9, 15, 15, 16, 22.5f, 22.5f}
                : new float[]{10, 18, 18, 27, 27});
        tabla.setHeaderRows(1);
        tabla.setSplitRows(true);
        tabla.setSplitLate(false);
        cabecera(tabla, "FECHA", cabeceraFont);
        cabecera(tabla, "PACIENTE", cabeceraFont);
        if (incluirMedico) cabecera(tabla, "MEDICO TRATANTE", cabeceraFont);
        cabecera(tabla, "OBRA SOCIAL", cabeceraFont);
        cabecera(tabla, "RESUMEN DE DIAGNOSTICOS", cabeceraFont);
        cabecera(tabla, "RESUMEN DE TRATAMIENTOS", cabeceraFont);

        if (registros.isEmpty()) {
            PdfPCell vacia = new PdfPCell(new Phrase("No hay actividad para el periodo seleccionado.", detalleFont));
            vacia.setColspan(columnas);
            vacia.setHorizontalAlignment(Element.ALIGN_CENTER);
            vacia.setPadding(12);
            tabla.addCell(vacia);
        } else {
            for (int i = 0; i < registros.size(); i++) {
                String[] fila = registros.get(i);
                BaseColor fondo = i % 2 == 0 ? BaseColor.WHITE : GRIS;
                dato(tabla, valor(fila, 0, "-"), cuerpoFont, fondo);
                dato(tabla, valor(fila, 1, "No informado"), cuerpoFont, fondo);
                if (incluirMedico) dato(tabla, valor(fila, 2, "No informado"), cuerpoFont, fondo);
                dato(tabla, valor(fila, 6, "Particular / Sin Obra Social"), cuerpoFont, fondo);
                dato(tabla, limpiar(valor(fila, 3, "No registrado")), cuerpoFont, fondo);
                dato(tabla, limpiar(valor(fila, 4, "No registrado")), cuerpoFont, fondo);
            }
        }
        documento.add(tabla);
        documento.close();
    }

    private static String formatoFecha(Date valor, SimpleDateFormat formato) {
        return valor == null ? "Sin limite" : formato.format(valor);
    }

    private static void cabecera(PdfPTable tabla, String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBackgroundColor(VERDE);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(6);
        tabla.addCell(celda);
    }

    private static void dato(PdfPTable tabla, String texto, Font font, BaseColor fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBackgroundColor(fondo);
        celda.setVerticalAlignment(Element.ALIGN_TOP);
        celda.setPadding(4);
        celda.setBorderColor(new BaseColor(215, 220, 225));
        tabla.addCell(celda);
    }

    private static String valor(String[] fila, int indice, String defecto) {
        if (fila == null || indice >= fila.length || fila[indice] == null || fila[indice].trim().isEmpty()) return defecto;
        return fila[indice].trim();
    }

    private static String limpiar(String valor) {
        return valor.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?s)<[^>]*>", "")
                .replace("&nbsp;", " ").replace("&amp;", "&")
                .replace("&lt;", "<").replace("&gt;", ">")
                .replace("  ||  ", "\n").replace("||", "\n")
                .replace("[", "").replace("]", "").replace(" | ", " - ").trim();
    }

    private static class PiePagina extends PdfPageEventHelper {
        private final Font font = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.GRAY);

        @Override public void onEndPage(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT,
                    new Phrase("Rey Salud - Reporte de actividad clinica", font),
                    document.left(), document.bottom() - 14, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                    new Phrase("Pagina " + writer.getPageNumber(), font),
                    document.right(), document.bottom() - 14, 0);
        }
    }
}
