package clinicabalbarrey.tools;

import clinicabalbarrey.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public final class LimpiarHistoriasTemporal {
    private static void actualizar(Connection con, int id, String enfermedad, String tratamiento) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE consulta SET enfermedad=COALESCE(?,enfermedad),tratamiento=? WHERE id_consulta=?")) {
            ps.setString(1, enfermedad);
            ps.setString(2, tratamiento);
            ps.setInt(3, id);
            if (ps.executeUpdate()!=1) throw new IllegalStateException("No se encontró consulta " + id);
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection con=ConexionBD.getConexion(); Statement st=con.createStatement()) {
            con.setAutoCommit(false);
            try {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS respaldo_consultas_limpieza_20260826 AS SELECT * FROM consulta WITH NO DATA");
                int respaldo=st.executeUpdate("INSERT INTO respaldo_consultas_limpieza_20260826 SELECT c.* FROM consulta c WHERE c.id_consulta IN (60,61,63,70,75) AND NOT EXISTS (SELECT 1 FROM respaldo_consultas_limpieza_20260826 r WHERE r.id_consulta=c.id_consulta)");
                actualizar(con,60,"H40 - Glaucoma agudo | Inicio: 02/06/2026 | Fin: 12/06/2026 | Estado: Agudo","ALIVIAPRES (Dorzolamida / Timolol) | Dosis: 1 gota por ojo | Frec: Cada 8 horas | Inicio: 02/06/2026 | Fin: 12/06/2026 | Estado: Actual");
                actualizar(con,61,"H04 - Síndrome de ojo seco | Inicio: 02/06/2026 | Fin: 10/06/2026 | Estado: Crónico","Lágrimas artificiales | Dosis: 1 gota por ojo | Frec: Cada 6 horas | Inicio: 02/06/2026 | Fin: 06/06/2026 | Estado: Actual");
                actualizar(con,63,"[H52 - Hipermetropía | Inicio: 03/06/2026 | Fin: 17/06/2026 | Est: Crónico]","[Corrección óptica con anteojos | Dosis: Según refracción | Frec: Uso diario | Inicio: 03/06/2026 | Fin: 03/06/2027 | Est: Actual]");
                actualizar(con,70,null,"[Control metabólico y de retina | Dosis: No aplica | Frec: Cada 90 días | Inicio: 18/06/2026 | Fin: 16/09/2026 | Est: Actual]");
                actualizar(con,75,null,"[Compresas tibias | Dosis: 10 minutos | Frec: Cada 8 horas | Inicio: 24/06/2026 | Fin: 01/07/2026 | Est: Finalizado]");
                con.commit();
                try (ResultSet rs=st.executeQuery("SELECT COUNT(*) FROM respaldo_consultas_limpieza_20260826")) { rs.next(); System.out.println("RESPALDADAS="+rs.getInt(1)); }
                System.out.println("ACTUALIZADAS=5");
            } catch(Exception ex) { con.rollback(); throw ex; }
        }
    }
}
