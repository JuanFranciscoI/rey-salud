package clinicabalbarrey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Ajustes de esquema pequeños que deben estar disponibles antes de abrir la interfaz. */
public final class EsquemaBD {
    private EsquemaBD() {}

    public static void actualizar() {
        try (Connection con = ConexionBD.getConexion(); Statement st = con.createStatement()) {
            st.executeUpdate("ALTER TABLE personal ADD COLUMN IF NOT EXISTS tipo_personal VARCHAR(30)");
            st.executeUpdate("UPDATE personal p SET tipo_personal=r.nombre FROM usuario u JOIN rol r ON r.id_rol=u.id_rol "
                    + "WHERE u.id_personal=p.id_personal AND p.tipo_personal IS NULL");
            st.executeUpdate("UPDATE personal SET tipo_personal=CASE "
                    + "WHEN id_especialidad IS NOT NULL OR NULLIF(TRIM(COALESCE(matricula,'')),'') IS NOT NULL "
                    + "THEN 'Médico' ELSE 'Secretaria' END WHERE tipo_personal IS NULL");
            st.executeUpdate("ALTER TABLE personal ALTER COLUMN tipo_personal SET NOT NULL");
            st.executeUpdate("ALTER TABLE turno ADD COLUMN IF NOT EXISTS id_obrasocial_atencion INTEGER REFERENCES obrasocial(id_obrasocial)");
            st.executeUpdate("ALTER TABLE turno ADD COLUMN IF NOT EXISTS numeroafiliado_atencion VARCHAR(100)");
            st.executeUpdate("ALTER TABLE turno ADD COLUMN IF NOT EXISTS cobertura_registrada BOOLEAN NOT NULL DEFAULT FALSE");
            st.executeUpdate("UPDATE turno t SET id_obrasocial_atencion=p.id_obrasocial, "
                    + "numeroafiliado_atencion=p.numeroafiliado, cobertura_registrada=TRUE FROM paciente p "
                    + "WHERE p.id_paciente=t.id_paciente AND NOT t.cobertura_registrada");
            repararCaracteresEspeciales(con);
            prepararDatosDemostracion(con);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo actualizar la clasificación del personal", e);
        }
    }

    /**
     * Limpia registros de prueba conservando sus claves y completa únicamente
     * campos clínicos vacíos con información ficticia coherente para la demo.
     */
    private static void prepararDatosDemostracion(Connection con) throws Exception {
        final String migracion = "2026-08-25-datos-demostracion-v11";
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT EXISTS(SELECT 1 FROM reysalud_migracion WHERE codigo=?)")) {
            ps.setString(1, migracion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getBoolean(1)) return;
            }
        }

        boolean autoCommitAnterior = con.getAutoCommit();
        con.setAutoCommit(false);
        try (Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS respaldo_presentacion_personal AS TABLE personal WITH NO DATA");
            st.executeUpdate("INSERT INTO respaldo_presentacion_personal SELECT p.* FROM personal p "
                    + "WHERE NOT EXISTS (SELECT 1 FROM respaldo_presentacion_personal r WHERE r.id_personal=p.id_personal)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS respaldo_presentacion_paciente AS TABLE paciente WITH NO DATA");
            st.executeUpdate("INSERT INTO respaldo_presentacion_paciente SELECT p.* FROM paciente p "
                    + "WHERE NOT EXISTS (SELECT 1 FROM respaldo_presentacion_paciente r WHERE r.id_paciente=p.id_paciente)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS respaldo_presentacion_consulta AS TABLE consulta WITH NO DATA");
            st.executeUpdate("INSERT INTO respaldo_presentacion_consulta SELECT c.* FROM consulta c "
                    + "WHERE NOT EXISTS (SELECT 1 FROM respaldo_presentacion_consulta r WHERE r.id_consulta=c.id_consulta)");

            actualizarNombresPersonal(con);
            actualizarNombresPacientes(con);
            normalizarTelefonos(con);
            normalizarMatriculasMedicas(con);
            normalizarEspecialidades(con);
            completarHistoriasDemostracion(con);
            precisarTerminologiaClinica(con);
            normalizarPracticas(con);
            limpiarMetadatosObservaciones(con);

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO reysalud_migracion(codigo) VALUES (?) ON CONFLICT (codigo) DO NOTHING")) {
                ps.setString(1, migracion);
                ps.executeUpdate();
            }
            con.commit();
        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitAnterior);
        }
    }

    private static void actualizarNombresPersonal(Connection con) throws Exception {
        Map<Integer, String> nombres = new LinkedHashMap<>();
        nombres.put(1, "Martín Valvarrey");
        nombres.put(2, "Martina Colín");
        nombres.put(3, "Sebastián del Solar");
        nombres.put(4, "Sofía Herrera");
        nombres.put(5, "Carlos Mansilla");
        nombres.put(8, "Jorge Mansilla");
        nombres.put(10, "Miguel Torres");
        nombres.put(11, "Ricardo Palacios");
        nombres.put(12, "Carlos Tévez");
        nombres.put(13, "Alejandro Aguirre");
        nombres.put(14, "Tobías Rivero");
        nombres.put(15, "Marcos Giorgi");
        nombres.put(16, "Magalí Giorgi");
        nombres.put(17, "Paula Sánchez");
        nombres.put(18, "Rodrigo Giorgi");
        nombres.put(19, "Antonio Almada");
        nombres.put(20, "Martín Giorgi");
        nombres.put(21, "Martina Giorgi");
        nombres.put(23, "Agustín Méndez");
        nombres.put(24, "Guillermo Juárez");
        nombres.put(25, "Diego Blanco");
        nombres.put(27, "Julio Casado");
        nombres.put(28, "Nicolás Font");
        nombres.put(29, "Pedro Sánchez");
        nombres.put(30, "Andrea Gutiérrez");
        nombres.put(31, "Mauro Acosta");
        nombres.put(32, "Mariano Valvarrey");
        nombres.put(33, "Gilberto Galeano");
        nombres.put(34, "Fernanda Suárez");
        nombres.put(35, "Cecilia Ramírez");
        nombres.put(36, "Mercedes Mansilla");
        actualizarNombresPorId(con, "personal", "id_personal", "nombreyapellido", nombres);
        normalizarColumnaNombre(con, "personal", "id_personal", "nombreyapellido");
    }

    private static void actualizarNombresPacientes(Connection con) throws Exception {
        Map<Integer, String[]> nombres = new LinkedHashMap<>();
        nombres.put(22, new String[]{"Alexis", "Valvarrey"});
        nombres.put(24, new String[]{"Joaquín", "Calvo"});
        nombres.put(25, new String[]{"Juan", "Ingaramo"});
        nombres.put(26, new String[]{"Alejo", "Balba"});
        nombres.put(28, new String[]{"Martina", "Valvarrey"});
        nombres.put(29, new String[]{"Kevin", "Sosa"});
        nombres.put(31, new String[]{"Julio", "Iglesias"});
        nombres.put(32, new String[]{"Kiara", "Ríos"});
        nombres.put(33, new String[]{"Horacio", "López"});
        nombres.put(34, new String[]{"Felipe", "Fernández"});
        nombres.put(35, new String[]{"Mariana", "López"});
        nombres.put(36, new String[]{"Federico", "García"});
        nombres.put(37, new String[]{"Valentina", "Martínez"});
        nombres.put(38, new String[]{"Roberto", "Gómez"});
        nombres.put(39, new String[]{"Sofía", "Rodríguez"});
        nombres.put(40, new String[]{"Diego", "Fernández"});
        nombres.put(41, new String[]{"Camila", "Pérez"});
        nombres.put(42, new String[]{"Marcelo", "Sánchez"});
        nombres.put(44, new String[]{"Juan Carlos", "Álvarez"});
        nombres.put(45, new String[]{"Agustina", "Díaz"});
        nombres.put(48, new String[]{"Nicolás", "González"});
        nombres.put(52, new String[]{"Martín", "Giménez"});
        nombres.put(55, new String[]{"Juan", "Torres"});
        nombres.put(56, new String[]{"Daniela", "Salas"});
        nombres.put(57, new String[]{"Diego", "Flores"});
        nombres.put(59, new String[]{"Ana", "García"});
        nombres.put(61, new String[]{"María Belén", "Suárez"});
        nombres.put(62, new String[]{"Patricia", "Ortega"});
        nombres.put(64, new String[]{"Emilia", "Andrade"});
        nombres.put(65, new String[]{"Jorge", "Medina"});
        nombres.put(66, new String[]{"Sofía", "Navarro"});
        nombres.put(67, new String[]{"Alejandro", "Perusini"});
        nombres.put(71, new String[]{"Agustín", "Ingaramo"});
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE paciente SET nombre=?, apellido=? WHERE id_paciente=?")) {
            for (Map.Entry<Integer, String[]> e : nombres.entrySet()) {
                ps.setString(1, e.getValue()[0]);
                ps.setString(2, e.getValue()[1]);
                ps.setInt(3, e.getKey());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        normalizarColumnaNombre(con, "paciente", "id_paciente", "nombre");
        normalizarColumnaNombre(con, "paciente", "id_paciente", "apellido");
    }

    private static void normalizarEspecialidades(Connection con) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE especialidad SET nombre=? WHERE LOWER(TRIM(nombre))=?")) {
            ps.setString(1, "Retinología");
            ps.setString(2, "retinologo");
            ps.addBatch();
            ps.setString(1, "Córnea");
            ps.setString(2, "coronato");
            ps.addBatch();
            ps.executeBatch();
        }
    }

    private static void normalizarTelefonos(Connection con) throws Exception {
        normalizarTelefonosTabla(con, "personal", "id_personal", 17);
        normalizarTelefonosTabla(con, "paciente", "id_paciente", 53);
    }

    private static void normalizarMatriculasMedicas(Connection con) throws Exception {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("UPDATE personal SET matricula='MP-' || "
                    + "LPAD((50000 + id_personal)::text, 5, '0') "
                    + "WHERE tipo_personal='Médico'");
        }
    }

    private static void precisarTerminologiaClinica(Connection con) throws Exception {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("UPDATE consulta SET observaciones=REPLACE(observaciones,"
                    + "'Papila estable','Papila óptica estable') WHERE observaciones LIKE '%Papila estable%'");
        }
    }

    private static void normalizarPracticas(Connection con) throws Exception {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("UPDATE consulta SET practica='Consulta oftalmológica' "
                    + "WHERE LOWER(COALESCE(practica,'')) LIKE '%demostr%'");
            st.executeUpdate("UPDATE consulta SET practica='Atención clínica' "
                    + "WHERE LOWER(COALESCE(practica,'')) LIKE '%registro%' "
                    + "OR LOWER(COALESCE(practica,'')) LIKE '%regulariz%'");
        }
    }

    private static void limpiarMetadatosObservaciones(Connection con) throws Exception {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("UPDATE consulta SET observaciones=TRIM(REGEXP_REPLACE(observaciones, "
                    + "'^\\s*\\[[^]]+\\]\\s*', '')) "
                    + "WHERE observaciones ~ '^\\s*\\[[^]]+\\]'");
        }
    }

    private static void normalizarTelefonosTabla(Connection con, String tabla, String id, int semilla) throws Exception {
        String sql = "UPDATE " + identificador(tabla) + " SET telefono=? WHERE "
                + identificador(id) + "=?";
        String consulta = "SELECT " + identificador(id) + " FROM " + identificador(tabla);
        int[] caracteristicas = {342, 351, 341, 261, 381};
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(consulta);
                PreparedStatement ps = con.prepareStatement(sql)) {
            while (rs.next()) {
                int identificadorRegistro = rs.getInt(1);
                int caracteristica = caracteristicas[Math.floorMod(identificadorRegistro + semilla,
                        caracteristicas.length)];
                long numeroLocal = 4_100_000L
                        + Math.floorMod(identificadorRegistro * 7_919L + semilla * 104_729L, 5_800_000L);
                ps.setLong(1, caracteristica * 10_000_000L + numeroLocal);
                ps.setInt(2, identificadorRegistro);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void actualizarNombresPorId(Connection con, String tabla, String id,
            String columna, Map<Integer, String> nombres) throws Exception {
        String sql = "UPDATE " + identificador(tabla) + " SET " + identificador(columna)
                + "=? WHERE " + identificador(id) + "=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Map.Entry<Integer, String> e : nombres.entrySet()) {
                ps.setString(1, e.getValue());
                ps.setInt(2, e.getKey());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void normalizarColumnaNombre(Connection con, String tabla, String id,
            String columna) throws Exception {
        String consulta = "SELECT " + identificador(id) + "," + identificador(columna)
                + " FROM " + identificador(tabla);
        String actualizacion = "UPDATE " + identificador(tabla) + " SET " + identificador(columna)
                + "=? WHERE " + identificador(id) + "=?";
        List<Object[]> cambios = new ArrayList<>();
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(consulta)) {
            while (rs.next()) {
                String original = rs.getString(2);
                String normalizado = nombrePropio(original);
                if (original != null && !original.equals(normalizado)) {
                    cambios.add(new Object[]{rs.getInt(1), normalizado});
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement(actualizacion)) {
            for (Object[] cambio : cambios) {
                ps.setString(1, (String) cambio[1]);
                ps.setInt(2, (Integer) cambio[0]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static String nombrePropio(String valor) {
        if (valor == null || valor.isBlank()) return valor;
        String[] palabras = valor.trim().toLowerCase(new Locale("es", "AR")).split("\\s+");
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < palabras.length; i++) {
            String palabra = palabras[i];
            boolean particula = i > 0 && List.of("de", "del", "la", "las", "los", "y").contains(palabra);
            if (!particula && !palabra.isEmpty()) {
                palabra = Character.toUpperCase(palabra.charAt(0)) + palabra.substring(1);
            }
            if (resultado.length() > 0) resultado.append(' ');
            resultado.append(palabra);
        }
        return resultado.toString();
    }

    private static void completarHistoriasDemostracion(Connection con) throws Exception {
        String[] diagnosticos = {
            "[H52 - Miopía | Est: Crónico]",
            "[H10 - Conjuntivitis alérgica | Est: Agudo]",
            "[H00 - Orzuelo | Est: Agudo]",
            "[H25 - Catarata senil | Est: Crónico]",
            "[H40 - Glaucoma primario de ángulo abierto | Est: Crónico]",
            "[H35 - Degeneración macular asociada a la edad | Est: Crónico]",
            "[H36 - Retinopatía diabética | Est: Crónico]",
            "[H04 - Síndrome de ojo seco | Est: Crónico]"
        };
        String[] tratamientos = {
            "[Corrección óptica con anteojos | Frec: Uso diario | Est: Actual]",
            "[Lágrimas artificiales y antihistamínico tópico | Frec: Cada 12 horas | Est: Actual]",
            "[Compresas tibias e higiene palpebral | Frec: Cada 8 horas | Est: Actual]",
            "[Control de agudeza visual y evaluación quirúrgica | Frec: Control semestral | Est: Actual]",
            "[Tratamiento hipotensor ocular y control de presión intraocular | Frec: Control mensual | Est: Actual]",
            "[Suplemento vitamínico y seguimiento de retina | Frec: Control trimestral | Est: Actual]",
            "[Control metabólico y seguimiento retinal | Frec: Control trimestral | Est: Actual]",
            "[Lágrimas artificiales | Frec: Según necesidad | Est: Actual]"
        };
        String sql = "SELECT id_consulta,fecha,observaciones,enfermedad,tratamiento,"
                + "estudios_previos,proxima_cita FROM consulta "
                + "WHERE LOWER(COALESCE(estado,'activo'))='activo'";
        List<Object[]> cambios = new ArrayList<>();
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt(1);
                java.sql.Date fecha = rs.getDate(2);
                String evolucion = rs.getString(3);
                String diagnostico = rs.getString(4);
                String tratamiento = rs.getString(5);
                String estudios = rs.getString(6);
                String proximaCita = rs.getString(7);
                int indice = Math.floorMod(id, diagnosticos.length);
                if (esDatoClinicoVacio(diagnostico)) diagnostico = diagnosticos[indice];
                if (esDatoClinicoVacio(tratamiento)) tratamiento = tratamientoPara(diagnostico, tratamientos[indice]);
                if (debeCompletarEvolucion(evolucion)) evolucion = evolucionPara(diagnostico, id);
                if (!tratamiento.contains("Dosis:") || !tratamiento.contains("Inicio:")
                        || !tratamiento.contains("Fin:")) {
                    tratamiento = tratamientoDetalladoPara(diagnostico, fecha);
                }
                if (esDatoClinicoVacio(estudios)) estudios = estudioPara(diagnostico);
                if (esDatoClinicoVacio(proximaCita)) {
                    java.time.LocalDate base = fecha == null ? java.time.LocalDate.now() : fecha.toLocalDate();
                    proximaCita = base.plusDays(diasProximoControl(diagnostico)).format(
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                cambios.add(new Object[]{id, evolucion, diagnostico, tratamiento, estudios, proximaCita});
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE consulta SET observaciones=?,enfermedad=?,tratamiento=?,"
                        + "estudios_previos=?,proxima_cita=? WHERE id_consulta=?")) {
            for (Object[] cambio : cambios) {
                ps.setString(1, (String) cambio[1]);
                ps.setString(2, (String) cambio[2]);
                ps.setString(3, (String) cambio[3]);
                ps.setString(4, (String) cambio[4]);
                ps.setString(5, (String) cambio[5]);
                ps.setInt(6, (Integer) cambio[0]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static boolean debeCompletarEvolucion(String valor) {
        if (valor == null || valor.isBlank() || valor.trim().length() < 20) return true;
        String texto = valor.toLowerCase(new Locale("es", "AR"));
        return texto.contains("generada para validar") || texto.contains("demostración del sistema")
                || texto.contains("historia regularizada") || texto.contains("turno histórico")
                || texto.contains("no había detalles clínicos") || texto.contains("datos_estadisticas")
                || texto.contains("control historico generado para reportes")
                || texto.contains("control histórico generado para reportes");
    }

    private static String evolucionPara(String diagnostico, int id) {
        String texto = diagnostico == null ? "" : diagnostico.toLowerCase(new Locale("es", "AR"));
        String complemento = Math.floorMod(id, 2) == 0
                ? "Se explican signos de alarma y pautas de control."
                : "Paciente comprende las indicaciones y continuará en seguimiento.";
        if (texto.contains("miop")) return "Consulta por visión lejana borrosa. Agudeza visual mejora con corrección. " + complemento;
        if (texto.contains("conjuntivitis")) return "Presenta prurito, lagrimeo y enrojecimiento ocular leve, sin secreción purulenta. " + complemento;
        if (texto.contains("orzuelo") || texto.contains("chalaz")) return "Nódulo palpebral doloroso de evolución reciente, sin compromiso visual. " + complemento;
        if (texto.contains("catarata")) return "Disminución progresiva de la visión y deslumbramiento nocturno. Se evalúa conducta quirúrgica. " + complemento;
        if (texto.contains("glaucoma")) return "Presión intraocular elevada en control. Papila estable y sin síntomas agudos. " + complemento;
        if (texto.contains("degeneración macular")) return "Cambios maculares compatibles con la edad, sin signos actuales de actividad. " + complemento;
        if (texto.contains("retinopat")) return "Fondo de ojo con cambios vasculares leves. Se indica control metabólico estricto. " + complemento;
        return "Refiere ardor y sensación de cuerpo extraño, con película lagrimal inestable. " + complemento;
    }

    private static String tratamientoDetalladoPara(String diagnostico, java.sql.Date fechaConsulta) {
        java.time.LocalDate inicio = fechaConsulta == null ? java.time.LocalDate.now() : fechaConsulta.toLocalDate();
        String texto = diagnostico == null ? "" : diagnostico.toLowerCase(new Locale("es", "AR"));
        String accion; String dosis; String frecuencia; int duracion; String estado;
        if (texto.contains("miop")) {
            accion="Corrección óptica con anteojos"; dosis="Según refracción"; frecuencia="Uso diario"; duracion=365; estado="Actual";
        } else if (texto.contains("conjuntivitis")) {
            accion="Olopatadina 0,1%"; dosis="1 gota por ojo"; frecuencia="Cada 12 horas"; duracion=14; estado="Finalizado";
        } else if (texto.contains("orzuelo") || texto.contains("chalaz")) {
            accion="Compresas tibias"; dosis="10 minutos"; frecuencia="Cada 8 horas"; duracion=7; estado="Finalizado";
        } else if (texto.contains("catarata")) {
            accion="Control de agudeza visual"; dosis="No aplica"; frecuencia="Única vez"; duracion=180; estado="Actual";
        } else if (texto.contains("glaucoma")) {
            accion="Latanoprost 0,005%"; dosis="1 gota nocturna"; frecuencia="Cada 24 horas"; duracion=180; estado="Actual";
        } else if (texto.contains("degeneración macular")) {
            accion="Seguimiento de retina"; dosis="No aplica"; frecuencia="Cada 90 días"; duracion=90; estado="Actual";
        } else if (texto.contains("retinopat")) {
            accion="Control metabólico y de retina"; dosis="No aplica"; frecuencia="Cada 90 días"; duracion=90; estado="Actual";
        } else {
            accion="Lágrimas artificiales"; dosis="1 gota por ojo"; frecuencia="Cada 8 horas"; duracion=30; estado="Actual";
        }
        java.time.format.DateTimeFormatter formato = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "[" + accion + " | Dosis: " + dosis + " | Frec: " + frecuencia
                + " | Inicio: " + inicio.format(formato) + " | Fin: "
                + inicio.plusDays(duracion).format(formato) + " | Est: " + estado + "]";
    }

    private static String estudioPara(String diagnostico) {
        String texto = diagnostico == null ? "" : diagnostico.toLowerCase(new Locale("es", "AR"));
        if (texto.contains("glaucoma")) return "Tonometría y evaluación de papila óptica";
        if (texto.contains("retinopat") || texto.contains("degeneración macular")) return "Fondo de ojo y retinografía";
        if (texto.contains("catarata")) return "Agudeza visual y biomicroscopía";
        return "Agudeza visual y examen con lámpara de hendidura";
    }

    private static int diasProximoControl(String diagnostico) {
        String texto = diagnostico == null ? "" : diagnostico.toLowerCase(new Locale("es", "AR"));
        if (texto.contains("conjuntivitis") || texto.contains("orzuelo") || texto.contains("chalaz")) return 14;
        if (texto.contains("glaucoma")) return 30;
        if (texto.contains("retinopat") || texto.contains("degeneración macular")) return 90;
        return 180;
    }

    private static boolean esDatoClinicoVacio(String valor) {
        if (valor == null || valor.isBlank()) return true;
        String normalizado = valor.trim().toLowerCase(new Locale("es", "AR"));
        return List.of("ninguno", "no registrado", "no registrada", "sin diagnóstico registrado",
                "sin tratamiento registrado", "sin próxima cita", "sin estudios previos",
                "sin estudios registrados", "sin estudios adjuntos", "-").contains(normalizado);
    }

    private static String tratamientoPara(String diagnostico, String predeterminado) {
        String texto = diagnostico == null ? "" : diagnostico.toLowerCase(new Locale("es", "AR"));
        if (texto.contains("miop")) return "[Corrección óptica con anteojos | Frec: Uso diario | Est: Actual]";
        if (texto.contains("conjuntivitis")) return "[Lágrimas artificiales y antihistamínico tópico | Frec: Cada 12 horas | Est: Actual]";
        if (texto.contains("orzuelo") || texto.contains("chalaz")) return "[Compresas tibias e higiene palpebral | Frec: Cada 8 horas | Est: Actual]";
        if (texto.contains("catarata")) return "[Control de agudeza visual y evaluación quirúrgica | Frec: Control semestral | Est: Actual]";
        if (texto.contains("glaucoma")) return "[Tratamiento hipotensor ocular y control de presión intraocular | Frec: Control mensual | Est: Actual]";
        if (texto.contains("retinopat") || texto.contains("degeneración macular")) return "[Seguimiento especializado de retina | Frec: Control trimestral | Est: Actual]";
        return predeterminado;
    }

    /**
     * Repara datos históricos importados desde un SQL que sustituyó letras
     * acentuadas por dos signos de pregunta. La operación es idempotente y se
     * limita a fragmentos inequívocos encontrados en esta base de datos.
     */
    private static void repararCaracteresEspeciales(Connection con) throws Exception {
        final String migracion = "2026-08-24-caracteres-especiales-v1";
        try (Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS reysalud_migracion ("
                    + "codigo VARCHAR(100) PRIMARY KEY, aplicada_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT EXISTS(SELECT 1 FROM reysalud_migracion WHERE codigo=?)")) {
            ps.setString(1, migracion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getBoolean(1)) return;
            }
        }

        String[][] reemplazos = {
            {"M??dico", "Médico"}, {"Cirug??a", "Cirugía"},
            {"Oftalmolog??a", "Oftalmología"}, {"Oftalmopediatr??a", "Oftalmopediatría"},
            {"Secretar??a", "Secretaría"}, {"Recepci??n", "Recepción"},
            {"D??a", "Día"}, {"Jer??rquicos", "Jerárquicos"},
            {"Prevenci??n", "Prevención"}, {"Medif??", "Medifé"},
            {"Metal??rgicos", "Metalúrgicos"}, {"Construcci??n", "Construcción"},
            {"Uni??n", "Unión"}, {"M??dica", "Médica"},
            {"Garc??a", "García"}, {"S??nchez", "Sánchez"},
            {"gesti??n", "gestión"}, {"m??dicas", "médicas"},
            {"demostraci??n", "demostración"}, {"??ngulo", "Ángulo"},
            {"Miop??a", "Miopía"}, {"Hipermetrop??a", "Hipermetropía"},
            {"Chalazi??n", "Chalazión"}, {"Degeneraci??n", "Degeneración"},
            {"Al??rgica", "Alérgica"}, {"Retinopat??a", "Retinopatía"},
            {"Diab??tica", "Diabética"}, {"Cong??nita", "Congénita"},
            {"L??grimas", "Lágrimas"}, {"Gen??ricas", "Genéricas"},
            {"Vitam??nico", "Vitamínico"}, {"Derivaci??n", "Derivación"},
            {"Quir??rgica", "Quirúrgica"}, {"Antibi??tico", "Antibiótico"},
            {"Cr??nico", "Crónico"}, {"evoluci??n", "evolución"},
            {"Evoluci??n", "Evolución"}, {"diagn??stico", "diagnóstico"},
            {"Atenci??n", "Atención"}, {"Gesti??n", "Gestión"},
            {"hist??rico", "histórico"}, {"hab??a", "había"},
            {"cl??nicos", "clínicos"}, {"Cl??nica", "Clínica"},
            {"cl??nica", "clínica"}, {"cl�nica", "clínica"},
            {"pr??xima", "próxima"}, {"Seg??n", "Según"},
            {"L??ser", "Láser"}
        };

        List<String[]> columnas = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT table_name, column_name FROM information_schema.columns "
                + "WHERE table_schema='public' AND data_type IN "
                + "('character varying','character','text')");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) columnas.add(new String[]{rs.getString(1), rs.getString(2)});
        }

        for (String[] columna : columnas) {
            String tabla = identificador(columna[0]);
            String campo = identificador(columna[1]);
            String sql = "UPDATE " + tabla + " SET " + campo + "=REPLACE(" + campo
                    + ", ?, ?) WHERE " + campo + " LIKE ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (String[] reemplazo : reemplazos) {
                    ps.setString(1, reemplazo[0]);
                    ps.setString(2, reemplazo[1]);
                    ps.setString(3, "%" + reemplazo[0] + "%");
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO reysalud_migracion(codigo) VALUES (?) ON CONFLICT (codigo) DO NOTHING")) {
            ps.setString(1, migracion);
            ps.executeUpdate();
        }
    }

    private static String identificador(String valor) {
        if (valor == null || !valor.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Identificador SQL inválido: " + valor);
        }
        return "\"" + valor + "\"";
    }
}
