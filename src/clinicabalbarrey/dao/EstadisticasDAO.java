package clinicabalbarrey.dao;

import clinicabalbarrey.ConexionBD;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Consultas parametrizadas para estadísticas de turnos. */
public class EstadisticasDAO {
    public static final String PARTICULAR = "Particular / Sin Obra Social";

    public record Opcion(Integer id, String nombre, String busqueda) {
        public Opcion(Integer id, String nombre) { this(id, nombre, nombre); }
        @Override public String toString() { return nombre; }
    }

    public record Filtro(Date desde, Date hasta, List<Integer> idsMedicos, List<Integer> idsPacientes,
                         List<String> obrasSociales, List<String> estados) {
        public Filtro sinEstados() {
            return new Filtro(desde, hasta, idsMedicos, idsPacientes, obrasSociales, null);
        }
    }
    public record Punto(String etiqueta, int valor) { }
    public record Resultado(List<String[]> filas, List<String[]> filasBaseEstado,
                            List<Punto> porMedico, List<Punto> porObraSocial,
                            List<Punto> porEstado) { }

    public List<Opcion> listarMedicos() {
        String sql = "SELECT DISTINCT p.id_personal, p.nombreyapellido FROM personal p "
                + "WHERE p.estado::text='activo' AND (NULLIF(TRIM(COALESCE(p.matricula,'')),'') IS NOT NULL "
                + "OR p.id_especialidad IS NOT NULL) ORDER BY p.nombreyapellido";
        return listarOpciones(sql);
    }

    public List<String> listarObrasSociales() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT obra FROM ("
                + "SELECT ?::text AS obra UNION "
                + "SELECT TRIM(nombre) FROM obrasocial "
                + "WHERE estado::text='activo' AND NULLIF(TRIM(nombre),'') IS NOT NULL"
                + ") opciones ORDER BY obra";
        try (Connection con=ConexionBD.getConexion(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1, PARTICULAR);
            try (ResultSet rs=ps.executeQuery()) { while(rs.next()) lista.add(rs.getString(1)); }
        } catch(SQLException e) { throw new IllegalStateException("No se pudieron listar las obras sociales", e); }
        return lista;
    }

    public List<Opcion> listarPacientes() {
        String sql = "SELECT p.id_paciente, TRIM(COALESCE(p.nombre,'') || ' ' || COALESCE(p.apellido,'')), "
                + "COALESCE(p.dni::text,'') "
                + "FROM paciente p WHERE p.estado::text='activo' "
                + "ORDER BY p.apellido, p.nombre, p.id_paciente";
        List<Opcion> lista = new ArrayList<>();
        try (Connection con=ConexionBD.getConexion();
             PreparedStatement ps=con.prepareStatement(sql);
             ResultSet rs=ps.executeQuery()) {
            while (rs.next()) {
                String nombre = rs.getString(2);
                lista.add(new Opcion(rs.getInt(1), nombre, nombre + " " + rs.getString(3)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron listar los pacientes", e);
        }
        return lista;
    }

    private List<Opcion> listarOpciones(String sql) {
        List<Opcion> lista=new ArrayList<>();
        try(Connection con=ConexionBD.getConexion(); PreparedStatement ps=con.prepareStatement(sql); ResultSet rs=ps.executeQuery()) {
            while(rs.next()) lista.add(new Opcion(rs.getInt(1), rs.getString(2)));
        } catch(SQLException e) { throw new IllegalStateException("No se pudieron listar los médicos", e); }
        return lista;
    }

    public Resultado consultar(Filtro filtro) {
        try(Connection con=ConexionBD.getConexion()) {
            List<String[]> filas = listarDetalle(con, filtro);
            List<String[]> base = listarDetalle(con, filtro.sinEstados());
            return new Resultado(
                    filas, base,
                    agruparFilas(filas, 2),
                    agruparFilas(filas, 4),
                    agruparFilas(filas, 5));
        } catch(SQLException e) { throw new IllegalStateException("No se pudieron consultar las estadísticas", e); }
    }

    private List<Punto> agruparFilas(List<String[]> filas, int indice) {
        Map<String, Long> cantidades = filas.stream().collect(Collectors.groupingBy(
                f -> indice == 5 ? capitalizar(f[indice]) : f[indice],
                LinkedHashMap::new, Collectors.counting()));
        return cantidades.entrySet().stream()
                .sorted((a, b) -> {
                    int cantidad = Long.compare(b.getValue(), a.getValue());
                    return cantidad != 0 ? cantidad : a.getKey().compareToIgnoreCase(b.getKey());
                })
                .map(e -> new Punto(e.getKey(), e.getValue().intValue()))
                .toList();
    }

    private String capitalizar(String texto) {
        return texto == null || texto.isBlank() ? ""
                : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private List<String[]> listarDetalle(Connection con, Filtro f) throws SQLException {
        String sql="SELECT t.fechahora, TRIM(COALESCE(p.nombre,'')||' '||COALESCE(p.apellido,'')) paciente, "
                + "per.nombreyapellido medico, COALESCE(NULLIF(TRIM(o.nombre),''), ?) obra, "
                + "CASE WHEN LOWER(t.estado)='finalizado' THEN 'atendido' ELSE LOWER(t.estado) END estado, "
                + "t.id_paciente, t.id_personal, "
                + "COALESCE(p.dni::text,'Sin dato') paciente_dni FROM turno t "
                + "JOIN paciente p ON p.id_paciente=t.id_paciente JOIN personal per ON per.id_personal=t.id_personal "
                + "LEFT JOIN obrasocial o ON o.id_obrasocial=t.id_obrasocial_atencion " + where(f) + " ORDER BY t.fechahora DESC";
        List<String[]> filas=new ArrayList<>();
        try(PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1, PARTICULAR); bind(ps,2,f);
            try(ResultSet rs=ps.executeQuery()) {
                SimpleDateFormat fecha=new SimpleDateFormat("dd/MM/yyyy"), hora=new SimpleDateFormat("HH:mm");
                while(rs.next()) { Timestamp fh=rs.getTimestamp("fechahora"); filas.add(new String[]{
                    fecha.format(fh), rs.getString("paciente"), rs.getString("medico"), hora.format(fh),
                    rs.getString("obra"), rs.getString("estado"), String.valueOf(rs.getInt("id_paciente")),
                    String.valueOf(rs.getInt("id_personal")), rs.getString("paciente_dni")}); }
            }
        }
        return filas;
    }

    private List<Punto> agrupar(Connection con, Filtro f, String agrupacion) throws SQLException {
        String expresion, agrupacionSql, orden;
        switch(agrupacion) {
            case "Obra social" -> {
                expresion="COALESCE(NULLIF(TRIM(o.nombre),''), '"+PARTICULAR.replace("'","''")+"')";
                agrupacionSql=expresion; orden="valor DESC, etiqueta";
            }
            case "Estado del turno" -> {
                expresion="INITCAP(LOWER(t.estado))"; agrupacionSql=expresion; orden="valor DESC, etiqueta";
            }
            case "Mes" -> {
                agrupacionSql="DATE_TRUNC('month', t.fechahora)";
                expresion="TO_CHAR("+agrupacionSql+", 'MM/YYYY')"; orden=agrupacionSql;
            }
            default -> {
                expresion="per.nombreyapellido"; agrupacionSql=expresion; orden="valor DESC, etiqueta";
            }
        }
        String medida="COUNT(*)";
        String sql="SELECT "+expresion+" etiqueta, "+medida+" valor FROM turno t "
                + "JOIN paciente p ON p.id_paciente=t.id_paciente JOIN personal per ON per.id_personal=t.id_personal "
                + "LEFT JOIN obrasocial o ON o.id_obrasocial=t.id_obrasocial_atencion " + where(f)
                + " GROUP BY "+agrupacionSql+" ORDER BY "+orden;
        List<Punto> puntos=new ArrayList<>();
        try(PreparedStatement ps=con.prepareStatement(sql)) { bind(ps,1,f); try(ResultSet rs=ps.executeQuery()) {
            while(rs.next()) puntos.add(new Punto(rs.getString("etiqueta"),rs.getInt("valor")));
        }}
        return puntos;
    }

    private String where(Filtro f) {
        StringBuilder w=new StringBuilder("WHERE NOT (LOWER(COALESCE(t.estado,''))='cancelado' AND EXISTS ("
                + "SELECT 1 FROM turno valido WHERE valido.id_turno<>t.id_turno AND valido.id_paciente=t.id_paciente "
                + "AND valido.id_personal=t.id_personal AND valido.fechahora=t.fechahora "
                + "AND LOWER(valido.estado) IN ('atendido','finalizado'))) ");
        if(f.desde()!=null) w.append("AND t.fechahora >= ? ");
        if(f.hasta()!=null) w.append("AND t.fechahora < (?::date + INTERVAL '1 day') ");
        if(tieneValores(f.idsMedicos())) w.append("AND t.id_personal = ANY (?) ");
        if(tieneValores(f.idsPacientes())) w.append("AND t.id_paciente = ANY (?) ");
        if(tieneValores(f.obrasSociales())) {
            w.append("AND COALESCE(NULLIF(TRIM(o.nombre),''), '")
                    .append(PARTICULAR.replace("'","''")).append("') = ANY (?) ");
        }
        if(tieneValores(f.estados())) {
            List<String> reales = estadosReales(f.estados());
            boolean sinAsistencia = f.estados().contains("__sin_asistencia__");
            if (sinAsistencia && !reales.isEmpty()) {
                w.append("AND (CASE WHEN LOWER(t.estado)='finalizado' THEN 'atendido' ELSE LOWER(t.estado) END = ANY (?) "
                        + "OR LOWER(t.estado) NOT IN ('atendido','finalizado')) ");
            } else if (sinAsistencia) {
                w.append("AND LOWER(t.estado) NOT IN ('atendido','finalizado') ");
            } else {
                w.append("AND CASE WHEN LOWER(t.estado)='finalizado' THEN 'atendido' ELSE LOWER(t.estado) END = ANY (?) ");
            }
        } else {
            // Por defecto se analizan turnos vigentes o realizados. Las bajas se
            // recuperan explícitamente mediante el filtro Cancelado.
            w.append("AND LOWER(COALESCE(t.estado,''))<>'cancelado' ");
        }
        return w.toString();
    }

    private void bind(PreparedStatement ps,int i,Filtro f) throws SQLException {
        if(f.desde()!=null) ps.setDate(i++,new java.sql.Date(f.desde().getTime()));
        if(f.hasta()!=null) ps.setDate(i++,new java.sql.Date(f.hasta().getTime()));
        if(tieneValores(f.idsMedicos())) {
            ps.setArray(i++, ps.getConnection().createArrayOf("int4", f.idsMedicos().toArray()));
        }
        if(tieneValores(f.idsPacientes())) {
            ps.setArray(i++, ps.getConnection().createArrayOf("int4", f.idsPacientes().toArray()));
        }
        if(tieneValores(f.obrasSociales())) {
            ps.setArray(i++, ps.getConnection().createArrayOf("text", f.obrasSociales().toArray()));
        }
        if(tieneValores(f.estados()) && !estadosReales(f.estados()).isEmpty()) {
            ps.setArray(i++, ps.getConnection().createArrayOf("text", estadosReales(f.estados()).toArray()));
        }
    }

    private List<String> estadosReales(List<String> estados) {
        return estados.stream().filter(e -> !"__sin_asistencia__".equals(e)).toList();
    }

    private boolean tieneValores(List<?> valores) {
        return valores != null && !valores.isEmpty();
    }
}
