package clinicabalbarrey;

import clinicabalbarrey.ConexionBD;
import clinicabalbarrey.model.Usuario;
import clinicabalbarrey.security.PoliticaTurnos;
import com.toedter.calendar.JCalendar;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class CalendarioTurnosPanel extends JPanel {
    private MainApplicationView mainApp;
    private JTable tablaCalendario;
    private DefaultTableModel modeloCalendario;
    private JCalendar miniCalendario;
    private JComboBox<String> cmbEspecialidades;
    private JComboBox<String> cmbMedicos;
    private JLabel lblEspecialidades;
    private JLabel lblMedicos;
    private JLabel lblMesAnio;
    private final ArrayList<Integer> idsEspecialidades = new ArrayList<>();
    private final ArrayList<Integer> todosLosIdsEspecialidades = new ArrayList<>();
    private final ArrayList<String> todasLasEspecialidades = new ArrayList<>();
    private ArrayList<Integer> idsMedicos = new ArrayList<>();
    private final ArrayList<Integer> todosLosIdsMedicos = new ArrayList<>();
    private final ArrayList<String> todosLosMedicos = new ArrayList<>();
    private final Map<Integer, Integer> especialidadPorMedico = new HashMap<>();
    private boolean actualizandoEspecialidades;
    private boolean ajustandoTextoFiltroEspecialidades;
    private boolean filtradoEspecialidadesPendiente;
    private boolean busquedaEspecialidadesConfigurada;
    private boolean actualizandoFiltroMedicos;
    private boolean ajustandoTextoFiltroMedicos;
    private boolean filtradoMedicosPendiente;
    private boolean abriendoPopupPorBusqueda;
    private boolean busquedaMedicosConfigurada;
    private int idMedicoActivo;
    private String nombreMedicoActivo = "";

    // Estructuras de datos para la grilla
    private boolean[][] horarioDisponible = new boolean[25][6];
    private String[][] nombresPacientes = new String[25][6];
    private String[][] estadosTurnos = new String[25][6];
    private int[][] idsTurnos = new int[25][6];

    private String[] horasLista = {
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
        "11:00", "11:30", "12:00", "12:30", "13:00", "13:30",
        "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
        "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00"
    };

    public CalendarioTurnosPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(242, 244, 246));
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- ENCABEZADO ---
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        JLabel lblTitulo = new JLabel("SACAR TURNOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(0, 77, 64));
        pnlTop.add(lblTitulo, BorderLayout.WEST);
        add(pnlTop, BorderLayout.NORTH);

        // --- PANEL LATERAL IZQUIERDO ---
        JPanel pnlIzquierdo = new JPanel();
        pnlIzquierdo.setLayout(new BoxLayout(pnlIzquierdo, BoxLayout.Y_AXIS));
        pnlIzquierdo.setOpaque(false);
        pnlIzquierdo.setPreferredSize(new Dimension(250, 0));

        miniCalendario = new JCalendar();
        miniCalendario.setWeekOfYearVisible(false);
        miniCalendario.setSundayForeground(new Color(0, 121, 107));
        miniCalendario.setWeekdayForeground(new Color(45, 55, 60));
        miniCalendario.setMaximumSize(new Dimension(250, 220));
        miniCalendario.setMinSelectableDate(new Date());
        pnlIzquierdo.add(miniCalendario);
        pnlIzquierdo.add(Box.createVerticalStrut(25));

        miniCalendario.addPropertyChangeListener("calendar", e -> {
            actualizarCabecerasDias();
            cargarTurnosReales();
            tablaCalendario.repaint();
        });

        cmbEspecialidades = new JComboBox<>();
        cmbEspecialidades.setMaximumRowCount(6);
        cmbEspecialidades.setEditable(true);
        EstiloRedondeado.aplicarComboBuscador(cmbEspecialidades);
        cmbEspecialidades.setUI(new SmartHeightComboBoxUI(6));
        estilizarFiltroTurnos(cmbEspecialidades);
        configurarAperturaUnClick(cmbEspecialidades);
        lblEspecialidades = agregarFiltro(pnlIzquierdo, "Filtrar por Especialidad:", cmbEspecialidades);
        cargarEspecialidadesComboBox();
        configurarBusquedaEspecialidades();
        cmbEspecialidades.addActionListener(e -> {
            if (actualizandoEspecialidades) return;
            int idEspecialidadSeleccionada = obtenerIdEspecialidadSeleccionada();
            String nombreEspecialidadSeleccionada = "";
            Object itemEspecialidad = cmbEspecialidades.getSelectedItem();
            if (itemEspecialidad != null) {
                nombreEspecialidadSeleccionada = itemEspecialidad.toString();
            }
            if (idEspecialidadSeleccionada > 0 && cmbEspecialidades.isEditable()) {
                restaurarListaCompletaEspecialidadesConSeleccion(idEspecialidadSeleccionada, nombreEspecialidadSeleccionada);
            }
            idMedicoActivo = 0;
            nombreMedicoActivo = "";
            actualizarOpcionesMedicos("");
            limpiarTextoComboMedicos();
            actualizarDisponibilidadFiltroMedicos();
            refrescarCalendarioActual();
        });

        cmbMedicos = new JComboBox<>();
        cmbMedicos.setMaximumRowCount(8);
        cmbMedicos.setEditable(true);
        EstiloRedondeado.aplicarComboBuscador(cmbMedicos);
        cmbMedicos.setUI(new SmartHeightComboBoxUI(8));
        estilizarFiltroTurnos(cmbMedicos);
        configurarAperturaUnClick(cmbMedicos);
        lblMedicos = agregarFiltro(pnlIzquierdo, "Filtrar por Médico:", cmbMedicos);
        cargarMedicosComboBox();
        configurarBusquedaMedicos();

        cmbMedicos.addActionListener(e -> {
            if (actualizandoFiltroMedicos) return;
            actualizarMedicoActivoDesdeCombo();
            if (idMedicoActivo > 0 && cmbMedicos.isEditable()) {
                restaurarListaCompletaMedicosConSeleccion(idMedicoActivo, nombreMedicoActivo);
            }
            refrescarCalendarioActual();
        });

        JButton btnLimpiar = new JButton("Limpiar Filtros");
        btnLimpiar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiar.addActionListener(e -> {
            limpiarFiltroMedico();
            refrescarCalendarioActual();
        });
        Usuario sesionActual = mainApp.getUsuarioSesion();
        btnLimpiar.setVisible(sesionActual != null && sesionActual.esSecretaria());
        if (btnLimpiar.isVisible()) {
            pnlIzquierdo.add(Box.createVerticalStrut(10));
            pnlIzquierdo.add(btnLimpiar);
        }

        add(pnlIzquierdo, BorderLayout.WEST);

        // --- PANEL CENTRAL ---
        JPanel pnlCentro = new JPanel(new BorderLayout(0, 10));
        pnlCentro.setOpaque(false);

        lblMesAnio = new JLabel("MES Y AÑO");
        lblMesAnio.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblMesAnio.setForeground(new Color(41, 128, 185));
        lblMesAnio.setHorizontalAlignment(SwingConstants.CENTER);
        pnlCentro.add(lblMesAnio, BorderLayout.NORTH);

        String[] columnas = {"Hora", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
        modeloCalendario = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaCalendario = new JTable(modeloCalendario);
        tablaCalendario.putClientProperty("estiloRedondeado.omitir", true);
        tablaCalendario.setRowHeight(55);
        tablaCalendario.setShowGrid(true);
        tablaCalendario.setGridColor(new Color(210, 210, 210));
        tablaCalendario.getTableHeader().setReorderingAllowed(false);
        tablaCalendario.getTableHeader().setFont(EstiloRedondeado.fuenteHeaderTabla());
        tablaCalendario.getTableHeader().setBackground(new Color(0, 121, 107));
        tablaCalendario.getTableHeader().setForeground(Color.WHITE);
        tablaCalendario.getTableHeader().setPreferredSize(new Dimension(0, 42));

        tablaCalendario.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCalendario.setCellSelectionEnabled(true);
        tablaCalendario.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer renderInteligente = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 0) {
                    label.setBackground(new Color(245, 245, 245));
                    label.setForeground(new Color(50, 50, 50));
                    label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    return label;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(miniCalendario.getDate());
                int diaActual = cal.get(Calendar.DAY_OF_WEEK);
                int difLunes = diaActual - Calendar.MONDAY;
                if (difLunes < 0) difLunes += 7;
                cal.add(Calendar.DAY_OF_MONTH, -difLunes);
                cal.add(Calendar.DAY_OF_MONTH, column - 1);
                boolean esFeriado = esFeriadoNacional(cal.getTime());

                if (esFeriado) {
                    label.setText(" Feriado");
                    label.setBackground(new Color(253, 237, 236));
                    label.setForeground(new Color(192, 57, 43));
                    label.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 11));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                }
                else if (nombresPacientes[row][column] != null) {
                    label.setText("<html><center><b>" + nombresPacientes[row][column] + "</b></center></html>");
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setFont(new Font("Segoe UI", Font.PLAIN, 11));

                    String est = estadosTurnos[row][column].toLowerCase();
                    switch(est) {
                        case "esperando": label.setBackground(new Color(255, 249, 196)); break;
                        case "en consulta": label.setBackground(new Color(227, 242, 253)); break;
                        case "atendido":
                        case "finalizado": label.setBackground(new Color(232, 245, 233)); break;
                        case "cancelado": label.setBackground(new Color(253, 237, 236)); break;
                        case "ausente": label.setBackground(new Color(236, 240, 241)); break;
                        default: label.setBackground(new Color(178, 223, 219)); break;
                    }
                    label.setForeground(Color.BLACK);
                }
                // ACÁ CAMBIÉ LA LÓGICA: Si no hay médico seleccionado (getSelectedIndex() < 0), pinta todo de gris.
                else if (obtenerIdMedicoSeleccionado() <= 0 || !horarioDisponible[row][column]) {
                    label.setText(" No atiende");
                    label.setBackground(new Color(230, 230, 230));
                    label.setForeground(new Color(160, 160, 160));
                    label.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                }
                else {
                    label.setText(" " + table.getValueAt(row, 0));
                    label.setForeground(new Color(210, 210, 210));
                    label.setBackground(Color.WHITE);
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (isSelected && !esFeriado) label.setBorder(EstiloRedondeado.borde(Color.BLUE, 1));
                else label.setBorder(null);

                return label;
            }
        };

        for (int i = 0; i < 6; i++) tablaCalendario.getColumnModel().getColumn(i).setCellRenderer(renderInteligente);

        tablaCalendario.addMouseListener(new MouseAdapter() {
            private void gestionarPopup(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int fila = tablaCalendario.rowAtPoint(e.getPoint());
                int columna = tablaCalendario.columnAtPoint(e.getPoint());
                if (fila >= 0 && columna > 0) gestionarClicEnCelda(fila, columna, e);
            }

            @Override public void mousePressed(MouseEvent e) { gestionarPopup(e); }
            @Override public void mouseReleased(MouseEvent e) { gestionarPopup(e); }

            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tablaCalendario.rowAtPoint(e.getPoint());
                int columna = tablaCalendario.columnAtPoint(e.getPoint());
                if (fila >= 0 && columna > 0 && SwingUtilities.isLeftMouseButton(e)
                        && e.getClickCount() == 2) {
                    gestionarClicEnCelda(fila, columna, e);
                }
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaCalendario);
        scrollTabla.setColumnHeaderView(tablaCalendario.getTableHeader());
        scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.setBorder(EstiloRedondeado.borde(new Color(200, 200, 200)));
        pnlCentro.add(scrollTabla, BorderLayout.CENTER);

        add(pnlCentro, BorderLayout.CENTER);

        generarGrillaHoraria();
        actualizarCabecerasDias();
    }

    private void actualizarCabecerasDias() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(miniCalendario.getDate());

        int diaActual = cal.get(Calendar.DAY_OF_WEEK);
        int diferenciaConLunes = diaActual - Calendar.MONDAY;
        if (diferenciaConLunes < 0) diferenciaConLunes += 7;
        cal.add(Calendar.DAY_OF_MONTH, -diferenciaConLunes);

        SimpleDateFormat sdfDia = new SimpleDateFormat("dd");
        String[] diasBase = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};

        for (int i = 1; i <= 5; i++) {
            String numDia = sdfDia.format(cal.getTime());
            tablaCalendario.getColumnModel().getColumn(i).setHeaderValue(diasBase[i-1] + " " + numDia);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        tablaCalendario.getTableHeader().repaint();

        SimpleDateFormat sdfMes = new SimpleDateFormat("MMMM 'de' yyyy", new Locale("es", "ES"));
        lblMesAnio.setText(sdfMes.format(miniCalendario.getDate()).toUpperCase());
    }

    private JLabel agregarFiltro(JPanel pnl, String titulo, JComboBox<String> cmb) {
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.DARK_GRAY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnl.add(lbl);
        cmb.setMaximumSize(new Dimension(250, 35));
        cmb.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnl.add(cmb);
        pnl.add(Box.createVerticalStrut(20));
        return lbl;
    }

    private void estilizarFiltroTurnos(JComboBox<String> combo) {
        Color verde = new Color(0, 150, 136);
        combo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        combo.setForeground(new Color(55, 65, 72));
        combo.setBackground(Color.WHITE);
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        combo.setBorder(BorderFactory.createCompoundBorder(
                EstiloRedondeado.borde(verde, 1),
                BorderFactory.createEmptyBorder(0, 8, 0, 3)));

        combo.setRenderer(new DefaultListCellRenderer() {
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

        if (combo.getEditor().getEditorComponent() instanceof JTextField editor) {
            editor.setFont(new Font("Segoe UI", Font.BOLD, 13));
            editor.setHorizontalAlignment(JTextField.CENTER);
            editor.setForeground(new Color(55, 65, 72));
            editor.setBackground(Color.WHITE);
            editor.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 6));
        }
    }

    private void generarGrillaHoraria() {
        modeloCalendario.setRowCount(0);
        for (String h : horasLista) modeloCalendario.addRow(new Object[]{h, "", "", "", "", ""});
    }

    private void cargarEspecialidadesComboBox() {
        if (cmbEspecialidades == null) return;
        actualizandoEspecialidades = true;
        idsEspecialidades.clear();
        todosLosIdsEspecialidades.clear();
        todasLasEspecialidades.clear();
        try {
            cmbEspecialidades.removeAllItems();

            String sql = "SELECT id_especialidad, nombre FROM especialidad ORDER BY nombre ASC";
            try (Connection con = ConexionBD.getConexion();
                 PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    if (esEspecialidadDeRol(nombre)) continue;
                    todosLosIdsEspecialidades.add(rs.getInt("id_especialidad"));
                    todasLasEspecialidades.add(nombre);
                }
            }
            if (todasLasEspecialidades.isEmpty()) {
                idsEspecialidades.add(0);
                cmbEspecialidades.addItem("Sin especialidades cargadas");
            } else {
                for (int i = 0; i < todasLasEspecialidades.size(); i++) {
                    idsEspecialidades.add(todosLosIdsEspecialidades.get(i));
                    cmbEspecialidades.addItem(todasLasEspecialidades.get(i));
                }
            }
            int filas = Math.max(1, Math.min(6, cmbEspecialidades.getItemCount()));
            cmbEspecialidades.setMaximumRowCount(filas);
            cmbEspecialidades.setEditable(true);
            cmbEspecialidades.setSelectedIndex(-1);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar las especialidades: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            actualizandoEspecialidades = false;
        }
    }

    private boolean esEspecialidadDeRol(String nombre) {
        if (nombre == null) return false;
        String normalizado = normalizarBusqueda(nombre);
        return normalizado.contains("secretar") || normalizado.contains("recep");
    }

    private int obtenerIdEspecialidadSeleccionada() {
        if (cmbEspecialidades == null || cmbEspecialidades.getSelectedIndex() < 0) return 0;
        int indice = cmbEspecialidades.getSelectedIndex();
        return indice >= 0 && indice < idsEspecialidades.size() ? idsEspecialidades.get(indice) : 0;
    }

    private void configurarBusquedaEspecialidades() {
        if (cmbEspecialidades == null || busquedaEspecialidadesConfigurada) return;
        busquedaEspecialidadesConfigurada = true;
        cmbEspecialidades.setEditable(true);
        JTextField editor = (JTextField) cmbEspecialidades.getEditor().getEditorComponent();
        editor.setToolTipText("Escriba la especialidad para buscar");
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { programarFiltradoEspecialidades(editor); }
            @Override public void removeUpdate(DocumentEvent e) { programarFiltradoEspecialidades(editor); }
            @Override public void changedUpdate(DocumentEvent e) { programarFiltradoEspecialidades(editor); }
        });
        editor.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    editor.selectAll();
                    mostrarListaCompletaEspecialidades(editor);
                });
            }
        });
        editor.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    mostrarListaCompletaEspecialidades(editor);
                }
            }
        });
        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    if (cmbEspecialidades.getSelectedIndex() < 0 && cmbEspecialidades.getItemCount() > 0) {
                        cmbEspecialidades.setSelectedIndex(0);
                    }
                    cmbEspecialidades.hidePopup();
                }
            }
        });
    }

    private void mostrarListaCompletaEspecialidades(JTextField editor) {
        SwingUtilities.invokeLater(() -> {
            if (!cmbEspecialidades.isShowing() || !cmbEspecialidades.isEnabled()) {
                return;
            }
            String textoActual = editor.getText();
            ajustandoTextoFiltroEspecialidades = true;
            try {
                actualizarOpcionesEspecialidades("");
                editor.setText(textoActual);
                editor.setCaretPosition(Math.min(editor.getText().length(), textoActual.length()));
            } finally {
                ajustandoTextoFiltroEspecialidades = false;
            }
            if (cmbEspecialidades.getItemCount() > 0) {
                try {
                    abriendoPopupPorBusqueda = true;
                    cmbEspecialidades.showPopup();
                } catch (IllegalComponentStateException ignored) {
                    // El combo todavía no está visible.
                } finally {
                    abriendoPopupPorBusqueda = false;
                }
            }
        });
    }

    private void programarFiltradoEspecialidades(JTextField editor) {
        if (actualizandoEspecialidades || ajustandoTextoFiltroEspecialidades || filtradoEspecialidadesPendiente) return;
        filtradoEspecialidadesPendiente = true;
        SwingUtilities.invokeLater(() -> {
            filtradoEspecialidadesPendiente = false;
            if (actualizandoEspecialidades || ajustandoTextoFiltroEspecialidades) return;

            String texto = editor.getText();
            int posicion = editor.getCaretPosition();
            actualizarOpcionesEspecialidades(texto);
            boolean seleccionExacta = seleccionarEspecialidadSiCoincideExacto(texto);

            ajustandoTextoFiltroEspecialidades = true;
            try {
                editor.setText(texto);
                editor.setCaretPosition(Math.min(posicion, texto.length()));
            } finally {
                ajustandoTextoFiltroEspecialidades = false;
            }

            if (editor.hasFocus() && cmbEspecialidades.getItemCount() > 0) {
                try {
                    abriendoPopupPorBusqueda = true;
                    cmbEspecialidades.showPopup();
                } catch (IllegalComponentStateException ignored) {
                    // El combo todavía no está visible.
                } finally {
                    abriendoPopupPorBusqueda = false;
                }
            }
            if (seleccionExacta) {
                int idEspecialidadSeleccionada = obtenerIdEspecialidadSeleccionada();
                Object itemEspecialidad = cmbEspecialidades.getSelectedItem();
                if (idEspecialidadSeleccionada > 0 && itemEspecialidad != null) {
                    restaurarListaCompletaEspecialidadesConSeleccion(idEspecialidadSeleccionada, itemEspecialidad.toString());
                }
                actualizarOpcionesMedicos("");
                limpiarTextoComboMedicos();
                refrescarCalendarioActual();
            }
        });
    }

    private void actualizarOpcionesEspecialidades(String filtro) {
        String buscado = normalizarBusqueda(filtro);
        actualizandoEspecialidades = true;
        try {
            cmbEspecialidades.removeAllItems();
            idsEspecialidades.clear();
            for (int i = 0; i < todasLasEspecialidades.size(); i++) {
                String nombre = todasLasEspecialidades.get(i);
                if (buscado.isEmpty() || coincideDesdeInicio(nombre, buscado)) {
                    cmbEspecialidades.addItem(nombre);
                    idsEspecialidades.add(todosLosIdsEspecialidades.get(i));
                }
            }
            if (cmbEspecialidades.getItemCount() == 0) {
                cmbEspecialidades.addItem("Sin especialidades para mostrar");
                idsEspecialidades.add(0);
            }
            int filas = Math.max(1, Math.min(6, cmbEspecialidades.getItemCount()));
            cmbEspecialidades.setMaximumRowCount(filas);
            cmbEspecialidades.setSelectedIndex(-1);
        } finally {
            actualizandoEspecialidades = false;
        }
    }

    private boolean seleccionarEspecialidadSiCoincideExacto(String texto) {
        String buscado = normalizarBusqueda(texto);
        if (buscado.isBlank()) {
            return false;
        }
        for (int item = 0; item < cmbEspecialidades.getItemCount(); item++) {
            Object valor = cmbEspecialidades.getItemAt(item);
            if (valor != null && normalizarBusqueda(valor.toString()).equals(buscado)) {
                actualizandoEspecialidades = true;
                try {
                    cmbEspecialidades.setSelectedIndex(item);
                } finally {
                    actualizandoEspecialidades = false;
                }
                return true;
            }
        }
        return false;
    }

    private void restaurarListaCompletaEspecialidadesConSeleccion(int idEspecialidad, String nombreEspecialidad) {
        if (idEspecialidad <= 0 || nombreEspecialidad == null || nombreEspecialidad.isBlank()) {
            return;
        }

        actualizandoEspecialidades = true;
        ajustandoTextoFiltroEspecialidades = true;
        try {
            cmbEspecialidades.removeAllItems();
            idsEspecialidades.clear();
            for (int i = 0; i < todasLasEspecialidades.size(); i++) {
                cmbEspecialidades.addItem(todasLasEspecialidades.get(i));
                idsEspecialidades.add(todosLosIdsEspecialidades.get(i));
            }

            int indice = idsEspecialidades.indexOf(idEspecialidad);
            if (indice >= 0) {
                cmbEspecialidades.setSelectedIndex(indice);
            }

            Object editorComponent = cmbEspecialidades.getEditor().getEditorComponent();
            if (editorComponent instanceof JTextField editor) {
                editor.setText(nombreEspecialidad);
                editor.setCaretPosition(editor.getText().length());
            } else {
                cmbEspecialidades.getEditor().setItem(nombreEspecialidad);
            }
        } finally {
            ajustandoTextoFiltroEspecialidades = false;
            actualizandoEspecialidades = false;
        }

        if (cmbEspecialidades.isPopupVisible()) {
            cmbEspecialidades.hidePopup();
        }
    }

    private void cargarMedicosComboBox() {
        todosLosMedicos.clear();
        todosLosIdsMedicos.clear();
        especialidadPorMedico.clear();

        Usuario sesion = mainApp.getUsuarioSesion();
        boolean esMedico = sesion != null && sesion.esMedico();
        boolean puedeBuscar = sesion == null || !esMedico;
        int idMedicoSesion = esMedico ? resolverIdPersonalSesion(sesion) : 0;
        String sql = "SELECT p.id_personal, p.nombreyapellido, COALESCE(p.id_especialidad, 0) AS id_especialidad FROM personal p JOIN usuario u ON p.id_personal=u.id_personal JOIN rol r ON r.id_rol=u.id_rol "
                + "WHERE LOWER(r.nombre) IN ('médico','medico') AND p.estado::text='activo' AND u.estado::text='activo' "
                + (esMedico ? "AND p.id_personal = ? " : "")
                + "ORDER BY p.nombreyapellido";
        sql = "SELECT DISTINCT p.id_personal, p.nombreyapellido, COALESCE(p.id_especialidad, 0) AS id_especialidad FROM personal p "
                + "LEFT JOIN usuario u ON p.id_personal=u.id_personal AND u.estado::text='activo' "
                + "LEFT JOIN rol r ON r.id_rol=u.id_rol "
                + "WHERE p.estado::text='activo' "
                + "AND (translate(lower(coalesce(r.nombre,'')), '\u00e1\u00e9\u00ed\u00f3\u00fa', 'aeiou') LIKE '%medico%' "
                + "OR NULLIF(TRIM(COALESCE(p.matricula,'')), '') IS NOT NULL) "
                + (esMedico ? "AND p.id_personal = ? " : "")
                + "ORDER BY p.nombreyapellido";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            if (esMedico) pst.setInt(1, idMedicoSesion);
            try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                int idPersonal = rs.getInt("id_personal");
                todosLosIdsMedicos.add(idPersonal);
                todosLosMedicos.add(rs.getString("nombreyapellido"));
                especialidadPorMedico.put(idPersonal, rs.getInt("id_especialidad"));
            }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los médicos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (esMedico && todosLosMedicos.isEmpty() && idMedicoSesion > 0) {
            String nombreSesion = mainApp.getNombreUsuarioActual();
            if (nombreSesion != null && !nombreSesion.isBlank()) {
                todosLosIdsMedicos.add(idMedicoSesion);
                todosLosMedicos.add(nombreSesion);
                especialidadPorMedico.put(idMedicoSesion, 0);
            }
        }

        cmbMedicos.setEditable(puedeBuscar);
        cmbMedicos.setEnabled(false);
        boolean mostrarFiltros = !esMedico;
        if (lblEspecialidades != null) lblEspecialidades.setVisible(mostrarFiltros);
        if (cmbEspecialidades != null) cmbEspecialidades.setVisible(mostrarFiltros);
        if (lblMedicos != null) lblMedicos.setVisible(mostrarFiltros);
        cmbMedicos.setVisible(mostrarFiltros);
        actualizarOpcionesMedicos("");
        cmbMedicos.setSelectedIndex(esMedico && !idsMedicos.isEmpty() ? 0 : -1);
        if (esMedico && !idsMedicos.isEmpty()) {
            idMedicoActivo = idsMedicos.get(0);
            nombreMedicoActivo = (String) cmbMedicos.getItemAt(0);
        } else if (!esMedico) {
            idMedicoActivo = 0;
            nombreMedicoActivo = "";
        }
        actualizarDisponibilidadFiltroMedicos();
    }

    private int resolverIdPersonalSesion(Usuario sesion) {
        if (sesion == null) return 0;
        if (sesion.getId_personal() > 0) return sesion.getId_personal();
        String sql = "SELECT id_personal FROM usuario WHERE id_usuario = ?";
        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, sesion.getId_usuario());
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt("id_personal") : 0;
            }
        } catch (Exception e) {
            System.err.println("No se pudo identificar al médico autenticado: " + e.getMessage());
            return 0;
        }
    }

    private void configurarBusquedaMedicos() {
        if (!cmbMedicos.isEditable() || busquedaMedicosConfigurada) return;
        busquedaMedicosConfigurada = true;
        JTextField editor = (JTextField) cmbMedicos.getEditor().getEditorComponent();
        editor.setToolTipText("Escriba el nombre del médico para buscar");
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { programarFiltradoMedicos(editor); }
            @Override public void removeUpdate(DocumentEvent e) { programarFiltradoMedicos(editor); }
            @Override public void changedUpdate(DocumentEvent e) { programarFiltradoMedicos(editor); }
        });
        editor.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    editor.selectAll();
                    actualizarOpcionesMedicos("");
                    if (cmbMedicos.getItemCount() > 0 && cmbMedicos.isShowing()) {
                        cmbMedicos.showPopup();
                    }
                });
            }
        });
        editor.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    mostrarListaCompletaMedicos(editor);
                }
            }

        });
        editor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    if (cmbMedicos.getSelectedIndex() < 0 && cmbMedicos.getItemCount() > 0) {
                        cmbMedicos.setSelectedIndex(0);
                    }
                    cmbMedicos.hidePopup();
                }
            }
        });
    }

    private void configurarAperturaUnClick(JComboBox<String> combo) {
        combo.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || !combo.isEnabled()) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    if (combo.isShowing() && combo.getItemCount() > 0) {
                        combo.showPopup();
                    }
                });
            }
        });
    }

    private void mostrarListaCompletaMedicos(JTextField editor) {
        SwingUtilities.invokeLater(() -> {
            if (!cmbMedicos.isShowing() || !cmbMedicos.isEnabled()) {
                return;
            }

            String textoActual = editor.getText();
            ajustandoTextoFiltroMedicos = true;
            try {
                actualizarOpcionesMedicos("");
                editor.setText(textoActual);
                editor.setCaretPosition(Math.min(editor.getText().length(), textoActual.length()));
            } finally {
                ajustandoTextoFiltroMedicos = false;
            }

            if (cmbMedicos.getItemCount() > 0) {
                try {
                    abriendoPopupPorBusqueda = true;
                    cmbMedicos.showPopup();
                } catch (IllegalComponentStateException ignored) {
                    // El combo todavía no está visible; no hace falta mostrar el popup en ese caso.
                } finally {
                    abriendoPopupPorBusqueda = false;
                }
            }
        });
    }

    private void programarFiltradoMedicos(JTextField editor) {
        if (actualizandoFiltroMedicos || ajustandoTextoFiltroMedicos || filtradoMedicosPendiente) return;
        filtradoMedicosPendiente = true;
        SwingUtilities.invokeLater(() -> {
            filtradoMedicosPendiente = false;
            if (actualizandoFiltroMedicos || ajustandoTextoFiltroMedicos) return;

            String texto = editor.getText();
            int posicion = editor.getCaretPosition();
            idMedicoActivo = 0;
            nombreMedicoActivo = "";
            actualizarOpcionesMedicos(texto);
            boolean seleccionExacta = seleccionarMedicoSiCoincideExacto(texto);

            ajustandoTextoFiltroMedicos = true;
            try {
                editor.setText(texto);
                editor.setCaretPosition(Math.min(posicion, texto.length()));
            } finally {
                ajustandoTextoFiltroMedicos = false;
            }

            if (editor.hasFocus() && cmbMedicos.getItemCount() > 0) {
                try {
                    abriendoPopupPorBusqueda = true;
                    cmbMedicos.showPopup();
                } catch (IllegalComponentStateException ignored) {
                    // El combo todavía no está visible; no hace falta mostrar el popup en ese caso.
                } finally {
                    abriendoPopupPorBusqueda = false;
                }
            } else if (cmbMedicos.isShowing()) {
                cmbMedicos.hidePopup();
            }
            if (seleccionExacta) {
                restaurarListaCompletaMedicosConSeleccion(idMedicoActivo, nombreMedicoActivo);
                refrescarCalendarioActual();
            }
        });
    }

    private boolean seleccionarMedicoSiCoincideExacto(String texto) {
        String buscado = normalizarBusqueda(texto);
        if (buscado.isBlank()) {
            return false;
        }

        int idEspecialidad = obtenerIdEspecialidadSeleccionada();
        for (int i = 0; i < todosLosMedicos.size(); i++) {
            int idMedico = todosLosIdsMedicos.get(i);
            if (idEspecialidad > 0 && especialidadPorMedico.getOrDefault(idMedico, 0) != idEspecialidad) {
                continue;
            }

            String nombre = todosLosMedicos.get(i);
            if (normalizarBusqueda(nombre).equals(buscado)) {
                idMedicoActivo = idMedico;
                nombreMedicoActivo = nombre;
                actualizandoFiltroMedicos = true;
                try {
                    for (int item = 0; item < cmbMedicos.getItemCount(); item++) {
                        Object valor = cmbMedicos.getItemAt(item);
                        if (valor != null && normalizarBusqueda(valor.toString()).equals(buscado)) {
                            cmbMedicos.setSelectedIndex(item);
                            break;
                        }
                    }
                } finally {
                    actualizandoFiltroMedicos = false;
                }
                return true;
            }
        }
        return false;
    }

    private void restaurarListaCompletaMedicosConSeleccion(int idMedico, String nombreMedico) {
        if (idMedico <= 0 || nombreMedico == null || nombreMedico.isBlank()) {
            return;
        }

        actualizarOpcionesMedicos("");
        int indice = idsMedicos.indexOf(idMedico);

        actualizandoFiltroMedicos = true;
        ajustandoTextoFiltroMedicos = true;
        try {
            if (indice >= 0) {
                cmbMedicos.setSelectedIndex(indice);
            }
            Object editorComponent = cmbMedicos.getEditor().getEditorComponent();
            if (editorComponent instanceof JTextField editor) {
                editor.setText(nombreMedico);
                editor.setCaretPosition(editor.getText().length());
            } else {
                cmbMedicos.getEditor().setItem(nombreMedico);
            }
        } finally {
            ajustandoTextoFiltroMedicos = false;
            actualizandoFiltroMedicos = false;
        }

        if (cmbMedicos.isPopupVisible()) {
            cmbMedicos.hidePopup();
        }
    }

    private void actualizarOpcionesMedicos(String filtro) {
        String buscado = normalizarBusqueda(filtro);
        int idEspecialidad = obtenerIdEspecialidadSeleccionada();
        Usuario sesion = mainApp.getUsuarioSesion();
        boolean requiereEspecialidad = sesion == null || !sesion.esMedico();
        actualizandoFiltroMedicos = true;
        try {
            cmbMedicos.removeAllItems();
            idsMedicos.clear();
            for (int i = 0; i < todosLosMedicos.size() && (!requiereEspecialidad || idEspecialidad > 0); i++) {
                int idMedico = todosLosIdsMedicos.get(i);
                if (idEspecialidad > 0 && especialidadPorMedico.getOrDefault(idMedico, 0) != idEspecialidad) {
                    continue;
                }
                String nombre = todosLosMedicos.get(i);
                if (buscado.isEmpty() || coincideDesdeInicio(nombre, buscado)) {
                    cmbMedicos.addItem(nombre);
                    idsMedicos.add(idMedico);
                }
            }
            if (cmbMedicos.getItemCount() == 0) {
                cmbMedicos.addItem(requiereEspecialidad && idEspecialidad <= 0
                        ? "Seleccione una especialidad" : "Sin m\u00e9dicos para mostrar");
                idsMedicos.add(0);
            }
            cmbMedicos.setSelectedIndex(-1);
        } finally {
            actualizandoFiltroMedicos = false;
        }
    }

    private void actualizarDisponibilidadFiltroMedicos() {
        if (cmbMedicos == null) return;
        Usuario sesion = mainApp.getUsuarioSesion();
        boolean esMedico = sesion != null && sesion.esMedico();
        cmbMedicos.setEnabled(!esMedico && obtenerIdEspecialidadSeleccionada() > 0);
        cmbMedicos.setToolTipText(cmbMedicos.isEnabled() || esMedico
                ? null : "Primero seleccione una especialidad");
    }

    private void actualizarMedicoActivoDesdeCombo() {
        int seleccionado = cmbMedicos.getSelectedIndex();
        if (seleccionado >= 0 && seleccionado < idsMedicos.size()) {
            idMedicoActivo = idsMedicos.get(seleccionado);
            Object item = cmbMedicos.getSelectedItem();
            nombreMedicoActivo = item == null ? "" : item.toString();
        } else {
            idMedicoActivo = 0;
            nombreMedicoActivo = "";
        }
    }

    private void limpiarTextoComboMedicos() {
        if (cmbMedicos == null || !cmbMedicos.isEditable()) return;
        ajustandoTextoFiltroMedicos = true;
        try {
            cmbMedicos.getEditor().setItem("");
            cmbMedicos.setSelectedIndex(-1);
        } finally {
            ajustandoTextoFiltroMedicos = false;
        }
    }

    private String obtenerNombreMedicoActivo() {
        if (nombreMedicoActivo != null && !nombreMedicoActivo.isBlank()) return nombreMedicoActivo;
        int id = obtenerIdMedicoSeleccionado();
        int indiceGlobal = todosLosIdsMedicos.indexOf(id);
        if (indiceGlobal >= 0) return todosLosMedicos.get(indiceGlobal);
        Object item = cmbMedicos.getSelectedItem();
        if (item != null) return item.toString();
        Object editorItem = cmbMedicos.isEditable() ? cmbMedicos.getEditor().getItem() : null;
        return editorItem == null ? "" : editorItem.toString();
    }

    private String normalizarBusqueda(String texto) {
        String valor = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return java.text.Normalizer.normalize(valor, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }

    private boolean coincideDesdeInicio(String texto, String buscadoNormalizado) {
        if (buscadoNormalizado == null || buscadoNormalizado.isEmpty()) return true;
        String textoNormalizado = normalizarBusqueda(texto).trim();
        if (textoNormalizado.startsWith(buscadoNormalizado)) return true;
        for (String palabra : textoNormalizado.split("\\s+")) {
            if (palabra.startsWith(buscadoNormalizado)) return true;
        }
        return false;
    }

    private void pintarHorariosMedico() {
        for (int r = 0; r < 25; r++) for (int c = 1; c <= 5; c++) horarioDisponible[r][c] = false;

        int idMedico = obtenerIdMedicoSeleccionado();
        if (idMedico > 0) {
            String sql = "SELECT diasemana, horainicio, horafin FROM horario WHERE id_personal = ? AND estado = 'activo'";

            try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, idMedico);
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    String dia = rs.getString("diasemana").toLowerCase();
                    if (dia.equals("sabado") || dia.equals("sábado") || dia.equals("domingo")) continue;

                    String hInicio = rs.getString("horainicio").substring(0, 5);
                    String hFin = rs.getString("horafin").substring(0, 5);

                    int col = obtenerColumnaPorDia(dia);
                    int filaIn = obtenerFilaPorHora(hInicio);
                    int filaFin = obtenerFilaPorHora(hFin);

                    if (col > 0 && filaIn >= 0 && filaFin >= 0) {
                        int ultimaFila = Math.min(filaFin, horarioDisponible.length - 1);
                        for (int r = filaIn; r <= ultimaFila; r++) horarioDisponible[r][col] = true;
                    }
                }
            } catch (Exception e) {
                System.err.println("No se pudo cargar el horario del médico: " + e.getMessage());
            }
        }
    }

    private void cargarTurnosReales() {
        new clinicabalbarrey.dao.TurnoDAO().marcarAusentesVencidos();
        for (int r = 0; r < 25; r++) {
            for (int c = 1; c <= 5; c++) {
                nombresPacientes[r][c] = null;
                estadosTurnos[r][c] = null;
                idsTurnos[r][c] = 0;
            }
        }

        int idMed = obtenerIdMedicoSeleccionado();
        if (idMed <= 0) return;

        Calendar cal = Calendar.getInstance();
        cal.setTime(miniCalendario.getDate());
        int diaActual = cal.get(Calendar.DAY_OF_WEEK);
        int diferenciaConLunes = diaActual - Calendar.MONDAY;
        if (diferenciaConLunes < 0) diferenciaConLunes += 7;
        cal.add(Calendar.DAY_OF_MONTH, -diferenciaConLunes);

        java.sql.Date fechaIn = new java.sql.Date(cal.getTimeInMillis());
        cal.add(Calendar.DAY_OF_MONTH, 4);
        java.sql.Date fechaFin = new java.sql.Date(cal.getTimeInMillis());

        String sql = "SELECT t.id_turno, t.fechahora, p.apellido, p.nombre, t.estado " +
                     "FROM turno t JOIN paciente p ON t.id_paciente = p.id_paciente " +
                     "WHERE t.id_personal = ? AND t.fechahora::DATE BETWEEN ? AND ? " +
                     "AND LOWER(t.estado) != 'cancelado' AND LOWER(p.estado::text) = 'activo'";

        try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idMed);
            pst.setDate(2, fechaIn);
            pst.setDate(3, fechaFin);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("fechahora");
                int idT = rs.getInt("id_turno");

                Calendar cTurno = Calendar.getInstance();
                cTurno.setTime(ts);

                int diaSemana = cTurno.get(Calendar.DAY_OF_WEEK);
                int col = (diaSemana == Calendar.SUNDAY) ? -1 : diaSemana - 1;

                String horaTurno = new SimpleDateFormat("HH:mm").format(ts);
                int fila = obtenerFilaPorHora(horaTurno);

                if (fila >= 0 && col >= 1 && col <= 5) {
                    nombresPacientes[fila][col] = rs.getString("apellido") + ", " + rs.getString("nombre");
                    estadosTurnos[fila][col] = rs.getString("estado");
                    idsTurnos[fila][col] = idT;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los turnos del calendario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int obtenerColumnaPorDia(String dia) {
        dia = dia.toLowerCase();
        if (dia.equals("lunes")) return 1;
        if (dia.equals("martes")) return 2;
        if (dia.equals("miercoles") || dia.equals("miércoles")) return 3;
        if (dia.equals("jueves")) return 4;
        if (dia.equals("viernes")) return 5;
        return -1;
    }

    private int obtenerFilaPorHora(String hora) {
        for (int i = 0; i < horasLista.length; i++) if (horasLista[i].equals(hora)) return i;
        return -1;
    }

    private void gestionarClicEnCelda(int fila, int columna, MouseEvent e) {
        int idMedicoSelec = obtenerIdMedicoSeleccionado();
        if (idMedicoSelec <= 0) {
            if (e.getClickCount() == 2) {
                JOptionPane.showMessageDialog(this, "Seleccione un Médico específico en el filtro izquierdo antes de asignar un turno.", "Atención", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        // =======================================================
        // 1. SI EL BLOQUE ESTÁ OCUPADO -> MOSTRAMOS MENÚ SOLO DE CANCELAR
        // =======================================================
        if (nombresPacientes[fila][columna] != null) {
            if (!e.isPopupTrigger() && !SwingUtilities.isRightMouseButton(e)) return;
            int idTurnoSeleccionado = idsTurnos[fila][columna];
            String pacienteSelec = nombresPacientes[fila][columna];
            String estadoTurno = estadosTurnos[fila][columna];

            JPopupMenu popup = new JPopupMenu();

            JMenuItem lblPaciente = new JMenuItem("Turno: " + pacienteSelec);
            lblPaciente.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblPaciente.setEnabled(false);
            popup.add(lblPaciente);
            popup.addSeparator();

            JMenuItem itemCancelar = new JMenuItem("Cancelar / Liberar Horario");
            itemCancelar.setForeground(new Color(211, 47, 47));
            itemCancelar.setFont(new Font("Segoe UI", Font.BOLD, 13));
            Usuario sesion = mainApp.getUsuarioSesion();
            boolean puedeCancelar = PoliticaTurnos.puedeCancelar(sesion, estadoTurno);
            boolean turnoTerminal = PoliticaTurnos.esDefinitivo(estadoTurno);
            boolean horarioPasado = !obtenerFechaHoraCelda(fila, columna).after(new Date());
            itemCancelar.setEnabled(puedeCancelar && !turnoTerminal && !horarioPasado);
            if (turnoTerminal) itemCancelar.setText("Turno " + estadoTurno + " (definitivo)");
            else if (horarioPasado) itemCancelar.setText("Horario pasado (no puede liberarse)");
            else if (!puedeCancelar) itemCancelar.setText("Sin permisos para cancelar");
            itemCancelar.addActionListener(ev -> {
                String motivoRol = PoliticaTurnos.validarCambio(mainApp.getUsuarioSesion(), estadoTurno, "cancelado");
                if (motivoRol != null) {
                    JOptionPane.showMessageDialog(this, motivoRol, "Cambio no permitido", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                clinicabalbarrey.dao.TurnoDAO dao = new clinicabalbarrey.dao.TurnoDAO();
                String motivo = dao.validarCambioEstado(idTurnoSeleccionado, "cancelado");
                if (motivo != null) {
                    JOptionPane.showMessageDialog(this, motivo, "Cambio no permitido", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int conf = JOptionPane.showConfirmDialog(this, "¿Seguro que querés cancelar este turno?\nEl horario quedará libre para asignar a otro paciente.", "Liberar Turno", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (conf == JOptionPane.YES_OPTION) {
                    if (dao.cambiarEstadoTurno(idTurnoSeleccionado, "cancelado")) {
                        refrescarCalendarioActual();
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo cancelar el turno. Volvé a intentarlo.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            popup.add(itemCancelar);

            popup.show(tablaCalendario, e.getX(), e.getY());
            return;
        }

        if ((e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e))
                && horarioDisponible[fila][columna]) {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem crearPaciente = new JMenuItem("Crear nuevo paciente");
            crearPaciente.setFont(new Font("Segoe UI", Font.BOLD, 13));
            crearPaciente.addActionListener(ev -> mainApp.irARegistrarPacienteDesdeCalendario());
            popup.add(crearPaciente);
            popup.show(tablaCalendario, e.getX(), e.getY());
            return;
        }

        // =======================================================
        // 2. SI EL BLOQUE ESTÁ LIBRE Y ES DOBLE CLIC -> ASIGNAR
        // =======================================================
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
            if (!horarioDisponible[fila][columna]) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }

            String horaSeleccionada = (String) tablaCalendario.getValueAt(fila, 0);
            Date fechaExactaObj = obtenerFechaHoraCelda(fila, columna);
            Date ahora = new Date();

            if (fechaExactaObj.before(ahora)) {
                JOptionPane.showMessageDialog(this, "No podés asignar un turno en una fecha o en un horario que ya pasó.", "Acción Inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (esFeriadoNacional(fechaExactaObj)) {
                JOptionPane.showMessageDialog(this, "La fecha seleccionada corresponde a un Feriado Nacional de Argentina. La clínica permanecerá cerrada.", "Día No Laborable", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String nombreMedico = obtenerNombreMedicoActivo();

            mainApp.irAProgramarTurnoDesdeCalendario(fechaExactaObj, horaSeleccionada, idMedicoSelec, nombreMedico);
        }
    }

    private Date obtenerFechaHoraCelda(int fila, int columna) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(miniCalendario.getDate());
        int diferenciaConLunes = cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
        if (diferenciaConLunes < 0) diferenciaConLunes += 7;
        cal.add(Calendar.DAY_OF_MONTH, -diferenciaConLunes + columna - 1);

        String[] partesHora = String.valueOf(tablaCalendario.getValueAt(fila, 0)).split(":");
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(partesHora[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(partesHora[1]));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public void cargarCalendario() {
        int idEspecialidadSeleccionada = obtenerIdEspecialidadSeleccionada();
        String nombreEspecialidadSeleccionada = cmbEspecialidades != null
                && cmbEspecialidades.getSelectedItem() != null
                ? cmbEspecialidades.getSelectedItem().toString() : "";
        int idMedicoSeleccionado = obtenerIdMedicoSeleccionado();
        String textoFiltroMedico = obtenerTextoFiltroMedicos();

        cargarEspecialidadesComboBox();
        cargarMedicosComboBox();
        configurarBusquedaMedicos();
        actualizarDisponibilidadFiltroMedicos();
        Usuario sesion = mainApp.getUsuarioSesion();
        if (sesion == null || !sesion.esMedico()) {
            if (idEspecialidadSeleccionada > 0) {
                restaurarListaCompletaEspecialidadesConSeleccion(
                        idEspecialidadSeleccionada, nombreEspecialidadSeleccionada);
                actualizarDisponibilidadFiltroMedicos();
                restaurarFiltroMedico(idMedicoSeleccionado, textoFiltroMedico);
            } else {
                actualizarOpcionesMedicos("");
                limpiarTextoComboMedicos();
                actualizarDisponibilidadFiltroMedicos();
            }
        } else if (!idsMedicos.isEmpty()) {
            cmbMedicos.setSelectedIndex(0);
            actualizarMedicoActivoDesdeCombo();
        }
        refrescarCalendarioActual();
    }

    private void refrescarCalendarioActual() {
        actualizarCabecerasDias();
        pintarHorariosMedico();
        cargarTurnosReales();
        tablaCalendario.repaint();
    }

    private int obtenerIdMedicoSeleccionado() {
        int seleccionado = cmbMedicos.getSelectedIndex();
        if (seleccionado >= 0 && seleccionado < idsMedicos.size()) {
            return idsMedicos.get(seleccionado);
        }
        return idMedicoActivo;
    }

    private String obtenerTextoFiltroMedicos() {
        if (cmbMedicos.isEditable()) {
            Object item = cmbMedicos.getEditor().getItem();
            return item == null ? "" : item.toString();
        }
        Object item = cmbMedicos.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private void limpiarFiltroMedico() {
        if (cmbMedicos == null) return;
        idMedicoActivo = 0;
        nombreMedicoActivo = "";
        if (cmbEspecialidades != null && cmbEspecialidades.isVisible()) {
            actualizandoEspecialidades = true;
            ajustandoTextoFiltroEspecialidades = true;
            try {
                actualizarOpcionesEspecialidades("");
                cmbEspecialidades.setSelectedIndex(-1);
                cmbEspecialidades.getEditor().setItem("");
            } finally {
                ajustandoTextoFiltroEspecialidades = false;
                actualizandoEspecialidades = false;
            }
        }
        actualizarOpcionesMedicos("");
        cmbMedicos.setSelectedIndex(-1);
        if (cmbMedicos.isEditable()) {
            ajustandoTextoFiltroMedicos = true;
            try {
                cmbMedicos.getEditor().setItem("");
            } finally {
                ajustandoTextoFiltroMedicos = false;
            }
        }
        actualizarDisponibilidadFiltroMedicos();
    }

    private void restaurarFiltroMedico(int idMedicoSeleccionado, String textoFiltro) {
        Usuario sesion = mainApp.getUsuarioSesion();
        boolean esMedico = sesion != null && sesion.esMedico();
        if (esMedico) {
            if (!idsMedicos.isEmpty()) cmbMedicos.setSelectedIndex(0);
            return;
        }

        String textoARestaurar = textoFiltro == null ? "" : textoFiltro.trim();
        if (idMedicoSeleccionado <= 0 && !textoARestaurar.isEmpty()) {
            String textoNormalizado = normalizarBusqueda(textoARestaurar);
            for (int i = 0; i < todosLosMedicos.size(); i++) {
                if (normalizarBusqueda(todosLosMedicos.get(i)).equals(textoNormalizado)) {
                    idMedicoSeleccionado = todosLosIdsMedicos.get(i);
                    break;
                }
            }
        }
        if (idMedicoSeleccionado > 0) {
            int indiceGlobal = todosLosIdsMedicos.indexOf(idMedicoSeleccionado);
            if (indiceGlobal >= 0) textoARestaurar = todosLosMedicos.get(indiceGlobal);
        }

        actualizarOpcionesMedicos("");

        int indiceRestaurado = -1;
        if (idMedicoSeleccionado > 0) {
            for (int i = 0; i < idsMedicos.size(); i++) {
                if (idsMedicos.get(i) == idMedicoSeleccionado) {
                    indiceRestaurado = i;
                    break;
                }
            }
        }
        if (indiceRestaurado >= 0) cmbMedicos.setSelectedIndex(indiceRestaurado);
        if (idMedicoSeleccionado > 0) {
            idMedicoActivo = idMedicoSeleccionado;
            nombreMedicoActivo = textoARestaurar;
        }

        if (cmbMedicos.isEditable()) {
            ajustandoTextoFiltroMedicos = true;
            try {
                cmbMedicos.getEditor().setItem(textoARestaurar);
            } finally {
                ajustandoTextoFiltroMedicos = false;
            }
        }
    }

    // =========================================================================
    // MOTOR DE CÁLCULO DE FERIADOS NACIONALES ARGENTINOS (LEY 27.399)
    // =========================================================================
    private boolean esFeriadoNacional(java.util.Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        int anio = cal.get(Calendar.YEAR);
        int mes = cal.get(Calendar.MONTH) + 1;
        int dia = cal.get(Calendar.DAY_OF_MONTH);

        String diaMes = String.format("%02d/%02d", dia, mes);

        java.util.List<String> inamovibles = java.util.Arrays.asList(
            "01/01", "24/03", "02/04", "01/05", "25/05",
            "20/06", "09/07", "08/12", "25/12"
        );
        if (inamovibles.contains(diaMes)) return true;

        int a = anio % 19;
        int b = anio / 100;
        int c = anio % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mesPascua = (h + l - 7 * m + 114) / 31;
        int diaPascua = ((h + l - 7 * m + 114) % 31) + 1;

        Calendar pascua = Calendar.getInstance();
        pascua.set(anio, mesPascua - 1, diaPascua);

        Calendar viernesSanto = (Calendar) pascua.clone();
        viernesSanto.add(Calendar.DAY_OF_MONTH, -2);

        Calendar carnavalMartes = (Calendar) pascua.clone();
        carnavalMartes.add(Calendar.DAY_OF_MONTH, -47);

        Calendar carnavalLunes = (Calendar) pascua.clone();
        carnavalLunes.add(Calendar.DAY_OF_MONTH, -48);

        if (esMismaFecha(cal, viernesSanto) || esMismaFecha(cal, carnavalMartes) || esMismaFecha(cal, carnavalLunes)) {
            return true;
        }

        if (esFeriadoTrasladableObservado(cal, anio, 6, 17)) return true;
        if (esFeriadoTrasladableObservado(cal, anio, 8, 17)) return true;
        if (esFeriadoTrasladableObservado(cal, anio, 10, 12)) return true;
        if (esFeriadoTrasladableObservado(cal, anio, 11, 20)) return true;

        return false;
    }

    private boolean esMismaFecha(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
               c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
               c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }

    private boolean esFeriadoTrasladableObservado(Calendar fechaConsultada, int anio, int mes, int diaOriginal) {
        Calendar original = Calendar.getInstance();
        original.set(anio, mes - 1, diaOriginal);
        int diaSemana = original.get(Calendar.DAY_OF_WEEK);

        if (diaSemana == Calendar.TUESDAY) original.add(Calendar.DAY_OF_MONTH, -1);
        else if (diaSemana == Calendar.WEDNESDAY) original.add(Calendar.DAY_OF_MONTH, -2);
        else if (diaSemana == Calendar.THURSDAY) original.add(Calendar.DAY_OF_MONTH, 4);
        else if (diaSemana == Calendar.FRIDAY) original.add(Calendar.DAY_OF_MONTH, 3);

        return esMismaFecha(fechaConsultada, original);
    }

    private static class SmartHeightComboBoxUI extends BasicComboBoxUI {
        private final int maxRows;

        SmartHeightComboBoxUI(int maxRows) {
            this.maxRows = maxRows;
        }

        @Override
        protected JButton createArrowButton() {
            JButton boton = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
                    int rowHeight = Math.max(28, comboBox.getFontMetrics(comboBox.getFont()).getHeight() + 10);
                    int rows = Math.max(1, Math.min(maxRows, comboBox.getItemCount()));
                    int alto = (rowHeight * rows) + 4;
                    int ancho = Math.max(1, comboBox.getWidth());
                    return new Dimension(ancho, alto);
                }
            };
        }
    }
}
