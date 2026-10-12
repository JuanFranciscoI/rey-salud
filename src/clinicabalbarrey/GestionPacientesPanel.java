    package clinicabalbarrey;

import clinicabalbarrey.dao.PacienteDAO;
import clinicabalbarrey.model.Paciente;
import clinicabalbarrey.model.Usuario;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class GestionPacientesPanel extends JPanel {
    private static final Color COLOR_FILA_SELECCIONADA = new Color(232, 245, 242);
    private static final Color COLOR_TEXTO_SELECCIONADO = new Color(0, 77, 64);

    private MainApplicationView mainApp;
    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField txtBuscar;
    private SelectorEstadoPaciente selectorEstadoPaciente;

    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnHistoria;
    private JButton btnOcultar;
    private JButton btnRestaurar;

    // --- VARIABLES DE PAGINACIÓN ---
    private int paginaActual = 1;
    private int registrosPorPagina = 15;
    private int totalPaginas = 1;
    private JLabel lblPaginacion;
    private JButton btnAnterior, btnSiguiente;
    private Timer temporizadorBusqueda;
    private int cargaToken;
    private boolean limpiandoBusqueda;

    public GestionPacientesPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(0, 25));
        setBackground(new Color(242, 244, 246));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTitulo = new JLabel("GESTIÓN DE PACIENTES");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitulo.setForeground(new Color(0, 77, 64));
        pnlTop.add(lblTitulo, BorderLayout.WEST);

        JPanel pnlBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBusqueda.setOpaque(false);
        JLabel lblBuscar = new JLabel("Buscar (Nombre o DNI):");
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBuscar.setForeground(Color.DARK_GRAY);
        txtBuscar = new JTextField(18);
        txtBuscar.setPreferredSize(new Dimension(250, 35));
        EstiloRedondeado.aplicarBuscadorTexto(txtBuscar);

        temporizadorBusqueda = new Timer(300, e -> { paginaActual = 1; cargarPacientes(txtBuscar.getText().trim()); });
        temporizadorBusqueda.setRepeats(false);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { programarBusqueda(); }
            public void removeUpdate(DocumentEvent e) { programarBusqueda(); }
            public void changedUpdate(DocumentEvent e) { programarBusqueda(); }
        });

        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEstado.setForeground(Color.DARK_GRAY);
        pnlBusqueda.add(lblEstado);
        selectorEstadoPaciente = new SelectorEstadoPaciente();
        selectorEstadoPaciente.setPreferredSize(new Dimension(190, 35));
        selectorEstadoPaciente.setAlAplicar(() -> {
            if (tabla == null) return;
            paginaActual = 1;
            tabla.clearSelection();
            cargarPacientes(txtBuscar.getText().trim());
        });
        pnlBusqueda.add(selectorEstadoPaciente);
        pnlBusqueda.add(lblBuscar);
        pnlBusqueda.add(txtBuscar);
        pnlTop.add(pnlBusqueda, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"ID", "DNI", "Nombre", "Apellido", "Teléfono", "Obra Social", "Estado"}, 0) {
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

        // --- DISTRIBUCIÓN ARMÓNICA DE ANCHOS ---
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.getColumnModel().getColumn(0).setMinWidth(0);          // ID oculto
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(0);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140);  // DNI
        tabla.getColumnModel().getColumn(2).setPreferredWidth(220);  // Nombre
        tabla.getColumnModel().getColumn(3).setPreferredWidth(220);  // Apellido
        tabla.getColumnModel().getColumn(4).setPreferredWidth(170);  // Teléfono
        tabla.getColumnModel().getColumn(5).setPreferredWidth(230);  // Obra Social
        tabla.getColumnModel().getColumn(6).setPreferredWidth(130);  // Estado

        // --- CENTRAR ABSOLUTAMENTE TODO ---
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                boolean filaSeleccionada = table.getSelectionModel().isSelectedIndex(row);
                Component c = super.getTableCellRendererComponent(table, value, filaSeleccionada, false, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setOpaque(true);
                if (filaSeleccionada) {
                    c.setBackground(COLOR_FILA_SELECCIONADA);
                    c.setForeground(COLOR_TEXTO_SELECCIONADO);
                } else if (column == 6 && "Inactivo".equalsIgnoreCase(String.valueOf(value))) {
                    c.setBackground(new Color(255, 245, 245));
                    c.setForeground(new Color(183, 28, 28));
                } else if (column == 6 && "Activo".equalsIgnoreCase(String.valueOf(value))) {
                    c.setBackground(new Color(242, 250, 247));
                    c.setForeground(new Color(0, 121, 107));
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
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, scroll.getPreferredSize().height));
        scroll.setBorder(EstiloRedondeado.borde(new Color(200, 200, 200), 1));

        JPanel pnlTabla = new JPanel();
        pnlTabla.setLayout(new BoxLayout(pnlTabla, BoxLayout.Y_AXIS));
        pnlTabla.setOpaque(false);
        pnlTabla.setBorder(new EmptyBorder(6, 0, 0, 0));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlTabla.add(scroll);
        add(pnlTabla, BorderLayout.CENTER);

        JPanel pnlSouth = new JPanel(new BorderLayout());
        pnlSouth.setOpaque(false);

        JPanel pnlPaginacion = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        pnlPaginacion.setOpaque(false);

        btnAnterior = new JButton("< Anterior");
        btnAnterior.setCursor(new Cursor(Cursor.HAND_CURSOR));
        EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(btnAnterior);
        btnAnterior.addActionListener(e -> {
            if (paginaActual > 1) { paginaActual--; cargarPacientes(txtBuscar.getText().trim()); }
        });

        lblPaginacion = new JLabel("Página 1 de 1");
        lblPaginacion.setFont(new Font("Segoe UI", Font.BOLD, 14));

        btnSiguiente = new JButton("Siguiente >");
        btnSiguiente.setCursor(new Cursor(Cursor.HAND_CURSOR));
        EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(btnSiguiente);
        btnSiguiente.addActionListener(e -> {
            if (paginaActual < totalPaginas) { paginaActual++; cargarPacientes(txtBuscar.getText().trim()); }
        });

        pnlPaginacion.add(btnAnterior);
        pnlPaginacion.add(lblPaginacion);
        pnlPaginacion.add(btnSiguiente);

        pnlSouth.add(pnlPaginacion, BorderLayout.NORTH);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        pnlBotones.setOpaque(false);

        btnNuevo = crearBoton("Nuevo Paciente", new Color(0, 150, 136));
        btnNuevo.addActionListener(e -> mainApp.irARegistrarPaciente());

        btnEditar = crearBoton("Editar Datos", new Color(255, 160, 0));
        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                 // Protección contra nulos
                Object idValue = modelo.getValueAt(fila, 0);
                if(idValue == null || idValue.toString().trim().isEmpty()) return;

                int idPaciente = (int) idValue;
                if (!pacienteSeleccionadoActivo(fila)) {
                    JOptionPane.showMessageDialog(this, "El paciente seleccionado está inactivo. Restauralo antes de editar sus datos.", "Paciente inactivo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                mainApp.irAEditarPaciente(idPaciente);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un paciente de la lista", "Atención", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnHistoria = crearBoton("Historia Médica", new Color(41, 128, 185));
        btnHistoria.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                Object idValue = modelo.getValueAt(fila, 0);
                if(idValue == null || idValue.toString().trim().isEmpty()) return;
                if (!pacienteSeleccionadoActivo(fila)) {
                    JOptionPane.showMessageDialog(this, "El paciente seleccionado está inactivo. Restauralo antes de abrir su historia clínica.", "Paciente inactivo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String dni = modelo.getValueAt(fila, 1).toString().trim();
                String nombreCompleto = modelo.getValueAt(fila, 2).toString() + " " + modelo.getValueAt(fila, 3).toString();
                mainApp.irAVerHistoria(dni, nombreCompleto);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un paciente de la lista", "Atención", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnOcultar = crearBoton("Ocultar Paciente", new Color(211, 47, 47));
        btnOcultar.addActionListener(e -> ocultarSeleccionado());

        btnRestaurar = crearBoton("Restaurar Paciente", new Color(46, 204, 113));
        btnRestaurar.addActionListener(e -> restaurarPacienteOculto());

        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnEditar);
        pnlBotones.add(btnHistoria);
        pnlBotones.add(btnOcultar);
        pnlBotones.add(btnRestaurar);

        pnlSouth.add(pnlBotones, BorderLayout.SOUTH);
        pnlSouth.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlTabla.add(Box.createVerticalStrut(12));
        pnlTabla.add(pnlSouth);
    }

    private JButton crearBoton(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        if (bg.getRed() >= 170 && bg.getGreen() <= 90 && bg.getBlue() <= 90) {
            EstiloRedondeado.aplicarBotonPeligro(btn);
        } else {
            EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(btn);
        }
        return btn;
    }

    private void ocultarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un paciente de la lista", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object idValue = modelo.getValueAt(fila, 0);
        if (idValue == null || idValue.toString().trim().isEmpty()) return;

        int idPaciente = (int) idValue;
        if (!pacienteSeleccionadoActivo(fila)) {
            JOptionPane.showMessageDialog(this, "El paciente seleccionado ya está inactivo.", "Paciente inactivo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String nombreCompleto = modelo.getValueAt(fila, 2).toString() + " " + modelo.getValueAt(fila, 3).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Seguro que querés ocultar al paciente " + nombreCompleto + "?\nNo se elimina de la base de datos.",
                "Confirmar ocultamiento", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = new PacienteDAO().ocultarPaciente(idPaciente);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Paciente ocultado correctamente.", "Ocultado", JOptionPane.INFORMATION_MESSAGE);
            cargarPacientes(txtBuscar.getText().trim());
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo ocultar el paciente.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restaurarPacienteOculto() {
        PacienteDAO dao = new PacienteDAO();
        int fila = tabla.getSelectedRow();
        if (fila >= 0 && !pacienteSeleccionadoActivo(fila)) {
            Object idValue = modelo.getValueAt(fila, 0);
            if (idValue == null || idValue.toString().trim().isEmpty()) return;

            int idPaciente = (int) idValue;
            String nombreCompleto = modelo.getValueAt(fila, 2).toString() + " " + modelo.getValueAt(fila, 3).toString();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Querés restaurar al paciente " + nombreCompleto + "?\nVolverá a figurar como activo.",
                    "Confirmar restauración", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            boolean ok = dao.restaurarPaciente(idPaciente);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Paciente restaurado correctamente.", "Restaurado", JOptionPane.INFORMATION_MESSAGE);
                cargarPacientes(txtBuscar.getText().trim());
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo restaurar el paciente seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        List<Paciente> ocultos = dao.listarPacientesOcultos();
        if (ocultos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay pacientes ocultos para restaurar.", "Sin pacientes ocultos", JOptionPane.INFORMATION_MESSAGE);
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
        List<Paciente> coincidencias = new java.util.ArrayList<>();
        Runnable actualizarOpciones = () -> {
            String filtro = normalizarBusqueda(buscar.getText());
            coincidencias.clear();
            combo.removeAllItems();
            for (Paciente p : ocultos) {
                String datos = normalizarBusqueda(
                        p.getDni() + " " + p.getNombre() + " " + p.getApellido());
                if (!filtro.isEmpty() && !datos.contains(filtro)) continue;
                coincidencias.add(p);
                combo.addItem(p.getDni() + " - " + p.getApellido() + ", " + p.getNombre());
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
        JLabel lblSeleccionarOculto = new JLabel("Paciente oculto a restaurar");
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

        int opcion = JOptionPane.showConfirmDialog(this, panel, "Restaurar paciente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION || combo.getSelectedIndex() < 0
                || coincidencias.isEmpty()) return;

        Paciente seleccionado = coincidencias.get(combo.getSelectedIndex());
        boolean ok = dao.restaurarPaciente(seleccionado.getId_paciente());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Paciente restaurado correctamente.", "Restaurado", JOptionPane.INFORMATION_MESSAGE);
            cargarPacientes(txtBuscar.getText().trim());
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo restaurar el paciente seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
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
                    tabla.changeSelection(fila, 0, false, false);
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

    private boolean pacienteSeleccionadoActivo(int fila) {
        if (fila < 0 || fila >= modelo.getRowCount()) return false;
        Object estado = modelo.getValueAt(fila, 6);
        return estado == null || !"Inactivo".equalsIgnoreCase(estado.toString().trim());
    }

    private String estadoFiltroSeleccionado() {
        return selectorEstadoPaciente != null ? selectorEstadoPaciente.valorSeleccionado() : "activo";
    }

    private String presentarEstadoPaciente(String estado) {
        if (estado == null || estado.isBlank()) return "Activo";
        return "inactivo".equalsIgnoreCase(estado.trim()) ? "Inactivo" : "Activo";
    }

    public void cargarPacientes() {
        limpiarBusqueda();
        cargarPacientes("");
    }

    public void cargarPacientes(String filtroDni) {
        final int token = ++cargaToken;
        final int pagina = paginaActual;
        final String filtro = filtroDni == null ? "" : filtroDni;
        new SwingWorker<CargaPacientes, Void>() {
            @Override protected CargaPacientes doInBackground() {
                PacienteDAO dao = new PacienteDAO();
                Usuario sesion = mainApp.getUsuarioSesion();
                Integer idPersonal = sesion != null && sesion.esMedico()
                        ? sesion.getId_personal() : null;
                String estadoFiltro = estadoFiltroSeleccionado();
                int total = dao.contarPacientes(filtro, idPersonal, estadoFiltro);
                int paginas = Math.max(1, (int)Math.ceil((double)total / registrosPorPagina));
                int offset = (Math.min(pagina, paginas) - 1) * registrosPorPagina;
                return new CargaPacientes(paginas,
                        dao.listarPacientesPaginados(filtro, registrosPorPagina, offset, idPersonal, estadoFiltro));
            }
            @Override protected void done() {
                if (token != cargaToken) return;
                try {
                    CargaPacientes carga = get();
                    totalPaginas = carga.paginas();
                    paginaActual = Math.min(paginaActual, totalPaginas);
                    lblPaginacion.setText("Página " + paginaActual + " de " + totalPaginas);
                    btnAnterior.setEnabled(paginaActual > 1);
                    btnSiguiente.setEnabled(paginaActual < totalPaginas);
                    modelo.setRowCount(0);
                    for (Paciente p : carga.pacientes()) {
                        String obra = p.getObraSocialNombre() != null ? p.getObraSocialNombre() : "Particular";
                        modelo.addRow(new Object[]{
                            p.getId_paciente(),
                            p.getDni(),
                            p.getNombre(),
                            p.getApellido(),
                            p.getTelefono(),
                            obra,
                            presentarEstadoPaciente(p.getEstado())
                        });
                    }
                    Usuario u = mainApp.getUsuarioSesion();
                    btnNuevo.setVisible(u != null);
                    btnEditar.setVisible(u != null);
                    btnHistoria.setVisible(u == null || !u.esSecretaria());
                    btnOcultar.setVisible(u != null);
                    btnRestaurar.setVisible(u != null);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(GestionPacientesPanel.this, "No se pudieron cargar los pacientes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private record CargaPacientes(int paginas, List<Paciente> pacientes) {}

    private static final class SelectorEstadoPaciente extends JButton {
        private final JPopupMenu popup = new JPopupMenu();
        private final JCheckBox activos = crearCasilla("Activos");
        private final JCheckBox inactivos = crearCasilla("Inactivos");
        private final JCheckBox todos = crearCasilla("Todos");
        private Runnable alAplicar = () -> {};
        private String valor = "activo";
        private boolean actualizando;

        SelectorEstadoPaciente() {
            EstiloRedondeado.aplicarSelectorFiltro(this);
            setText("Activos");
            activos.setSelected(true);
            configurarOpcion(activos, "activo");
            configurarOpcion(inactivos, "inactivo");
            configurarOpcion(todos, "todos");
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

        private void configurarOpcion(JCheckBox opcion, String nuevoValor) {
            opcion.addActionListener(e -> {
                if (actualizando) return;
                actualizando = true;
                activos.setSelected(opcion == activos);
                inactivos.setSelected(opcion == inactivos);
                todos.setSelected(opcion == todos);
                actualizando = false;
                valor = nuevoValor;
            });
        }

        private void construirPopup() {
            JPanel contenido = new JPanel();
            contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
            contenido.setBackground(Color.WHITE);
            contenido.setBorder(new EmptyBorder(10, 12, 10, 12));
            contenido.setPreferredSize(new Dimension(245, 155));
            contenido.add(activos);
            contenido.add(Box.createVerticalStrut(5));
            contenido.add(inactivos);
            contenido.add(Box.createVerticalStrut(5));
            contenido.add(todos);
            contenido.add(Box.createVerticalStrut(10));

            JButton listo = new JButton("Listo");
            listo.setAlignmentX(Component.CENTER_ALIGNMENT);
            listo.setBackground(new Color(0, 121, 107));
            listo.setForeground(Color.WHITE);
            listo.setFocusPainted(false);
            listo.setPreferredSize(new Dimension(90, 30));
            listo.setMaximumSize(new Dimension(90, 30));
            listo.addActionListener(e -> {
                setText(activos.isSelected() ? "Activos"
                        : inactivos.isSelected() ? "Inactivos" : "Todos");
                popup.setVisible(false);
                alAplicar.run();
            });
            contenido.add(listo);
            popup.setBorder(BorderFactory.createLineBorder(new Color(170, 181, 186)));
            popup.add(contenido);
        }

        void setAlAplicar(Runnable accion) {
            alAplicar = accion != null ? accion : () -> {};
        }

        String valorSeleccionado() {
            return valor;
        }

        private void mostrarPopup() {
            if (!isShowing()) return;
            popup.setPopupSize(Math.max(245, getWidth()), 155);
            popup.show(this, 0, getHeight());
        }
    }
}
