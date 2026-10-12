package clinicabalbarrey;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public final class EstiloRedondeado {

    private static final int RADIO_DEFAULT = 12;
    private static final Color COLOR_BOTON_PRINCIPAL = new Color(0, 121, 107);
    private static final Color COLOR_BOTON_PRINCIPAL_BORDE = new Color(0, 105, 92);
    private static final Color COLOR_BOTON_ROJO = new Color(211, 47, 47);
    private static final Color COLOR_BOTON_ROJO_BORDE = new Color(183, 28, 28);
    private static final Color COLOR_BOTON_DESHABILITADO = new Color(232, 238, 241);
    private static final Color COLOR_BOTON_DESHABILITADO_BORDE = new Color(198, 208, 213);
    private static final Color COLOR_TEXTO_DESHABILITADO = new Color(125, 137, 145);
    private static final Color COLOR_TABLA_HEADER = new Color(0, 121, 107);
    private static final Color COLOR_TABLA_BORDE = new Color(200, 205, 208);
    private static final Color COLOR_TABLA_SELECCION = new Color(178, 223, 219);
    private static Font fuenteSistema(int estilo, float tamanio) {
        Font base = UIManager.getFont("defaultFont");
        if (base == null) {
            base = new Font("Segoe UI", Font.PLAIN, 14);
        }
        return base.deriveFont(estilo, tamanio);
    }

    public static Font fuenteHeaderTabla() {
        return new Font("Segoe UI Semibold", Font.PLAIN, 15);
    }

    private EstiloRedondeado() {
    }

    public static Border borde(Color color) {
        return borde(color, 1, RADIO_DEFAULT);
    }

    public static Border borde(Color color, int grosor) {
        return borde(color, grosor, RADIO_DEFAULT);
    }

    public static void aplicarSelectorFiltro(AbstractButton boton) {
        if (boton == null) return;
        Color verde = new Color(0, 128, 111);
        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBackground(verde);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                borde(verde, 1), BorderFactory.createEmptyBorder(0, 10, 0, 10)));
    }

    public static Border borde(Color color, int grosor, int radio) {
        return new Borde(color, grosor, radio);
    }

    public static void aplicarBotones(Component componente) {
        if (componente == null) {
            return;
        }

        if (componente instanceof AbstractButton boton && esBotonDePagina(boton)) {
            if (esRojo(boton.getBackground())) {
                aplicarBotonPeligro(boton);
            } else {
                aplicarBotonPrincipalSiCorresponde(boton);
            }
        }

        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                aplicarBotones(hijo);
            }
        }
    }

    public static void aplicarTablas(Component componente) {
        if (componente == null) {
            return;
        }

        if (componente instanceof JTable tabla) {
            aplicarTabla(tabla);
        }

        if (componente instanceof JScrollPane scroll) {
            scroll.setBorder(borde(COLOR_TABLA_BORDE, 1, 14));
            scroll.setOpaque(false);
            if (scroll.getViewport() != null) {
                scroll.getViewport().setOpaque(false);
            }
        }

        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                aplicarTablas(hijo);
            }
        }
    }

    public static void aplicarBuscadorTexto(JTextField campo) {
        if (campo == null) {
            return;
        }
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        campo.setBackground(Color.WHITE);
        campo.setForeground(new Color(30, 40, 45));
        campo.setBorder(BorderFactory.createCompoundBorder(
                borde(new Color(200, 205, 208), 1, 12),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        campo.putClientProperty("JComponent.roundRect", true);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void aplicarComboBuscador(JComboBox combo) {
        if (combo == null) {
            return;
        }
        combo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(30, 40, 45));
        combo.setMaximumRowCount(8);
        combo.setBorder(BorderFactory.createCompoundBorder(
                borde(new Color(200, 205, 208), 1, 12),
                BorderFactory.createEmptyBorder(0, 8, 0, 0)
        ));
        combo.putClientProperty("JComponent.roundRect", true);
        combo.setUI(new ComboBuscadorUI(8));

        if (combo.getEditor() != null && combo.getEditor().getEditorComponent() instanceof JTextField editor) {
            aplicarBuscadorTexto(editor);
            editor.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
        }

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                label.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
                label.setOpaque(true);
                if (isSelected && index >= 0) {
                    label.setBackground(new Color(232, 245, 242));
                    label.setForeground(new Color(0, 77, 64));
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(30, 40, 45));
                }
                return label;
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void aplicarComboFiltroTurnos(JComboBox combo) {
        if (combo == null) return;
        aplicarComboBuscador(combo);
        Color verde = new Color(0, 150, 136);
        combo.setUI(new ComboFiltroTurnosUI(8));
        combo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        combo.setForeground(new Color(55, 65, 72));
        combo.setBackground(Color.WHITE);
        combo.setFocusable(false);
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        combo.setBorder(BorderFactory.createCompoundBorder(
                borde(verde, 1), BorderFactory.createEmptyBorder(0, 8, 0, 3)));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, false);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(index < 0
                        ? BorderFactory.createEmptyBorder(0, 10, 0, 10)
                        : BorderFactory.createEmptyBorder(7, 10, 7, 10));
                label.setOpaque(true);
                if (isSelected && index >= 0) {
                    label.setBackground(verde);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(55, 65, 72));
                }
                return label;
            }
        });
    }

    private static void aplicarTabla(JTable tabla) {
        if (Boolean.TRUE.equals(tabla.getClientProperty("estiloRedondeado.omitir"))) {
            return;
        }
        tabla.setFont(fuenteSistema(Font.PLAIN, 15f));
        JTableHeader header = tabla.getTableHeader();
        if (header != null) {
            header.setFont(fuenteHeaderTabla());
            header.setBackground(COLOR_TABLA_HEADER);
            header.setForeground(Color.WHITE);
            header.setReorderingAllowed(false);
        }
        if (Boolean.TRUE.equals(tabla.getClientProperty("estiloRedondeado.aplicado"))) {
            return;
        }
        tabla.putClientProperty("estiloRedondeado.aplicado", Boolean.TRUE);
        tabla.setShowHorizontalLines(false);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new java.awt.Dimension(0, 0));
        tabla.setAutoscrolls(false);
        tabla.setCellSelectionEnabled(false);
        tabla.setColumnSelectionAllowed(false);
        tabla.setSelectionBackground(COLOR_TABLA_SELECCION);
        tabla.setSelectionForeground(Color.BLACK);
        tabla.putClientProperty("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder());
        UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder());
        UIManager.put("Table.focusSelectedCellHighlightBorder", BorderFactory.createEmptyBorder());
        aplicarRendererSeleccion(tabla);
        bloquearMovimientoAlSeleccionar(tabla);

        if (header != null) {
            header.setDefaultRenderer(new HeaderRedondeadoRenderer());
            header.setOpaque(false);
        }
    }

    private static void aplicarRendererSeleccion(JTable tabla) {
        TableCellRenderer rendererActual = tabla.getDefaultRenderer(Object.class);
        if (rendererActual instanceof RendererSeleccionTabla) {
            return;
        }
        tabla.setDefaultRenderer(Object.class, new RendererSeleccionTabla(rendererActual));
    }

    private static void bloquearMovimientoAlSeleccionar(JTable tabla) {
        if (Boolean.TRUE.equals(tabla.getClientProperty("estiloRedondeado.seleccionEstatica"))) {
            return;
        }
        tabla.putClientProperty("estiloRedondeado.seleccionEstatica", Boolean.TRUE);
        tabla.addMouseListener(new MouseAdapter() {
            private Point posicionAntes;

            @Override
            public void mousePressed(MouseEvent e) {
                guardarPosicion();
                SwingUtilities.invokeLater(this::restaurarPosicion);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                restaurarPosicion();
                SwingUtilities.invokeLater(this::restaurarPosicion);
            }

            private void guardarPosicion() {
                JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, tabla);
                posicionAntes = viewport != null ? viewport.getViewPosition() : null;
            }

            private void restaurarPosicion() {
                if (posicionAntes == null) {
                    return;
                }
                JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, tabla);
                if (viewport != null) {
                    viewport.setViewPosition(posicionAntes);
                }
            }
        });
    }

    public static void aplicarBotonPrincipalSiCorresponde(AbstractButton boton) {
        if (boton == null || esRojo(boton.getBackground())) {
            return;
        }

        boton.setBackground(COLOR_BOTON_PRINCIPAL);
        boton.setForeground(Color.WHITE);
        boton.setFont(fuenteSistema(Font.BOLD, 14f));
        boton.setUI(new BotonRedondeadoUI(COLOR_BOTON_PRINCIPAL, COLOR_BOTON_PRINCIPAL_BORDE, false));
        aplicarPropiedadesRedondeadas(boton);
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setRolloverEnabled(true);
        boton.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        if (esBotonPaginacion(boton)) {
            boton.setPreferredSize(new java.awt.Dimension(140, 42));
            boton.setMinimumSize(new java.awt.Dimension(140, 42));
        }
    }

    public static void aplicarBotonPeligro(AbstractButton boton) {
        if (boton == null) {
            return;
        }

        boton.setBackground(COLOR_BOTON_ROJO);
        boton.setForeground(Color.WHITE);
        boton.setUI(new BotonRedondeadoUI(COLOR_BOTON_ROJO, COLOR_BOTON_ROJO_BORDE, true));
        aplicarPropiedadesRedondeadas(boton);
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setRolloverEnabled(true);
        boton.setFont(fuenteSistema(Font.BOLD, 14f));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    private static void aplicarPropiedadesRedondeadas(AbstractButton boton) {
        boton.putClientProperty("JButton.buttonType", "roundRect");
        boton.putClientProperty("JComponent.roundRect", true);
    }

    private static boolean esBotonDePagina(AbstractButton boton) {
        if (Boolean.TRUE.equals(boton.getClientProperty("estiloRedondeado.omitirBoton"))) {
            return false;
        }

        String texto = boton.getText();
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }

        if (boton instanceof JCheckBox || boton instanceof JRadioButton) {
            return false;
        }

        Component actual = boton;
        while (actual != null) {
            if (actual instanceof JComboBox || actual instanceof JSpinner
                    || actual instanceof JScrollBar || actual instanceof JViewport) {
                return false;
            }
            Package paquete = actual.getClass().getPackage();
            if (paquete != null && paquete.getName().startsWith("com.toedter.calendar")) {
                return false;
            }
            actual = actual.getParent();
        }
        String textoNormalizado = texto.trim().toLowerCase(java.util.Locale.ROOT);
        if (textoNormalizado.matches("\\d{1,2}|\\.{1,3}|[a-záéíóú]{2,4}")) {
            return false;
        }
        return true;
    }

    private static boolean esRojo(Color color) {
        if (color == null) {
            return false;
        }
        return color.getRed() >= 170 && color.getGreen() <= 90 && color.getBlue() <= 90;
    }

    private static class ComboBuscadorUI extends BasicComboBoxUI {
        private final int maxRows;

        ComboBuscadorUI(int maxRows) {
            this.maxRows = maxRows;
        }

        @Override
        protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
                @Override
                public void show() {
                    Dimension tamano = calcularTamanoPopup();
                    scroller.setPreferredSize(tamano);
                    scroller.setMinimumSize(tamano);
                    scroller.setMaximumSize(tamano);
                    list.setFixedCellWidth(tamano.width);
                    super.show();
                }

                @Override
                protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
                    Dimension tamano = calcularTamanoPopup();
                    return super.computePopupBounds(px, py + 6, tamano.width, tamano.height);
                }

                private Dimension calcularTamanoPopup() {
                    int rowHeight = Math.max(32, comboBox.getFontMetrics(comboBox.getFont()).getHeight() + 14);
                    int anchoContenido = comboBox.getWidth();
                    if (list.getCellRenderer() != null) {
                        for (int i = 0; i < comboBox.getItemCount(); i++) {
                            Component celda = list.getCellRenderer().getListCellRendererComponent(
                                    list, comboBox.getItemAt(i), i, false, false);
                            Dimension preferido = celda.getPreferredSize();
                            rowHeight = Math.max(rowHeight, preferido.height + 2);
                            anchoContenido = Math.max(anchoContenido, preferido.width + 34);
                        }
                    }
                    int rows = Math.max(1, Math.min(maxRows, comboBox.getItemCount()));
                    int alto = (rowHeight * rows) + 8;
                    int ancho = Math.max(1, anchoContenido);
                    return new Dimension(ancho, alto);
                }
            };
        }
    }

    private static class ComboFiltroTurnosUI extends ComboBuscadorUI {
        ComboFiltroTurnosUI(int maxRows) {
            super(maxRows);
        }

        @Override
        protected JButton createArrowButton() {
            JButton boton = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    int margen = 3;
                    g2.setColor(new Color(0, 150, 136));
                    g2.fillRoundRect(margen, margen, getWidth() - margen * 2,
                            getHeight() - margen * 2, 8, 8);
                    int centroX = getWidth() / 2;
                    int centroY = getHeight() / 2 + 1;
                    Polygon flecha = new Polygon(
                            new int[]{centroX - 4, centroX + 4, centroX},
                            new int[]{centroY - 2, centroY - 2, centroY + 3}, 3);
                    g2.setColor(Color.WHITE);
                    g2.fillPolygon(flecha);
                    g2.dispose();
                }
            };
            boton.setPreferredSize(new Dimension(30, 30));
            boton.setMinimumSize(new Dimension(30, 24));
            boton.setBorder(BorderFactory.createEmptyBorder());
            boton.setFocusPainted(false);
            boton.setContentAreaFilled(false);
            boton.setOpaque(false);
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return boton;
        }
    }

    private static boolean esBotonPaginacion(AbstractButton boton) {
        String texto = boton.getText();
        if (texto == null) {
            return false;
        }
        String normalizado = texto.toLowerCase(java.util.Locale.ROOT);
        return normalizado.contains("anterior") || normalizado.contains("siguiente");
    }

    private static class BotonRedondeadoUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final Color fondo;
        private final Color borde;
        private final boolean peligro;

        BotonRedondeadoUI(Color fondo, Color borde, boolean peligro) {
            this.fondo = fondo;
            this.borde = borde;
            this.peligro = peligro;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton boton = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            Color colorFondo;
            Color colorBorde;
            Color colorTexto;
            if (!boton.isEnabled()) {
                colorFondo = COLOR_BOTON_DESHABILITADO;
                colorBorde = COLOR_BOTON_DESHABILITADO_BORDE;
                colorTexto = COLOR_TEXTO_DESHABILITADO;
            } else {
                colorFondo = boton.getModel().isPressed()
                        ? fondo.darker()
                        : boton.getModel().isRollover() ? aclarar(fondo) : fondo;
                colorBorde = peligro ? COLOR_BOTON_ROJO_BORDE : borde;
                colorTexto = Color.WHITE;
            }

            g2.setColor(colorFondo);
            g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 14, 14);
            g2.setColor(colorBorde);
            g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 14, 14);
            g2.dispose();

            Color anterior = boton.getForeground();
            boton.setForeground(colorTexto);
            super.paint(g, c);
            boton.setForeground(anterior);
        }

        @Override
        protected void paintText(Graphics g, AbstractButton b, java.awt.Rectangle textRect, String text) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(b.getFont());
            g2.setFont(b.getFont());
            java.awt.FontMetrics fm = g2.getFontMetrics();
            g2.setColor(b.isEnabled() ? Color.WHITE : COLOR_TEXTO_DESHABILITADO);
            javax.swing.plaf.basic.BasicGraphicsUtils.drawStringUnderlineCharAt(
                    g2,
                    text,
                    b.getDisplayedMnemonicIndex(),
                    textRect.x + getTextShiftOffset(),
                    textRect.y + fm.getAscent() + getTextShiftOffset()
            );
            g2.dispose();
        }

        private static Color aclarar(Color color) {
            return new Color(
                    Math.min(255, color.getRed() + 12),
                    Math.min(255, color.getGreen() + 12),
                    Math.min(255, color.getBlue() + 12)
            );
        }
    }

    private static class Borde extends AbstractBorder {

        private final Color color;
        private final int grosor;
        private final int radio;

        Borde(Color color, int grosor, int radio) {
            this.color = color != null ? color : new Color(200, 205, 208);
            this.grosor = Math.max(1, grosor);
            this.radio = Math.max(4, radio);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(grosor));

            float ajuste = grosor / 2f;
            g2.draw(new RoundRectangle2D.Float(
                    x + ajuste,
                    y + ajuste,
                    width - grosor,
                    height - grosor,
                    radio,
                    radio
            ));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int margen = Math.max(4, grosor + 3);
            return new Insets(margen, margen, margen, margen);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            Insets calculado = getBorderInsets(c);
            insets.top = calculado.top;
            insets.left = calculado.left;
            insets.bottom = calculado.bottom;
            insets.right = calculado.right;
            return insets;
        }
    }

    private static class HeaderRedondeadoRenderer extends JComponent implements TableCellRenderer {

        private Object valor;
        private JTable tabla;
        private int columna;

        HeaderRedondeadoRenderer() {
            setOpaque(false);
            setFont(UIManager.getFont("TableHeader.font"));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            this.tabla = table;
            this.valor = value;
            this.columna = column;
            setFont(table.getTableHeader().getFont());
            setForeground(Color.WHITE);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int ancho = getWidth();
            int alto = getHeight();
            int radio = 14;
            boolean primera = esPrimeraColumnaVisible();
            boolean ultima = esUltimaColumnaVisible();

            g2.setColor(COLOR_TABLA_HEADER);
            if (primera && ultima) {
                g2.fillRoundRect(0, 0, ancho, alto, radio, radio);
                g2.fillRect(0, alto - radio, ancho, radio);
            } else if (primera) {
                g2.fillRoundRect(0, 0, ancho + radio, alto, radio, radio);
                g2.fillRect(0, alto - radio, ancho, radio);
            } else if (ultima) {
                g2.fillRoundRect(-radio, 0, ancho + radio, alto, radio, radio);
                g2.fillRect(0, alto - radio, ancho, radio);
            } else {
                g2.fillRect(0, 0, ancho, alto);
            }

            g2.setColor(new Color(225, 235, 235));
            g2.drawLine(0, alto - 1, ancho, alto - 1);
            if (!ultima) {
                g2.drawLine(ancho - 1, 0, ancho - 1, alto);
            }

            String texto = valor == null ? "" : valor.toString();
            g2.setFont(getFont());
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int x = (ancho - fm.stringWidth(texto)) / 2;
            int y = ((alto - fm.getHeight()) / 2) + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(texto, Math.max(8, x), y);
            g2.dispose();
        }

        private boolean esPrimeraColumnaVisible() {
            if (tabla == null) return columna == 0;
            for (int i = 0; i < tabla.getColumnModel().getColumnCount(); i++) {
                if (tabla.getColumnModel().getColumn(i).getWidth() > 0) {
                    return columna == i;
                }
            }
            return columna == 0;
        }

        private boolean esUltimaColumnaVisible() {
            if (tabla == null) return true;
            for (int i = tabla.getColumnModel().getColumnCount() - 1; i >= 0; i--) {
                if (tabla.getColumnModel().getColumn(i).getWidth() > 0) {
                    return columna == i;
                }
            }
            return true;
        }
    }

    private static class RendererSeleccionTabla extends DefaultTableCellRenderer {
        private final TableCellRenderer delegado;

        RendererSeleccionTabla(TableCellRenderer delegado) {
            this.delegado = delegado;
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c;
            if (delegado != null && delegado != this) {
                c = delegado.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            } else {
                c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            }

            if (c instanceof JLabel label) {
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setOpaque(true);
                label.setFont(table.getFont());
                label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (isSelected) {
                    label.setBackground(COLOR_TABLA_SELECCION);
                    label.setForeground(Color.BLACK);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(Color.BLACK);
                }
            } else {
                c.setBackground(isSelected ? COLOR_TABLA_SELECCION : Color.WHITE);
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }
}
