package clinicabalbarrey.tools;

import clinicabalbarrey.ConexionBD;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public final class AuditoriaPresentacionTemporal {
    private static void consulta(Statement st, String titulo, String sql) throws Exception {
        System.out.println("\n== " + titulo + " ==");
        try (ResultSet rs = st.executeQuery(sql)) {
            int columnas = rs.getMetaData().getColumnCount();
            int filas = 0;
            while (rs.next()) {
                filas++;
                StringBuilder linea = new StringBuilder();
                for (int i = 1; i <= columnas; i++) {
                    if (i > 1) linea.append(" | ");
                    linea.append(rs.getMetaData().getColumnLabel(i)).append('=').append(rs.getString(i));
                }
                System.out.println(linea);
            }
            if (filas == 0) System.out.println("SIN HALLAZGOS");
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection con = ConexionBD.getConexion(); Statement st = con.createStatement()) {
            consulta(st, "Pacientes y afiliacion", """
                SELECT COUNT(*) total,
                       COUNT(*) FILTER (WHERE id_obrasocial IS NOT NULL) con_obra,
                       COUNT(*) FILTER (WHERE id_obrasocial IS NOT NULL AND NULLIF(TRIM(COALESCE(numeroafiliado,'')),'') IS NULL) con_obra_sin_numero,
                       COUNT(*) FILTER (WHERE id_obrasocial IS NULL AND COALESCE(numeroafiliado,'') NOT IN ('','-')) particulares_con_numero
                FROM paciente
                """);
            consulta(st, "Detalle con obra sin afiliado", """
                SELECT p.id_paciente,p.dni,p.nombre,p.apellido,o.nombre obra_social,p.numeroafiliado
                FROM paciente p JOIN obrasocial o ON o.id_obrasocial=p.id_obrasocial
                WHERE NULLIF(TRIM(COALESCE(p.numeroafiliado,'')),'') IS NULL
                ORDER BY p.id_paciente
                """);
            consulta(st, "DNI duplicados", """
                SELECT TRIM(dni::text) dni,COUNT(*) cantidad FROM paciente
                GROUP BY TRIM(dni::text) HAVING COUNT(*)>1 ORDER BY cantidad DESC
                """);
            consulta(st, "Campos obligatorios vacios de pacientes", """
                SELECT id_paciente,dni,nombre,apellido,telefono FROM paciente
                WHERE NULLIF(TRIM(COALESCE(dni::text,'')),'') IS NULL
                   OR NULLIF(TRIM(COALESCE(nombre,'')),'') IS NULL
                   OR NULLIF(TRIM(COALESCE(apellido,'')),'') IS NULL
                   OR NULLIF(TRIM(COALESCE(telefono,'')),'') IS NULL
                ORDER BY id_paciente
                """);
            consulta(st, "Marcadores tecnicos inadmisibles en consultas", """
                SELECT id_consulta,id_paciente,fecha,observaciones,enfermedad,tratamiento FROM consulta
                WHERE LOWER(COALESCE(observaciones,'')||' '||COALESCE(enfermedad,'')||' '||COALESCE(tratamiento,''))
                      ~ '(\\[(datos_|test_|prueba_)|datos_estadisticas|pendiente-|dummy|generad[ao] para|texto de prueba)'
                ORDER BY id_consulta
                """);
            consulta(st, "Valores clinicos con aspecto de prueba", """
                SELECT id_consulta,id_paciente,fecha,observaciones,enfermedad,tratamiento FROM consulta
                WHERE LOWER(COALESCE(observaciones,'')||' '||COALESCE(enfermedad,'')||' '||COALESCE(tratamiento,''))
                      ~ '(asd|qwe|jkj|s{5,}|d{5,}|f{5,})'
                ORDER BY id_consulta
                """);
            consulta(st, "Caracteres potencialmente corruptos", """
                SELECT 'paciente' origen,id_paciente::text id,nombre||' '||apellido texto FROM paciente
                WHERE nombre LIKE '%??%' OR apellido LIKE '%??%'
                UNION ALL
                SELECT 'personal',id_personal::text,nombreyapellido FROM personal WHERE nombreyapellido LIKE '%??%'
                UNION ALL
                SELECT 'consulta',id_consulta::text,COALESCE(observaciones,'')||' '||COALESCE(enfermedad,'')||' '||COALESCE(tratamiento,'')
                FROM consulta WHERE observaciones LIKE '%??%' OR enfermedad LIKE '%??%' OR tratamiento LIKE '%??%'
                """);
            consulta(st, "Matriculas medicas invalidas", """
                SELECT p.id_personal,p.nombreyapellido,p.matricula FROM personal p
                JOIN usuario u ON u.id_personal=p.id_personal JOIN rol r ON r.id_rol=u.id_rol
                WHERE LOWER(r.nombre) LIKE '%medic%'
                  AND (NULLIF(TRIM(COALESCE(p.matricula,'')),'') IS NULL OR UPPER(p.matricula) LIKE 'PENDIENTE%')
                ORDER BY p.id_personal
                """);
            consulta(st, "Telefonos y DNI con formato invalido", """
                SELECT 'paciente' origen,id_paciente::text id,dni::text,telefono::text telefono FROM paciente
                WHERE dni::text !~ '^[0-9]{7,8}$' OR telefono::text !~ '^[0-9]{10}$'
                UNION ALL
                SELECT 'personal',id_personal::text,dni::text,telefono::text telefono FROM personal
                WHERE dni::text !~ '^[0-9]{7,8}$' OR telefono::text !~ '^[0-9]{10}$'
                """);
            consulta(st, "Turnos atendidos sin historia activa", """
                SELECT t.id_turno,t.fechahora,t.estado,t.id_paciente,t.id_personal FROM turno t
                WHERE LOWER(t.estado) IN ('atendido','finalizado')
                  AND NOT EXISTS (SELECT 1 FROM consulta c WHERE c.id_turno=t.id_turno AND LOWER(c.estado::text)='activo')
                ORDER BY t.fechahora DESC LIMIT 30
                """);
            consulta(st, "Historias huerfanas o inconsistentes", """
                SELECT c.id_consulta,c.id_turno,c.id_paciente,c.id_personal FROM consulta c
                LEFT JOIN paciente p ON p.id_paciente=c.id_paciente
                LEFT JOIN personal per ON per.id_personal=c.id_personal
                WHERE p.id_paciente IS NULL OR per.id_personal IS NULL
                ORDER BY c.id_consulta
                """);
            consulta(st, "Turnos activos duplicados por paciente y dia", """
                SELECT id_paciente,fechahora::date fecha,COUNT(*) cantidad FROM turno
                WHERE LOWER(estado) NOT IN ('cancelado','ausente','finalizado')
                GROUP BY id_paciente,fechahora::date HAVING COUNT(*)>1
                ORDER BY fecha DESC
                """);
        }
    }
}
