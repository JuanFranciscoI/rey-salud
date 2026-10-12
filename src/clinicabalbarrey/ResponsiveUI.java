package clinicabalbarrey;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;

/** Ajusta la interfaz al espacio lógico disponible (resolución y escala DPI). */
public final class ResponsiveUI {
    private static final String FUENTE_BASE = "responsive.fuenteBase";
    private static final String PREFERIDO_BASE = "responsive.preferidoBase";
    private static final String MINIMO_BASE = "responsive.minimoBase";
    private static final String MAXIMO_BASE = "responsive.maximoBase";
    private static final String FILA_BASE = "responsive.filaBase";

    private static double escala = 1.0;
    private static Rectangle areaUtil = new Rectangle(0, 0, 1280, 800);

    private ResponsiveUI() { }

    public static void inicializar() {
        if (!GraphicsEnvironment.isHeadless()) {
            areaUtil = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        }
        escala = calcularEscala(areaUtil.width, areaUtil.height);

        Font base = new Font("Segoe UI", Font.PLAIN, 14);
        UIManager.put("defaultFont", base);
        UIManager.put("Button.font", base.deriveFont(Font.BOLD, 14f));
        UIManager.put("Table.font", base.deriveFont(Font.PLAIN, 15f));
        UIManager.put("TableHeader.font", new Font("Segoe UI Semibold", Font.PLAIN, 15));
    }

    static double calcularEscala(int ancho, int alto) {
        if (ancho < 1200 || alto < 680) return 0.76;
        if (ancho < 1450 || alto < 800) return 0.84;
        if (ancho < 1700 || alto < 900) return 0.92;
        return 1.0;
    }

    public static double calcularEscalaParaPrueba(int ancho, int alto) {
        return calcularEscala(ancho, alto);
    }

    public static Rectangle getAreaUtil() {
        return new Rectangle(areaUtil);
    }

    public static boolean actualizarPara(GraphicsConfiguration configuracion) {
        if (configuracion == null || GraphicsEnvironment.isHeadless()) return false;
        Rectangle limites = configuracion.getBounds();
        Insets barras = Toolkit.getDefaultToolkit().getScreenInsets(configuracion);
        Rectangle nuevaArea = new Rectangle(
                limites.x + barras.left,
                limites.y + barras.top,
                limites.width - barras.left - barras.right,
                limites.height - barras.top - barras.bottom);
        double nuevaEscala = calcularEscala(nuevaArea.width, nuevaArea.height);
        boolean cambio = Double.compare(escala, nuevaEscala) != 0;
        areaUtil = nuevaArea;
        escala = nuevaEscala;
        return cambio;
    }

    public static int px(int valor) {
        return Math.max(1, (int) Math.round(valor * escala));
    }

    public static float escalarFuente(float valor) {
        return Math.max(10f, (float) (valor * escala));
    }

    public static void aplicar(Component componente) {
        if (componente == null) return;

        if (componente instanceof JComponent jc) {
            Font base = (Font) jc.getClientProperty(FUENTE_BASE);
            if (base == null && jc.getFont() != null) {
                base = jc.getFont();
                jc.putClientProperty(FUENTE_BASE, base);
            }
            if (base != null) jc.setFont(base.deriveFont(escalarFuente(base.getSize2D())));

            escalarDimension(jc, PREFERIDO_BASE, 0);
            escalarDimension(jc, MINIMO_BASE, 1);
            escalarDimension(jc, MAXIMO_BASE, 2);

            if (jc instanceof JTable tabla) {
                if (Boolean.TRUE.equals(tabla.getClientProperty("responsive.filaDinamica"))) {
                    return;
                }
                Integer fila = (Integer) tabla.getClientProperty(FILA_BASE);
                if (fila == null) {
                    fila = tabla.getRowHeight();
                    tabla.putClientProperty(FILA_BASE, fila);
                }
                tabla.setRowHeight(px(fila));
            }
        }

        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) aplicar(hijo);
        }
    }

    private static void escalarDimension(JComponent componente, String clave, int tipo) {
        Dimension actual = switch (tipo) {
            case 0 -> componente.isPreferredSizeSet() ? componente.getPreferredSize() : null;
            case 1 -> componente.isMinimumSizeSet() ? componente.getMinimumSize() : null;
            default -> componente.isMaximumSizeSet() ? componente.getMaximumSize() : null;
        };
        Dimension base = (Dimension) componente.getClientProperty(clave);
        if (base == null && actual != null) {
            base = new Dimension(actual);
            componente.putClientProperty(clave, base);
        }
        if (base == null) return;

        Dimension ajustada = new Dimension(escalarEje(base.width), escalarEje(base.height));
        switch (tipo) {
            case 0 -> componente.setPreferredSize(ajustada);
            case 1 -> componente.setMinimumSize(ajustada);
            default -> componente.setMaximumSize(ajustada);
        }
    }

    private static int escalarEje(int valor) {
        if (valor == Integer.MAX_VALUE || valor <= 0) return valor;
        return px(valor);
    }
}
