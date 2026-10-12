package clinicabalbarrey.tools;

import clinicabalbarrey.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Corrige datos de prueba incoherentes: un turno futuro no puede figurar como
 * atendido/finalizado ni tener historia clínica activa.
 */
public final class LimpiarTurnosFuturosInvalidos {
    private LimpiarTurnosFuturosInvalidos() {}

    public static void main(String[] args) throws Exception {
        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try {
                int turnosInvalidos = contarTurnosInvalidos(con);
                ejecutar(con, "ALTER TABLE turno DISABLE TRIGGER trg_turno_clinico_inmutable");
                int historiasDesactivadas = desactivarHistoriasFuturas(con);
                int turnosCancelados = cancelarTurnosFuturosInvalidos(con);
                ejecutar(con, "ALTER TABLE turno ENABLE TRIGGER trg_turno_clinico_inmutable");
                con.commit();
                System.out.println("Turnos futuros atendidos/finalizados encontrados: " + turnosInvalidos);
                System.out.println("Historias clínicas futuras desactivadas: " + historiasDesactivadas);
                System.out.println("Turnos futuros inválidos ocultados como cancelados: " + turnosCancelados);
            } catch (Exception e) {
                try {
                    ejecutar(con, "ALTER TABLE turno ENABLE TRIGGER trg_turno_clinico_inmutable");
                } catch (Exception ignored) {
                    // Si la transacción se revierte, PostgreSQL también revierte el cambio del trigger.
                }
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private static void ejecutar(Connection con, String sql) throws Exception {
        try (Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }

    private static int contarTurnosInvalidos(Connection con) throws Exception {
        String sql = "SELECT COUNT(*) FROM turno "
                + "WHERE fechahora > CURRENT_TIMESTAMP "
                + "AND LOWER(COALESCE(estado,'')) IN ('atendido','finalizado')";
        try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static int desactivarHistoriasFuturas(Connection con) throws Exception {
        String sql = "UPDATE consulta c SET estado='inactivo' "
                + "WHERE LOWER(COALESCE(c.estado::text,''))='activo' "
                + "AND EXISTS (SELECT 1 FROM turno t WHERE t.id_turno=c.id_turno "
                + "AND t.fechahora > CURRENT_TIMESTAMP "
                + "AND LOWER(COALESCE(t.estado,'')) IN ('atendido','finalizado'))";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            return pst.executeUpdate();
        }
    }

    private static int cancelarTurnosFuturosInvalidos(Connection con) throws Exception {
        String sql = "UPDATE turno SET estado='cancelado', fechacancelacion=CURRENT_DATE "
                + "WHERE fechahora > CURRENT_TIMESTAMP "
                + "AND LOWER(COALESCE(estado,'')) IN ('atendido','finalizado')";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            return pst.executeUpdate();
        }
    }
}
