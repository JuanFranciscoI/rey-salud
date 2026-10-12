package clinicabalbarrey.dao;

import clinicabalbarrey.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class RolDAO {
    private RolDAO() {}

    public static int obtenerId(String nombre) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return obtenerId(con, nombre);
        }
    }

    public static int obtenerId(Connection con, String nombre) throws SQLException {
        String normalizado = normalizarNombre(nombre);
        String sql = "SELECT id_rol FROM rol WHERE LOWER(nombre) = LOWER(?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, normalizado);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No existe el rol configurado: " + normalizado);
    }

    private static String normalizarNombre(String nombre) {
        if (nombre == null) return "";
        if (nombre.toLowerCase().startsWith("secretar")) return "Secretaria";
        if (nombre.toLowerCase().startsWith("médico") || nombre.toLowerCase().startsWith("medico")) return "Médico";
        return nombre.trim();
    }
}
