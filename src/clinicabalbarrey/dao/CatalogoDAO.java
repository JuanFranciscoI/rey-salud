package clinicabalbarrey.dao;

import clinicabalbarrey.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CatalogoDAO {

    public List<String> obtenerDiagnosticosActivos() {
        List<String> lista = new ArrayList<>();
        lista.add("Seleccione un diagnóstico...");
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "SELECT descripcion FROM catalogo_diagnostico WHERE estado = 'ACTIVO' ORDER BY descripcion ASC";
            PreparedStatement pst = cn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("descripcion"));
            }
        } catch (Exception e) {
            System.out.println("Error al obtener diagnósticos: " + e.getMessage());
        }
        return lista;
    }

    public List<String> obtenerDiagnosticosActivosPorEspecialidad(int idEspecialidad) {
        List<String> lista = new ArrayList<>();
        lista.add("Seleccione un diagnóstico...");
        if (idEspecialidad <= 0) return obtenerDiagnosticosActivos();
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "SELECT d.descripcion "
                    + "FROM catalogo_diagnostico d "
                    + "INNER JOIN catalogo_diagnostico_especialidad de ON de.id_diagnostico = d.id "
                    + "WHERE d.estado = 'ACTIVO' AND de.id_especialidad = ? "
                    + "ORDER BY d.descripcion ASC";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, idEspecialidad);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("descripcion"));
            }
        } catch (Exception e) {
            System.out.println("Error al obtener diagnósticos por especialidad: " + e.getMessage());
        }
        return lista;
    }

    public List<String> obtenerDiagnosticosInactivos() {
        List<String> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "SELECT descripcion FROM catalogo_diagnostico WHERE estado = 'INACTIVO' ORDER BY descripcion ASC";
            PreparedStatement pst = cn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) lista.add(rs.getString("descripcion"));
        } catch (Exception e) {
            System.out.println("Error al obtener diagnósticos ocultos: " + e.getMessage());
        }
        return lista;
    }

    public List<String> obtenerDiagnosticosInactivosPorEspecialidad(int idEspecialidad) {
        List<String> lista = new ArrayList<>();
        if (idEspecialidad <= 0) return obtenerDiagnosticosInactivos();
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "SELECT d.descripcion "
                    + "FROM catalogo_diagnostico d "
                    + "INNER JOIN catalogo_diagnostico_especialidad de ON de.id_diagnostico = d.id "
                    + "WHERE d.estado = 'INACTIVO' AND de.id_especialidad = ? "
                    + "ORDER BY d.descripcion ASC";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, idEspecialidad);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) lista.add(rs.getString("descripcion"));
        } catch (Exception e) {
            System.out.println("Error al obtener diagnósticos ocultos por especialidad: " + e.getMessage());
        }
        return lista;
    }

    public List<String> obtenerMedicamentosActivos() {
        List<String> lista = new ArrayList<>();
        lista.add("Seleccione un medicamento...");
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "SELECT descripcion FROM catalogo_medicamento WHERE estado = 'ACTIVO' ORDER BY descripcion ASC";
            PreparedStatement pst = cn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("descripcion"));
            }
        } catch (Exception e) {
            System.out.println("Error al obtener medicamentos: " + e.getMessage());
        }
        return lista;
    }

    public List<String> obtenerMedicamentosActivosPorEspecialidad(int idEspecialidad) {
        List<String> lista = new ArrayList<>();
        lista.add("Seleccione un medicamento...");
        if (idEspecialidad <= 0) return obtenerMedicamentosActivos();
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "SELECT m.descripcion "
                    + "FROM catalogo_medicamento m "
                    + "INNER JOIN catalogo_medicamento_especialidad me ON me.id_medicamento = m.id "
                    + "WHERE m.estado = 'ACTIVO' AND me.id_especialidad = ? "
                    + "ORDER BY m.descripcion ASC";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, idEspecialidad);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("descripcion"));
            }
        } catch (Exception e) {
            System.out.println("Error al obtener medicamentos por especialidad: " + e.getMessage());
        }
        return lista;
    }

    public List<String> obtenerMedicamentosInactivos() {
        List<String> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "SELECT descripcion FROM catalogo_medicamento WHERE estado = 'INACTIVO' ORDER BY descripcion ASC";
            PreparedStatement pst = cn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) lista.add(rs.getString("descripcion"));
        } catch (Exception e) {
            System.out.println("Error al obtener medicamentos ocultos: " + e.getMessage());
        }
        return lista;
    }

    public List<String> obtenerMedicamentosInactivosPorEspecialidad(int idEspecialidad) {
        List<String> lista = new ArrayList<>();
        if (idEspecialidad <= 0) return obtenerMedicamentosInactivos();
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "SELECT m.descripcion "
                    + "FROM catalogo_medicamento m "
                    + "INNER JOIN catalogo_medicamento_especialidad me ON me.id_medicamento = m.id "
                    + "WHERE m.estado = 'INACTIVO' AND me.id_especialidad = ? "
                    + "ORDER BY m.descripcion ASC";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, idEspecialidad);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) lista.add(rs.getString("descripcion"));
        } catch (Exception e) {
            System.out.println("Error al obtener medicamentos ocultos por especialidad: " + e.getMessage());
        }
        return lista;
    }

    public boolean insertarDiagnostico(String descripcion) {
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "INSERT INTO catalogo_diagnostico (descripcion, estado) VALUES (?, 'ACTIVO') "
                    + "ON CONFLICT (descripcion) DO UPDATE SET estado = 'ACTIVO'";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, descripcion);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al insertar diagnóstico: " + e.getMessage());
            return false;
        }
    }

    public boolean insertarDiagnostico(String descripcion, int idEspecialidad) {
        if (idEspecialidad <= 0) return insertarDiagnostico(descripcion);
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "WITH item AS ("
                    + " INSERT INTO catalogo_diagnostico (descripcion, estado) VALUES (?, 'ACTIVO') "
                    + " ON CONFLICT (descripcion) DO UPDATE SET estado = 'ACTIVO' "
                    + " RETURNING id"
                    + ") "
                    + "INSERT INTO catalogo_diagnostico_especialidad (id_diagnostico, id_especialidad) "
                    + "SELECT id, ? FROM item ON CONFLICT DO NOTHING";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, descripcion);
            pst.setInt(2, idEspecialidad);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al insertar diagnóstico por especialidad: " + e.getMessage());
            return false;
        }
    }

    public boolean restaurarDiagnostico(String descripcion) {
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "UPDATE catalogo_diagnostico SET estado = 'ACTIVO' WHERE descripcion = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, descripcion);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al restaurar diagnóstico: " + e.getMessage());
            return false;
        }
    }

    public boolean restaurarDiagnostico(String descripcion, int idEspecialidad) {
        if (idEspecialidad <= 0) return restaurarDiagnostico(descripcion);
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "WITH actualizado AS ("
                    + " UPDATE catalogo_diagnostico SET estado = 'ACTIVO' WHERE descripcion = ? RETURNING id"
                    + "), vinculo AS ("
                    + "INSERT INTO catalogo_diagnostico_especialidad (id_diagnostico, id_especialidad) "
                    + "SELECT id, ? FROM actualizado ON CONFLICT DO NOTHING"
                    + " RETURNING 1"
                    + ") SELECT COUNT(*) FROM actualizado";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, descripcion);
            pst.setInt(2, idEspecialidad);
            ResultSet rs = pst.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            System.out.println("Error al restaurar diagnóstico por especialidad: " + e.getMessage());
            return false;
        }
    }

    public boolean insertarMedicamento(String descripcion) {
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "INSERT INTO catalogo_medicamento (descripcion, estado) VALUES (?, 'ACTIVO') "
                    + "ON CONFLICT (descripcion) DO UPDATE SET estado = 'ACTIVO'";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, descripcion);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al insertar medicamento: " + e.getMessage());
            return false;
        }
    }

    public boolean insertarMedicamento(String descripcion, int idEspecialidad) {
        if (idEspecialidad <= 0) return insertarMedicamento(descripcion);
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "WITH item AS ("
                    + " INSERT INTO catalogo_medicamento (descripcion, estado) VALUES (?, 'ACTIVO') "
                    + " ON CONFLICT (descripcion) DO UPDATE SET estado = 'ACTIVO' "
                    + " RETURNING id"
                    + ") "
                    + "INSERT INTO catalogo_medicamento_especialidad (id_medicamento, id_especialidad) "
                    + "SELECT id, ? FROM item ON CONFLICT DO NOTHING";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, descripcion);
            pst.setInt(2, idEspecialidad);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al insertar medicamento por especialidad: " + e.getMessage());
            return false;
        }
    }

    public boolean restaurarMedicamento(String descripcion) {
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "UPDATE catalogo_medicamento SET estado = 'ACTIVO' WHERE descripcion = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, descripcion);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al restaurar medicamento: " + e.getMessage());
            return false;
        }
    }

    public boolean restaurarMedicamento(String descripcion, int idEspecialidad) {
        if (idEspecialidad <= 0) return restaurarMedicamento(descripcion);
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "WITH actualizado AS ("
                    + " UPDATE catalogo_medicamento SET estado = 'ACTIVO' WHERE descripcion = ? RETURNING id"
                    + "), vinculo AS ("
                    + "INSERT INTO catalogo_medicamento_especialidad (id_medicamento, id_especialidad) "
                    + "SELECT id, ? FROM actualizado ON CONFLICT DO NOTHING"
                    + " RETURNING 1"
                    + ") SELECT COUNT(*) FROM actualizado";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, descripcion);
            pst.setInt(2, idEspecialidad);
            ResultSet rs = pst.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            System.out.println("Error al restaurar medicamento por especialidad: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarDiagnostico(String descripcion) {
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "UPDATE catalogo_diagnostico SET estado = 'INACTIVO' WHERE descripcion = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, descripcion);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al ocultar diagnóstico: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarMedicamento(String descripcion) {
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "UPDATE catalogo_medicamento SET estado = 'INACTIVO' WHERE descripcion = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, descripcion);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al ocultar medicamento: " + e.getMessage());
            return false;
        }
    }

    public boolean editarDiagnostico(String viejo, String nuevo) {
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "UPDATE catalogo_diagnostico SET descripcion = ? WHERE descripcion = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, nuevo);
            pst.setString(2, viejo);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al editar diagnóstico: " + e.getMessage());
            return false;
        }
    }

    public boolean editarMedicamento(String viejo, String nuevo) {
        try (Connection cn = ConexionBD.getConexion()) {
            String sql = "UPDATE catalogo_medicamento SET descripcion = ? WHERE descripcion = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, nuevo);
            pst.setString(2, viejo);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error al editar medicamento: " + e.getMessage());
            return false;
        }
    }
}
