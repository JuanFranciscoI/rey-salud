package clinicabalbarrey.dao;

import clinicabalbarrey.ConexionBD;
import clinicabalbarrey.model.Personal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonalDAO {

    public int insertarYObtenerId(Personal p) {
        String sql = "INSERT INTO personal (dni, nombreyapellido, matricula, telefono, correoelectronico, direccion, id_especialidad, estado) " +
                     "VALUES (?, ?, ?, ?::numeric, ?, ?, ?, 'activo'::estado_enum)";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, p.getDni());
            pst.setString(2, p.getNombreYApellido());
            pst.setString(3, p.getMatricula());
            pst.setString(4, p.getTelefono());
            pst.setString(5, p.getCorreoElectronico());
            pst.setString(6, p.getDireccion());
            if (p.getIdEspecialidad() > 0) pst.setInt(7, p.getIdEspecialidad());
            else pst.setNull(7, Types.INTEGER);

            if (pst.executeUpdate() > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    public List<String[]> listarEspecialidades() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id_especialidad, nombre FROM especialidad ORDER BY nombre ASC";
        try (Connection con = ConexionBD.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new String[]{rs.getString(1), rs.getString(2)});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public List<Object[]> listarTodos() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT p.id_personal, p.dni, p.nombreyapellido, p.matricula, e.nombre as especialidad " +
                     "FROM personal p LEFT JOIN especialidad e ON p.id_especialidad = e.id_especialidad " +
                     "WHERE p.estado = 'activo'::estado_enum ORDER BY p.nombreyapellido ASC";
        try (Connection con = ConexionBD.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public int contarPersonal(String filtro) {
        return contarPersonal(filtro, null);
    }

    public int contarPersonal(String filtro, List<String> estadosAcceso) {
        int total = 0;
        String sql = "SELECT COUNT(*) as total FROM personal p " +
                     "LEFT JOIN LATERAL (" +
                     " SELECT (u.estado::text = 'activo') AS activo, TRUE AS tiene_usuario" +
                     " FROM usuario u WHERE u.id_personal = p.id_personal" +
                     " ORDER BY (u.estado::text = 'activo') DESC, u.id_usuario DESC LIMIT 1" +
                     ") acceso ON TRUE WHERE p.estado::text = 'activo'";

        if (filtro != null && !filtro.trim().isEmpty()) {
            sql += " AND (p.nombreyapellido ILIKE ? OR CAST(p.dni AS TEXT) ILIKE ?)";
        }
        sql += condicionAcceso(estadosAcceso);

        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            int indice = 1;
            if (filtro != null && !filtro.trim().isEmpty()) {
                pst.setString(indice++, "%" + filtro.trim() + "%");
                pst.setString(indice++, "%" + filtro.trim() + "%");
            }
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) total = rs.getInt("total");
            }
        } catch (Exception e) {
            System.err.println("Error contando personal: " + e.getMessage());
        }
        return total;
    }

    // --- ACÁ OCURRE LA MAGIA DEL CRUCE DE TABLAS (JOIN) ---
    public List<Personal> listarPersonalPaginado(String filtro, int limite, int offset) {
        return listarPersonalPaginado(filtro, limite, offset, null);
    }

    public List<Personal> listarPersonalPaginado(String filtro, int limite, int offset,
                                                 List<String> estadosAcceso) {
        List<Personal> lista = new ArrayList<>();

        // LATERAL elige un solo acceso por persona: prioriza el activo y conserva el rol del inactivo.
        String sql = "SELECT p.*, COALESCE(acceso.rol_nombre, p.tipo_personal) AS rol_nombre, COALESCE(acceso.activo, FALSE) AS acceso_activo, " +
                     "COALESCE(acceso.tiene_usuario, FALSE) AS tiene_usuario, e.nombre AS especialidad_nombre " +
                     "FROM personal p " +
                     "LEFT JOIN LATERAL (" +
                     " SELECT r.nombre AS rol_nombre, (u.estado::text = 'activo') AS activo, TRUE AS tiene_usuario" +
                     " FROM usuario u LEFT JOIN rol r ON r.id_rol = u.id_rol" +
                     " WHERE u.id_personal = p.id_personal" +
                     " ORDER BY (u.estado::text = 'activo') DESC, u.id_usuario DESC LIMIT 1" +
                     ") acceso ON TRUE " +
                     "LEFT JOIN especialidad e ON p.id_especialidad = e.id_especialidad " +
                     "WHERE p.estado::text = 'activo'";

        if (filtro != null && !filtro.trim().isEmpty()) {
            sql += " AND (p.nombreyapellido ILIKE ? OR CAST(p.dni AS TEXT) ILIKE ?)";
        }
        sql += condicionAcceso(estadosAcceso);
        sql += " ORDER BY p.nombreyapellido ASC LIMIT ? OFFSET ?";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            int paramIndex = 1;

            if (filtro != null && !filtro.trim().isEmpty()) {
                pst.setString(paramIndex++, "%" + filtro.trim() + "%");
                pst.setString(paramIndex++, "%" + filtro.trim() + "%");
            }
            pst.setInt(paramIndex++, limite);
            pst.setInt(paramIndex++, offset);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Personal p = new Personal();
                    p.setIdPersonal(rs.getInt("id_personal"));
                    p.setDni(rs.getString("dni"));
                    p.setNombreYApellido(rs.getString("nombreyapellido"));
                    p.setTelefono(rs.getString("telefono"));
                    p.setEstado(rs.getString("estado"));

                    // Extraemos el nombre del rol. Si no tiene usuario asignado, dirá "Sin asignar"
                    String nombreRol = rs.getString("rol_nombre");
                    p.setRol(nombreRol != null ? nombreRol : "Sin asignar");
                    p.setAccesoActivo(rs.getBoolean("acceso_activo"));
                    p.setTieneUsuario(rs.getBoolean("tiene_usuario"));
                    String especialidad = rs.getString("especialidad_nombre");
                    p.setEspecialidad(especialidad != null ? especialidad : "");

                    lista.add(p);
                }
            }
        } catch (Exception e) {
            System.err.println("Error listando personal paginado: " + e.getMessage());
        }
        return lista;
    }

    private String condicionAcceso(List<String> estadosAcceso) {
        if (estadosAcceso == null || estadosAcceso.isEmpty()) return "";
        List<String> condiciones = new ArrayList<>();
        if (estadosAcceso.contains("activo")) {
            condiciones.add("(COALESCE(acceso.tiene_usuario, FALSE) AND COALESCE(acceso.activo, FALSE))");
        }
        if (estadosAcceso.contains("inactivo")) {
            condiciones.add("(COALESCE(acceso.tiene_usuario, FALSE) AND NOT COALESCE(acceso.activo, FALSE))");
        }
        if (estadosAcceso.contains("sin_usuario")) {
            condiciones.add("(NOT COALESCE(acceso.tiene_usuario, FALSE))");
        }
        return condiciones.isEmpty() ? "" : " AND (" + String.join(" OR ", condiciones) + ")";
    }

    public Personal buscarPorId(int idPersonal) {
        Personal p = null;
        String sql = "SELECT * FROM personal WHERE id_personal = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPersonal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = new Personal();
                    p.setIdPersonal(rs.getInt("id_personal"));
                    p.setDni(rs.getString("dni"));
                    p.setNombreYApellido(rs.getString("nombreyapellido"));
                    p.setTelefono(rs.getString("telefono"));
                    p.setCorreoElectronico(rs.getString("correoelectronico"));
                    p.setDireccion(rs.getString("direccion"));
                    p.setMatricula(rs.getString("matricula"));
                    p.setIdEspecialidad(rs.getInt("id_especialidad"));
                    p.setRol(rs.getString("tipo_personal"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al buscar por ID: " + e.getMessage());
        }
        return p;
    }

    public boolean ocultarPersonal(int idPersonal) {
        String sqlPersonal = "UPDATE personal SET estado = 'inactivo' WHERE id_personal = ? AND estado::text = 'activo'";
        String sqlUsuario = "UPDATE usuario SET estado = 'inactivo' WHERE id_personal = ? AND estado::text = 'activo'";
        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try {
                int filasPersonal;
                try (PreparedStatement pst = con.prepareStatement(sqlPersonal)) {
                    pst.setInt(1, idPersonal);
                    filasPersonal = pst.executeUpdate();
                }
                try (PreparedStatement pst = con.prepareStatement(sqlUsuario)) {
                    pst.setInt(1, idPersonal);
                    pst.executeUpdate();
                }
                con.commit();
                return filasPersonal > 0;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error al ocultar personal: " + e.getMessage());
            return false;
        }
    }

    public List<Personal> listarPersonalOculto() {
        List<Personal> lista = new ArrayList<>();
        String sql = "SELECT p.id_personal, p.dni, p.nombreyapellido, p.telefono, acceso.rol_nombre " +
                     "FROM personal p " +
                     "LEFT JOIN LATERAL (" +
                     " SELECT r.nombre AS rol_nombre FROM usuario u " +
                     " LEFT JOIN rol r ON u.id_rol = r.id_rol " +
                     " WHERE u.id_personal = p.id_personal " +
                     " ORDER BY u.id_usuario DESC LIMIT 1" +
                     ") acceso ON TRUE " +
                     "WHERE p.estado::text = 'inactivo' " +
                     "ORDER BY p.nombreyapellido ASC";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Personal p = new Personal();
                p.setIdPersonal(rs.getInt("id_personal"));
                p.setDni(rs.getString("dni"));
                p.setNombreYApellido(rs.getString("nombreyapellido"));
                p.setTelefono(rs.getString("telefono"));
                String nombreRol = rs.getString("rol_nombre");
                p.setRol(nombreRol != null ? nombreRol : "Sin asignar");
                lista.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar personal oculto: " + e.getMessage());
        }
        return lista;
    }

    public boolean restaurarPersonal(int idPersonal) {
        String sqlPersonal = "UPDATE personal SET estado = 'activo' WHERE id_personal = ? AND estado::text = 'inactivo'";
        String sqlUsuario = "UPDATE usuario SET estado = 'activo' WHERE id_personal = ? AND estado::text = 'inactivo'";
        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try {
                int filasPersonal;
                try (PreparedStatement pst = con.prepareStatement(sqlPersonal)) {
                    pst.setInt(1, idPersonal);
                    filasPersonal = pst.executeUpdate();
                }
                try (PreparedStatement pst = con.prepareStatement(sqlUsuario)) {
                    pst.setInt(1, idPersonal);
                    pst.executeUpdate();
                }
                con.commit();
                return filasPersonal > 0;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error al restaurar personal: " + e.getMessage());
            return false;
        }
    }
}
