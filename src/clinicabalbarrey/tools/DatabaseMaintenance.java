package clinicabalbarrey.tools;

import clinicabalbarrey.ConexionBD;
import clinicabalbarrey.security.PasswordUtil;
import clinicabalbarrey.storage.ArchivoAdjuntoService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseMaintenance {
    private DatabaseMaintenance() {}

    public static void main(String[] args) throws Exception {
        Path migracion = Path.of("database", "migration_2026_06_23.sql");
        String sql = Files.readString(migracion, StandardCharsets.UTF_8);
        try (Connection con = ConexionBD.getConexion(); Statement st = con.createStatement()) {
            st.execute(sql);
        }
        int migradas = migrarContrasenasLegacy();
        int adjuntos = migrarAdjuntosLegacy();
        System.out.println("Migración aplicada. Contraseñas protegidas: " + migradas + ". Consultas con adjuntos migrados: " + adjuntos);
    }

    static int migrarContrasenasLegacy() throws Exception {
        List<ClaveLegacy> pendientes = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement pst = con.prepareStatement("SELECT id_usuario,contrasena FROM usuario");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String clave = rs.getString("contrasena");
                if (!PasswordUtil.esHash(clave)) pendientes.add(new ClaveLegacy(rs.getInt("id_usuario"), clave));
            }
        }
        if (pendientes.isEmpty()) return 0;
        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement pst = con.prepareStatement("UPDATE usuario SET contrasena=? WHERE id_usuario=?")) {
                for (ClaveLegacy item : pendientes) {
                    pst.setString(1, PasswordUtil.hash(item.clave().toCharArray()));
                    pst.setInt(2, item.id());
                    pst.addBatch();
                }
                pst.executeBatch();
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
        return pendientes.size();
    }

    static int migrarAdjuntosLegacy() throws Exception {
        List<AdjuntoLegacy> cambios = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement pst = con.prepareStatement("SELECT id_consulta,ruta_archivo FROM consulta WHERE ruta_archivo IS NOT NULL AND TRIM(ruta_archivo)<>''");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String original = rs.getString("ruta_archivo");
                List<String> nuevas = new ArrayList<>();
                boolean cambio = false;
                for (String parte : original.split("\\s*\\|\\s*")) {
                    Path ruta = Path.of(parte.trim());
                    if (ruta.isAbsolute() && Files.isRegularFile(ruta)) {
                        nuevas.add(ArchivoAdjuntoService.copiar(List.of(ruta.toFile())));
                        cambio = true;
                    } else nuevas.add(parte.trim());
                }
                if (cambio) cambios.add(new AdjuntoLegacy(rs.getInt("id_consulta"), String.join(" | ", nuevas)));
            }
        }
        if (cambios.isEmpty()) return 0;
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement("UPDATE consulta SET ruta_archivo=? WHERE id_consulta=?")) {
            con.setAutoCommit(false);
            try {
                for (AdjuntoLegacy cambio : cambios) {
                    pst.setString(1, cambio.ruta()); pst.setInt(2, cambio.idConsulta()); pst.addBatch();
                }
                pst.executeBatch(); con.commit();
            } catch (Exception e) { con.rollback(); throw e; }
            finally { con.setAutoCommit(true); }
        }
        return cambios.size();
    }

    private record ClaveLegacy(int id, String clave) {}
    private record AdjuntoLegacy(int idConsulta, String ruta) {}
}
