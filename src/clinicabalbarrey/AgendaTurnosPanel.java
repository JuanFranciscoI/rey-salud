package clinicabalbarrey;

import clinicabalbarrey.ConexionBD;
import clinicabalbarrey.dao.TurnoDAO;
import clinicabalbarrey.model.Usuario;
import clinicabalbarrey.security.PoliticaTurnos;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

public class AgendaTurnosPanel extends JPanel {

    private MainApplicationView mainApp;
    private JTable tablaTurnos;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> cmbFiltroMedico;

    private List<String[]> listaTurnosBD = new ArrayList<>();
    private List<String> todosLosMedicos = new ArrayList<>();
    private boolean actualizandoEstado;
    private boolean actualizandoFiltroMedicos;
    private boolean ajustandoTextoFiltroMedicos;
    private boolean filtradoMedicosPendiente;
    private boolean busquedaMedicosConfigurada;
    private static final long MARGEN_POSIBLE_AUSENTE_MS = 30L * 60L * 1000L;

    public AgendaTurnosPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(new Color(242, 245, 247));

        // =====================================================================
        // 1. ENCABEZADO Y FILTROS
        // =====================================================================
        JPanel pnlNorte = new JPanel(new BorderLayout());
        pnlNorte.setBackground(new Color(242, 245, 247));
        pnlNorte.setBorder(new EmptyBorder(30, 60, 20, 60));

        JLabel lblTitulo = new JLabel("AGENDA DE TURNOS (HOY)");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(0, 105, 92));
        pnlNorte.add(lblTitulo, BorderLayout.WEST);

        JPanel pnlFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlFiltros.setOpaque(false);

        Usuario sesionActual = mainApp.getUsuarioSesion();
        boolean esMedicoActual = sesionActual != null && sesionActual.esMedico();
        JLabel lblFiltro = new JLabel(esMedicoActual ? "Médico:" : "Buscar médico:");
        lblFiltro.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFiltro.setForeground(new Color(100, 110, 115));

        cmbFiltroMedico = new JComboBox<>();
        cmbFiltroMedico.setEditable(!esMedicoActual);
        estilizarComboBox(cmbFiltroMedico);
        cmbFiltroMedico.setPreferredSize(new Dimension(250, 35));

        configurarBuscadorInteligente(cmbFiltroMedico, () -> aplicarFiltro());
        cmbFiltroMedico.addItemListener(e -> {
            if (e.getStateChange() != java.awt.event.ItemEvent.SELECTED
                    || actualizandoFiltroMedicos || !cmbFiltroMedico.isEditable()) return;
            String nombreSeleccionado = e.getItem() != null ? e.getItem().toString().trim() : "";
            if (nombreSeleccionado.isEmpty()) return;

            // La edición del combo también dispara el DocumentListener. Esperamos a que
            // termine esa actualización y confirmamos la opción elegida con un solo clic.
            SwingUtilities.invokeLater(() -> {
                restaurarListaCompletaMedicosConSeleccion(nombreSeleccionado);
                aplicarFiltro();
            });
        });

        pnlFiltros.add(lblFiltro);
        pnlFiltros.add(cmbFiltroMedico);
        pnlNorte.add(pnlFiltros, BorderLayout.EAST);

        add(pnlNorte, BorderLayout.NORTH);

        // =====================================================================
        // 2. CUERPO CENTRAL Y TABLA
        // =====================================================================
        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.setBackground(new Color(242, 245, 247));
        pnlCentro.setBorder(new EmptyBorder(0, 60, 40, 60));

        String[] columnas = {"Fecha", "Hora", "Paciente", "Médico Asignado", "Especialidad", "Estado", "DNI_OCULTO", "ID_TURNO_OCULTO", "FECHAHORA_OCULTA"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 && puedeEditarEstadoFila(row);
            }
        };

        tablaTurnos = new JTable(modeloTabla);
        tablaTurnos.setRowHeight(75);
        tablaTurnos.setShowVerticalLines(false);
        tablaTurnos.setShowHorizontalLines(true);
        tablaTurnos.setGridColor(new Color(235, 238, 240));
        tablaTurnos.setIntercellSpacing(new Dimension(0, 0));
        tablaTurnos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaTurnos.setCellSelectionEnabled(false);
        tablaTurnos.setRowSelectionAllowed(true);
        tablaTurnos.setColumnSelectionAllowed(false);

        JTableHeader header = tablaTurnos.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(0, 121, 107));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        for (int col = 6; col <= 8; col++) {
            tablaTurnos.getColumnModel().getColumn(col).setMinWidth(0);
            tablaTurnos.getColumnModel().getColumn(col).setMaxWidth(0);
            tablaTurnos.getColumnModel().getColumn(col).setWidth(0);
        }

        String[] opcionesEstado = sesionActual != null && sesionActual.esSecretaria()
                ? new String[]{"Cancelado"}
                : new String[]{"Reservado", "Esperando", "En Consulta", "Cancelado", "Ausente"};
        JComboBox<String> comboEstados = new JComboBox<>(opcionesEstado);

        comboEstados.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton("▼");
                button.setFont(new Font("Segoe UI", Font.BOLD, 10));
                button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
                button.setContentAreaFilled(false);
                button.setFocusPainted(false);
                return button;
            }
        });

        comboEstados.setOpaque(true);
        comboEstados.setFont(new Font("Segoe UI", Font.BOLD, 12));
        comboEstados.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comboEstados.setBorder(BorderFactory.createEmptyBorder());

        comboEstados.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, false, false);
                String estado = value != null ? value.toString() : "Reservado";
                label.setText(estado);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setOpaque(true);
                label.setBackground(getColorPildoraBg(estado));
                label.setForeground(getColorPildoraFg(estado));
                if (index >= 0) {
                    label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                    if (isSelected) {
                        label.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(getColorPildoraFg(estado)),
                                BorderFactory.createEmptyBorder(5, 9, 5, 9)));
                    }
                } else {
                    label.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 4));
                }
                return label;
            }
        });

        TableColumn columnaEstado = tablaTurnos.getColumnModel().getColumn(5);
        columnaEstado.setCellEditor(new CeldaApiladaEditor(comboEstados));
        columnaEstado.setCellRenderer(new CeldaApiladaRenderer());
        tablaTurnos.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                int filaVista = tablaTurnos.rowAtPoint(e.getPoint());
                int columna = tablaTurnos.columnAtPoint(e.getPoint());
                if (filaVista < 0 || columna != 5) return;
                int filaModelo = tablaTurnos.convertRowIndexToModel(filaVista);
                if (!modeloTabla.isCellEditable(filaModelo, columna)) return;

                tablaTurnos.editCellAt(filaVista, columna, e);
                SwingUtilities.invokeLater(() -> {
                    if (tablaTurnos.isEditing()
                            && tablaTurnos.getEditingRow() == filaVista
                            && tablaTurnos.getEditingColumn() == columna) {
                        comboEstados.requestFocusInWindow();
                        comboEstados.showPopup();
                    }
                });
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                int filaVista = tablaTurnos.rowAtPoint(e.getPoint());
                int columna = tablaTurnos.columnAtPoint(e.getPoint());
                if (filaVista < 0 || columna != 5) return;

                int filaModelo = tablaTurnos.convertRowIndexToModel(filaVista);
                String estado = String.valueOf(modeloTabla.getValueAt(filaModelo, 5));
                Usuario sesion = mainApp.getUsuarioSesion();
                if (!puedeCargarHistoria(sesion) || !puedeAbrirHistoriaDesdeEstado(estado)) return;

                Rectangle celda = tablaTurnos.getCellRect(filaVista, columna, true);
                if (e.getY() - celda.y <= 36) abrirHistoriaFila(filaModelo);
            }
        });

        FilaColorRenderer filaRenderer = new FilaColorRenderer();
        for (int i = 0; i < 5; i++) {
            tablaTurnos.getColumnModel().getColumn(i).setCellRenderer(filaRenderer);
        }

        tablaTurnos.getColumnModel().getColumn(0).setPreferredWidth(100);
        tablaTurnos.getColumnModel().getColumn(1).setPreferredWidth(80);
        tablaTurnos.getColumnModel().getColumn(2).setPreferredWidth(300);
        tablaTurnos.getColumnModel().getColumn(3).setPreferredWidth(230);
        tablaTurnos.getColumnModel().getColumn(4).setPreferredWidth(180);
        tablaTurnos.getColumnModel().getColumn(5).setPreferredWidth(190);

        JScrollPane scrollTabla = new JScrollPane(tablaTurnos);
        scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());

        JPanel pnlTablaContenedor = new JPanel(new BorderLayout());
        pnlTablaContenedor.setBackground(Color.WHITE);
        pnlTablaContenedor.setBorder(EstiloRedondeado.borde(new Color(225, 228, 230), 1));
        pnlTablaContenedor.add(scrollTabla, BorderLayout.CENTER);

        pnlCentro.add(pnlTablaContenedor, BorderLayout.CENTER);
        add(pnlCentro, BorderLayout.CENTER);

        // =====================================================================
        // EVENTOS DE ACTUALIZACIÓN
        // =====================================================================
        modeloTabla.addTableModelListener(e -> {
            if (!actualizandoEstado && e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == 5) {
                    // Al cerrar sesión Swing puede entregar el último evento del editor
                    // después de ocultar la agenda. No debe interpretarse como un cambio real.
                    if (mainApp.getUsuarioSesion() == null) return;
                    String nuevoEstadoVisual = modeloTabla.getValueAt(row, 5).toString();
                    String nuevoEstadoDB = nuevoEstadoVisual.toLowerCase();
                    String idTurnoStr = modeloTabla.getValueAt(row, 7).toString();

                    try {
                        int idTurno = Integer.parseInt(idTurnoStr);
                        String estadoAnterior = "Reservado";
                        for (String[] filaLista : listaTurnosBD) {
                            if (filaLista[7].equals(idTurnoStr)) { estadoAnterior = filaLista[5]; break; }
                        }
                        // Al pulsar "Historia Clinica" Swing cierra el editor de la celda.
                        // Ese cierre no es un cambio de estado y no debe volver a persistir
                        // el mismo valor (ni mostrar un error si la historia abre enseguida).
                        if (normalizarTexto(estadoAnterior).equals(normalizarTexto(nuevoEstadoVisual))) {
                            return;
                        }
                        TurnoDAO turnoDAO = new TurnoDAO();
                        String motivoRol = PoliticaTurnos.validarCambio(mainApp.getUsuarioSesion(), estadoAnterior, nuevoEstadoDB);
                        if (motivoRol != null) {
                            actualizandoEstado = true;
                            modeloTabla.setValueAt(estadoAnterior, row, 5);
                            actualizandoEstado = false;
                            JOptionPane.showMessageDialog(this, motivoRol, "Cambio de estado no permitido", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        boolean correccionAdmin = mainApp.getUsuarioSesion() != null
                                && mainApp.getUsuarioSesion().esAdministrador();
                        String motivoInvalido = turnoDAO.validarCambioEstado(idTurno, nuevoEstadoDB, correccionAdmin);
                        if (motivoInvalido != null) {
                            actualizandoEstado = true;
                            modeloTabla.setValueAt(estadoAnterior, row, 5);
                            actualizandoEstado = false;
                            JOptionPane.showMessageDialog(this, motivoInvalido, "Cambio de estado no permitido", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if (!turnoDAO.cambiarEstadoTurno(idTurno, nuevoEstadoDB, correccionAdmin)) {
                            actualizandoEstado = true;
                            modeloTabla.setValueAt(estadoAnterior, row, 5);
                            actualizandoEstado = false;
                            JOptionPane.showMessageDialog(this, "No se pudo guardar el nuevo estado. La tabla fue restaurada.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        if (nuevoEstadoDB.equals("cancelado")) {
                            SwingUtilities.invokeLater(() -> {
                                listaTurnosBD.removeIf(fila -> fila[7].equals(idTurnoStr));
                                aplicarFiltro();
                            });
                        } else {
                            for (String[] filaLista : listaTurnosBD) {
                                if (filaLista[7].equals(idTurnoStr)) {
                                    filaLista[5] = nuevoEstadoVisual;
                                    break;
                                }
                            }
                            tablaTurnos.repaint();
                        }
                    } catch (Exception ex) {
                        actualizandoEstado = false;
                        JOptionPane.showMessageDialog(this, "No se pudo actualizar el turno: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                cargarTurnos();
            }
        });

        new javax.swing.Timer(60_000, e -> {
            if (!isShowing()) return;
            int actualizados = new TurnoDAO().marcarAusentesVencidos();
            if (actualizados > 0) cargarTurnos();
            else tablaTurnos.repaint();
        }).start();
    }

    // =====================================================================
    // LOGICA DEL BUSCADOR INTELIGENTE
    // =====================================================================
    private void configurarBuscadorInteligente(JComboBox<String> cmb, Runnable accionDeFiltro) {
        Usuario sesion = mainApp.getUsuarioSesion();
        boolean puedeBuscar = sesion == null || !sesion.esMedico();
        cmb.setEditable(puedeBuscar);
        if (!puedeBuscar) return;
        if (busquedaMedicosConfigurada) return;
        busquedaMedicosConfigurada = true;
        JTextField editor = (JTextField) cmb.getEditor().getEditorComponent();
        editor.setToolTipText("Escriba el nombre del médico para buscar");
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { programarFiltradoMedicos(editor, accionDeFiltro); }
            @Override public void removeUpdate(DocumentEvent e) { programarFiltradoMedicos(editor, accionDeFiltro); }
            @Override public void changedUpdate(DocumentEvent e) { programarFiltradoMedicos(editor, accionDeFiltro); }
        });
        editor.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                SwingUtilities.invokeLater(() -> mostrarListaCompletaMedicos(editor, cmb));
            }
        });
        editor.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    SwingUtilities.invokeLater(() -> mostrarListaCompletaMedicos(editor, cmb));
                }
            }
        });
        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    if (!editor.getText().isBlank()
                            && cmb.getSelectedIndex() < 0 && cmb.getItemCount() > 0) {
                        cmb.setSelectedIndex(0);
                    }
                    cmb.hidePopup();
                }
            }
        });
    }

    private void mostrarListaCompletaMedicos(JTextField editor, JComboBox<String> cmb) {
        recargarListaCompletaConTextoActual(editor);
        if (cmb.getItemCount() > 0 && cmb.isShowing()) {
            try {
                cmb.showPopup();
            } catch (IllegalComponentStateException ignored) {
                // El combo todavía no está visible.
            }
        }
    }

    private void recargarListaCompletaConTextoActual(JTextField editor) {
        String textoActual = editor.getText();
        int posicion = editor.getCaretPosition();
        ajustandoTextoFiltroMedicos = true;
        try {
            actualizarOpcionesMedicos("");
            editor.setText(textoActual);
            editor.setCaretPosition(Math.min(posicion, textoActual.length()));
        } finally {
            ajustandoTextoFiltroMedicos = false;
        }
    }

    private void programarFiltradoMedicos(JTextField editor, Runnable accionDeFiltro) {
        if (actualizandoFiltroMedicos || ajustandoTextoFiltroMedicos || filtradoMedicosPendiente) return;
        filtradoMedicosPendiente = true;
        SwingUtilities.invokeLater(() -> {
            filtradoMedicosPendiente = false;
            if (actualizandoFiltroMedicos || ajustandoTextoFiltroMedicos) return;

            String texto = editor.getText();
            int posicion = editor.getCaretPosition();
            actualizarOpcionesMedicos(texto);

            ajustandoTextoFiltroMedicos = true;
            try {
                editor.setText(texto);
                editor.setCaretPosition(Math.min(posicion, texto.length()));
            } finally {
                ajustandoTextoFiltroMedicos = false;
            }

            if (editor.hasFocus() && cmbFiltroMedico.getItemCount() > 0) {
                try {
                    cmbFiltroMedico.showPopup();
                } catch (IllegalComponentStateException ignored) {
                    // El combo todavia no esta visible.
                }
            } else if (cmbFiltroMedico.isShowing()) {
                cmbFiltroMedico.hidePopup();
            }
            accionDeFiltro.run();
        });
    }

    private void actualizarOpcionesMedicos(String filtro) {
        String buscado = normalizarTexto(filtro == null ? "" : filtro.trim());
        java.awt.event.ActionListener[] listeners = cmbFiltroMedico.getActionListeners();
        actualizandoFiltroMedicos = true;
        try {
            for (java.awt.event.ActionListener listener : listeners) cmbFiltroMedico.removeActionListener(listener);
            cmbFiltroMedico.removeAllItems();
            List<String> medicosOrdenados = new ArrayList<>(todosLosMedicos);
            medicosOrdenados.sort((a, b) -> normalizarTexto(a).compareTo(normalizarTexto(b)));
            for (String medico : medicosOrdenados) {
                if (buscado.isEmpty() || coincideDesdeInicio(medico, buscado)) {
                    cmbFiltroMedico.addItem(medico);
                }
            }
            cmbFiltroMedico.setSelectedIndex(-1);
        } finally {
            for (java.awt.event.ActionListener listener : listeners) cmbFiltroMedico.addActionListener(listener);
            actualizandoFiltroMedicos = false;
        }
    }

    private void restaurarListaCompletaMedicosConSeleccion(String nombreMedico) {
        if (nombreMedico == null || nombreMedico.isBlank()) return;

        actualizandoFiltroMedicos = true;
        ajustandoTextoFiltroMedicos = true;
        try {
            cmbFiltroMedico.removeAllItems();

            List<String> medicosOrdenados = new ArrayList<>(todosLosMedicos);
            medicosOrdenados.sort((a, b) -> normalizarTexto(a).compareTo(normalizarTexto(b)));
            for (String medico : medicosOrdenados) {
                cmbFiltroMedico.addItem(medico);
            }

            cmbFiltroMedico.setSelectedItem(nombreMedico);
            if (cmbFiltroMedico.getEditor().getEditorComponent() instanceof JTextField editor) {
                editor.setText(nombreMedico);
                editor.setCaretPosition(0);
                editor.selectAll();
            }
        } finally {
            ajustandoTextoFiltroMedicos = false;
            actualizandoFiltroMedicos = false;
        }

        if (cmbFiltroMedico.isPopupVisible()) {
            cmbFiltroMedico.hidePopup();
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String limpio = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
        limpio = limpio.replaceAll("[\\p{M}]", "");
        return limpio.toLowerCase();
    }

    private boolean coincideDesdeInicio(String texto, String buscadoNormalizado) {
        if (buscadoNormalizado == null || buscadoNormalizado.isEmpty()) return true;
        String textoNormalizado = normalizarTexto(texto).trim();
        if (textoNormalizado.startsWith(buscadoNormalizado)) return true;
        for (String palabra : textoNormalizado.split("\\s+")) {
            if (palabra.startsWith(buscadoNormalizado)) return true;
        }
        return false;
    }

    private void estilizarComboBox(JComboBox<String> cmb) {
        EstiloRedondeado.aplicarComboBuscador(cmb);
        cmb.setUI(new FiltroTurnosComboBoxUI(8));
        Color verde = new Color(0, 150, 136);
        cmb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cmb.setForeground(new Color(55, 65, 72));
        cmb.setBackground(Color.WHITE);
        cmb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cmb.setBorder(BorderFactory.createCompoundBorder(
                EstiloRedondeado.borde(verde, 1),
                BorderFactory.createEmptyBorder(0, 8, 0, 3)));
        cmb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
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
        if (cmb.getEditor().getEditorComponent() instanceof JTextField txt) {
            txt.setFont(new Font("Segoe UI", Font.BOLD, 13));
            txt.setHorizontalAlignment(JTextField.CENTER);
            txt.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
        }
    }

    private static class FiltroTurnosComboBoxUI extends javax.swing.plaf.basic.BasicComboBoxUI {
        private final int maxRows;

        FiltroTurnosComboBoxUI(int maxRows) {
            this.maxRows = maxRows;
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

        @Override
        protected javax.swing.plaf.basic.ComboPopup createPopup() {
            return new javax.swing.plaf.basic.BasicComboPopup(comboBox) {
                {
                    setOpaque(true);
                    setBackground(Color.WHITE);
                    setBorder(BorderFactory.createLineBorder(new Color(190, 198, 202)));
                    list.setOpaque(true);
                    list.setBackground(Color.WHITE);
                    scroller.setOpaque(true);
                    scroller.setBackground(Color.WHITE);
                    scroller.getViewport().setOpaque(true);
                    scroller.getViewport().setBackground(Color.WHITE);
                }

                @Override
                public void show() {
                    Dimension tamano = calcularTamanoPopup();
                    setOpaque(true);
                    setBackground(Color.WHITE);
                    list.setOpaque(true);
                    list.setBackground(Color.WHITE);
                    scroller.setOpaque(true);
                    scroller.getViewport().setOpaque(true);
                    scroller.getViewport().setBackground(Color.WHITE);
                    scroller.setPreferredSize(tamano);
                    scroller.setMinimumSize(tamano);
                    scroller.setMaximumSize(tamano);
                    list.setFixedCellWidth(tamano.width);
                    list.setFixedCellHeight(calcularAltoFila());
                    super.show();
                }

                @Override
                protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
                    Dimension tamano = calcularTamanoPopup();
                    return super.computePopupBounds(px, py + 6, tamano.width, tamano.height);
                }

                private Dimension calcularTamanoPopup() {
                    int altoFila = calcularAltoFila();
                    int filas = Math.max(1, Math.min(maxRows, comboBox.getItemCount()));
                    int alto = altoFila * filas + 6;
                    return new Dimension(Math.max(1, comboBox.getWidth()), alto);
                }

                private int calcularAltoFila() {
                    int altoFila = Math.max(32,
                            comboBox.getFontMetrics(comboBox.getFont()).getHeight() + 14);
                    if (list.getCellRenderer() != null) {
                        for (int i = 0; i < comboBox.getItemCount(); i++) {
                            Component celda = list.getCellRenderer().getListCellRendererComponent(
                                    list, comboBox.getItemAt(i), i, false, false);
                            altoFila = Math.max(altoFila, celda.getPreferredSize().height);
                        }
                    }
                    return altoFila;
                }
            };
        }
    }

    // =====================================================================
    // SEGURIDAD DE CARGA: FILTRO OBLIGATORIO POR ROL
    // =====================================================================
    private void cargarMedicosDesdeBD(boolean limpiarFiltro) {
        actualizandoFiltroMedicos = true;
        String textoBuscador = "";
        if (!limpiarFiltro && cmbFiltroMedico.getEditor().getEditorComponent() instanceof JTextField) {
            textoBuscador = ((JTextField) cmbFiltroMedico.getEditor().getEditorComponent()).getText();
        }

        java.awt.event.ActionListener[] listeners = cmbFiltroMedico.getActionListeners();
        for (java.awt.event.ActionListener l : listeners) cmbFiltroMedico.removeActionListener(l);

        todosLosMedicos.clear();
        cmbFiltroMedico.removeAllItems();

        Usuario sesion = mainApp.getUsuarioSesion();
        boolean esMedico = sesion != null && sesion.esMedico();
        String nombreMedicoSesion = "";

        // Si es médico, descubrimos su nombre real en la BD para auto-filtrarlo
        if (esMedico) {
            try {
                Connection cn = ConexionBD.getConexion();
                int idPers = sesion.getId_personal();
                if (idPers <= 0) {
                    PreparedStatement pst = cn.prepareStatement("SELECT id_personal FROM usuario WHERE id_usuario = ?");
                    pst.setInt(1, sesion.getId_usuario());
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) idPers = rs.getInt("id_personal");
                }
                PreparedStatement pst2 = cn.prepareStatement("SELECT nombreyapellido FROM personal WHERE id_personal = ?");
                pst2.setInt(1, idPers);
                ResultSet rs2 = pst2.executeQuery();
                if (rs2.next()) nombreMedicoSesion = rs2.getString("nombreyapellido");
                cn.close();
            } catch(Exception e){
                System.err.println("No se pudo cargar el médico autenticado: " + e.getMessage());
            }
        }

        if (esMedico && !nombreMedicoSesion.isEmpty()) {
            // BLOQUEO ABSOLUTO: Si es médico, su combobox solo tiene su nombre y se desactiva
            todosLosMedicos.add(nombreMedicoSesion);
            cmbFiltroMedico.addItem(nombreMedicoSesion);
            cmbFiltroMedico.setSelectedItem(nombreMedicoSesion);
            cmbFiltroMedico.setEditable(false);
            cmbFiltroMedico.setEnabled(false);
        } else if (esMedico) {
            // Sesión médica inconsistente: no ofrecer acceso a agendas ajenas.
            String nombreSesion = mainApp.getNombreUsuarioActual();
            if (nombreSesion != null && !nombreSesion.isBlank()) {
                todosLosMedicos.add(nombreSesion);
                cmbFiltroMedico.addItem(nombreSesion);
                cmbFiltroMedico.setSelectedItem(nombreSesion);
            }
            cmbFiltroMedico.setEditable(false);
            cmbFiltroMedico.setEnabled(false);
        } else {
            // Carga normal para Admins/Recepción
            cmbFiltroMedico.setEnabled(true);
            cmbFiltroMedico.setEditable(true);

            try {
                Connection cn = ConexionBD.getConexion();
                String sql = "SELECT p.nombreyapellido FROM personal p INNER JOIN usuario u ON p.id_personal = u.id_personal INNER JOIN rol r ON u.id_rol = r.id_rol WHERE LOWER(r.nombre) LIKE '%medico%' OR LOWER(r.nombre) LIKE '%médico%' ORDER BY p.nombreyapellido ASC";
                sql = "SELECT DISTINCT p.nombreyapellido FROM personal p "
                        + "LEFT JOIN usuario u ON p.id_personal = u.id_personal AND u.estado::text = 'activo' "
                        + "LEFT JOIN rol r ON u.id_rol = r.id_rol "
                        + "WHERE p.estado::text = 'activo' "
                        + "AND (translate(lower(coalesce(r.nombre,'')), '\u00e1\u00e9\u00ed\u00f3\u00fa', 'aeiou') LIKE '%medico%' "
                        + "OR lower(coalesce(r.nombre,'')) LIKE '%dico%' "
                        + "OR NULLIF(TRIM(COALESCE(p.matricula,'')), '') IS NOT NULL "
                        + "OR p.id_especialidad IS NOT NULL) "
                        + "ORDER BY p.nombreyapellido ASC";
                PreparedStatement pst = cn.prepareStatement(sql);
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    String nombreMedico = rs.getString("nombreyapellido");
                    todosLosMedicos.add(nombreMedico);
                }
                todosLosMedicos.sort((a, b) -> normalizarTexto(a).compareTo(normalizarTexto(b)));
                for (String nombreMedico : todosLosMedicos) cmbFiltroMedico.addItem(nombreMedico);
                cn.close();
            } catch (Exception e) {
                System.err.println("No se pudo cargar la lista de médicos: " + e.getMessage());
            }

            if (!textoBuscador.isEmpty()) {
                ((JTextField) cmbFiltroMedico.getEditor().getEditorComponent()).setText(textoBuscador);
            } else {
                cmbFiltroMedico.setSelectedIndex(-1);
                ((JTextField) cmbFiltroMedico.getEditor().getEditorComponent()).setText("");
            }
        }

        for (java.awt.event.ActionListener l : listeners) cmbFiltroMedico.addActionListener(l);
        actualizandoFiltroMedicos = false;
    }

    private void cargarTurnosDesdeBD() {
        listaTurnosBD.clear();
        Usuario sesion = mainApp.getUsuarioSesion();
        boolean esMedico = sesion != null && sesion.esMedico();
        int idPersonal = 0;

        // Buscamos el ID exacto del médico
        if (esMedico) {
             try {
                Connection cn = ConexionBD.getConexion();
                idPersonal = sesion.getId_personal();
                if (idPersonal <= 0) {
                    PreparedStatement pst = cn.prepareStatement("SELECT id_personal FROM usuario WHERE id_usuario = ?");
                    pst.setInt(1, sesion.getId_usuario());
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) idPersonal = rs.getInt("id_personal");
                }
                cn.close();
             } catch(Exception e){
                 System.err.println("No se pudo identificar al médico autenticado: " + e.getMessage());
             }
        }

        if (esMedico && idPersonal <= 0) {
            return;
        }

        try {
            Connection cn = ConexionBD.getConexion();
            String sql = "SELECT t.id_turno, t.fechahora, p.nombre, p.apellido, p.dni, per.nombreyapellido AS medico, COALESCE(e.nombre, '') AS especialidad, t.estado " +
                         "FROM turno t " +
                         "INNER JOIN paciente p ON t.id_paciente = p.id_paciente " +
                         "INNER JOIN personal per ON t.id_personal = per.id_personal " +
                         "LEFT JOIN especialidad e ON per.id_especialidad = e.id_especialidad " +
                         "WHERE DATE(t.fechahora) = CURRENT_DATE " +
                         "AND LOWER(t.estado) != 'cancelado' " +
                         "AND LOWER(p.estado::text) = 'activo' ";

            // FILTRO DE SEGURIDAD EN LA BASE DE DATOS:
            // La BD solo le devuelve los turnos propios si es Médico. Es impenetrable.
            if (esMedico) {
                sql += "AND t.id_personal = ? ";
            }

            sql += "ORDER BY t.fechahora ASC";

            PreparedStatement pst = cn.prepareStatement(sql);
            if (esMedico) {
                pst.setInt(1, idPersonal);
            }

            ResultSet rs = pst.executeQuery();
            SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");

            while (rs.next()) {
                int idTurno = rs.getInt("id_turno");
                Timestamp fh = rs.getTimestamp("fechahora");
                String fecha = (fh != null) ? sdfFecha.format(fh) : "Sin Fecha";
                String hora = (fh != null) ? sdfHora.format(fh) : "Sin Hora";
                String pacienteNombre = rs.getString("nombre") + " " + rs.getString("apellido");
                String dniPaciente = rs.getString("dni");
                String medico = rs.getString("medico");
                String especialidad = rs.getString("especialidad");
                String estado = rs.getString("estado");
                if (estado == null || estado.trim().isEmpty() || estado.equalsIgnoreCase("pendiente")) estado = "Reservado";
                else if (estado.equalsIgnoreCase("en consulta")) estado = "En Consulta";
                else estado = estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();

                listaTurnosBD.add(new String[]{fecha, hora, pacienteNombre, medico, especialidad != null ? especialidad : "", estado, dniPaciente, String.valueOf(idTurno), String.valueOf(fh != null ? fh.getTime() : 0L)});
            }
            cn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los turnos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    // MÉTODO PÚBLICO
    // =====================================================================
    public void cargarTurnos() {
        new TurnoDAO().marcarAusentesVencidos();
        cargarMedicosDesdeBD(true);
        cargarTurnosDesdeBD();
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        if (modeloTabla == null) return;
        modeloTabla.setRowCount(0);
        String textoBuscador = "";
        if (cmbFiltroMedico.getEditor().getEditorComponent() instanceof JTextField) {
            textoBuscador = ((JTextField) cmbFiltroMedico.getEditor().getEditorComponent()).getText();
        }
        String medSelNorm = normalizarTexto(textoBuscador);

        for (String[] fila : listaTurnosBD) {
            String medFilaNorm = normalizarTexto(fila[3]);
            if (medSelNorm.isEmpty() || coincideDesdeInicio(medFilaNorm, medSelNorm)) {
                modeloTabla.addRow(fila);
            }
        }
    }

    private Color getColorFila(String estado) {
        if (estado == null) return Color.WHITE;
        switch (estado.toLowerCase()) {
            case "esperando": return new Color(255, 250, 240);
            case "en consulta": return new Color(244, 250, 255);
            case "atendido": return new Color(244, 252, 246);
            case "cancelado": return new Color(255, 246, 246);
            case "ausente": return new Color(249, 249, 249);
            default: return Color.WHITE;
        }
    }

    private Color getColorPildoraBg(String estado) {
        if (estado == null) return new Color(248, 249, 249);
        switch (estado.toLowerCase()) {
            case "esperando": return new Color(253, 242, 233);
            case "en consulta": return new Color(235, 245, 251);
            case "atendido": return new Color(233, 247, 239);
            case "cancelado": return new Color(253, 237, 236);
            case "ausente": return new Color(242, 243, 244);
            default: return new Color(248, 249, 249);
        }
    }

    private Color getColorPildoraFg(String estado) {
        if (estado == null) return new Color(52, 73, 94);
        switch (estado.toLowerCase()) {
            case "esperando": return new Color(230, 126, 34);
            case "en consulta": return new Color(41, 128, 185);
            case "atendido": return new Color(39, 174, 96);
            case "cancelado": return new Color(192, 57, 43);
            case "ausente": return new Color(127, 140, 141);
            default: return new Color(52, 73, 94);
        }
    }

    /** Descarta una selección de estado abierta antes de abandonar la sesión. */
    public void cancelarEdicionPendiente() {
        if (tablaTurnos != null && tablaTurnos.isEditing()) {
            TableCellEditor editor = tablaTurnos.getCellEditor();
            if (editor != null) editor.cancelCellEditing();
        }
        tablaTurnos.clearSelection();
    }

    private boolean puedeEditarEstados() {
        return PoliticaTurnos.puedeEditarEstadosClinicos(mainApp.getUsuarioSesion());
    }

    private boolean puedeEditarEstadoFila(int filaModelo) {
        String estado = String.valueOf(modeloTabla.getValueAt(filaModelo, 5));
        Usuario sesion = mainApp.getUsuarioSesion();
        if (sesion != null && sesion.esAdministrador()) return true;
        if (esEstadoTerminal(estado)) return false;
        if (PoliticaTurnos.puedeEditarEstadosClinicos(sesion)) return true;
        if (sesion != null && sesion.esSecretaria()) {
            return estado.equalsIgnoreCase("Reservado") || estado.equalsIgnoreCase("Esperando");
        }
        return false;
    }

    private boolean esTurnoFuturo(int filaModelo) {
        try {
            long fechaHora = Long.parseLong(modeloTabla.getValueAt(filaModelo, 8).toString());
            return fechaHora > System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean puedeMarcarAusente(int filaModelo) {
        try {
            long fechaHora = Long.parseLong(modeloTabla.getValueAt(filaModelo, 8).toString());
            return fechaHora > 0
                    && System.currentTimeMillis() >= fechaHora + MARGEN_POSIBLE_AUSENTE_MS;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean esEstadoTerminal(String estado) {
        return PoliticaTurnos.esDefinitivo(estado);
    }

    private boolean puedeCargarHistoria(Usuario sesion) {
        return sesion != null && (sesion.esMedico() || sesion.esAdministrador());
    }

    private boolean puedeAbrirHistoriaDesdeEstado(String estado) {
        if (estado == null) return false;
        String normalizado = estado.trim().toLowerCase();
        return normalizado.equals("en consulta");
    }

    private boolean esPosibleAusente(JTable table, int filaVista) {
        int filaModelo = table.convertRowIndexToModel(filaVista);
        Object estadoObj = table.getModel().getValueAt(filaModelo, 5);
        if (estadoObj == null || !"reservado".equalsIgnoreCase(estadoObj.toString())) return false;
        try {
            long fechaHora = Long.parseLong(table.getModel().getValueAt(filaModelo, 8).toString());
            return fechaHora > 0 && System.currentTimeMillis() >= fechaHora + MARGEN_POSIBLE_AUSENTE_MS;
        } catch (Exception e) {
            return false;
        }
    }

    class FilaColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setBackground(esPosibleAusente(table, row) ? new Color(255, 248, 230) : getColorFila(table.getValueAt(row, 5).toString()));
            setHorizontalAlignment(JLabel.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            c.setForeground(Color.DARK_GRAY);
            return c;
        }
    }

    class CeldaApiladaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel pnlContenedor = new JPanel(new GridBagLayout());
            String estado = (value != null) ? value.toString() : "Reservado";
            boolean posibleAusente = esPosibleAusente(table, row);
            pnlContenedor.setBackground(posibleAusente ? new Color(255, 248, 230) : getColorFila(estado));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(2, 0, 2, 0);

            Usuario sesion = mainApp.getUsuarioSesion();
            boolean puedeCargarHistoria = puedeCargarHistoria(sesion);
            if (puedeCargarHistoria && puedeAbrirHistoriaDesdeEstado(estado)) {
                JLabel lblBtn = new JLabel("Historia Clínica \u2192") {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                        g2.setColor(new Color(52, 152, 219));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                lblBtn.setHorizontalAlignment(JLabel.CENTER);
                lblBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblBtn.setForeground(new Color(52, 152, 219));
                lblBtn.setPreferredSize(new Dimension(165, 28));
                pnlContenedor.add(lblBtn, gbc);
                gbc.gridy = 1;
            }

            int filaModelo = table.convertRowIndexToModel(row);
            boolean estadoEditable = puedeEditarEstadoFila(filaModelo);
            JLabel lblPill = new JLabel((posibleAusente ? "Posible ausente" : estado) + (estadoEditable ? "  ▼" : "")) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblPill.setHorizontalAlignment(JLabel.CENTER);
            lblPill.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblPill.setBackground(posibleAusente ? new Color(255, 238, 190) : getColorPildoraBg(estado));
            lblPill.setForeground(posibleAusente ? new Color(166, 95, 0) : getColorPildoraFg(estado));
            lblPill.setToolTipText(posibleAusente
                    ? (sesion != null && sesion.esSecretaria()
                        ? "Pasaron 30 minutos: Secretaría puede confirmar Ausente o registrar la llegada como Esperando."
                        : "Pasaron 30 minutos desde el horario y el paciente no fue registrado como presente. El Médico puede confirmar Ausente.")
                    : estadoEditable ? "Cambiar estado del turno" : "El estado no puede modificarse con este rol.");
            lblPill.setPreferredSize(new Dimension(posibleAusente ? 150 : 130, 26));
            pnlContenedor.add(lblPill, gbc);
            return pnlContenedor;
        }
    }

    class CeldaApiladaEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel pnlWrapper;
        private JComboBox<String> combo;
        private String estadoSeleccionado;
        private boolean configurandoEditor;

        public CeldaApiladaEditor(JComboBox<String> comboBox) {
            this.combo = comboBox;
            this.combo.addActionListener(e -> {
                if (configurandoEditor) return;
                Object seleccion = combo.getSelectedItem();
                if (seleccion == null || seleccion.toString().isBlank()) return;
                estadoSeleccionado = seleccion.toString();
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            pnlWrapper = new JPanel(new GridBagLayout());
            estadoSeleccionado = (value != null) ? value.toString() : "Reservado";
            pnlWrapper.setBackground(getColorFila(estadoSeleccionado));

            configurandoEditor = true;
            try {
                configurarOpcionesPermitidas(table.convertRowIndexToModel(row), estadoSeleccionado);
                combo.setSelectedItem(estadoSeleccionado);
                if (combo.getSelectedItem() == null) {
                    combo.insertItemAt(estadoSeleccionado, 0);
                    combo.setSelectedIndex(0);
                }
            } finally {
                configurandoEditor = false;
            }
            combo.setBackground(getColorPildoraBg(estadoSeleccionado));
            combo.setForeground(getColorPildoraFg(estadoSeleccionado));
            combo.setPreferredSize(new Dimension(155, 28));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(2, 0, 2, 0);

            Usuario sesion = mainApp.getUsuarioSesion();
            boolean puedeCargarHistoria = puedeCargarHistoria(sesion);
            if (puedeCargarHistoria && puedeAbrirHistoriaDesdeEstado(estadoSeleccionado)) {
                JButton btnHistoria = new JButton("Historia Clínica \u2192") {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getModel().isPressed() ? new Color(230, 240, 250) : Color.WHITE);
                        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                        g2.setColor(new Color(52, 152, 219));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                btnHistoria.setContentAreaFilled(false);
                btnHistoria.setFocusPainted(false);
                btnHistoria.setBorderPainted(false);
                btnHistoria.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnHistoria.setForeground(new Color(52, 152, 219));
                btnHistoria.setPreferredSize(new Dimension(165, 28));
                btnHistoria.addActionListener(e -> {
                    // Abrir la historia no modifica el estado seleccionado.
                    fireEditingCanceled();
                    int filaModelo = table.convertRowIndexToModel(row);
                    abrirHistoriaFila(filaModelo);
                });
                pnlWrapper.add(btnHistoria, gbc);
                gbc.gridy = 1;
            }
            pnlWrapper.add(combo, gbc);
            return pnlWrapper;
        }

        private void configurarOpcionesPermitidas(int filaModelo, String estadoActual) {
            combo.removeAllItems();
            Usuario sesion = mainApp.getUsuarioSesion();
            if (sesion != null && sesion.esAdministrador()) {
                for (String opcion : new String[]{"Reservado", "Esperando", "En Consulta", "Cancelado", "Ausente"}) {
                    combo.addItem(opcion);
                }
                return;
            }
            if (sesion != null && sesion.esSecretaria()) {
                boolean esReservado = estadoActual.equalsIgnoreCase("Reservado");
                boolean esEsperando = estadoActual.equalsIgnoreCase("Esperando");
                combo.addItem(estadoActual);
                if (esReservado) combo.addItem("Esperando");
                if (esEsperando) combo.addItem("Reservado");
                if (esTurnoFuturo(filaModelo)) {
                    combo.addItem("Cancelado");
                } else if (esReservado || esEsperando) {
                    combo.addItem("En Consulta");
                }
                if (esReservado && puedeMarcarAusente(filaModelo)) combo.addItem("Ausente");
                return;
            }

            long fechaHora = 0L;
            try {
                fechaHora = Long.parseLong(modeloTabla.getValueAt(filaModelo, 8).toString());
            } catch (Exception ignored) {
                // Sin una fecha válida, se conserva únicamente el estado actual.
            }
            long ahora = System.currentTimeMillis();
            boolean posibleAusente = fechaHora > 0 && ahora >= fechaHora + MARGEN_POSIBLE_AUSENTE_MS;

            combo.addItem("Reservado");
            combo.addItem("Esperando");
            combo.addItem("En Consulta");
            if (esTurnoFuturo(filaModelo)) combo.addItem("Cancelado");
            if (posibleAusente) combo.addItem("Ausente");

            boolean existeEstadoActual = false;
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (estadoActual.equalsIgnoreCase(combo.getItemAt(i))) {
                    existeEstadoActual = true;
                    break;
                }
            }
            if (!existeEstadoActual) combo.insertItemAt(estadoActual, 0);
        }
        @Override
        public Object getCellEditorValue() { return estadoSeleccionado; }
    }

    private void abrirHistoriaFila(int filaModelo) {
        String dniPaciente = modeloTabla.getValueAt(filaModelo, 6).toString();
        int idTurno = Integer.parseInt(modeloTabla.getValueAt(filaModelo, 7).toString());
        mainApp.irANuevaConsulta(dniPaciente, idTurno);
    }
}
