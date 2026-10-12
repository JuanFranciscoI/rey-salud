package clinicabalbarrey;

import clinicabalbarrey.model.Usuario;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SidebarPanel extends JPanel {
    private MainApplicationView mainApp;

    // Paneles "caja" para ocultar botones
    private JPanel pnlDashboardBox;
    private JPanel pnlAdminBox;
    private JPanel pnlTodosLosTurnosBox;

    private JButton btnDashboard;
    private JButton btnEstadisticas;
    private JButton btnPacientes;
    private JButton btnCalendario;
    private JButton btnTurnos;
    private JButton btnTodosLosTurnos;
    private JButton btnPersonal;

    // Textos para mostrar quién está logueado
    private JLabel lblUsuarioActual;
    private JLabel lblRolActual;
    private boolean submenuAtencionesVisible = false;

    public SidebarPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(new Color(0, 77, 64));
        setPreferredSize(new Dimension(280, 0));

        JPanel pnlMenu = new JPanel();
        pnlMenu.setLayout(new BoxLayout(pnlMenu, BoxLayout.Y_AXIS));
        pnlMenu.setOpaque(false);
        pnlMenu.setBorder(new EmptyBorder(ResponsiveUI.px(30), ResponsiveUI.px(20), ResponsiveUI.px(20), ResponsiveUI.px(20)));

        // --- ENCABEZADO ---
        JLabel lblTitulo = new JLabel("REY SALUD");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 34));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMenu.add(lblTitulo);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(240, 1));
        sep.setForeground(Color.WHITE);
        sep.setBackground(Color.WHITE);
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMenu.add(Box.createVerticalStrut(15));
        pnlMenu.add(sep);

        // --- CAJA DASHBOARD (se agrega al final, antes del usuario) ---
        pnlDashboardBox = new JPanel();
        pnlDashboardBox.setLayout(new BoxLayout(pnlDashboardBox, BoxLayout.Y_AXIS));
        pnlDashboardBox.setOpaque(false);
        pnlDashboardBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblAnalisis = new JLabel("ANÁLISIS");
        lblAnalisis.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAnalisis.setForeground(new Color(200, 200, 200));
        lblAnalisis.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlDashboardBox.add(lblAnalisis);
        pnlDashboardBox.add(Box.createVerticalStrut(12));

        btnDashboard = crearBotonMenu("Reportes  \u25bc", "DASHBOARD", "reportes");
        btnDashboard.addActionListener(e -> alternarSubmenuAtenciones());
        pnlDashboardBox.add(btnDashboard);
        btnEstadisticas = crearBotonSubmenu("Estad\u00edsticas", "ESTADISTICAS");
        btnEstadisticas.setVisible(false);
        pnlDashboardBox.add(Box.createVerticalStrut(8));
        pnlDashboardBox.add(btnEstadisticas);
        pnlDashboardBox.add(Box.createVerticalStrut(10));

        // --- ADMINISTRACIÓN ARRIBA ---
        pnlAdminBox = new JPanel();
        pnlAdminBox.setLayout(new BoxLayout(pnlAdminBox, BoxLayout.Y_AXIS));
        pnlAdminBox.setOpaque(false);
        pnlAdminBox.add(Box.createVerticalStrut(18));

        JLabel lblAdmin = new JLabel("ADMINISTRACIÓN");
        lblAdmin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAdmin.setForeground(new Color(200, 200, 200));
        lblAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlAdminBox.add(lblAdmin);
        pnlAdminBox.add(Box.createVerticalStrut(12));

        btnPersonal = crearBotonMenu("Personal y Accesos", "GESTION_PERSONAL", "grupo");
        pnlAdminBox.add(btnPersonal);
        pnlAdminBox.add(Box.createVerticalStrut(20));
        pnlMenu.add(pnlAdminBox);

        // --- GESTIÓN CLÍNICA ---
        JLabel lblGestion = new JLabel("GESTIÓN");
        lblGestion.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGestion.setForeground(new Color(200, 200, 200));
        lblGestion.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMenu.add(lblGestion);
        pnlMenu.add(Box.createVerticalStrut(12));

        btnPacientes = crearBotonMenu("Pacientes", "GESTION_PACIENTES", "pacientes");
        pnlMenu.add(btnPacientes);
        pnlMenu.add(Box.createVerticalStrut(12));

        btnCalendario = crearBotonMenu("Sacar Turnos", "CALENDARIO_TURNOS", "calendario");
        pnlMenu.add(btnCalendario);
        pnlMenu.add(Box.createVerticalStrut(12));

        btnTurnos = crearBotonMenu("Turnos de Hoy", "AGENDA_TURNOS", "reloj");
        pnlMenu.add(btnTurnos);
        pnlMenu.add(Box.createVerticalStrut(12));

        // La funcionalidad se conserva, pero su acceso queda oculto para todos
        // los roles. El contenedor completo se colapsa para no dejar un hueco.
        pnlTodosLosTurnosBox = new JPanel();
        pnlTodosLosTurnosBox.setLayout(new BoxLayout(pnlTodosLosTurnosBox, BoxLayout.Y_AXIS));
        pnlTodosLosTurnosBox.setOpaque(false);
        pnlTodosLosTurnosBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTodosLosTurnos = crearBotonMenu("Ver todos los turnos", "TODOS_TURNOS", "reportes");
        pnlTodosLosTurnosBox.add(btnTodosLosTurnos);
        pnlTodosLosTurnosBox.add(Box.createVerticalStrut(18));
        pnlTodosLosTurnosBox.setVisible(false);
        pnlMenu.add(pnlTodosLosTurnosBox);

        // El bloque de análisis continúa la navegación; el espacio flexible queda debajo.
        pnlMenu.add(pnlDashboardBox);
        pnlMenu.add(Box.createVerticalGlue());

        add(pnlMenu, BorderLayout.CENTER);

        // --- SECCIÓN INFERIOR: USUARIO Y CERRAR SESIÓN ---
        JPanel pnlAbajo = new JPanel();
        pnlAbajo.setLayout(new BoxLayout(pnlAbajo, BoxLayout.Y_AXIS));
        pnlAbajo.setOpaque(false);
        pnlAbajo.setBorder(new EmptyBorder(ResponsiveUI.px(20), ResponsiveUI.px(20), ResponsiveUI.px(40), ResponsiveUI.px(20)));

        // Información del Usuario Logueado
        AvatarUsuario avatarUsuario = new AvatarUsuario();
        avatarUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblUsuarioActual = new JLabel("Usuario");
        lblUsuarioActual.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblUsuarioActual.setForeground(Color.WHITE);
        lblUsuarioActual.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblRolActual = new JLabel("Rol");
        lblRolActual.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRolActual.setForeground(new Color(220, 240, 235));
        lblRolActual.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblRolActual.setVisible(true);

        pnlAbajo.add(avatarUsuario);
        pnlAbajo.add(Box.createVerticalStrut(10));
        pnlAbajo.add(lblUsuarioActual);
        pnlAbajo.add(Box.createVerticalStrut(5));
        pnlAbajo.add(lblRolActual);
        pnlAbajo.add(Box.createVerticalStrut(25));

        // Botón Cerrar Sesión
        JButton btnCerrar = new BotonSidebar("CERRAR SESIÓN", new Color(211, 47, 47), 16, "salir");
        btnCerrar.setBackground(new Color(211, 47, 47));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCerrar.setMaximumSize(new Dimension(240, 50));
        btnCerrar.setPreferredSize(new Dimension(240, 50));
        btnCerrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setOpaque(false);
        btnCerrar.putClientProperty("estiloRedondeado.omitirBoton", Boolean.TRUE);
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> {
            mainApp.cerrarSesion();
        });

        pnlAbajo.add(btnCerrar);
        add(pnlAbajo, BorderLayout.SOUTH);
    }

    private JButton crearBotonMenu(String texto, String panelDestino, String icono) {
        JButton btn = new BotonSidebar(texto, new Color(0, 121, 107), 16, icono);
        btn.setBackground(new Color(0, 105, 92));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));

        btn.setMaximumSize(new Dimension(240, 55));
        btn.setPreferredSize(new Dimension(240, 55));

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.putClientProperty("estiloRedondeado.omitirBoton", Boolean.TRUE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> mainApp.cambiarPanel(panelDestino));
        return btn;
    }

    private JButton crearBotonSubmenu(String texto, String panelDestino) {
        JButton btn = new BotonSidebar(texto, new Color(0, 121, 107), 16);
        btn.setBackground(new Color(0, 121, 107));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setMaximumSize(new Dimension(210, 38));
        btn.setPreferredSize(new Dimension(210, 38));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.putClientProperty("estiloRedondeado.omitirBoton", Boolean.TRUE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        btn.addActionListener(e -> mainApp.cambiarPanel(panelDestino));
        return btn;
    }

    private void alternarSubmenuAtenciones() {
        submenuAtencionesVisible = !submenuAtencionesVisible;
        btnEstadisticas.setVisible(submenuAtencionesVisible);
        btnDashboard.setText(submenuAtencionesVisible ? "Reportes  \u25b2" : "Reportes  \u25bc");
        pnlDashboardBox.revalidate();
        pnlDashboardBox.repaint();
    }

    public void aplicarPermisos() {
        Usuario u = mainApp.getUsuarioSesion();
        if (u != null) {
            boolean esAdmin = u.esAdministrador();
            boolean esSecretaria = u.esSecretaria();

            // --- ACÁ ESTÁ EL ARREGLO DE SEGURIDAD ---
            // Si es Secretaria, Ocultamos el Dashboard.
            // Si es Médico o Admin, sí lo ven.
            if (esSecretaria) {
                pnlDashboardBox.setVisible(false);
            } else {
                pnlDashboardBox.setVisible(true);
                submenuAtencionesVisible = false;
                btnEstadisticas.setVisible(false);
                btnDashboard.setText("Reportes  \u25bc");
            }

            pnlAdminBox.setVisible(esAdmin);
            pnlTodosLosTurnosBox.setVisible(false);

            lblUsuarioActual.setText(mainApp.getNombreCortoUsuarioActual());
            String rol = mainApp.getRolUsuarioActual();
            lblRolActual.setText(rol.isBlank() ? "" : "(" + rol + ")");
            lblRolActual.setVisible(!rol.isBlank());

            this.revalidate();
            this.repaint();
        }
    }

    private static class BotonSidebar extends JButton {
        private final Color colorBase;
        private final int radio;

        BotonSidebar(String texto, Color colorBase, int radio) {
            this(texto, colorBase, radio, null);
        }

        BotonSidebar(String texto, Color colorBase, int radio, String icono) {
            super(texto);
            this.colorBase = colorBase;
            this.radio = radio;
            if (icono != null && !icono.isBlank()) {
                setIcon(new IconoMenu(icono, ResponsiveUI.px(24), Color.WHITE));
                setIconTextGap(ResponsiveUI.px(16));
            }
            setHorizontalAlignment(SwingConstants.LEFT);
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setVerticalTextPosition(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, ResponsiveUI.px(34), 0, ResponsiveUI.px(14)));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            putClientProperty("estiloRedondeado.omitirBoton", Boolean.TRUE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fondo = getModel().isPressed()
                    ? colorBase.darker()
                    : getModel().isRollover() ? colorBase.brighter() : colorBase;
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radio, radio);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class AvatarUsuario extends JComponent {
        AvatarUsuario() {
            setPreferredSize(new Dimension(54, 54));
            setMaximumSize(new Dimension(54, 54));
            setMinimumSize(new Dimension(54, 54));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 188, 174));
            int diametro = Math.min(getWidth(), getHeight());
            int circuloX = (getWidth() - diametro) / 2;
            int circuloY = (getHeight() - diametro) / 2;
            g2.fillOval(circuloX, circuloY, diametro, diametro);
            IconoMenu icono = new IconoMenu("pacientes", 30, Color.WHITE);
            int iconoX = (getWidth() - icono.getIconWidth()) / 2;
            int iconoY = (getHeight() - icono.getIconHeight()) / 2;
            icono.paintIcon(this, g2, iconoX, iconoY);
            g2.dispose();
        }
    }

    private static class IconoMenu implements Icon {
        private final String tipo;
        private final int tamano;
        private final Color color;

        IconoMenu(String tipo, int tamano, Color color) {
            this.tipo = tipo;
            this.tamano = tamano;
            this.color = color;
        }

        @Override public int getIconWidth() { return tamano; }
        @Override public int getIconHeight() { return tamano; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(2f, tamano / 12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (tipo) {
                case "reportes" -> dibujarBarras(g2, x, y);
                case "calendario" -> dibujarCalendario(g2, x, y);
                case "reloj" -> dibujarReloj(g2, x, y);
                case "grupo" -> dibujarGrupo(g2, x, y);
                case "salir" -> dibujarSalir(g2, x, y);
                default -> dibujarPersona(g2, x, y);
            }
            g2.dispose();
        }

        private void dibujarBarras(Graphics2D g2, int x, int y) {
            int base = y + tamano - 4;
            int w = Math.max(4, tamano / 6);
            g2.fillRoundRect(x + 3, base - tamano / 3, w, tamano / 3, 3, 3);
            g2.fillRoundRect(x + tamano / 2 - w / 2, base - tamano / 2, w, tamano / 2, 3, 3);
            g2.fillRoundRect(x + tamano - w - 3, y + 3, w, tamano - 7, 3, 3);
        }

        private void dibujarPersona(Graphics2D g2, int x, int y) {
            int cabeza = Math.max(7, Math.round(tamano * 0.30f));
            int centroX = x + tamano / 2;
            int cabezaY = y + Math.round(tamano * 0.20f);
            int cuerpoW = Math.round(tamano * 0.50f);
            int cuerpoH = Math.round(tamano * 0.30f);
            int cuerpoY = y + Math.round(tamano * 0.56f);
            g2.fillOval(centroX - cabeza / 2, cabezaY, cabeza, cabeza);
            g2.fillRoundRect(centroX - cuerpoW / 2, cuerpoY, cuerpoW, cuerpoH, cuerpoH, cuerpoH);
        }

        private void dibujarGrupo(Graphics2D g2, int x, int y) {
            dibujarPersona(g2, x + 3, y + 2);
            g2.fillOval(x + tamano - 10, y + 6, 6, 6);
            g2.fillRoundRect(x + tamano - 14, y + tamano / 2 + 2, 10, tamano / 4, 8, 8);
        }

        private void dibujarCalendario(Graphics2D g2, int x, int y) {
            int m = tamano / 6;
            g2.drawRoundRect(x + m, y + m + 2, tamano - m * 2, tamano - m * 2, 4, 4);
            g2.drawLine(x + m, y + tamano / 3, x + tamano - m, y + tamano / 3);
            g2.drawLine(x + tamano / 3, y + m - 1, x + tamano / 3, y + m + 5);
            g2.drawLine(x + (tamano * 2) / 3, y + m - 1, x + (tamano * 2) / 3, y + m + 5);
        }

        private void dibujarReloj(Graphics2D g2, int x, int y) {
            int m = 3;
            g2.drawOval(x + m, y + m, tamano - m * 2, tamano - m * 2);
            g2.drawLine(x + tamano / 2, y + tamano / 2, x + tamano / 2, y + tamano / 4);
            g2.drawLine(x + tamano / 2, y + tamano / 2, x + (tamano * 2) / 3, y + (tamano * 2) / 3);
        }

        private void dibujarSalir(Graphics2D g2, int x, int y) {
            int m = tamano / 5;
            g2.drawRoundRect(x + m, y + m, tamano / 2, tamano - m * 2, 3, 3);
            g2.drawLine(x + tamano / 2, y + tamano / 2, x + tamano - 3, y + tamano / 2);
            g2.drawLine(x + tamano - 8, y + tamano / 2 - 5, x + tamano - 3, y + tamano / 2);
            g2.drawLine(x + tamano - 8, y + tamano / 2 + 5, x + tamano - 3, y + tamano / 2);
        }
    }
}
