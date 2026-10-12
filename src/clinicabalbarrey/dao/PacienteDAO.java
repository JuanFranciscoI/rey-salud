package clinicabalbarrey.dao;

import clinicabalbarrey.ConexionBD;
import clinicabalbarrey.model.Paciente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    public List<Paciente> listarTodos() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT p.*, o.nombre as nombre_obra_social FROM paciente p " +
                     "LEFT JOIN obrasocial o ON p.id_obrasocial = o.id_obrasocial " +
                     "WHERE p.estado = 'activo'";

        try (Connection con = ConexionBD.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Paciente p = new Paciente();
                p.setId_paciente(rs.getInt("id_paciente"));
                p.setDni(rs.getString("dni"));
                p.setNombre(rs.getString("nombre"));
                p.setApellido(rs.getString("apellido"));
                p.setFechaNacimiento(rs.getDate("fechanacimiento"));
                p.setSexo(rs.getString("sexo"));
                p.setTelefono(rs.getString("telefono"));
                p.setEmail(rs.getString("email"));
                p.setDireccion(rs.getString("direccion"));
                p.setEstado(rs.getString("estado"));

                p.setIdObraSocial(rs.getInt("id_obrasocial"));
                p.setObraSocialNombre(rs.getString("nombre_obra_social"));
                p.setNumeroAfiliado(rs.getString("numeroafiliado"));

                lista.add(p);
            }
        } catch (Exception e) { System.err.println("Error listar pacientes: " + e.getMessage()); }
        return lista;
    }

    public Paciente obtenerPorId(int idPaciente) {
        String sql = "SELECT p.*, o.nombre as nombre_obra_social FROM paciente p " +
                     "LEFT JOIN obrasocial o ON p.id_obrasocial = o.id_obrasocial " +
                     "WHERE p.id_paciente = ? AND p.estado = 'activo'";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idPaciente);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Paciente p = new Paciente();
                p.setId_paciente(rs.getInt("id_paciente"));
                p.setDni(rs.getString("dni"));
                p.setNombre(rs.getString("nombre"));
                p.setApellido(rs.getString("apellido"));
                p.setFechaNacimiento(rs.getDate("fechanacimiento"));
                p.setSexo(rs.getString("sexo"));
                p.setTelefono(rs.getString("telefono"));
                p.setEmail(rs.getString("email"));
                p.setDireccion(rs.getString("direccion"));
                p.setEstado(rs.getString("estado"));
                p.setIdObraSocial(rs.getInt("id_obrasocial"));
                p.setObraSocialNombre(rs.getString("nombre_obra_social"));
                p.setNumeroAfiliado(rs.getString("numeroafiliado"));
                return p;
            }
        } catch (Exception e) { System.err.println("Error obtenerPorId: " + e.getMessage()); }
        return null;
    }

    public Paciente obtenerPorDni(String dni) {
        String sql = "SELECT p.*, o.nombre AS nombre_obra_social FROM paciente p "
                + "LEFT JOIN obrasocial o ON o.id_obrasocial=p.id_obrasocial "
                + "WHERE TRIM(p.dni)=TRIM(?) AND p.estado::text='activo'";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, dni);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? mapearCompleto(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar paciente por DNI: " + e.getMessage());
            return null;
        }
    }

    public void registrarPaciente(Paciente p) throws SQLException {
        String sql = "INSERT INTO paciente (dni, nombre, apellido, fechanacimiento, sexo, email, telefono, direccion, estado, id_obrasocial, numeroafiliado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'activo', ?, ?)";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, p.getDni());
            pst.setString(2, p.getNombre());
            pst.setString(3, p.getApellido());
            pst.setDate(4, p.getFechaNacimiento());
            pst.setString(5, p.getSexo());
            pst.setString(6, p.getEmail());
            pst.setString(7, p.getTelefono());
            pst.setString(8, p.getDireccion());

            if (p.getIdObraSocial() > 0) {
                pst.setInt(9, p.getIdObraSocial());
            } else {
                pst.setNull(9, java.sql.Types.INTEGER);
            }
            pst.setString(10, p.getNumeroAfiliado());

            pst.executeUpdate();
        }
    }

    public void actualizarPaciente(Paciente p, int idOriginal) throws SQLException {
        String sql = "UPDATE paciente SET dni=?, nombre=?, apellido=?, fechanacimiento=?, sexo=?, email=?, telefono=?, direccion=?, id_obrasocial=?, numeroafiliado=? WHERE id_paciente=?";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, p.getDni());
            pst.setString(2, p.getNombre());
            pst.setString(3, p.getApellido());
            pst.setDate(4, p.getFechaNacimiento());
            pst.setString(5, p.getSexo());
            pst.setString(6, p.getEmail());
            pst.setString(7, p.getTelefono());
            pst.setString(8, p.getDireccion());

            if (p.getIdObraSocial() > 0) {
                pst.setInt(9, p.getIdObraSocial());
            } else {
                pst.setNull(9, java.sql.Types.INTEGER);
            }
            pst.setString(10, p.getNumeroAfiliado());
            pst.setInt(11, idOriginal);

            pst.executeUpdate();
        }
    }

    public boolean ocultarPaciente(int idPaciente) {
        String sql = "UPDATE paciente SET estado = 'inactivo' WHERE id_paciente = ? AND estado::text = 'activo'";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idPaciente);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al ocultar paciente: " + e.getMessage());
            return false;
        }
    }

    // --- MÉTODOS NUEVOS PARA LA PAGINACIÓN ---
    public List<Paciente> listarPacientesOcultos() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT p.*, o.nombre as nombre_obra_social FROM paciente p "
                + "LEFT JOIN obrasocial o ON p.id_obrasocial = o.id_obrasocial "
                + "WHERE p.estado::text = 'inactivo' "
                + "ORDER BY p.apellido, p.nombre";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Paciente p = new Paciente();
                p.setId_paciente(rs.getInt("id_paciente"));
                p.setDni(rs.getString("dni"));
                p.setNombre(rs.getString("nombre"));
                p.setApellido(rs.getString("apellido"));
                p.setTelefono(rs.getString("telefono"));
                p.setEstado(rs.getString("estado"));
                p.setObraSocialNombre(rs.getString("nombre_obra_social"));
                lista.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar pacientes ocultos: " + e.getMessage());
        }
        return lista;
    }

    public boolean restaurarPaciente(int idPaciente) {
        String sql = "UPDATE paciente SET estado = 'activo' WHERE id_paciente = ? AND estado::text = 'inactivo'";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idPaciente);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al restaurar paciente: " + e.getMessage());
            return false;
        }
    }

    public int contarPacientes(String filtro) {
        return contarPacientes(filtro, null);
    }

    public int contarPacientes(String filtro, Integer idPersonal) {
        return contarPacientes(filtro, idPersonal, "activo");
    }

    public int contarPacientes(String filtro, Integer idPersonal, String estadoPaciente) {
        int total = 0;
        boolean filtrar = filtro != null && !filtro.isBlank();
        String condicionFiltro = " AND (CAST(dni AS TEXT) ILIKE ? OR nombre ILIKE ? OR apellido ILIKE ? OR (nombre || ' ' || apellido) ILIKE ? OR (apellido || ' ' || nombre) ILIKE ?)";
        String condicionEstado = condicionEstadoPaciente("estado", estadoPaciente);
        String condicionMedico = idPersonal != null
                ? " AND EXISTS (SELECT 1 FROM consulta c WHERE c.id_paciente=paciente.id_paciente "
                        + "AND c.id_personal=? AND LOWER(c.estado)='activo')"
                : "";
        String sql = "SELECT COUNT(*) FROM paciente WHERE 1=1 " + condicionEstado
                + condicionMedico + (filtrar ? condicionFiltro : "");
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            int indice = 1;
            if (idPersonal != null) pst.setInt(indice++, idPersonal);
            if (filtrar) {
                String patron = "%" + filtro.trim() + "%";
                for (int i = 0; i < 5; i++) pst.setString(indice++, patron);
            }
            try (ResultSet rs = pst.executeQuery()) { if (rs.next()) total = rs.getInt(1); }
        } catch (SQLException e) {
            System.err.println("Error al contar pacientes: " + e.getMessage());
        }
        return total;
    }

    public List<Paciente> listarPacientesPaginados(String filtro, int limite, int offset) {
        return listarPacientesPaginados(filtro, limite, offset, null);
    }

    public List<Paciente> listarPacientesPaginados(String filtro, int limite, int offset, Integer idPersonal) {
        return listarPacientesPaginados(filtro, limite, offset, idPersonal, "activo");
    }

    public List<Paciente> listarPacientesPaginados(String filtro, int limite, int offset, Integer idPersonal, String estadoPaciente) {
        List<Paciente> lista = new ArrayList<>();
        boolean filtrar = filtro != null && !filtro.isBlank();
        String condicionFiltro = " AND (CAST(p.dni AS TEXT) ILIKE ? OR p.nombre ILIKE ? OR p.apellido ILIKE ? OR (p.nombre || ' ' || p.apellido) ILIKE ? OR (p.apellido || ' ' || p.nombre) ILIKE ?)";
        String condicionEstado = condicionEstadoPaciente("p.estado", estadoPaciente);
        String condicionMedico = idPersonal != null
                ? " AND EXISTS (SELECT 1 FROM consulta c WHERE c.id_paciente=p.id_paciente "
                        + "AND c.id_personal=? AND LOWER(c.estado)='activo')"
                : "";
        String sql = "SELECT p.*, o.nombre as nombre_obra_social FROM paciente p " +
                     "LEFT JOIN obrasocial o ON p.id_obrasocial = o.id_obrasocial " +
                     "WHERE 1=1 " + condicionEstado + condicionMedico + (filtrar ? condicionFiltro : "") +
                     " ORDER BY p.apellido, p.nombre LIMIT ? OFFSET ?";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            int i = 1;
            if (idPersonal != null) pst.setInt(i++, idPersonal);
            if (filtrar) {
                String patron = "%" + filtro.trim() + "%";
                for (int j = 0; j < 5; j++) pst.setString(i++, patron);
            }
            pst.setInt(i++, Math.max(1, limite));
            pst.setInt(i, Math.max(0, offset));
            try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Paciente p = new Paciente();
                p.setId_paciente(rs.getInt("id_paciente"));
                p.setDni(rs.getString("dni"));
                p.setNombre(rs.getString("nombre"));
                p.setApellido(rs.getString("apellido"));
                p.setTelefono(rs.getString("telefono"));
                p.setEstado(rs.getString("estado"));
                p.setObraSocialNombre(rs.getString("nombre_obra_social"));
                lista.add(p);
            }
            }
        } catch (Exception e) { System.err.println("Error paginando: " + e.getMessage()); }
        return lista;
    }

    private Paciente mapearCompleto(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();
        p.setId_paciente(rs.getInt("id_paciente"));
        p.setDni(rs.getString("dni"));
        p.setNombre(rs.getString("nombre"));
        p.setApellido(rs.getString("apellido"));
        p.setFechaNacimiento(rs.getDate("fechanacimiento"));
        p.setSexo(rs.getString("sexo"));
        p.setTelefono(rs.getString("telefono"));
        p.setEmail(rs.getString("email"));
        p.setDireccion(rs.getString("direccion"));
        p.setEstado(rs.getString("estado"));
        p.setIdObraSocial(rs.getInt("id_obrasocial"));
        p.setObraSocialNombre(rs.getString("nombre_obra_social"));
        p.setNumeroAfiliado(rs.getString("numeroafiliado"));
        return p;
    }

    private String condicionEstadoPaciente(String columnaEstado, String estadoPaciente) {
        if (estadoPaciente == null || estadoPaciente.isBlank()
                || "todos".equalsIgnoreCase(estadoPaciente)) {
            return "";
        }
        String estado = estadoPaciente.trim().toLowerCase();
        if (!estado.equals("activo") && !estado.equals("inactivo")) {
            estado = "activo";
        }
        return " AND " + columnaEstado + "::text='" + estado + "' ";
    }
}
