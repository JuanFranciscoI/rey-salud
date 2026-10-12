package clinicabalbarrey.dao;

import clinicabalbarrey.ConexionBD;
import clinicabalbarrey.model.Turno;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TurnoDAO {

    public record OpcionHistorial(Integer id, String nombre, String busqueda) {
        public OpcionHistorial(Integer id, String nombre) { this(id, nombre, nombre); }
        @Override public String toString() { return nombre; }
    }

    public record DatosHistoriaTurno(int idPersonal, int idEspecialidad, String estado) { }

    public record FiltroHistorial(java.util.Date desde, java.util.Date hasta,
            List<Integer> idsMedicos, List<Integer> idsPacientes,
            List<Integer> idsObrasSociales, List<String> estados,
            List<Boolean> historias, Integer idPersonalPermitido) { }

    public void registrarTurno(Turno turno) throws SQLException {
        String error = reservarTurno(turno.getIdPaciente(), turno.getIdPersonal(), turno.getFechaHora(), turno.getObservaciones());
        if (error != null) throw new SQLException(error);
    }

    public String reservarTurno(int idPaciente, int idMedico, Timestamp fechaHora, String observaciones) {
        if (idPaciente <= 0 || idMedico <= 0 || fechaHora == null) return "Los datos del turno están incompletos.";
        String conflictos = "SELECT "
                + "COUNT(*) FILTER (WHERE id_personal=? AND fechahora=?) AS medico_ocupado, "
                + "COUNT(*) FILTER (WHERE id_paciente=? AND fechahora=?) AS paciente_ocupado "
                + "FROM turno WHERE LOWER(estado) NOT IN ('cancelado','finalizado','ausente')";
        String insertar = "INSERT INTO turno (id_paciente,id_personal,fechahora,observaciones,fechacreacion,estado,id_obrasocial_atencion,numeroafiliado_atencion,cobertura_registrada) "
                + "SELECT ?,?,?,?,CURRENT_DATE,'reservado',p.id_obrasocial,p.numeroafiliado,TRUE FROM paciente p WHERE p.id_paciente=?";
        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try {
                int bloqueHorario = Long.hashCode(fechaHora.getTime());
                bloquear(con, idMedico, bloqueHorario);
                bloquear(con, -idPaciente, bloqueHorario);
                try (PreparedStatement pst = con.prepareStatement(conflictos)) {
                    pst.setInt(1, idMedico);
                    pst.setTimestamp(2, fechaHora);
                    pst.setInt(3, idPaciente);
                    pst.setTimestamp(4, fechaHora);
                    try (ResultSet rs = pst.executeQuery()) {
                        rs.next();
                        if (rs.getInt("medico_ocupado") > 0) return rollback(con, "El médico ya cuenta con un turno asignado en este horario.");
                        if (rs.getInt("paciente_ocupado") > 0) return rollback(con,
                                "El paciente ya tiene un turno en ese mismo horario.");
                    }
                }
                try (PreparedStatement pst = con.prepareStatement(insertar)) {
                    pst.setInt(1, idPaciente);
                    pst.setInt(2, idMedico);
                    pst.setTimestamp(3, fechaHora);
                    pst.setString(4, observaciones == null ? "" : observaciones.trim());
                    pst.setInt(5, idPaciente);
                    pst.executeUpdate();
                }
                con.commit();
                return null;
            } catch (SQLException e) {
                con.rollback();
                if ("23505".equals(e.getSQLState())) return "Ese horario acaba de ser reservado. Elegí otro turno.";
                return "No se pudo guardar el turno: " + e.getMessage();
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            return "No se pudo guardar el turno: " + e.getMessage();
        }
    }

    public boolean estaHorarioOcupado(int idMedico, Timestamp fechaHora) {
        String sql = "SELECT EXISTS(SELECT 1 FROM turno WHERE id_personal=? AND fechahora=? AND LOWER(estado) NOT IN ('cancelado','finalizado','ausente'))";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idMedico);
            pst.setTimestamp(2, fechaHora);
            try (ResultSet rs = pst.executeQuery()) { return rs.next() && rs.getBoolean(1); }
        } catch (SQLException e) {
            System.err.println("Error al verificar disponibilidad: " + e.getMessage());
            return true;
        }
    }

    public String verificarConflictosHorario(int idPaciente, int idMedico, String fechaHoraStr) {
        try {
            Timestamp fechaHora = Timestamp.valueOf(fechaHoraStr);
            String sql = "SELECT COUNT(*) FILTER (WHERE id_personal=? AND fechahora=?) AS medico_ocupado, "
                    + "COUNT(*) FILTER (WHERE id_paciente=? AND fechahora=?) AS paciente_ocupado FROM turno "
                    + "WHERE LOWER(estado) NOT IN ('cancelado','finalizado','ausente')";
            try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, idMedico);
                pst.setTimestamp(2, fechaHora);
                pst.setInt(3, idPaciente);
                pst.setTimestamp(4, fechaHora);
                try (ResultSet rs = pst.executeQuery()) {
                    rs.next();
                    if (rs.getInt("medico_ocupado") > 0) return "El médico ya cuenta con un turno asignado en este mismo horario.";
                    if (rs.getInt("paciente_ocupado") > 0) return "El paciente seleccionado ya tiene un turno en ese mismo horario.";
                }
            }
        } catch (Exception e) {
            return "No se pudo verificar el horario: " + e.getMessage();
        }
        return null;
    }

    public List<Turno> listarTurnos(int idPersonalFiltro) {
        return listar(idPersonalFiltro, null, null, true);
    }

    /**
     * Convierte automáticamente en Ausente una reserva que no fue gestionada
     * dentro de los 30 minutos posteriores a su horario. Una reserva vencida
     * no puede seguir apareciendo como disponible o pendiente de atención.
     */
    public int marcarAusentesVencidos() {
        String sql = "UPDATE turno SET estado='ausente' "
                + "WHERE LOWER(estado)='reservado' "
                + "AND fechahora + INTERVAL '30 minutes' <= CURRENT_TIMESTAMP "
                + "AND NOT EXISTS (SELECT 1 FROM consulta c WHERE c.id_turno=turno.id_turno "
                + "AND LOWER(c.estado::text)='activo')";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            return pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("No se pudieron cerrar los turnos ausentes pendientes: " + e.getMessage());
            return 0;
        }
    }

    public boolean cambiarEstadoTurno(int idTurno, String nuevoEstado) {
        return cambiarEstadoTurno(idTurno, nuevoEstado, false);
    }

    public boolean cambiarEstadoTurno(int idTurno, String nuevoEstado, boolean correccionAdministrador) {
        String estado = normalizarEstado(nuevoEstado);
        if (estado == null) return false;
        String sql = "UPDATE turno SET estado=?, fechacancelacion=CASE WHEN ?='cancelado' THEN CURRENT_DATE ELSE fechacancelacion END "
                + "WHERE id_turno=? "
                + "AND (?<>'atendido' OR EXISTS (SELECT 1 FROM consulta c WHERE c.id_turno=turno.id_turno AND LOWER(c.estado::text)='activo')) "
                + "AND (?='atendido' OR NOT EXISTS (SELECT 1 FROM consulta c WHERE c.id_turno=turno.id_turno AND LOWER(c.estado::text)='activo')) "
                + "AND (? NOT IN ('atendido','finalizado') OR fechahora <= CURRENT_TIMESTAMP) "
                + (correccionAdministrador ? "" :
                    "AND (LOWER(estado) NOT IN ('atendido','finalizado','ausente') OR ?=LOWER(estado)) "
                  + "AND (?<>'cancelado' OR fechahora > CURRENT_TIMESTAMP) "
                  + "AND (?<>'ausente' OR fechahora + INTERVAL '30 minutes' <= CURRENT_TIMESTAMP)");
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, estado);
            pst.setString(2, estado);
            pst.setInt(3, idTurno);
            pst.setString(4, estado);
            pst.setString(5, estado);
            pst.setString(6, estado);
            if (!correccionAdministrador) {
                pst.setString(7, estado);
                pst.setString(8, estado);
                pst.setString(9, estado);
            }
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del turno: " + e.getMessage());
            return false;
        }
    }

    public String validarCambioEstado(int idTurno, String nuevoEstado) {
        return validarCambioEstado(idTurno, nuevoEstado, false);
    }

    public String validarCambioEstado(int idTurno, String nuevoEstado, boolean correccionAdministrador) {
        String estado = normalizarEstado(nuevoEstado);
        if (estado == null) return "El estado seleccionado no es válido.";
        String sql = "SELECT fechahora > CURRENT_TIMESTAMP AS es_futuro, "
                + "fechahora + INTERVAL '30 minutes' <= CURRENT_TIMESTAMP AS puede_ausente, LOWER(estado) AS estado_actual, "
                + "EXISTS (SELECT 1 FROM consulta c WHERE c.id_turno=t.id_turno AND LOWER(c.estado::text)='activo') AS tiene_historia "
                + "FROM turno t WHERE id_turno=?";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idTurno);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) return "El turno ya no existe.";
                String estadoActual = rs.getString("estado_actual");
                if (estado.equals(estadoActual)) return null;
                boolean tieneHistoria = rs.getBoolean("tiene_historia");
                if ("atendido".equals(estado) && !tieneHistoria) {
                    return "El turno solo se marca Atendido cuando se guarda su historia clínica.";
                }
                if (tieneHistoria && !"atendido".equals(estado)) {
                    return "El turno tiene una historia clínica registrada y no puede cancelarse ni retroceder de estado.";
                }
                if ((estado.equals("atendido") || estado.equals("finalizado")) && rs.getBoolean("es_futuro")) {
                    return "No se puede marcar un turno futuro como '" + nuevoEstado + "'.";
                }
                if (correccionAdministrador) return null;
                if ("atendido".equals(estadoActual) || "finalizado".equals(estadoActual) || "ausente".equals(estadoActual)) {
                    return "Un turno " + estadoActual + " es definitivo y no puede cambiarse a otro estado.";
                }
                if (estado.equals("cancelado") && !rs.getBoolean("es_futuro")) {
                    return "El horario del turno ya pasó y no puede cancelarse ni liberarse.";
                }
                if (estado.equals("ausente") && !rs.getBoolean("puede_ausente")) {
                    return "No se puede marcar Ausente hasta que hayan pasado 30 minutos desde el horario del turno.";
                }
            }
        } catch (SQLException e) {
            return "No se pudo validar el horario del turno: " + e.getMessage();
        }
        return null;
    }

    public String validarAccesoHistoria(int idTurno, int idPersonal) {
        String sql = "SELECT LOWER(estado) AS estado, id_personal=? AS medico_asignado FROM turno WHERE id_turno=?";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idPersonal);
            pst.setInt(2, idTurno);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) return "El turno ya no existe.";
                if (!rs.getBoolean("medico_asignado")) return "Solo el médico asignado al turno puede crear su historia clínica.";
                String estado = rs.getString("estado");
                if (!"en consulta".equals(estado) && !"atendido".equals(estado)) {
                    return "La historia clínica solo puede crearse cuando el turno está En Consulta o Atendido.";
                }
            }
        } catch (SQLException e) {
            return "No se pudo validar el acceso a la historia clínica: " + e.getMessage();
        }
        return null;
    }

    public DatosHistoriaTurno obtenerDatosHistoriaTurno(int idTurno) {
        String sql = "SELECT t.id_personal, COALESCE(p.id_especialidad, 0) AS id_especialidad, "
                + "LOWER(COALESCE(t.estado, 'reservado')) AS estado "
                + "FROM turno t INNER JOIN personal p ON p.id_personal=t.id_personal "
                + "WHERE t.id_turno=?";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idTurno);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new DatosHistoriaTurno(
                            rs.getInt("id_personal"),
                            rs.getInt("id_especialidad"),
                            rs.getString("estado")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("No se pudieron obtener datos del turno para historia clínica: " + e.getMessage());
        }
        return null;
    }

    public String prepararTurnoParaHistoria(int idTurno, int idPersonalSesion, boolean administrador) {
        DatosHistoriaTurno datos = obtenerDatosHistoriaTurno(idTurno);
        if (datos == null) return "El turno ya no existe.";
        if (!administrador && datos.idPersonal() != idPersonalSesion) {
            return "Solo el médico asignado al turno puede crear su historia clínica.";
        }
        String estado = datos.estado() == null ? "reservado" : datos.estado().trim().toLowerCase(Locale.ROOT);
        if (!estado.equals("en consulta")) {
            if (estado.equals("atendido") || estado.equals("finalizado")) {
                return "Este turno ya fue atendido y no admite una nueva historia clínica.";
            }
            return "La historia clínica solo puede crearse cuando el turno está En consulta.";
        }
        return null;
    }

    public int contarTurnos(int idMedico) {
        String sql = "SELECT COUNT(*) FROM turno WHERE LOWER(estado) NOT IN ('cancelado','finalizado')" + (idMedico > 0 ? " AND id_personal=?" : "");
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            if (idMedico > 0) pst.setInt(1, idMedico);
            try (ResultSet rs = pst.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) {
            System.err.println("Error al contar turnos: " + e.getMessage());
            return 0;
        }
    }

    public List<Turno> listarTurnosPaginados(int idPersonalFiltro, int limite, int offset) {
        return listar(idPersonalFiltro, Math.max(1, limite), Math.max(0, offset), false);
    }

    /**
     * Devuelve los turnos tal como existen en la base para la pantalla de
     * verificacion. A diferencia de la agenda, no excluye estados terminales ni
     * limita la consulta al dia actual.
     */
    public List<String[]> listarTodosLosTurnos(FiltroHistorial filtro,
            int limite, int offset) throws SQLException {
        List<String[]> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.id_turno, t.fechahora, "
                + "TRIM(COALESCE(p.nombre,'') || ' ' || COALESCE(p.apellido,'')) AS paciente, "
                + "COALESCE(p.dni::text, '') AS dni, per.nombreyapellido AS medico, "
                + "COALESCE(NULLIF(TRIM(o.nombre), ''), 'Particular / Sin Obra Social') AS obra_social, "
                + "CASE WHEN LOWER(COALESCE(t.estado, 'reservado'))='finalizado' "
                + "THEN 'atendido' ELSE LOWER(COALESCE(t.estado, 'reservado')) END AS estado, "
                + "CASE WHEN EXISTS (SELECT 1 FROM consulta c WHERE c.id_turno=t.id_turno) "
                + "THEN 'Sí' ELSE 'No' END AS historia "
                + "FROM turno t "
                + "INNER JOIN paciente p ON p.id_paciente=t.id_paciente "
                + "INNER JOIN personal per ON per.id_personal=t.id_personal "
                + "LEFT JOIN obrasocial o ON o.id_obrasocial=t.id_obrasocial_atencion "
                + "WHERE NOT (LOWER(COALESCE(t.estado,''))='cancelado' AND EXISTS ("
                + "SELECT 1 FROM turno valido WHERE valido.id_turno<>t.id_turno "
                + "AND valido.id_paciente=t.id_paciente AND valido.id_personal=t.id_personal "
                + "AND valido.fechahora=t.fechahora AND LOWER(valido.estado) IN ('atendido','finalizado'))) ");
        List<Object> parametros = agregarFiltrosHistorial(sql, filtro);
        sql.append("ORDER BY t.fechahora DESC, t.id_turno DESC LIMIT ? OFFSET ?");
        parametros.add(Math.max(1, limite));
        parametros.add(Math.max(0, offset));

        Connection con = ConexionBD.getConexion();
        if (con == null) throw new SQLException("No hay conexión disponible con PostgreSQL.");
        try (con; PreparedStatement pst = con.prepareStatement(sql.toString())) {
            asignarParametros(pst, parametros);
            try (ResultSet rs = pst.executeQuery()) {
                SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat hora = new SimpleDateFormat("HH:mm");
                while (rs.next()) {
                    Timestamp fechaHora = rs.getTimestamp("fechahora");
                    lista.add(new String[]{
                        String.valueOf(rs.getInt("id_turno")),
                        fechaHora == null ? "" : fecha.format(fechaHora),
                        fechaHora == null ? "" : hora.format(fechaHora),
                        rs.getString("paciente"), rs.getString("dni"),
                        rs.getString("medico"), rs.getString("obra_social"), rs.getString("estado"),
                        rs.getString("historia")
                    });
                }
            }
        }
        return lista;
    }

    public int contarTodosLosTurnos(FiltroHistorial filtro) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM turno t "
                + "INNER JOIN paciente p ON p.id_paciente=t.id_paciente "
                + "INNER JOIN personal per ON per.id_personal=t.id_personal "
                + "WHERE NOT (LOWER(COALESCE(t.estado,''))='cancelado' AND EXISTS ("
                + "SELECT 1 FROM turno valido WHERE valido.id_turno<>t.id_turno "
                + "AND valido.id_paciente=t.id_paciente AND valido.id_personal=t.id_personal "
                + "AND valido.fechahora=t.fechahora AND LOWER(valido.estado) IN ('atendido','finalizado'))) ");
        List<Object> parametros = agregarFiltrosHistorial(sql, filtro);
        Connection con = ConexionBD.getConexion();
        if (con == null) throw new SQLException("No hay conexión disponible con PostgreSQL.");
        try (con; PreparedStatement pst = con.prepareStatement(sql.toString())) {
            asignarParametros(pst, parametros);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Compatibilidad para llamados existentes; usa la regla vigente de 30 minutos. */
    public int marcarAusentesPendientesDeDiasAnteriores() {
        return marcarAusentesVencidos();
    }

    /** Cuenta personas distintas con los mismos criterios de la vista de verificación. */
    public int contarPacientesUnicos(FiltroHistorial filtro) throws SQLException {
        StringBuilder sql = consultaHistorialBase("SELECT COUNT(DISTINCT t.id_paciente) ");
        List<Object> parametros = agregarFiltrosHistorial(sql, filtro);
        Connection con = ConexionBD.getConexion();
        if (con == null) throw new SQLException("No hay conexión disponible con PostgreSQL.");
        try (con; PreparedStatement pst = con.prepareStatement(sql.toString())) {
            asignarParametros(pst, parametros);
            try (ResultSet rs = pst.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    /** Devuelve el turno más reciente de cada paciente para comprobar estadísticas de pacientes únicos. */
    public List<String[]> listarPacientesUnicos(FiltroHistorial filtro, int limite, int offset) throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String seleccion = "SELECT DISTINCT ON (t.id_paciente) t.id_turno, t.fechahora, "
                + "TRIM(COALESCE(p.nombre,'') || ' ' || COALESCE(p.apellido,'')) AS paciente, "
                + "COALESCE(p.dni::text, '') AS dni, per.nombreyapellido AS medico, "
                + "COALESCE(NULLIF(TRIM(o.nombre), ''), 'Particular / Sin Obra Social') AS obra_social, "
                + "CASE WHEN LOWER(COALESCE(t.estado, 'reservado'))='finalizado' THEN 'atendido' "
                + "ELSE LOWER(COALESCE(t.estado, 'reservado')) END AS estado, "
                + "CASE WHEN EXISTS (SELECT 1 FROM consulta c WHERE c.id_turno=t.id_turno) THEN 'Sí' ELSE 'No' END AS historia ";
        StringBuilder sql = new StringBuilder("SELECT * FROM (");
        sql.append(consultaHistorialBase(seleccion));
        List<Object> parametros = agregarFiltrosHistorial(sql, filtro);
        sql.append("ORDER BY t.id_paciente, t.fechahora DESC, t.id_turno DESC) pacientes_unicos ")
                .append("ORDER BY fechahora DESC, id_turno DESC LIMIT ? OFFSET ?");
        parametros.add(Math.max(1, limite));
        parametros.add(Math.max(0, offset));

        Connection con = ConexionBD.getConexion();
        if (con == null) throw new SQLException("No hay conexión disponible con PostgreSQL.");
        try (con; PreparedStatement pst = con.prepareStatement(sql.toString())) {
            asignarParametros(pst, parametros);
            try (ResultSet rs = pst.executeQuery()) {
                SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat hora = new SimpleDateFormat("HH:mm");
                while (rs.next()) {
                    Timestamp fechaHora = rs.getTimestamp("fechahora");
                    lista.add(new String[]{String.valueOf(rs.getInt("id_turno")),
                        fechaHora == null ? "" : fecha.format(fechaHora), fechaHora == null ? "" : hora.format(fechaHora),
                        rs.getString("paciente"), rs.getString("dni"), rs.getString("medico"), rs.getString("obra_social"),
                        rs.getString("estado"), rs.getString("historia")});
                }
            }
        }
        return lista;
    }

    private static StringBuilder consultaHistorialBase(String seleccion) {
        return new StringBuilder(seleccion
                + "FROM turno t INNER JOIN paciente p ON p.id_paciente=t.id_paciente "
                + "INNER JOIN personal per ON per.id_personal=t.id_personal "
                + "LEFT JOIN obrasocial o ON o.id_obrasocial=t.id_obrasocial_atencion "
                + "WHERE NOT (LOWER(COALESCE(t.estado,''))='cancelado' AND EXISTS ("
                + "SELECT 1 FROM turno valido WHERE valido.id_turno<>t.id_turno "
                + "AND valido.id_paciente=t.id_paciente AND valido.id_personal=t.id_personal "
                + "AND valido.fechahora=t.fechahora AND LOWER(valido.estado) IN ('atendido','finalizado'))) ");
    }

    public List<String> listarEstadosExistentes(Integer idPersonalFiltro) throws SQLException {
        List<String> estados = new ArrayList<>();
        String sql = "SELECT DISTINCT LOWER(COALESCE(estado, 'reservado')) AS estado "
                + "FROM turno "
                + (idPersonalFiltro != null ? "WHERE id_personal=? " : "")
                + "ORDER BY estado";
        Connection con = ConexionBD.getConexion();
        if (con == null) throw new SQLException("No hay conexión disponible con PostgreSQL.");
        try (con; PreparedStatement pst = con.prepareStatement(sql)) {
            if (idPersonalFiltro != null) pst.setInt(1, idPersonalFiltro);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) estados.add(rs.getString("estado"));
            }
        }
        return estados;
    }

    public List<OpcionHistorial> listarMedicosConTurnos(Integer idPersonalFiltro) throws SQLException {
        String sql = "SELECT DISTINCT per.id_personal, per.nombreyapellido "
                + "FROM turno t INNER JOIN personal per ON per.id_personal=t.id_personal "
                + (idPersonalFiltro != null ? "WHERE t.id_personal=? " : "")
                + "ORDER BY per.nombreyapellido";
        return listarOpcionesHistorial(sql, idPersonalFiltro);
    }

    public List<OpcionHistorial> listarPacientesConTurnos(Integer idPersonalFiltro) throws SQLException {
        String sql = "SELECT DISTINCT p.id_paciente, "
                + "TRIM(COALESCE(p.nombre,'') || ' ' || COALESCE(p.apellido,'')) AS nombre, "
                + "COALESCE(p.dni::text, '') AS dni "
                + "FROM turno t INNER JOIN paciente p ON p.id_paciente=t.id_paciente "
                + (idPersonalFiltro != null ? "WHERE t.id_personal=? " : "")
                + "ORDER BY nombre";
        List<OpcionHistorial> opciones = new ArrayList<>();
        Connection con = ConexionBD.getConexion();
        if (con == null) throw new SQLException("No hay conexión disponible con PostgreSQL.");
        try (con; PreparedStatement pst = con.prepareStatement(sql)) {
            if (idPersonalFiltro != null) pst.setInt(1, idPersonalFiltro);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    opciones.add(new OpcionHistorial(rs.getInt("id_paciente"), nombre,
                            nombre + " " + rs.getString("dni")));
                }
            }
        }
        return opciones;
    }

    public List<OpcionHistorial> listarObrasSocialesConTurnos(Integer idPersonalFiltro) throws SQLException {
        List<OpcionHistorial> opciones = new ArrayList<>();
        String sql = "SELECT DISTINCT COALESCE(o.id_obrasocial, -1) AS id, "
                + "COALESCE(NULLIF(TRIM(o.nombre), ''), 'Particular / Sin Obra Social') AS nombre "
                + "FROM turno t INNER JOIN paciente p ON p.id_paciente=t.id_paciente "
                + "LEFT JOIN obrasocial o ON o.id_obrasocial=t.id_obrasocial_atencion "
                + (idPersonalFiltro != null ? "WHERE t.id_personal=? " : "")
                + "ORDER BY nombre";
        Connection con = ConexionBD.getConexion();
        if (con == null) throw new SQLException("No hay conexión disponible con PostgreSQL.");
        try (con; PreparedStatement pst = con.prepareStatement(sql)) {
            if (idPersonalFiltro != null) pst.setInt(1, idPersonalFiltro);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) opciones.add(new OpcionHistorial(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return opciones;
    }

    private List<OpcionHistorial> listarOpcionesHistorial(String sql,
            Integer idPersonalFiltro) throws SQLException {
        List<OpcionHistorial> opciones = new ArrayList<>();
        Connection con = ConexionBD.getConexion();
        if (con == null) throw new SQLException("No hay conexión disponible con PostgreSQL.");
        try (con; PreparedStatement pst = con.prepareStatement(sql)) {
            if (idPersonalFiltro != null) pst.setInt(1, idPersonalFiltro);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) opciones.add(new OpcionHistorial(rs.getInt(1), rs.getString(2)));
            }
        }
        return opciones;
    }

    private static List<Object> agregarFiltrosHistorial(StringBuilder sql, FiltroHistorial filtro) {
        List<Object> parametros = new ArrayList<>();
        if (filtro.idPersonalPermitido() != null) {
            sql.append("AND t.id_personal=? ");
            parametros.add(filtro.idPersonalPermitido());
        }
        if (filtro.desde() != null) {
            sql.append("AND t.fechahora::date>=? ");
            parametros.add(new java.sql.Date(filtro.desde().getTime()));
        }
        if (filtro.hasta() != null) {
            sql.append("AND t.fechahora::date<=? ");
            parametros.add(new java.sql.Date(filtro.hasta().getTime()));
        }
        if (filtro.idsMedicos() != null && !filtro.idsMedicos().isEmpty()) {
            agregarFiltroLista(sql, parametros, "t.id_personal", filtro.idsMedicos());
        }
        if (filtro.idsPacientes() != null && !filtro.idsPacientes().isEmpty()) {
            agregarFiltroLista(sql, parametros, "t.id_paciente", filtro.idsPacientes());
        }
        if (filtro.idsObrasSociales() != null && !filtro.idsObrasSociales().isEmpty()) {
            boolean incluyeParticular = filtro.idsObrasSociales().contains(-1);
            List<Integer> idsConObra = new ArrayList<>();
            for (Integer id : filtro.idsObrasSociales()) if (id != null && id != -1) idsConObra.add(id);
            sql.append("AND (");
            if (!idsConObra.isEmpty()) {
                agregarCondicionLista(sql, parametros, "t.id_obrasocial_atencion", idsConObra);
                if (incluyeParticular) sql.append(" OR ");
            }
            if (incluyeParticular) sql.append("t.id_obrasocial_atencion IS NULL");
            sql.append(") ");
        }
        if (filtro.estados() != null && !filtro.estados().isEmpty()) {
            List<String> estadosNormalizados = new ArrayList<>();
            for (String estado : filtro.estados()) {
                if (estado != null && !estado.isBlank()) estadosNormalizados.add(estado.trim().toLowerCase(Locale.ROOT));
            }
            List<String> estadosReales = new ArrayList<>();
            for (String estado : estadosNormalizados) if (!"__sin_asistencia__".equals(estado)) estadosReales.add(estado);
            boolean noAtendidos = estadosNormalizados.contains("__sin_asistencia__");
            if (noAtendidos && !estadosReales.isEmpty()) {
                sql.append("AND (CASE WHEN LOWER(COALESCE(t.estado, 'reservado'))='finalizado' ")
                        .append("THEN 'atendido' ELSE LOWER(COALESCE(t.estado, 'reservado')) END ");
                agregarCondicionLista(sql, parametros, "", estadosReales);
                sql.append(" OR LOWER(COALESCE(t.estado, 'reservado')) NOT IN ('atendido','finalizado')) ");
            } else if (noAtendidos) {
                sql.append("AND LOWER(COALESCE(t.estado, 'reservado')) NOT IN ('atendido','finalizado') ");
            } else if (!estadosReales.isEmpty()) {
                // La vista de estadísticas considera "finalizado" como una atención realizada.
                // Se aplica la misma equivalencia en el historial para que ambos módulos
                // respondan igual ante el filtro visible "Atendido".
                agregarFiltroLista(sql, parametros,
                        "CASE WHEN LOWER(COALESCE(t.estado, 'reservado'))='finalizado' "
                                + "THEN 'atendido' ELSE LOWER(COALESCE(t.estado, 'reservado')) END",
                        estadosReales);
            }
        } else {
            // La vista inicial muestra turnos vigentes o efectivamente realizados.
            // Las cancelaciones permanecen auditables al elegir el estado Cancelado.
            sql.append("AND LOWER(COALESCE(t.estado, 'reservado')) <> 'cancelado' ");
        }
        if (filtro.historias() != null && filtro.historias().size() == 1) {
            boolean conHistoria = Boolean.TRUE.equals(filtro.historias().get(0));
            sql.append(conHistoria ? "AND EXISTS " : "AND NOT EXISTS ");
            sql.append("(SELECT 1 FROM consulta c WHERE c.id_turno=t.id_turno) ");
        }
        return parametros;
    }

    private static void agregarFiltroLista(StringBuilder sql, List<Object> parametros,
            String columna, List<?> valores) {
        sql.append("AND ");
        agregarCondicionLista(sql, parametros, columna, valores);
        sql.append(' ');
    }

    private static void agregarCondicionLista(StringBuilder sql, List<Object> parametros,
            String columna, List<?> valores) {
        sql.append(columna).append(" IN (");
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) sql.append(',');
            sql.append('?');
            parametros.add(valores.get(i));
        }
        sql.append(')');
    }

    private static void asignarParametros(PreparedStatement pst, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            Object valor = parametros.get(i);
            if (valor instanceof Integer) pst.setInt(i + 1, (Integer) valor);
            else if (valor instanceof java.sql.Date) pst.setDate(i + 1, (java.sql.Date) valor);
            else pst.setString(i + 1, String.valueOf(valor));
        }
    }

    public List<String[]> listarDatosEstadisticas(Integer idPersonalFiltro) {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT t.fechahora, "
                + "TRIM(COALESCE(p.nombre,'') || ' ' || COALESCE(p.apellido,'')) AS paciente, "
                + "per.nombreyapellido AS medico, "
                + "COALESCE(NULLIF(TRIM(o.nombre), ''), 'Particular / Sin Obra Social') AS obra_social, "
                + "CASE WHEN LOWER(COALESCE(t.estado, 'reservado'))='finalizado' "
                + "THEN 'atendido' ELSE LOWER(COALESCE(t.estado, 'reservado')) END AS estado, "
                + "t.id_paciente, t.id_personal "
                + "FROM turno t "
                + "INNER JOIN paciente p ON p.id_paciente = t.id_paciente "
                + "INNER JOIN personal per ON per.id_personal = t.id_personal "
                + "LEFT JOIN obrasocial o ON t.id_obrasocial_atencion = o.id_obrasocial "
                + "WHERE NOT (LOWER(COALESCE(t.estado,''))='cancelado' AND EXISTS ("
                + "SELECT 1 FROM turno valido WHERE valido.id_turno<>t.id_turno "
                + "AND valido.id_paciente=t.id_paciente AND valido.id_personal=t.id_personal "
                + "AND valido.fechahora=t.fechahora AND LOWER(valido.estado) IN ('atendido','finalizado'))) "
                + (idPersonalFiltro != null && idPersonalFiltro > 0 ? "AND t.id_personal = ? " : "")
                + "ORDER BY t.fechahora DESC";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            if (idPersonalFiltro != null && idPersonalFiltro > 0) {
                pst.setInt(1, idPersonalFiltro);
            }

            try (ResultSet rs = pst.executeQuery()) {
                SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yy");
                SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");
                while (rs.next()) {
                    Timestamp fechaHora = rs.getTimestamp("fechahora");
                    String fecha = fechaHora != null ? formatoFecha.format(fechaHora) : "";
                    String hora = fechaHora != null ? formatoHora.format(fechaHora) : "";
                    String estado = rs.getString("estado");

                    lista.add(new String[]{
                        fecha,
                        rs.getString("paciente"),
                        rs.getString("medico"),
                        hora,
                        rs.getString("obra_social"),
                        estado == null ? "reservado" : estado,
                        String.valueOf(rs.getInt("id_paciente")),
                        String.valueOf(rs.getInt("id_personal"))
                });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar datos de estadisticas: " + e.getMessage());
        }
        return lista;
    }

    private List<Turno> listar(int idPersonal, Integer limite, Integer offset, boolean soloFuturos) {
        List<Turno> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT t.id_turno,t.fechahora,t.estado,t.observaciones,p.nombre,p.apellido,med.nombreyapellido AS medico "
                + "FROM turno t JOIN paciente p ON p.id_paciente=t.id_paciente JOIN personal med ON med.id_personal=t.id_personal "
                + "WHERE LOWER(t.estado) NOT IN ('cancelado','finalizado') AND LOWER(p.estado::text)='activo'");
        if (soloFuturos) sql.append(" AND t.fechahora::date>=CURRENT_DATE");
        if (idPersonal > 0) sql.append(" AND t.id_personal=?");
        sql.append(" ORDER BY t.fechahora ASC");
        if (limite != null) sql.append(" LIMIT ? OFFSET ?");
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (idPersonal > 0) pst.setInt(i++, idPersonal);
            if (limite != null) { pst.setInt(i++, limite); pst.setInt(i, offset); }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar turnos: " + e.getMessage());
        }
        return lista;
    }

    public List<String> obtenerHorariosDisponibles(int idMedico, java.util.Date fecha) {
        List<String> disponibles = new ArrayList<>();
        if (fecha == null || idMedico <= 0) return disponibles;
        Calendar dia = Calendar.getInstance();
        dia.setTime(fecha);
        String[] dias = {"", "domingo", "lunes", "martes", "miercoles", "jueves", "viernes", "sabado"};
        String sqlHorario = "SELECT horainicio,horafin,duracionturnominutos FROM horario WHERE id_personal=? AND diasemana::text ILIKE ? AND estado::text='activo'";
        String sqlOcupados = "SELECT fechahora FROM turno WHERE id_personal=? AND fechahora::date=? AND LOWER(estado) NOT IN ('cancelado','finalizado','ausente')";
        try (Connection con = ConexionBD.getConexion()) {
            Set<String> ocupados = new HashSet<>();
            try (PreparedStatement pst = con.prepareStatement(sqlOcupados)) {
                pst.setInt(1, idMedico);
                pst.setDate(2, new Date(fecha.getTime()));
                try (ResultSet rs = pst.executeQuery()) {
                    SimpleDateFormat hora = new SimpleDateFormat("HH:mm");
                    while (rs.next()) ocupados.add(hora.format(rs.getTimestamp(1)));
                }
            }
            try (PreparedStatement pst = con.prepareStatement(sqlHorario)) {
                pst.setInt(1, idMedico);
                pst.setString(2, dias[dia.get(Calendar.DAY_OF_WEEK)]);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) agregarRango(disponibles, ocupados, dia, rs.getTime(1), rs.getTime(2), rs.getInt(3));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener horarios disponibles: " + e.getMessage());
        }
        return disponibles;
    }

    private static void agregarRango(List<String> disponibles, Set<String> ocupados, Calendar fecha, Time inicio, Time fin, int duracion) {
        if (inicio == null || fin == null) return;
        if (duracion <= 0) duracion = 30;
        Calendar desde = Calendar.getInstance(); desde.setTime(inicio);
        Calendar hasta = Calendar.getInstance(); hasta.setTime(fin);
        SimpleDateFormat formato = new SimpleDateFormat("HH:mm", Locale.ROOT);
        for (long actual = desde.getTimeInMillis(); actual <= hasta.getTimeInMillis(); actual += duracion * 60_000L) {
            Calendar hora = Calendar.getInstance(); hora.setTimeInMillis(actual);
            Calendar turno = (Calendar) fecha.clone();
            turno.set(Calendar.HOUR_OF_DAY, hora.get(Calendar.HOUR_OF_DAY));
            turno.set(Calendar.MINUTE, hora.get(Calendar.MINUTE));
            turno.set(Calendar.SECOND, 0); turno.set(Calendar.MILLISECOND, 0);
            String valor = formato.format(turno.getTime());
            if (turno.getTimeInMillis() >= System.currentTimeMillis() && !ocupados.contains(valor)) disponibles.add(valor);
        }
    }

    private static Turno mapear(ResultSet rs) throws SQLException {
        Turno t = new Turno();
        t.setIdTurno(rs.getInt("id_turno"));
        t.setFechaHora(rs.getTimestamp("fechahora"));
        t.setEstadoTurno(rs.getString("estado"));
        t.setObservaciones(rs.getString("observaciones"));
        t.setPacienteNombreCompleto(rs.getString("nombre") + " " + rs.getString("apellido"));
        t.setMedicoNombreCompleto(rs.getString("medico"));
        return t;
    }

    private static void bloquear(Connection con, int entidad, int horario) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("SELECT pg_advisory_xact_lock(?,?)")) {
            pst.setInt(1, entidad); pst.setInt(2, horario); pst.executeQuery();
        }
    }

    private static String rollback(Connection con, String mensaje) throws SQLException { con.rollback(); return mensaje; }

    private static String normalizarEstado(String estado) {
        if (estado == null) return null;
        String valor = estado.trim().toLowerCase(Locale.ROOT);
        if (valor.equals("pendiente")) valor = "reservado";
        return Set.of("reservado", "esperando", "en consulta", "atendido", "cancelado", "ausente", "finalizado").contains(valor) ? valor : null;
    }
}
