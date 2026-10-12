package clinicabalbarrey;

import clinicabalbarrey.dao.PersonalDAO;
import clinicabalbarrey.model.Usuario;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class GestionPersonalPanel extends JPanel {
    private static final Color COLOR_FILA_SELECCIONADA = new Color(232, 245, 242);
    private static final Color COLOR_TEXTO_SELECCIONADO = new Color(0, 77, 64);
    private MainApplicationView mainApp;
    private JTable tabla;
    private DefaultTableModel modelo;

    private JTextField txtBuscar;
    private SelectorAcceso selectorAcceso;

    private int paginaActual = 1;
    private int registrosPorPagina = registrosAdecuadosParaPantalla();
    private int totalPaginas = 1;
    private JLabel lblPaginacion;
    private JButton btnAnterior, btnSiguiente;
    private Timer temporizadorBusqueda;
    private int cargaToken;
    private boolean limpiandoBusqueda;

    private static int registrosAdecuadosParaPantalla() {
        if (GraphicsEnvironment.isHeadless()) return 10;
        return Toolkit.getDefaultToolkit().getScreenSize().height <= 800 ? 10 : 15;
    }

    public GestionPersonalPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(0, 25));
        setBackground(new Color(242, 244, 246));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTitulo = new JLabel("GESTIÓN DE PERSONAL");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitulo.setForeground(new Color(0, 77, 64));
        pnlTop.add(lblTitulo, BorderLayout.WEST);

        JPanel pnlBuscar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlBuscar.setOpaque(false);

        JLabel lblBuscar = new JLabel("Buscar (Nombre o DNI):");
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBuscar.setForeground(Color.DARK_GRAY);

        txtBuscar = new JTextField(20);
        txtBuscar.setPreferredSize(new Dimension(250, 35));
        EstiloRedondeado.aplicarBuscadorTexto(txtBuscar);

        JLabel lblEstado = new JLabel("Acceso:");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEstado.setForeground(Color.DARK_GRAY);
        selectorAcceso = new SelectorAcceso();
        selectorAcceso.setPreferredSize(new Dimension(190, 35));
        selectorAcceso.setAlAplicar(() -> {
            paginaActual = 1;
            cargarPersonal(false);
        });

        temporizadorBusqueda = new Timer(300, e -> { paginaActual = 1; cargarPersonal(false); });
        temporizadorBusqueda.setRepeats(false);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { programarBusqueda(); }
            public void removeUpdate(DocumentEvent e) { programarBusqueda(); }
            public void changedUpdate(DocumentEvent e) { programarBusqueda(); }
        });

        pnlBuscar.add(lblEstado);
        pnlBuscar.add(selectorAcceso);
        pnlBuscar.add(lblBuscar);
        pnlBuscar.add(txtBuscar);
        pnlTop.add(pnlBuscar, BorderLayout.EAST);

        add(pnlTop, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"ID", "DNI", "Nombre y Apellido", "Teléfono", "Especialidad", "Rol", "Acceso"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modelo);

        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setResizingAllowed(false);
        tabla.getTableHeader().setBackground(new Color(0, 121, 107));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(EstiloRedondeado.fuenteHeaderTabla());
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 42));
        ((DefaultTableCellRenderer) tabla.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        tabla.setRowHeight(44);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tabla.setSelectionBackground(COLOR_FILA_SELECCIONADA);
        tabla.setSelectionForeground(COLOR_TEXTO_SELECCIONADO);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setCellSelectionEnabled(false);
        tabla.setRowSelectionAllowed(true);
        tabla.setColumnSelectionAllowed(false);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        habilitarSeleccionPorClick();

        tabla.setFillsViewportHeight(false);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // --- PROPORCIONES ARMÓNICAS DE COLUMNAS (CORREGIDO) ---
        tabla.getColumnModel().getColumn(0).setMinWidth(0);          // ID oculto
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(0);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(145);  // DNI
        tabla.getColumnModel().getColumn(2).setPreferredWidth(310);  // Nombre y Apellido
        tabla.getColumnModel().getColumn(3).setPreferredWidth(165);  // Teléfono
        tabla.getColumnModel().getColumn(4).setPreferredWidth(210);  // Especialidad
        tabla.getColumnModel().getColumn(5).setPreferredWidth(150);  // Rol
        tabla.getColumnModel().getColumn(6).setPreferredWidth(120);  // Acceso al sistema

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                boolean filaSeleccionada = table.getSelectionModel().isSelectedIndex(row);
                Component c = super.getTableCellRendererComponent(table, value, filaSeleccionada, false, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setOpaque(true);
                String acceso = String.valueOf(value);
                if (filaSeleccionada) {
                    c.setBackground(COLOR_FILA_SELECCIONADA);
                    c.setForeground(COLOR_TEXTO_SELECCIONADO);
                } else if (column == 6 && "Activo".equalsIgnoreCase(acceso)) {
                    c.setBackground(new Color(242, 250, 247));
                    c.setForeground(new Color(0, 121, 107));
                } else if (column == 6 && "No activo".equalsIgnoreCase(acceso)) {
                    c.setBackground(new Color(255, 245, 245));
                    c.setForeground(new Color(183, 28, 28));
                } else if (column == 6 && "Sin usuario".equalsIgnoreCase(acceso)) {
                    c.setBackground(new Color(255, 249, 235));
                    c.setForeground(new Color(156, 100, 0));
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, tabla.getTableHeader().getPreferredSize().height + (tabla.getRowHeight() * registrosPorPagina) + 2));
        scroll.setBorder(EstiloRedondeado.borde(new Color(200, 200, 200), 1));

        JPanel pnlTabla = new JPanel(new BorderLayout());
        pnlTabla.setOpaque(false);
        pnlTabla.setBorder(new EmptyBorder(6, 0, 0, 0));
        pnlTabla.add(scroll, BorderLayout.NORTH);
        add(pnlTabla, BorderLayout.CENTER);

        JPanel pnlInferior = new JPanel(new BorderLayout());
        pnlInferior.setOpaque(false);

        JPanel pnlFlechitas = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        pnlFlechitas.setOpaque(false);
        btnAnterior = new JButton("< Anterior");
        btnAnterior.setCursor(new Cursor(Cursor.HAND_CURSOR));
        EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(btnAnterior);
        btnAnterior.addActionListener(e -> { paginaActual--; cargarPersonal(false); });
        lblPaginacion = new JLabel("Página 1 de 1");
        lblPaginacion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSiguiente = new JButton("Siguiente >");
        btnSiguiente.setCursor(new Cursor(Cursor.HAND_CURSOR));
        EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(btnSiguiente);
        btnSiguiente.addActionListener(e -> { paginaActual++; cargarPersonal(false); });
        pnlFlechitas.add(btnAnterior);
        pnlFlechitas.add(lblPaginacion);
        pnlFlechitas.add(btnSiguiente);
        pnlInferior.add(pnlFlechitas, BorderLayout.NORTH);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pnlBotones.setOpaque(false);

        JButton btnNuevo = crearBoton("NUEVO PERSONAL", new Color(0, 150, 136));
        btnNuevo.addActionListener(e -> mainApp.irARegistrarPersonal());

        JButton btnEditar = crearBoton("EDITAR SELECCIONADO", new Color(41, 128, 185));
        btnEditar.addActionListener(e -> editarSeleccionado());

        JButton btnOcultar = crearBoton("OCULTAR SELECCIONADO", new Color(211, 47, 47));
        btnOcultar.addActionListener(e -> ocultarSeleccionado());

        JButton btnRestaurar = crearBoton("RESTAURAR PERSONAL", new Color(46, 204, 113));
        btnRestaurar.addActionListener(e -> restaurarPersonalOculto());

        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnEditar);
        pnlBotones.add(btnOcultar);
        pnlBotones.add(btnRestaurar);

        pnlInferior.add(pnlBotones, BorderLayout.SOUTH);
        add(pnlInferior, BorderLayout.SOUTH);
    }

    private JButton crearBoton(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        if (bg.getRed() >= 170 && bg.getGreen() <= 90 && bg.getBlue() <= 90) {
            EstiloRedondeado.aplicarBotonPeligro(btn);
        } else {
            EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(btn);
        }
        return btn;
    }

    private void editarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un empleado de la lista.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Object idValue = modelo.getValueAt(fila, 0);
            if (idValue == null || idValue.toString().trim().isEmpty()) {
                return;
            }
            int id = (int) idValue;
            mainApp.irAEditarPersonal(id);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void ocultarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un empleado de la lista.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Object idValue = modelo.getValueAt(fila, 0);
            if (idValue == null || idValue.toString().trim().isEmpty()) return;

            int id = (int) idValue;
            Usuario sesion = mainApp.getUsuarioSesion();
            if (sesion != null && sesion.getId_personal() == id) {
                JOptionPane.showMessageDialog(this, "No podés ocultar tu propio usuario de personal.", "Acción no permitida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String nombre = modelo.getValueAt(fila, 2).toString();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Seguro que querés ocultar a " + nombre + "?\nTambién se desactivará su acceso al sistema si tiene usuario.",
                    "Confirmar ocultamiento", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            boolean ok = new PersonalDAO().ocultarPersonal(id);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Personal ocultado correctamente.", "Ocultado", JOptionPane.INFORMATION_MESSAGE);
                cargarPersonal(false);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo ocultar el personal seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restaurarPersonalOculto() {
        PersonalDAO dao = new PersonalDAO();
        List<clinicabalbarrey.model.Personal> ocultos = dao.listarPersonalOculto();
        if (ocultos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay personal oculto para restaurar.", "Sin personal oculto", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextField buscar = new JTextField();
        buscar.setPreferredSize(new Dimension(400, 38));
        buscar.setMaximumSize(new Dimension(400, 38));
        buscar.setAlignmentX(Component.CENTER_ALIGNMENT);
        buscar.setHorizontalAlignment(JTextField.CENTER);
        EstiloRedondeado.aplicarBuscadorTexto(buscar);
        buscar.setToolTipText("Buscar por nombre o DNI");

        JComboBox<String> combo = new JComboBox<>();
        combo.setPreferredSize(new Dimension(400, 38));
        combo.setMaximumSize(new Dimension(400, 38));
        combo.setAlignmentX(Component.CENTER_ALIGNMENT);
        EstiloRedondeado.aplicarComboFiltroTurnos(combo);
        List<clinicabalbarrey.model.Personal> coincidencias = new java.util.ArrayList<>();
        Runnable actualizarOpciones = () -> {
            String filtro = normalizarBusqueda(buscar.getText());
            coincidencias.clear();
            combo.removeAllItems();
            for (clinicabalbarrey.model.Personal p : ocultos) {
                String datos = normalizarBusqueda(p.getDni() + " " + p.getNombreYApellido());
                if (!filtro.isEmpty() && !datos.contains(filtro)) continue;
                coincidencias.add(p);
                String rol = p.getRol() != null && !p.getRol().isBlank() ? " | " + p.getRol() : "";
                combo.addItem(p.getDni() + " - " + p.getNombreYApellido() + rol);
            }
            combo.setEnabled(!coincidencias.isEmpty());
        };
        buscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { actualizarOpciones.run(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizarOpciones.run(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizarOpciones.run(); }
        });
        actualizarOpciones.run();

        JPanel campos = new JPanel();
        campos.setLayout(new BoxLayout(campos, BoxLayout.Y_AXIS));
        JLabel lblBuscarOculto = new JLabel("Buscar por nombre o DNI");
        lblBuscarOculto.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBuscarOculto.setHorizontalAlignment(SwingConstants.CENTER);
        lblBuscarOculto.setFont(new Font("Segoe UI", Font.BOLD, 13));
        campos.add(lblBuscarOculto);
        campos.add(Box.createVerticalStrut(5));
        campos.add(buscar);
        campos.add(Box.createVerticalStrut(14));
        JLabel lblSeleccionarOculto = new JLabel("Personal oculto a restaurar");
        lblSeleccionarOculto.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSeleccionarOculto.setHorizontalAlignment(SwingConstants.CENTER);
        lblSeleccionarOculto.setFont(new Font("Segoe UI", Font.BOLD, 13));
        campos.add(lblSeleccionarOculto);
        campos.add(Box.createVerticalStrut(5));
        campos.add(combo);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(4, 8, 4, 8));
        panel.setPreferredSize(new Dimension(420, 155));
        panel.add(campos, BorderLayout.CENTER);

        int opcion = JOptionPane.showConfirmDialog(this, panel, "Restaurar personal", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION || combo.getSelectedIndex() < 0
                || coincidencias.isEmpty()) return;

        clinicabalbarrey.model.Personal seleccionado = coincidencias.get(combo.getSelectedIndex());
        boolean ok = dao.restaurarPersonal(seleccionado.getIdPersonal());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Personal restaurado correctamente.", "Restaurado", JOptionPane.INFORMATION_MESSAGE);
            cargarPersonal();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo restaurar el personal seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String normalizarBusqueda(String texto) {
        if (texto == null) return "";
        String normalizado = java.text.Normalizer.normalize(
                texto.trim().toLowerCase(), java.text.Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{M}", "");
    }

    private void programarBusqueda() {
        if (!limpiandoBusqueda) temporizadorBusqueda.restart();
    }

    private void habilitarSeleccionPorClick() {
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                seleccionarFilaEnPunto(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarFilaEnPunto(e);
            }

            private void seleccionarFilaEnPunto(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                if (fila >= 0 && fila < tabla.getRowCount()) {
                    tabla.getSelectionModel().setSelectionInterval(fila, fila);
                    tabla.requestFocusInWindow();
                    tabla.repaint();
                }
            }
        });
    }

    private void limpiarBusqueda() {
        if (txtBuscar == null) return;
        temporizadorBusqueda.stop();
        limpiandoBusqueda = true;
        try {
            txtBuscar.setText("");
        } finally {
            limpiandoBusqueda = false;
        }
        paginaActual = 1;
        tabla.clearSelection();
    }

    public void cargarPersonal() {
        cargarPersonal(true);
    }

    private void cargarPersonal(boolean limpiarBusqueda) {
        if (limpiarBusqueda) limpiarBusqueda();
        final int token = ++cargaToken;
        final int pagina = paginaActual;
        final String filtro = txtBuscar != null ? txtBuscar.getText().trim() : "";
        final List<String> estadosAcceso = selectorAcceso != null
                ? selectorAcceso.valoresSeleccionados() : List.of();
        new SwingWorker<CargaPersonal, Void>() {
            @Override protected CargaPersonal doInBackground() {
                PersonalDAO dao = new PersonalDAO();
                int total = dao.contarPersonal(filtro, estadosAcceso);
                int paginas = Math.max(1, (int)Math.ceil((double)total / registrosPorPagina));
                int offset = (Math.min(pagina, paginas) - 1) * registrosPorPagina;
                return new CargaPersonal(paginas,
                        dao.listarPersonalPaginado(filtro, registrosPorPagina, offset, estadosAcceso));
            }
            @Override protected void done() {
                if (token != cargaToken) return;
                try {
                    CargaPersonal carga = get();
                    totalPaginas = carga.paginas();
                    paginaActual = Math.min(paginaActual, totalPaginas);
                    lblPaginacion.setText("Página " + paginaActual + " de " + totalPaginas);
                    btnAnterior.setEnabled(paginaActual > 1);
                    btnSiguiente.setEnabled(paginaActual < totalPaginas);
                    modelo.setRowCount(0);
                    for (clinicabalbarrey.model.Personal p : carga.personal()) {
                        modelo.addRow(new Object[]{
                            p.getIdPersonal(),
                            p.getDni(),
                            p.getNombreYApellido(),
                            p.getTelefono(),
                            p.getEspecialidad() != null ? p.getEspecialidad() : "",
                            p.getRol(),
                            !p.isTieneUsuario() ? "Sin usuario"
                                    : p.isAccesoActivo() ? "Activo" : "No activo"
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(GestionPersonalPanel.this, "No se pudo cargar el personal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static class SelectorAcceso extends JButton {
        private final JPopupMenu popup = new JPopupMenu();
        private final JCheckBox activos = crearCasilla("Activos");
        private final JCheckBox noActivos = crearCasilla("No activos");
        private final JCheckBox sinUsuario = crearCasilla("Sin usuario");
        private Runnable alAplicar = () -> {};

        SelectorAcceso() {
            EstiloRedondeado.aplicarSelectorFiltro(this);
            setText("Sin filtro");
            construirPopup();
            addActionListener(e -> mostrarPopup());
        }

        private static JCheckBox crearCasilla(String texto) {
            JCheckBox casilla = new JCheckBox(texto);
            casilla.setBackground(Color.WHITE);
            casilla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            casilla.setAlignmentX(Component.CENTER_ALIGNMENT);
            casilla.setHorizontalAlignment(SwingConstants.LEFT);
            casilla.setPreferredSize(new Dimension(140, 26));
            casilla.setMinimumSize(new Dimension(140, 26));
            casilla.setMaximumSize(new Dimension(140, 26));
            return casilla;
        }

        private void construirPopup() {
            JPanel contenido = new JPanel();
            contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
            contenido.setBackground(Color.WHITE);
            contenido.setBorder(new EmptyBorder(10, 12, 10, 12));
            contenido.setPreferredSize(new Dimension(245, 155));

            contenido.add(activos);
            contenido.add(Box.createVerticalStrut(5));
            contenido.add(noActivos);
            contenido.add(Box.createVerticalStrut(5));
            contenido.add(sinUsuario);
            contenido.add(Box.createVerticalStrut(10));

            JButton listo = new JButton("Listo");
            listo.setAlignmentX(Component.CENTER_ALIGNMENT);
            listo.setBackground(new Color(0, 121, 107));
            listo.setForeground(Color.WHITE);
            listo.setFocusPainted(false);
            listo.setPreferredSize(new Dimension(90, 30));
            listo.setMaximumSize(new Dimension(90, 30));
            listo.addActionListener(e -> {
                actualizarTexto();
                popup.setVisible(false);
                alAplicar.run();
            });
            contenido.add(listo);
            popup.setBorder(BorderFactory.createLineBorder(new Color(170, 181, 186)));
            popup.add(contenido);
        }

        void setAlAplicar(Runnable alAplicar) {
            this.alAplicar = alAplicar != null ? alAplicar : () -> {};
        }

        List<String> valoresSeleccionados() {
            List<String> valores = new ArrayList<>();
            if (activos.isSelected()) valores.add("activo");
            if (noActivos.isSelected()) valores.add("inactivo");
            if (sinUsuario.isSelected()) valores.add("sin_usuario");
            return valores;
        }

        private void actualizarTexto() {
            List<String> valores = valoresSeleccionados();
            if (valores.isEmpty()) {
                setText("Sin filtro");
            } else if (valores.size() == 1) {
                setText(activos.isSelected() ? "Activos"
                        : noActivos.isSelected() ? "No activos" : "Sin usuario");
            } else {
                setText(valores.size() + " seleccionados");
            }
            setToolTipText(valores.isEmpty() ? "Sin filtro de acceso"
                    : String.join(", ", valores));
        }

        private void mostrarPopup() {
            if (!isShowing()) return;
            popup.setPopupSize(Math.max(245, getWidth()), 155);
            popup.show(this, 0, getHeight());
        }
    }

    private record CargaPersonal(int paginas, List<clinicabalbarrey.model.Personal> personal) {}
}
