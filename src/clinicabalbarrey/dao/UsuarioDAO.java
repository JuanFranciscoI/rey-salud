package clinicabalbarrey.dao;

import clinicabalbarrey.ConexionBD;
import clinicabalbarrey.model.Usuario;
import clinicabalbarrey.security.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.username, u.id_personal, u.id_rol, r.nombre AS cargo, "
                + "p.nombreyapellido, p.dni, p.matricula "
                + "FROM usuario u "
                + "LEFT JOIN personal p ON p.id_personal = u.id_personal "
                + "LEFT JOIN rol r ON r.id_rol = u.id_rol "
                + "WHERE u.estado::text = 'activo' ORDER BY u.username";
        try (Connection con = ConexionBD.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapearListado(rs));
        } catch (SQLException e) {
            System.err.println("Error SQL en listarTodos: " + e.getMessage());
        }
        return lista;
    }

    public List<Usuario> listarPersonalSinUsuario() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT p.id_personal, p.nombreyapellido, p.dni "
                + "FROM personal p LEFT JOIN usuario u ON u.id_personal = p.id_personal "
                + "WHERE p.estado::text = 'activo' AND u.id_usuario IS NULL "
                + "ORDER BY p.nombreyapellido";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId_personal(rs.getInt("id_personal"));
                u.setDni(rs.getString("dni"));
                separarNombre(u, rs.getString("nombreyapellido"));
                lista.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar personal sin usuario: " + e.getMessage());
        }
        return lista;
    }

    public Usuario validarLogin(String username, String password) {
        String sql = "SELECT u.id_usuario, u.username, u.id_rol, u.id_personal, u.contrasena, "
                + "p.nombreyapellido, p.id_especialidad, r.nombre AS rol "
                + "FROM usuario u "
                + "LEFT JOIN personal p ON p.id_personal = u.id_personal "
                + "INNER JOIN rol r ON r.id_rol = u.id_rol "
                + "WHERE LOWER(u.username) = LOWER(?) AND u.estado::text = 'activo' "
                + "AND (u.id_personal IS NULL OR p.estado::text = 'activo')";
        char[] clave = password == null ? new char[0] : password.toCharArray();
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) return null;
                String almacenada = rs.getString("contrasena");
                if (!PasswordUtil.verificar(clave, almacenada)) return null;

                Usuario u = new Usuario();
                u.setId_usuario(rs.getInt("id_usuario"));
                u.setUsername(rs.getString("username"));
                u.setId_rol(rs.getInt("id_rol"));
                u.setId_personal(rs.getInt("id_personal"));
                u.setRol(rs.getString("rol"));
                u.setIdEspecialidad(rs.getInt("id_especialidad"));
                separarNombre(u, rs.getString("nombreyapellido"));

                if (!PasswordUtil.esHash(almacenada)) actualizarHashLegacy(con, u.getId_usuario(), clave);
                try (PreparedStatement acceso = con.prepareStatement("UPDATE usuario SET ultimoacceso = CURRENT_TIMESTAMP WHERE id_usuario = ?")) {
                    acceso.setInt(1, u.getId_usuario());
                    acceso.executeUpdate();
                }
                return u;
            }
        } catch (SQLException e) {
            System.err.println("Error en validarLogin: " + e.getMessage());
            return null;
        } finally {
            java.util.Arrays.fill(clave, '\0');
        }
    }

    public Usuario obtenerPorDni(String dni) {
        String sql = "SELECT p.*, u.id_usuario, u.username, u.id_rol, r.nombre AS rol "
                + "FROM personal p LEFT JOIN usuario u ON p.id_personal = u.id_personal AND u.estado::text = 'activo' "
                + "LEFT JOIN rol r ON r.id_rol = u.id_rol WHERE TRIM(p.dni) = TRIM(?)";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, dni);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) return null;
                Usuario u = new Usuario();
                u.setId_personal(rs.getInt("id_personal"));
                u.setDni(rs.getString("dni"));
                separarNombre(u, rs.getString("nombreyapellido"));
                u.setMatricula(rs.getString("matricula"));
                u.setTelefono(rs.getString("telefono"));
                u.setEmail(rs.getString("correoelectronico"));
                u.setDireccion(rs.getString("direccion"));
                u.setIdEspecialidad(rs.getInt("id_especialidad"));
                u.setId_usuario(rs.getInt("id_usuario"));
                u.setUsername(rs.getString("username"));
                u.setId_rol(rs.getInt("id_rol"));
                u.setRol(rs.getString("rol"));
                return u;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por DNI: " + e.getMessage());
            return null;
        }
    }

    public boolean actualizarPersonal(Usuario u, String dniOriginal) {
        String sql = "UPDATE personal SET dni=?, nombreyapellido=?, matricula=?, telefono=?, correoelectronico=?, direccion=?, id_especialidad=? WHERE TRIM(dni)=TRIM(?)";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, u.getDni());
            pst.setString(2, (u.getNombre() + " " + u.getApellido()).trim());
            pst.setString(3, u.getMatricula());
            if (u.getTelefono() == null || u.getTelefono().isBlank()) pst.setNull(4, java.sql.Types.NUMERIC);
            else pst.setLong(4, Long.parseLong(u.getTelefono().trim()));
            pst.setString(5, u.getEmail());
            pst.setString(6, u.getDireccion());
            if (u.getIdEspecialidad() > 0) pst.setInt(7, u.getIdEspecialidad()); else pst.setNull(7, java.sql.Types.INTEGER);
            pst.setString(8, dniOriginal);
            return pst.executeUpdate() > 0;
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error al actualizar personal: " + e.getMessage());
            return false;
        }
    }

    public boolean cambiarContrasenaPorDni(String dni, String nuevaContrasena) {
        String sql = "UPDATE usuario SET contrasena=? WHERE id_personal=(SELECT id_personal FROM personal WHERE TRIM(dni)=TRIM(?))";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, PasswordUtil.hash(nuevaContrasena.toCharArray()));
            pst.setString(2, dni);
            return pst.executeUpdate() > 0;
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error al cambiar contraseña: " + e.getMessage());
            return false;
        }
    }

    public boolean crearCuentaAcceso(int idPersonal, String user, String pass, int rol) {
        if (idPersonal <= 0) return false;
        String sql = "INSERT INTO usuario (username, contrasena, id_personal, id_rol, estado) VALUES (?, ?, ?, ?, 'activo')";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, user);
            pst.setString(2, PasswordUtil.hash(pass.toCharArray()));
            pst.setInt(3, idPersonal);
            pst.setInt(4, rol);
            return pst.executeUpdate() > 0;
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error al crear cuenta: " + e.getMessage());
            return false;
        }
    }

    public boolean darDeBajaUsuario(int idUsuario, int idUsuarioSesion) {
        if (idUsuario <= 0 || idUsuario == idUsuarioSesion) return false;
        String sql = "UPDATE usuario SET estado='inactivo' WHERE id_usuario=? AND id_usuario<>?";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idUsuario);
            pst.setInt(2, idUsuarioSesion);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al dar de baja usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarUsuario(Usuario u, String nuevaPass) {
        boolean cambiarPass = nuevaPass != null && !nuevaPass.isBlank();
        String sql = cambiarPass
                ? "UPDATE usuario SET username=?, id_rol=?, contrasena=? WHERE id_usuario=?"
                : "UPDATE usuario SET username=?, id_rol=? WHERE id_usuario=?";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, u.getUsername());
            pst.setInt(2, u.getId_rol());
            if (cambiarPass) {
                pst.setString(3, PasswordUtil.hash(nuevaPass.toCharArray()));
                pst.setInt(4, u.getId_usuario());
            } else pst.setInt(3, u.getId_usuario());
            return pst.executeUpdate() > 0;
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean insertar(Usuario u, String password, boolean activo) {
        return u != null && crearCuentaAcceso(u.getId_personal(), u.getUsername(), password, u.getId_rol());
    }

    public Usuario obtenerUsuarioPorId(int idUsuario) {
        String sql = "SELECT u.id_usuario,u.username,u.id_rol,u.id_personal,r.nombre AS rol FROM usuario u JOIN rol r ON r.id_rol=u.id_rol WHERE u.id_usuario=?";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idUsuario);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) return null;
                Usuario u = new Usuario();
                u.setId_usuario(rs.getInt("id_usuario"));
                u.setUsername(rs.getString("username"));
                u.setId_rol(rs.getInt("id_rol"));
                u.setId_personal(rs.getInt("id_personal"));
                u.setRol(rs.getString("rol"));
                return u;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario: " + e.getMessage());
            return null;
        }
    }

    private static Usuario mapearListado(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId_usuario(rs.getInt("id_usuario"));
        u.setUsername(rs.getString("username"));
        u.setId_personal(rs.getInt("id_personal"));
        u.setId_rol(rs.getInt("id_rol"));
        u.setRol(rs.getString("cargo"));
        u.setDni(rs.getString("dni"));
        u.setMatricula(rs.getString("matricula"));
        separarNombre(u, rs.getString("nombreyapellido"));
        return u;
    }

    private static void separarNombre(Usuario u, String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            u.setNombre("Usuario del sistema");
            u.setApellido("");
            return;
        }
        String[] partes = nombreCompleto.trim().split("\\s+", 2);
        u.setNombre(partes[0]);
        u.setApellido(partes.length > 1 ? partes[1] : "");
    }

    private static void actualizarHashLegacy(Connection con, int idUsuario, char[] password) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("UPDATE usuario SET contrasena=? WHERE id_usuario=?")) {
            pst.setString(1, PasswordUtil.hash(password));
            pst.setInt(2, idUsuario);
            pst.executeUpdate();
        }
    }
}
