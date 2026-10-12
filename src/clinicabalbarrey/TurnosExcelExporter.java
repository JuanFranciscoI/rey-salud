package clinicabalbarrey;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Genera el libro XLSX correspondiente a la última estadística aplicada. */
public final class TurnosExcelExporter {
    private TurnosExcelExporter() { }

    public static void exportar(File destino, String[] encabezados,
            List<String[]> resultados, Map<String, String> filtros) throws Exception {
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(destino))) {
            agregar(zip, "[Content_Types].xml", tiposContenido());
            agregar(zip, "_rels/.rels", relacionesRaiz());
            agregar(zip, "xl/workbook.xml", libro());
            agregar(zip, "xl/_rels/workbook.xml.rels", relacionesLibro());
            agregar(zip, "xl/styles.xml", estilos());
            agregar(zip, "xl/worksheets/sheet1.xml", hojaResumen(filtros));
            agregar(zip, "xl/worksheets/sheet2.xml", hojaResultados(encabezados, resultados));
        }
    }

    private static String hojaResumen(Map<String, String> filtros) {
        String[] encabezados = filtros.keySet().stream()
                .map(TurnosExcelExporter::encabezadoResumen)
                .toArray(String[]::new);
        String[] valores = filtros.values().stream()
                .map(TurnosExcelExporter::valorResumen)
                .toArray(String[]::new);
        double[] anchos = new double[encabezados.length];
        for (int i = 0; i < anchos.length; i++) {
            anchos[i] = i < 2 ? 16 : (i == anchos.length - 1 ? 18 : 26);
        }
        StringBuilder xml = inicioHoja(encabezados.length, anchos);
        xml.append("<row r=\"1\" ht=\"28\" customHeight=\"1\">");
        for (int columna = 0; columna < encabezados.length; columna++) {
            celda(xml, 1, columna, encabezados[columna], 1);
        }
        xml.append("</row>");
        xml.append("<row r=\"2\" ht=\"28\" customHeight=\"1\">");
        for (int columna = 0; columna < valores.length; columna++) {
            celda(xml, 2, columna, valores[columna], 2);
        }
        xml.append("</row></sheetData><autoFilter ref=\"A1:")
                .append(referenciaCelda(encabezados.length - 1, 2)).append("\"/>")
                .append("<pageSetup orientation=\"landscape\" fitToWidth=\"1\" fitToHeight=\"0\"/></worksheet>");
        return xml.toString();
    }

    private static String encabezadoResumen(String encabezado) {
        return switch (encabezado) {
            case "Estados del turno" -> "Estados";
            case "Valores mostrados" -> "Mostrar";
            case "Resultado agrupado por" -> "Agrupar por";
            case "Total filtrado" -> "Total de turnos";
            default -> limpiar(encabezado);
        };
    }

    private static String valorResumen(String valor) {
        String limpio = limpiar(valor);
        return limpio.equalsIgnoreCase("Sin filtro") ? "" : limpio;
    }

    private static String hojaResultados(String[] encabezados, List<String[]> resultados) {
        int columnas = Math.max(1, encabezados.length);
        double[] anchos = new double[columnas];
        for (int i = 0; i < columnas; i++) anchos[i] = i == 0 ? 34 : 22;
        StringBuilder xml = inicioHoja(columnas, anchos);

        xml.append("<row r=\"1\" ht=\"28\" customHeight=\"1\">");
        for (int columna = 0; columna < encabezados.length; columna++) {
            celda(xml, 1, columna, limpiar(encabezados[columna]), 1);
        }
        xml.append("</row>");

        for (int indice = 0; indice < resultados.size(); indice++) {
            int fila = indice + 2;
            String[] valores = resultados.get(indice);
            xml.append("<row r=\"").append(fila).append("\" ht=\"23\" customHeight=\"1\">");
            for (int columna = 0; columna < encabezados.length; columna++) {
                celda(xml, fila, columna, limpiar(valor(valores, columna)), 2);
            }
            xml.append("</row>");
        }

        int ultimaFila = Math.max(1, resultados.size() + 1);
        String referencia = "A1:" + referenciaCelda(columnas - 1, ultimaFila);
        xml.append("</sheetData><autoFilter ref=\"").append(referencia).append("\"/>")
                .append("<pageSetup orientation=\"landscape\" fitToWidth=\"1\" fitToHeight=\"0\"/></worksheet>");
        return xml.toString();
    }

    private static StringBuilder inicioHoja(int columnas, double[] anchos) {
        StringBuilder xml = new StringBuilder(4096);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
                .append("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>")
                .append("<cols>");
        for (int i = 0; i < columnas; i++) {
            xml.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
                    .append("\" width=\"").append(anchos[i]).append("\" customWidth=\"1\"/>");
        }
        return xml.append("</cols><sheetData>");
    }

    private static void celda(StringBuilder xml, int fila, int columna, String valor, int estilo) {
        xml.append("<c r=\"").append(referenciaCelda(columna, fila))
                .append("\" t=\"inlineStr\" s=\"").append(estilo)
                .append("\"><is><t xml:space=\"preserve\">")
                .append(xml(valor)).append("</t></is></c>");
    }

    private static String referenciaCelda(int columna, int fila) {
        StringBuilder letras = new StringBuilder();
        int valor = columna + 1;
        while (valor > 0) {
            valor--;
            letras.insert(0, (char) ('A' + valor % 26));
            valor /= 26;
        }
        return letras.append(fila).toString();
    }

    private static String estilos() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font><font><b/><color rgb=\"FFFFFFFF\"/><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                + "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill><fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF00796B\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>"
                + "<borders count=\"2\"><border/><border><left style=\"thin\"><color rgb=\"FFD9E0E3\"/></left><right style=\"thin\"><color rgb=\"FFD9E0E3\"/></right><top style=\"thin\"><color rgb=\"FFD9E0E3\"/></top><bottom style=\"thin\"><color rgb=\"FFD9E0E3\"/></bottom></border></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"3\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyAlignment=\"1\"><alignment vertical=\"center\" wrapText=\"1\"/></xf></cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles></styleSheet>";
    }

    private static String tiposContenido() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet2.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/></Types>";
    }

    private static String relacionesRaiz() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>";
    }

    private static String libro() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><bookViews><workbookView/></bookViews><sheets><sheet name=\"Resumen\" sheetId=\"1\" r:id=\"rId1\"/><sheet name=\"Resultados\" sheetId=\"2\" r:id=\"rId2\"/></sheets></workbook>";
    }

    private static String relacionesLibro() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/><Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet2.xml\"/><Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/></Relationships>";
    }

    private static void agregar(ZipOutputStream zip, String ruta, String contenido) throws Exception {
        zip.putNextEntry(new ZipEntry(ruta));
        zip.write(contenido.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String valor(String[] fila, int indice) {
        return fila != null && indice < fila.length && fila[indice] != null ? fila[indice] : "";
    }

    static String limpiar(String valor) {
        if (valor == null) return "";
        return valor.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?s)<[^>]*>", "")
                .replace("&nbsp;", " ").replace("&amp;", "&")
                .replace("&lt;", "<").replace("&gt;", ">").trim();
    }

    private static String xml(String valor) {
        return valor.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}
