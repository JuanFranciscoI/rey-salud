package clinicabalbarrey.dao;

import clinicabalbarrey.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultaDAO {

    public record EstadisticaObraSocial(String obraSocial, int pacientesAtendidos, int atenciones) {}
    public record ResumenAtenciones(int pacientesAtendidos, int diagnosticosCargados, int tratamientosIndicados) {}

    public String guardarConsultaSegura(String dniPaciente, int idPersonal, String practica, String observaciones,
                                        String enfermedad, String tratamiento, String estudios, String proximaCita, String rutaArchivoAdjunto) {
        if (idPersonal <= 0) {
            return "ERROR DE SESIÓN: No se pudo identificar al profesional autenticado.";
        }
        if (observaciones == null || observaciones.isBlank() || observaciones.length() > 300) {
            return "La evolución debe contener entre 1 y 300 caracteres.";
        }

        String sqlInsert = "INSERT INTO consulta (id_paciente, id_personal, fecha, practica, observaciones, enfermedad, tratamiento, estudios_previos, proxima_cita, ruta_archivo, estado) " +
                           "SELECT id_paciente, ?, CURRENT_DATE, ?, ?, ?, ?, ?, ?, ?, 'activo' FROM paciente WHERE TRIM(CAST(dni AS VARCHAR))=TRIM(?) AND estado::text='activo'";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst2 = con.prepareStatement(sqlInsert)) {
            pst2.setInt(1, idPersonal);
            pst2.setString(2, practica);
            pst2.setString(3, observaciones);
            pst2.setString(4, enfermedad);
            pst2.setString(5, tratamiento);
            pst2.setString(6, estudios);
            pst2.setString(7, proximaCita);
            pst2.setString(8, rutaArchivoAdjunto);
            pst2.setString(9, dniPaciente);

            int filas = pst2.executeUpdate();
            if (filas > 0) {
                return "OK";
            } else {
                return "ERROR DESCONOCIDO: No se pudo insertar la consulta.";
            }
        } catch (SQLException e) {
            return "ERROR EN BASE DE DATOS: " + e.getMessage();
        }
    }

    public String guardarConsultaSegura(String dniPaciente, int idPersonal, int idTurno, String practica, String observaciones,
                                        String enfermedad, String tratamiento, String estudios, String proximaCita, String rutaArchivoAdjunto) {
        if (idPersonal <= 0) {
            return "ERROR DE SESIÓN: No se pudo identificar al profesional autenticado.";
        }
        if (idTurno <= 0) {
            return "La historia clínica debe estar asociada a un turno.";
        }
        if (observaciones == null || observaciones.isBlank() || observaciones.length() > 300) {
            return "La evolución debe contener entre 1 y 300 caracteres.";
        }

        String sqlInsert = "INSERT INTO consulta (id_paciente, id_personal, id_turno, fecha, practica, observaciones, enfermedad, tratamiento, estudios_previos, proxima_cita, ruta_archivo, estado) "
                + "SELECT p.id_paciente, ?, t.id_turno, CURRENT_DATE, ?, ?, ?, ?, ?, ?, ?, 'activo' "
                + "FROM turno t "
                + "INNER JOIN paciente p ON p.id_paciente = t.id_paciente "
                + "WHERE t.id_turno = ? "
                + "AND t.id_personal = ? "
                + "AND TRIM(CAST(p.dni AS VARCHAR)) = TRIM(?) "
                + "AND p.estado::text = 'activo' "
                + "AND LOWER(t.estado) = 'en consulta' "
                + "AND t.fechahora <= CURRENT_TIMESTAMP "
                + "AND NOT EXISTS (SELECT 1 FROM consulta c WHERE c.id_turno = t.id_turno AND LOWER(c.estado::text) = 'activo')";

        try (Connection con = ConexionBD.getConexion()) {
            try {
                asegurarEstructuraConsultaPorTurno(con);
            } catch (SQLException migracionError) {
                return "Falta actualizar la base de datos. Ejecute: ALTER TABLE consulta ADD COLUMN IF NOT EXISTS id_turno INTEGER; "
                        + "Detalle: " + migracionError.getMessage();
            }

            con.setAutoCommit(false);
            try (PreparedStatement pst = con.prepareStatement(sqlInsert)) {
                pst.setInt(1, idPersonal);
                pst.setString(2, practica);
                pst.setString(3, observaciones);
                pst.setString(4, enfermedad);
                pst.setString(5, tratamiento);
                pst.setString(6, estudios);
                pst.setString(7, proximaCita);
                pst.setString(8, rutaArchivoAdjunto);
                pst.setInt(9, idTurno);
                pst.setInt(10, idPersonal);
                pst.setString(11, dniPaciente);

                int filas = pst.executeUpdate();
                if (filas > 0) {
                    try (PreparedStatement pstTurno = con.prepareStatement(
                            "UPDATE turno SET estado='atendido' WHERE id_turno=? AND id_personal=? AND fechahora <= CURRENT_TIMESTAMP")) {
                        pstTurno.setInt(1, idTurno);
                        pstTurno.setInt(2, idPersonal);
                        if (pstTurno.executeUpdate() != 1) {
                            con.rollback();
                            return "La evolución no se guardó porque no se pudo finalizar el turno asociado.";
                        }
                    }
                    con.commit();
                    return "OK";
                }
                con.rollback();
                return "La historia clínica solo puede guardarse cuando el turno está En consulta y su horario ya llegó.";
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return "Este turno ya tiene una historia clínica cargada.";
            }
            return "ERROR EN BASE DE DATOS: " + e.getMessage();
        }
    }

    private boolean existeColumnaIdTurno(Connection con) throws SQLException {
        String sql = "SELECT EXISTS ("
                + "SELECT 1 FROM information_schema.columns "
                + "WHERE table_schema = current_schema() "
                + "AND table_name = 'consulta' "
                + "AND column_name = 'id_turno')";
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            return rs.next() && rs.getBoolean(1);
        }
    }

    private void asegurarEstructuraConsultaPorTurno(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("ALTER TABLE consulta ADD COLUMN IF NOT EXISTS id_turno INTEGER");
            st.execute("DO $$ BEGIN "
                    + "IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_consulta_turno') THEN "
                    + "ALTER TABLE consulta ADD CONSTRAINT fk_consulta_turno FOREIGN KEY (id_turno) REFERENCES turno(id_turno); "
                    + "END IF; END $$");
            st.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS ux_consulta_turno_activa "
                    + "ON consulta(id_turno) WHERE id_turno IS NOT NULL AND LOWER(estado::text)='activo'");
        }
    }

    public boolean existeConsultaActivaPorTurno(int idTurno) {
        if (idTurno <= 0) return false;

        try (Connection con = ConexionBD.getConexion()) {
            if (!existeColumnaIdTurno(con)) return false;

            String sql = "SELECT EXISTS (SELECT 1 FROM consulta WHERE id_turno = ? AND LOWER(estado::text) = 'activo')";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, idTurno);
                try (ResultSet rs = pst.executeQuery()) {
                    return rs.next() && rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar historia por turno: " + e.getMessage());
            return false;
        }
    }

    public String actualizarConsultaSegura(int idConsulta, String observaciones, String enfermedad, String tratamiento, String rutaArchivoAdjunto, Integer idPersonalPermitido) {
        if (observaciones == null || observaciones.isBlank() || observaciones.length() > 300) return "La evolución debe contener entre 1 y 300 caracteres.";
        String sqlUpdate = "UPDATE consulta SET observaciones=?, enfermedad=?, tratamiento=?, ruta_archivo=? WHERE id_consulta=? AND LOWER(estado)='activo'"
                + (idPersonalPermitido != null ? " AND id_personal=?" : "");
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sqlUpdate)) {
            pst.setString(1, observaciones);
            pst.setString(2, enfermedad);
            pst.setString(3, tratamiento);
            pst.setString(4, rutaArchivoAdjunto);
            pst.setInt(5, idConsulta);
            if (idPersonalPermitido != null) pst.setInt(6, idPersonalPermitido);

            int filas = pst.executeUpdate();
            if (filas > 0) {
                return "OK";
            } else {
                return "ERROR DESCONOCIDO: No se pudo actualizar la consulta.";
            }
        } catch (SQLException e) {
            return "ERROR EN BASE DE DATOS: " + e.getMessage();
        }
    }

    public List<String[]> obtenerHistorialPorDni(String dni) {
        return obtenerHistorialPorDni(dni, null);
    }

    public List<String[]> obtenerHistorialPorDni(String dni, Integer idPersonal) {
        List<String[]> historial = new ArrayList<>();

        String sql = "SELECT c.id_consulta, c.fecha, c.practica, c.observaciones, per.nombreyapellido, " +
                     "c.enfermedad, c.tratamiento, c.estudios_previos, c.proxima_cita, c.ruta_archivo, c.id_personal, " +
                     "COALESCE(e.nombre, '') AS especialidad " +
                     "FROM consulta c " +
                     "INNER JOIN paciente p ON c.id_paciente = p.id_paciente " +
                     "INNER JOIN personal per ON c.id_personal = per.id_personal " +
                     "LEFT JOIN especialidad e ON per.id_especialidad = e.id_especialidad " +
                     "WHERE TRIM(CAST(p.dni AS VARCHAR)) = TRIM(?) AND LOWER(c.estado) = 'activo' " +
                     (idPersonal != null ? "AND c.id_personal = ? " : "") +
                     "ORDER BY c.fecha DESC, c.id_consulta DESC";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, dni);
            if (idPersonal != null) pst.setInt(2, idPersonal);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Date f = rs.getDate("fecha");
                String fStr = (f != null) ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(f) : "-";

                historial.add(new String[]{
                    fStr,
                    rs.getString("practica"),
                    rs.getString("nombreyapellido"),
                    rs.getString("observaciones"),
                    rs.getString("enfermedad"),
                    rs.getString("tratamiento"),
                    rs.getString("estudios_previos"),
                    rs.getString("proxima_cita"),
                    rs.getString("ruta_archivo"),
                    rs.getString("id_consulta"),
                    rs.getString("id_personal"),
                    rs.getString("especialidad")
                });
            }
        } catch (Exception e) {
            System.err.println("Error al obtener historial: " + e.getMessage());
        }
        return historial;
    }

    public List<String[]> obtenerConsultasDashboard(Integer idPersonal) {
        List<String[]> consultas = new ArrayList<>();

        String sql = "SELECT c.fecha, p.nombre, p.apellido, p.dni, per.nombreyapellido AS medico, c.enfermedad, c.tratamiento, " +
                     "COALESCE(NULLIF(TRIM(o.nombre), ''), 'Particular / Sin Obra Social') AS obra_social " +
                     "FROM consulta c " +
                     "INNER JOIN turno t ON t.id_turno = c.id_turno " +
                     "INNER JOIN paciente p ON c.id_paciente = p.id_paciente " +
                     "INNER JOIN personal per ON c.id_personal = per.id_personal " +
                     "LEFT JOIN obrasocial o ON t.id_obrasocial_atencion = o.id_obrasocial " +
                     "WHERE LOWER(c.estado) = 'activo' " +
                     "AND LOWER(t.estado) IN ('atendido', 'finalizado') " +
                     (idPersonal != null ? "AND c.id_personal = ? " : "") +
                     "ORDER BY c.fecha DESC";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            if (idPersonal != null) {
                pst.setInt(1, idPersonal);
            }
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Date f = rs.getDate("fecha");
                String fStr = (f != null) ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(f) : "Sin Fecha";

                String pacienteNombre = rs.getString("nombre") + " " + rs.getString("apellido");
                String medico = rs.getString("medico");

                // --- PARSEO DIAGNÓSTICO (SIN PUNTOS, SOLO SALTO DE LÍNEA) ---
                String diagCrudo = rs.getString("enfermedad");
                String diagLimpio = "Ninguno";
                if (diagCrudo != null && !diagCrudo.isEmpty() && !diagCrudo.equals("No registrado") && !diagCrudo.equals("No registrada")) {
                    List<String> items = new ArrayList<>();
                    String[] bloques = diagCrudo.split("\\|\\|");
                    for(String b : bloques) {
                        String texto = b.replace("[", "").replace("]", "").trim();
                        String[] partes = texto.split(" \\| ");
                        if(partes.length > 0) {
                            String valorPrincipal = partes[0].trim();
                            if (valorPrincipal.matches("^[A-Z][0-9]{2}\\s*-\\s*.*")) {
                                valorPrincipal = valorPrincipal.replaceFirst("^[A-Z][0-9]{2}\\s*-\\s*", "");
                            }
                            valorPrincipal = resumirDiagnostico(valorPrincipal);
                            valorPrincipal = valorPrincipal.replaceAll("\\s*\\+\\s*", "<br>");
                            items.add(valorPrincipal);
                        }
                    }

                    if (!items.isEmpty()) {
                        StringBuilder sb = new StringBuilder("<html><div style='text-align: center;'>");
                        for (int i = 0; i < items.size(); i++) {
                            sb.append(items.get(i));
                            if (i < items.size() - 1) sb.append("<br>");
                        }
                        sb.append("</div></html>");
                        diagLimpio = sb.toString();
                    }
                }

                // --- PARSEO TRATAMIENTO (SIN PUNTOS, SOLO SALTO DE LÍNEA) ---
                String tratCrudo = rs.getString("tratamiento");
                String tratLimpio = "Ninguno";
                if (tratCrudo != null && !tratCrudo.isEmpty() && !tratCrudo.equals("No registrado")) {
                    List<String> items = new ArrayList<>();
                    String[] bloques = tratCrudo.split("\\|\\|");
                    for(String b : bloques) {
                        String texto = b.replace("[", "").replace("]", "").trim();
                        String[] partes = texto.split(" \\| ");
                        if(partes.length > 0) {
                            String valorPrincipal = partes[0].trim();
                            valorPrincipal = resumirTratamiento(valorPrincipal);
                            valorPrincipal = valorPrincipal.replaceAll("\\s*\\+\\s*", "<br>");
                            items.add(valorPrincipal);
                        }
                    }

                    if (!items.isEmpty()) {
                        StringBuilder sb = new StringBuilder("<html><div style='text-align: center;'>");
                        for (int i = 0; i < items.size(); i++) {
                            sb.append(items.get(i));
                            if (i < items.size() - 1) sb.append("<br>");
                        }
                        sb.append("</div></html>");
                        tratLimpio = sb.toString();
                    }
                }

                String dniPaciente = rs.getString("dni");
                String obraSocial = rs.getString("obra_social");
                consultas.add(new String[]{fStr, pacienteNombre, medico, diagLimpio, tratLimpio, dniPaciente, obraSocial});
            }
        } catch (Exception e) {
            System.err.println("Error al cargar datos del Dashboard: " + e.getMessage());
        }
        return consultas;
    }

    private String resumirDiagnostico(String valor) {
        String normalizado = valor == null ? "" : valor.toLowerCase();
        if (normalizado.contains("degeneración macular")) return "Degeneración macular";
        if (normalizado.contains("glaucoma")) return "Glaucoma";
        if (normalizado.contains("síndrome de ojo seco")) return "Ojo seco";
        return valor;
    }

    private String resumirTratamiento(String valor) {
        String normalizado = valor == null ? "" : valor.toLowerCase();
        if (normalizado.contains("agudeza visual")) return "Control de agudeza visual";
        if (normalizado.contains("retina") || normalizado.contains("retinal")) return "Seguimiento de retina";
        if (normalizado.contains("lágrimas artificiales")) return "Lágrimas artificiales";
        if (normalizado.contains("corrección óptica")) return "Corrección óptica";
        if (normalizado.contains("presión intraocular") || normalizado.contains("hipotensor")) {
            return "Control de presión ocular";
        }
        if (normalizado.contains("compresas tibias")) return "Compresas tibias";
        return valor;
    }

    public List<EstadisticaObraSocial> obtenerPacientesAtendidosPorObraSocial(java.sql.Date desde, java.sql.Date hasta) {
        return obtenerPacientesAtendidosPorObraSocial(desde, hasta, null);
    }

    public List<EstadisticaObraSocial> obtenerPacientesAtendidosPorObraSocial(java.sql.Date desde, java.sql.Date hasta, Integer idPersonal) {
        List<EstadisticaObraSocial> estadisticas = new ArrayList<>();
        if (desde == null || hasta == null) return estadisticas;

        String sql = "SELECT COALESCE(NULLIF(TRIM(o.nombre), ''), 'Particular / Sin Obra Social') AS obra_social, "
                + "COUNT(DISTINCT c.id_paciente) AS pacientes_atendidos, "
                + "COUNT(*) AS atenciones "
                + "FROM consulta c "
                + "INNER JOIN paciente p ON c.id_paciente = p.id_paciente "
                + "LEFT JOIN turno t ON t.id_turno = c.id_turno "
                + "LEFT JOIN obrasocial o ON t.id_obrasocial_atencion = o.id_obrasocial "
                + "WHERE LOWER(c.estado) = 'activo' AND c.fecha BETWEEN ? AND ? "
                + (idPersonal != null ? "AND c.id_personal = ? " : "")
                + "GROUP BY COALESCE(NULLIF(TRIM(o.nombre), ''), 'Particular / Sin Obra Social') "
                + "ORDER BY pacientes_atendidos DESC, atenciones DESC, obra_social ASC";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, desde);
            pst.setDate(2, hasta);
            if (idPersonal != null) pst.setInt(3, idPersonal);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    estadisticas.add(new EstadisticaObraSocial(
                            rs.getString("obra_social"),
                            rs.getInt("pacientes_atendidos"),
                            rs.getInt("atenciones")
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar estadisticas por obra social: " + e.getMessage());
        }
        return estadisticas;
    }

    public ResumenAtenciones obtenerResumenAtenciones(java.sql.Date desde, java.sql.Date hasta, Integer idPersonal) {
        if (desde == null || hasta == null) return new ResumenAtenciones(0, 0, 0);

        String condicionMedico = idPersonal != null ? "AND c.id_personal = ? " : "";
        String sql = "SELECT "
                + "COUNT(DISTINCT c.id_paciente) AS pacientes_atendidos, "
                + "SUM(CASE WHEN c.enfermedad IS NOT NULL "
                + "AND TRIM(c.enfermedad) <> '' "
                + "AND LOWER(TRIM(c.enfermedad)) NOT IN ('no registrado', 'no registrada') THEN 1 ELSE 0 END) AS diagnosticos_cargados, "
                + "SUM(CASE WHEN c.tratamiento IS NOT NULL "
                + "AND TRIM(c.tratamiento) <> '' "
                + "AND LOWER(TRIM(c.tratamiento)) NOT IN ('no registrado', 'no registrada') THEN 1 ELSE 0 END) AS tratamientos_indicados "
                + "FROM consulta c "
                + "WHERE LOWER(c.estado) = 'activo' AND c.fecha BETWEEN ? AND ? "
                + condicionMedico;

        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, desde);
            pst.setDate(2, hasta);
            if (idPersonal != null) pst.setInt(3, idPersonal);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new ResumenAtenciones(
                            rs.getInt("pacientes_atendidos"),
                            rs.getInt("diagnosticos_cargados"),
                            rs.getInt("tratamientos_indicados")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar resumen de atenciones: " + e.getMessage());
        }
        return new ResumenAtenciones(0, 0, 0);
    }

    public List<String> obtenerObrasSocialesDisponibles() {
        return obtenerObrasSocialesDisponibles(null);
    }

    public List<String> obtenerObrasSocialesDisponibles(Integer idPersonal) {
        List<String> obras = new ArrayList<>();
        String sql = "SELECT DISTINCT COALESCE(NULLIF(TRIM(o.nombre), ''), "
                + "'Particular / Sin Obra Social') AS nombre "
                + "FROM consulta c JOIN paciente p ON p.id_paciente=c.id_paciente "
                + "LEFT JOIN turno t ON t.id_turno=c.id_turno "
                + "LEFT JOIN obrasocial o ON o.id_obrasocial=t.id_obrasocial_atencion "
                + "WHERE LOWER(c.estado)='activo' "
                + (idPersonal != null ? "AND c.id_personal=? " : "")
                + "ORDER BY nombre ASC";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement pst = con.prepareStatement(sql)) {
            if (idPersonal != null) pst.setInt(1, idPersonal);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    if (nombre != null && !nombre.trim().isEmpty()) {
                        obras.add(nombre.trim());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar obras sociales disponibles: " + e.getMessage());
        }
        return obras;
    }
}
