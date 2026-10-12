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

public final class HistoriaClinicaPdfExporter {

    private static final BaseColor VERDE = new BaseColor(0, 105, 92);
    private static final BaseColor GRIS_CLARO = new BaseColor(242, 245, 247);

    private HistoriaClinicaPdfExporter() {}

    public static void exportar(File destino, String paciente, String dni, List<String[]> registros) throws Exception {
        Document documento = new Document(PageSize.A4.rotate(), 24, 24, 34, 32);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(destino));
        writer.setPageEvent(new PiePagina());
        documento.open();

        Font tituloFont = new Font(Font.FontFamily.HELVETICA, 17, Font.BOLD, VERDE);
        Font subtituloFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);
        Font cabeceraFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.WHITE);
        Font cuerpoFont = new Font(Font.FontFamily.HELVETICA, 7.2f, Font.NORMAL, BaseColor.DARK_GRAY);

        Paragraph titulo = new Paragraph("HISTORIA CLÍNICA COMPLETA", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(4);
        documento.add(titulo);

        PdfPTable datosPaciente = new PdfPTable(2);
        datosPaciente.setWidthPercentage(100);
        datosPaciente.setWidths(new float[]{70, 30});
        datosPaciente.setSpacingAfter(8);
        datosPaciente.addCell(celdaSinBorde("Paciente: " + texto(paciente, "No informado") + "    DNI: " + texto(dni, "No informado"), subtituloFont, Element.ALIGN_LEFT));
        datosPaciente.addCell(celdaSinBorde("Evoluciones: " + registros.size() + "    Emitido: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), subtituloFont, Element.ALIGN_RIGHT));
        documento.add(datosPaciente);

        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{10, 17, 25, 24, 24});
        tabla.setHeaderRows(1);
        tabla.setSplitLate(false);
        tabla.setSplitRows(true);
        agregarCabecera(tabla, "FECHA", cabeceraFont);
        agregarCabecera(tabla, "PROFESIONAL", cabeceraFont);
        agregarCabecera(tabla, "EVOLUCIÓN Y DATOS ASOCIADOS", cabeceraFont);
        agregarCabecera(tabla, "DIAGNÓSTICOS", cabeceraFont);
        agregarCabecera(tabla, "TRATAMIENTOS", cabeceraFont);

        if (registros.isEmpty()) {
            PdfPCell vacia = new PdfPCell(new Phrase("No hay evoluciones activas registradas.", subtituloFont));
            vacia.setColspan(5);
            vacia.setHorizontalAlignment(Element.ALIGN_CENTER);
            vacia.setPadding(12);
            tabla.addCell(vacia);
        } else {
            for (int i = 0; i < registros.size(); i++) {
                String[] registro = registros.get(i);
                BaseColor fondo = i % 2 == 0 ? BaseColor.WHITE : GRIS_CLARO;
                agregarDato(tabla, valor(registro, 0, "-"), cuerpoFont, fondo);
                agregarDato(tabla, valor(registro, 2, "No informado"), cuerpoFont, fondo);
                agregarDatoConInterlineado(tabla, resumenClinico(registro), cuerpoFont, fondo, 1.5f);
                agregarDato(tabla, limpiarListado(valor(registro, 4, "No registrado")), cuerpoFont, fondo);
                agregarDato(tabla, limpiarListado(valor(registro, 5, "No registrado")), cuerpoFont, fondo);
            }
        }
        documento.add(tabla);
        documento.close();
    }

    private static void agregarCabecera(PdfPTable tabla, String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBackgroundColor(VERDE);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(6);
        tabla.addCell(celda);
    }

    private static void agregarDato(PdfPTable tabla, String texto, Font font, BaseColor fondo) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBackgroundColor(fondo);
        celda.setVerticalAlignment(Element.ALIGN_TOP);
        celda.setPadding(4);
        celda.setBorderColor(new BaseColor(215, 220, 225));
        tabla.addCell(celda);
    }

    private static void agregarDatoConInterlineado(PdfPTable tabla, String texto, Font font,
            BaseColor fondo, float multiplicador) {
        Phrase contenido = new Phrase(texto, font);
        contenido.setLeading(font.getSize() * Math.max(1.5f, multiplicador));
        PdfPCell celda = new PdfPCell(contenido);
        celda.setBackgroundColor(fondo);
        celda.setVerticalAlignment(Element.ALIGN_TOP);
        celda.setPaddingTop(5);
        celda.setPaddingBottom(5);
        celda.setPaddingLeft(4);
        celda.setPaddingRight(4);
        celda.setBorderColor(new BaseColor(215, 220, 225));
        tabla.addCell(celda);
    }

    private static PdfPCell celdaSinBorde(String texto, Font font, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBorder(PdfPCell.NO_BORDER);
        celda.setHorizontalAlignment(alineacion);
        return celda;
    }

    private static String valor(String[] registro, int indice, String porDefecto) {
        if (registro == null || indice >= registro.length) return porDefecto;
        return texto(registro[indice], porDefecto);
    }

    private static String texto(String valor, String porDefecto) {
        return valor == null || valor.trim().isEmpty() ? porDefecto : valor.trim();
    }

    private static String limpiarListado(String valor) {
        return valor.replace("  ||  ", "\n").replace("||", "\n")
                .replace("[", "").replace("]", "").replace(" | ", " - ");
    }

    private static String resumenClinico(String[] registro) {
        StringBuilder resumen = new StringBuilder();
        resumen.append(valor(registro, 3, "Sin evolución registrada"));
        String estudios = valor(registro, 6, "");
        if (!estudios.isEmpty() && !estudios.equalsIgnoreCase("No registrado") && !estudios.equalsIgnoreCase("Ver notas de evolución")) {
            resumen.append("\nEstudios: ").append(estudios);
        }
        String proximaCita = valor(registro, 7, "");
        if (!proximaCita.isEmpty()) resumen.append("\nPróxima cita: ").append(proximaCita);
        return resumen.toString();
    }

    private static class PiePagina extends PdfPageEventHelper {
        private final Font font = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.GRAY);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Phrase izquierda = new Phrase("Documento clínico confidencial", font);
            Phrase derecha = new Phrase("Página " + writer.getPageNumber(), font);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, izquierda,
                    document.left(), document.bottom() - 14, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, derecha,
                    document.right(), document.bottom() - 14, 0);
        }
    }
}
