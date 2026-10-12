package clinicabalbarrey;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.JOptionPane;

public class ConexionBD {

    private static boolean driverCargado = false;

    private static synchronized void cargarDriver() {
        if (!driverCargado) {
            try {
                Class.forName("org.postgresql.Driver");
                driverCargado = true;
                System.out.println("Driver PostgreSQL cargado exitosamente.");
            } catch (ClassNotFoundException e) {
                System.out.println("Error: No se encontró el driver JDBC de PostgreSQL.");
                JOptionPane.showMessageDialog(null,
                        "Error crítico: No se encontró el driver de PostgreSQL.\n"
                        + "Revisá que tengas agregado el JAR de PostgreSQL en Libraries.",
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                throw new IllegalStateException("No se encontró el controlador de PostgreSQL", e);
            }
        }
    }

    public static Connection getConexion() {
        cargarDriver();

        try {
            // Configuración local por defecto y externa para instalaciones en red.
            String host = configuracion("reysalud.db.host", "REYSALUD_DB_HOST", "localhost");
            String port = configuracion("reysalud.db.port", "REYSALUD_DB_PORT", "5432");
            String dbName = configuracion("reysalud.db.name", "REYSALUD_DB_NAME", "clinica2");
            String user = configuracion("reysalud.db.user", "REYSALUD_DB_USER", "postgres");
            String password = configuracion("reysalud.db.password", "REYSALUD_DB_PASSWORD", "postgres");
            String schema = configuracion("reysalud.db.schema", "REYSALUD_DB_SCHEMA", "public");

            String dbNameEncoded = URLEncoder
                    .encode(dbName, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbNameEncoded;

            Properties props = new Properties();
            props.setProperty("user", user);
            props.setProperty("password", password);
            props.setProperty("currentSchema", schema);

            Connection con = DriverManager.getConnection(url, props);

            return con;

        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos.");
            e.printStackTrace(); // Imprime la causa exacta en la consola de NetBeans
            JOptionPane.showMessageDialog(null,
                    "Error al conectar a la base de datos:\n" + e.getMessage(),
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("No se pudo conectar a la base de datos", e);

        } catch (Exception e) {
            System.out.println("Error cargando configuración de base de datos.");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error cargando configuración de base de datos:\n" + e.getMessage(),
                    "Error de Configuración",
                    JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("No se pudo cargar la configuración de base de datos", e);
        }
    }

    private static String configuracion(String propiedad, String variable, String predeterminado) {
        String valor = System.getProperty(propiedad);
        if (valor == null || valor.isBlank()) valor = System.getenv(variable);
        return valor == null || valor.isBlank() ? predeterminado : valor.trim();
    }
}
