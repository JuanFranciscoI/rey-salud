package clinicabalbarrey;

import com.formdev.flatlaf.themes.FlatMacLightLaf; // Usamos el tema estilo Mac Claro
import javax.swing.UIManager;

public class ClinicaBalbarrey {

    public static void main(String[] args) {

        // --- 1. ACTIVAR EL DISEÑO PROFESIONAL LIMPIO (Mac Light) ---
        try {
            // Establecer una fuente base un poco más grande y legible para toda la app
            // "Segoe UI" es la fuente estándar moderna de Windows. Tamaño 14 se lee muy bien.
            // Iniciamos el tema estilo Mac. Es limpio, gris claro y blanco, muy ordenado.
            FlatMacLightLaf.setup();
            ResponsiveUI.inicializar();

            // Redondeo general para que botones, inputs, combos y paneles no se vean rectangulares.
            UIManager.put("Button.arc", 18);
            UIManager.put("Component.arc", 18);
            UIManager.put("TextComponent.arc", 18);
            UIManager.put("ComboBox.arc", 18);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);

        } catch (Exception ex) {
            System.err.println("No se pudo cargar el diseño MacLight: " + ex.getMessage());
        }

        // --- 2. ACTUALIZAR ESTRUCTURA E INICIAR LA VENTANA ---
        EsquemaBD.actualizar();
        java.awt.EventQueue.invokeLater(() -> new MainApplicationView().setVisible(true));
    }
}
