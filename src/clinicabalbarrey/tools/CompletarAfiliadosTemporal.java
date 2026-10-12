package clinicabalbarrey.tools;

import clinicabalbarrey.ConexionBD;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public final class CompletarAfiliadosTemporal {
    public static void main(String[] args) throws Exception {
        try (Connection con = ConexionBD.getConexion(); Statement st = con.createStatement()) {
            con.setAutoCommit(false);
            try {
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS respaldo_afiliados_20260826 (
                        id_paciente INTEGER PRIMARY KEY,
                        numeroafiliado_anterior VARCHAR(100),
                        respaldado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)
                    """);
                int respaldados = st.executeUpdate("""
                    INSERT INTO respaldo_afiliados_20260826 (id_paciente, numeroafiliado_anterior)
                    SELECT id_paciente,numeroafiliado FROM paciente
                    WHERE id_obrasocial IS NOT NULL
                      AND (NULLIF(TRIM(COALESCE(numeroafiliado,'')),'') IS NULL OR TRIM(numeroafiliado)='-')
                    ON CONFLICT (id_paciente) DO NOTHING
                    """);
                int actualizados = st.executeUpdate("""
                    UPDATE paciente SET numeroafiliado=
                        LPAD((90000000+id_paciente)::TEXT,8,'0')||'-'||
                        LPAD((((id_paciente*37)%97)+1)::TEXT,2,'0')
                    WHERE id_obrasocial IS NOT NULL
                      AND (NULLIF(TRIM(COALESCE(numeroafiliado,'')),'') IS NULL OR TRIM(numeroafiliado)='-')
                    """);
                con.commit();
                System.out.println("RESPALDADOS=" + respaldados);
                System.out.println("ACTUALIZADOS=" + actualizados);
                try (ResultSet rs = st.executeQuery("""
                    SELECT COUNT(*) total,
                           COUNT(*) FILTER (WHERE id_obrasocial IS NOT NULL
                             AND (NULLIF(TRIM(COALESCE(numeroafiliado,'')),'') IS NULL OR TRIM(numeroafiliado)='-')) pendientes,
                           COUNT(DISTINCT numeroafiliado) numeros_unicos
                    FROM paciente
                    """)) {
                    rs.next();
                    System.out.println("TOTAL=" + rs.getInt(1));
                    System.out.println("PENDIENTES=" + rs.getInt(2));
                    System.out.println("NUMEROS_UNICOS=" + rs.getInt(3));
                }
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        }
    }
}
