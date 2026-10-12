package clinicabalbarrey;

import clinicabalbarrey.dao.ConsultaDAO;
import clinicabalbarrey.dao.CatalogoDAO;
import clinicabalbarrey.model.Usuario;
import clinicabalbarrey.storage.ArchivoAdjuntoService;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class VerHistoriaMedicaPanel extends JPanel {
    private static final int MAX_PESTANAS_POR_SECCION = 5;

    private MainApplicationView mainApp;
    private CardLayout cardLayout;

    private String dniActual;
    private String nombreActual;
    private List<String[]> historialCompleto = new ArrayList<>();
    private String[] datosConsultaActual;
    private int historialCargaToken;
    private int idEspecialidadMedicoActual;

    private JLabel lblNombreDni;
    private JTable tabla;
    private DefaultTableModel modelo;
    private int paginaHistorialActual = 1;
    private final int historiasPorPagina = 15;
    private int totalPaginasHistorial = 1;
    private JLabel lblPaginacionHistorial;
    private JButton btnHistorialAnterior;
    private JButton btnHistorialSiguiente;
    private JLabel lblHistoriaCompletaPaciente;
    private JPanel pnlHistoriaCompletaRegistros;
    private final List<JCheckBox> seleccionHistoriaCompleta = new ArrayList<>();

    private JLabel lblSubtituloDetalle;
    private JPanel pnlContenedorDetalle;
    private JTextArea txtDetalleEvolucion;

    private JButton btnDiagDetalle, btnTratDetalle, btnArchivoDetalle;

    private JPanel pnlDiagDetalle;
    private JTabbedPane tabDiagnosticosDetalle;
    private JPanel pnlTratDetalle;
    private JTabbedPane tabTratamientosDetalle;
    private JPanel pnlArchivoDetalle;
    private JButton btnAbrirAdjunto;
    private JLabel lblRutaAdjunto;

    private JPanel pnlContenedorEditar;
    private JTextArea txtEvolucionEdit;
    private JLabel lblContadorEvolucionEdit;

    private JButton btnDiagEdit, btnTratEdit, btnArchivoEdit;

    private JPanel pnlDiagEdit;
    private JTabbedPane tabDiagnosticosEdit;
    private JButton btnAgregarTabDiagEdit;
    private JButton btnEliminarTabDiagEdit;
    private List<DatosDiagnostico> tabsDiagDataEdit = new ArrayList<>();

    private JPanel pnlTratEdit;
    private JTabbedPane tabTratamientosEdit;
    private JButton btnAgregarTabTratEdit;
    private JButton btnEliminarTabTratEdit;
    private List<DatosTratamiento> tabsTratDataEdit = new ArrayList<>();

    private JPanel pnlArchivoEdit;
    private String rutaArchivoAdjuntoEdit = "";
    private String rutaArchivoOriginalEdit = "";
    private JLabel lblRutaAdjuntoEdit;
    private ArrayList<File> archivosAdjuntosEdit = new ArrayList<>();

    private Border bordeSuaveInptus;
    private final Border BORDE_ERROR = EstiloRedondeado.borde(Color.RED, 2);
    private final Border BORDE_FECHA_NORMAL = UIManager.getBorder("TextField.border");

    private List<String> listaEnfermedades = new ArrayList<>();
    private List<String> listaTratamientos = new ArrayList<>();

    class DatosDiagnostico { JComboBox<String> cmbBuscar; JComboBox<String> cmbEstado; }
    class DatosTratamiento {
        JComboBox<String> cmbBuscar; JTextField txtDosis; JComboBox<String> cmbFrecuencia;
        JDateChooser dateInicio; JDateChooser dateFin; JComboBox<String> cmbEstado;
    }

    public VerHistoriaMedicaPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        bordeSuaveInptus = BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(200, 205, 208), 1),
            new EmptyBorder(8, 12, 8, 12)
        );

        add(construirVistaTabla(), "VISTA_TABLA");
        add(construirVistaCompleta(), "VISTA_COMPLETA");
        add(construirVistaDetalle(), "VISTA_DETALLE");
        add(construirVistaEditar(), "VISTA_EDITAR");
    }

    private JPanel construirVistaTabla() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE); panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel headerPanel = new JPanel(); headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("HISTORIA CLÍNICA"); lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(0, 105, 92)); headerPanel.add(lblTitulo); headerPanel.add(Box.createVerticalStrut(10));

        lblNombreDni = new JLabel("Paciente: --"); lblNombreDni.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblNombreDni.setForeground(Color.DARK_GRAY); headerPanel.add(lblNombreDni); panel.add(headerPanel, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"FECHA", "EVOLUCI\u00d3N Y DATOS ASOCIADOS", "M\u00c9DICO TRATANTE", "ESPECIALIDAD", "ACCI\u00d3N"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modelo); tabla.getTableHeader().setReorderingAllowed(false); tabla.getTableHeader().setResizingAllowed(false);
        tabla.setRowHeight(40); tabla.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tabla.getTableHeader().setFont(EstiloRedondeado.fuenteHeaderTabla()); tabla.getTableHeader().setBackground(new Color(0, 121, 107));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 42));
        tabla.getTableHeader().setForeground(Color.WHITE); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setCellSelectionEnabled(false); tabla.setRowSelectionAllowed(true); tabla.setColumnSelectionAllowed(false);
        tabla.setSelectionBackground(new Color(178, 223, 219));
        tabla.setIntercellSpacing(new Dimension(0, 0));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer(); centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tabla.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); tabla.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); tabla.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        ((DefaultTableCellRenderer) tabla.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        tabla.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(new Color(41, 128, 185)); ((JLabel)c).setHorizontalAlignment(JLabel.CENTER); c.setFont(c.getFont().deriveFont(Font.BOLD)); return c;
            }
        });

        tabla.getColumnModel().getColumn(0).setPreferredWidth(130); tabla.getColumnModel().getColumn(1).setPreferredWidth(260);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(230); tabla.getColumnModel().getColumn(3).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(140);

        JScrollPane scrollTabla = new JScrollPane(tabla); scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.setColumnHeaderView(tabla.getTableHeader());
        scrollTabla.setBorder(EstiloRedondeado.borde(new Color(200, 200, 200))); panel.add(scrollTabla, BorderLayout.CENTER);

        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint()); int columna = tabla.columnAtPoint(e.getPoint());
                if (fila >= 0) {
                    if (columna == 4) { abrirPantallaDetalle(indiceHistorialReal(fila)); }
                }
            }
        });

        JPanel inferior = new JPanel();
        inferior.setLayout(new BoxLayout(inferior, BoxLayout.Y_AXIS));
        inferior.setBackground(Color.WHITE);

        JPanel paginacionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        paginacionPanel.setBackground(Color.WHITE);
        btnHistorialAnterior = new JButton("< Anterior");
        btnHistorialSiguiente = new JButton("Siguiente >");
        lblPaginacionHistorial = new JLabel("P\u00e1gina 1 de 1");
        lblPaginacionHistorial.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPaginacionHistorial.setForeground(new Color(35, 45, 55));
        prepararBotonPaginacionHistorial(btnHistorialAnterior);
        prepararBotonPaginacionHistorial(btnHistorialSiguiente);
        btnHistorialAnterior.addActionListener(e -> cambiarPaginaHistorial(-1));
        btnHistorialSiguiente.addActionListener(e -> cambiarPaginaHistorial(1));
        paginacionPanel.add(btnHistorialAnterior);
        paginacionPanel.add(lblPaginacionHistorial);
        paginacionPanel.add(btnHistorialSiguiente);
        inferior.add(paginacionPanel);
        inferior.add(Box.createVerticalStrut(4));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); btnPanel.setBackground(Color.WHITE);
        JButton btnHistoriaCompleta = crearBotonHistoria("Ver Historia Completa", false, 220);
        btnHistoriaCompleta.addActionListener(e -> abrirHistoriaCompleta());
        JButton btnVolver = crearBotonHistoria("Volver a Pacientes", true, 220);
        btnVolver.addActionListener(e -> mainApp.cambiarPanel("GESTION_PACIENTES"));
        btnPanel.add(btnHistoriaCompleta); btnPanel.add(btnVolver);
        inferior.add(btnPanel);
        panel.add(inferior, BorderLayout.SOUTH);
        return panel;
    }

    private void prepararBotonPaginacionHistorial(JButton boton) {
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setPreferredSize(new Dimension(140, 42));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(boton);
    }

    private JButton crearBotonHistoria(String texto, boolean peligro, int ancho) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setPreferredSize(new Dimension(ancho, 45));
        boton.setMinimumSize(new Dimension(ancho, 45));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setFocusPainted(false);
        if (peligro) {
            EstiloRedondeado.aplicarBotonPeligro(boton);
        } else {
            EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(boton);
        }
        return boton;
    }

    private int indiceHistorialReal(int filaTabla) {
        return ((paginaHistorialActual - 1) * historiasPorPagina) + filaTabla;
    }

    private void cambiarPaginaHistorial(int delta) {
        int nuevaPagina = paginaHistorialActual + delta;
        if (nuevaPagina < 1 || nuevaPagina > totalPaginasHistorial) return;
        paginaHistorialActual = nuevaPagina;
        cargarPaginaHistorial();
    }

    private void cargarPaginaHistorial() {
        modelo.setRowCount(0);
        totalPaginasHistorial = Math.max(1, (int) Math.ceil(historialCompleto.size() / (double) historiasPorPagina));
        if (paginaHistorialActual > totalPaginasHistorial) paginaHistorialActual = totalPaginasHistorial;

        int inicio = (paginaHistorialActual - 1) * historiasPorPagina;
        int fin = Math.min(inicio + historiasPorPagina, historialCompleto.size());
        for (int i = inicio; i < fin; i++) {
            String[] fila = historialCompleto.get(i);
            modelo.addRow(new Object[]{fila[0], fila[1], fila[2], valor(fila, 11, ""), "Ver Detalle"});
        }
        actualizarControlesPaginacionHistorial();
    }

    private void actualizarControlesPaginacionHistorial() {
        totalPaginasHistorial = Math.max(1, (int) Math.ceil(historialCompleto.size() / (double) historiasPorPagina));
        if (lblPaginacionHistorial != null) {
            lblPaginacionHistorial.setText("P\u00e1gina " + paginaHistorialActual + " de " + totalPaginasHistorial);
        }
        if (btnHistorialAnterior != null) btnHistorialAnterior.setEnabled(paginaHistorialActual > 1);
        if (btnHistorialSiguiente != null) btnHistorialSiguiente.setEnabled(paginaHistorialActual < totalPaginasHistorial);
    }

    private JPanel construirVistaCompleta() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(new Color(242, 245, 247));
        panel.setBorder(new EmptyBorder(25, 50, 25, 50));

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setOpaque(false);
        JLabel titulo = new JLabel("HISTORIA CLÍNICA COMPLETA");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(new Color(0, 105, 92));
        lblHistoriaCompletaPaciente = new JLabel("Paciente: --");
        lblHistoriaCompletaPaciente.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblHistoriaCompletaPaciente.setForeground(Color.DARK_GRAY);
        encabezado.add(titulo);
        encabezado.add(Box.createVerticalStrut(6));
        encabezado.add(lblHistoriaCompletaPaciente);
        panel.add(encabezado, BorderLayout.NORTH);

        pnlHistoriaCompletaRegistros = new JPanel();
        pnlHistoriaCompletaRegistros.setLayout(new GridBagLayout());
        pnlHistoriaCompletaRegistros.setBackground(Color.WHITE);
        pnlHistoriaCompletaRegistros.setBorder(new EmptyBorder(12, 12, 12, 12));
        JScrollPane scroll = new JScrollPane(pnlHistoriaCompletaRegistros);
        scroll.setBorder(EstiloRedondeado.borde(new Color(215, 220, 225)));
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        acciones.setOpaque(false);
        JButton volver = crearBotonHistoria("Volver al Listado", true, 170);
        volver.addActionListener(e -> cardLayout.show(this, "VISTA_TABLA"));
        JButton exportar = crearBotonHistoria("Exportar PDF", false, 150);
        exportar.addActionListener(e -> exportarHistoriaCompleta());
        JButton todas = crearBotonHistoria("Seleccionar Todo", false, 190);
        todas.addActionListener(e -> marcarHistorias(true));
        JButton ninguna = new JButton("QUITAR SELECCIÓN");
        ninguna.setText("Quitar Selección");
        EstiloRedondeado.aplicarBotonPeligro(ninguna);
        ninguna.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ninguna.setPreferredSize(new Dimension(170, 45));
        ninguna.setMinimumSize(new Dimension(170, 45));
        ninguna.addActionListener(e -> marcarHistorias(false));
        acciones.add(todas);
        acciones.add(ninguna);
        acciones.add(exportar);
        acciones.add(volver);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirVistaDetalle() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setBackground(new Color(242, 245, 247));
        JPanel pnlNorte = new JPanel(); pnlNorte.setLayout(new BoxLayout(pnlNorte, BoxLayout.Y_AXIS));
        pnlNorte.setBackground(new Color(242, 245, 247)); pnlNorte.setBorder(new EmptyBorder(25, 60, 10, 60));
        JLabel lblTitulo = new JLabel("DETALLE DE HISTORIA CLÍNICA"); lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(0, 105, 92)); lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT); pnlNorte.add(lblTitulo); pnlNorte.add(Box.createVerticalStrut(5));
        lblSubtituloDetalle = new JLabel("Cargando datos..."); lblSubtituloDetalle.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        lblSubtituloDetalle.setForeground(new Color(108, 117, 125)); lblSubtituloDetalle.setAlignmentX(Component.LEFT_ALIGNMENT); pnlNorte.add(lblSubtituloDetalle); panel.add(pnlNorte, BorderLayout.NORTH);

        JPanel pnlFondoWeb = new JPanel(new GridBagLayout()); pnlFondoWeb.setBackground(new Color(242, 245, 247));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.NORTH; gbc.insets = new Insets(10, 30, 40, 30);
        pnlContenedorDetalle = new JPanel();
        pnlContenedorDetalle.setLayout(new BoxLayout(pnlContenedorDetalle, BoxLayout.Y_AXIS)); pnlContenedorDetalle.setBackground(Color.WHITE);
        pnlContenedorDetalle.setMaximumSize(new Dimension(1000, Integer.MAX_VALUE));
        pnlContenedorDetalle.setBorder(BorderFactory.createCompoundBorder(EstiloRedondeado.borde(new Color(225, 228, 230), 1), new EmptyBorder(16, 30, 22, 30)));

        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); pnlToolbar.setBackground(Color.WHITE); pnlToolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); pnlToolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 228, 230))); pnlToolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDiagDetalle = crearBotonToggle("+ Diagnóstico"); btnTratDetalle = crearBotonToggle("+ Tratamiento"); btnArchivoDetalle = crearBotonToggle("+ Archivo");
        pnlToolbar.add(btnDiagDetalle); pnlToolbar.add(btnTratDetalle); pnlToolbar.add(btnArchivoDetalle); pnlContenedorDetalle.add(pnlToolbar); pnlContenedorDetalle.add(Box.createVerticalStrut(20));

        JPanel pnlEvo = new JPanel(new BorderLayout()); pnlEvo.setOpaque(false); pnlEvo.setAlignmentX(Component.LEFT_ALIGNMENT); pnlEvo.setPreferredSize(new Dimension(0, 120)); pnlEvo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        JLabel lblEvo = new JLabel("  Evolución clínica detallada:"); lblEvo.setFont(new Font("Segoe UI", Font.BOLD, 15)); lblEvo.setForeground(new Color(100, 110, 115)); pnlEvo.add(lblEvo, BorderLayout.NORTH);
        txtDetalleEvolucion = new JTextArea(); txtDetalleEvolucion.setLineWrap(true); txtDetalleEvolucion.setWrapStyleWord(true); txtDetalleEvolucion.setEditable(false); txtDetalleEvolucion.setFont(new Font("Segoe UI", Font.PLAIN, 15)); txtDetalleEvolucion.setBackground(new Color(250, 251, 252)); txtDetalleEvolucion.setForeground(new Color(50, 50, 50)); txtDetalleEvolucion.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollEvo = new JScrollPane(txtDetalleEvolucion); scrollEvo.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(6, 0, 0, 0), EstiloRedondeado.borde(new Color(215, 220, 225), 1))); pnlEvo.add(scrollEvo, BorderLayout.CENTER); pnlContenedorDetalle.add(pnlEvo); pnlContenedorDetalle.add(Box.createVerticalStrut(15));

        construirPanelDiagDetalle(); construirPanelTratDetalle(); construirPanelArchivoDetalle();
        pnlContenedorDetalle.add(pnlDiagDetalle); pnlContenedorDetalle.add(pnlTratDetalle); pnlContenedorDetalle.add(pnlArchivoDetalle);
        ajustarTarjetaDetalle();
        pnlFondoWeb.add(pnlContenedorDetalle, gbc); JScrollPane scrollGeneral = new JScrollPane(pnlFondoWeb); scrollGeneral.setBorder(null); scrollGeneral.getVerticalScrollBar().setUnitIncrement(20); scrollGeneral.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); panel.add(scrollGeneral, BorderLayout.CENTER);

        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15)); pnlSur.setBackground(Color.WHITE); pnlSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 228, 230)));
        JButton btnVolverLista = new JButton("Volver a la lista"); btnVolverLista.setBackground(Color.WHITE); btnVolverLista.setForeground(new Color(108, 117, 125)); btnVolverLista.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnVolverLista.setPreferredSize(new Dimension(160, 45)); btnVolverLista.setBorder(EstiloRedondeado.borde(new Color(200, 205, 208), 1)); btnVolverLista.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnVolverLista.setFocusPainted(false); btnVolverLista.addActionListener(e -> cardLayout.show(this, "VISTA_TABLA"));
        pnlSur.add(btnVolverLista); panel.add(pnlSur, BorderLayout.SOUTH);

        btnDiagDetalle.addActionListener(e -> alternarAcordeon(pnlDiagDetalle, pnlTratDetalle, pnlArchivoDetalle, btnDiagDetalle, btnTratDetalle, btnArchivoDetalle, pnlContenedorDetalle));
        btnTratDetalle.addActionListener(e -> alternarAcordeon(pnlTratDetalle, pnlDiagDetalle, pnlArchivoDetalle, btnTratDetalle, btnDiagDetalle, btnArchivoDetalle, pnlContenedorDetalle));
        btnArchivoDetalle.addActionListener(e -> alternarAcordeon(pnlArchivoDetalle, pnlDiagDetalle, pnlTratDetalle, btnArchivoDetalle, btnDiagDetalle, btnTratDetalle, pnlContenedorDetalle));
        return panel;
    }

    private void construirPanelDiagDetalle() {
        pnlDiagDetalle = new JPanel(new BorderLayout()); pnlDiagDetalle.setBackground(new Color(250, 251, 252));
        pnlDiagDetalle.setBorder(BorderFactory.createCompoundBorder(EstiloRedondeado.borde(new Color(225, 228, 230), 1), new EmptyBorder(12, 20, 12, 20))); pnlDiagDetalle.setAlignmentX(Component.LEFT_ALIGNMENT); pnlDiagDetalle.setVisible(false); pnlDiagDetalle.setPreferredSize(new Dimension(0, 380)); pnlDiagDetalle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));
        JLabel lbl = new JLabel("Diagnósticos Registrados"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 16)); lbl.setForeground(new Color(41, 128, 185)); pnlDiagDetalle.add(lbl, BorderLayout.NORTH);
        tabDiagnosticosDetalle = new JTabbedPane(); tabDiagnosticosDetalle.setFont(new Font("Segoe UI", Font.BOLD, 13)); tabDiagnosticosDetalle.setBackground(Color.WHITE); tabDiagnosticosDetalle.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT); pnlDiagDetalle.add(tabDiagnosticosDetalle, BorderLayout.CENTER);
    }

    private void construirPanelTratDetalle() {
        pnlTratDetalle = new JPanel(new BorderLayout()); pnlTratDetalle.setBackground(new Color(250, 251, 252)); pnlTratDetalle.setBorder(BorderFactory.createCompoundBorder(EstiloRedondeado.borde(new Color(225, 228, 230), 1), new EmptyBorder(12, 20, 12, 20))); pnlTratDetalle.setAlignmentX(Component.LEFT_ALIGNMENT); pnlTratDetalle.setVisible(false); pnlTratDetalle.setPreferredSize(new Dimension(0, 460)); pnlTratDetalle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 460));
        JLabel lbl = new JLabel("Tratamientos e Indicaciones"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 16)); lbl.setForeground(new Color(41, 128, 185)); pnlTratDetalle.add(lbl, BorderLayout.NORTH);
        tabTratamientosDetalle = new JTabbedPane(); tabTratamientosDetalle.setFont(new Font("Segoe UI", Font.BOLD, 13)); tabTratamientosDetalle.setBackground(Color.WHITE); tabTratamientosDetalle.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT); pnlTratDetalle.add(tabTratamientosDetalle, BorderLayout.CENTER);
    }

    private void construirPanelArchivoDetalle() {
        pnlArchivoDetalle = new JPanel(); pnlArchivoDetalle.setLayout(new BoxLayout(pnlArchivoDetalle, BoxLayout.Y_AXIS)); pnlArchivoDetalle.setBackground(new Color(250, 251, 252)); pnlArchivoDetalle.setBorder(BorderFactory.createCompoundBorder(EstiloRedondeado.borde(new Color(225, 228, 230), 1), new EmptyBorder(15, 25, 15, 25))); pnlArchivoDetalle.setAlignmentX(Component.LEFT_ALIGNMENT); pnlArchivoDetalle.setVisible(false);
        JLabel lbl = new JLabel("Estudios y Archivos Adjuntos"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 16)); lbl.setForeground(new Color(41, 128, 185)); lbl.setAlignmentX(Component.LEFT_ALIGNMENT); pnlArchivoDetalle.add(lbl); pnlArchivoDetalle.add(Box.createVerticalStrut(15));
        JPanel pnlBtnAdj = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); pnlBtnAdj.setOpaque(false); pnlBtnAdj.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAbrirAdjunto = new JButton("Ver Estudios Adjuntos"); btnAbrirAdjunto.setBackground(Color.WHITE); btnAbrirAdjunto.setForeground(new Color(41, 128, 185)); btnAbrirAdjunto.setFont(new Font("Segoe UI", Font.BOLD, 13)); btnAbrirAdjunto.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnAbrirAdjunto.setFocusPainted(false); btnAbrirAdjunto.setBorder(BorderFactory.createCompoundBorder(EstiloRedondeado.borde(new Color(41, 128, 185), 1), new EmptyBorder(8, 15, 8, 15))); btnAbrirAdjunto.addActionListener(e -> abrirArchivosInteligente());
        lblRutaAdjunto = new JLabel("   Cargando archivos..."); lblRutaAdjunto.setFont(new Font("Segoe UI", Font.ITALIC, 16)); lblRutaAdjunto.setForeground(new Color(149, 165, 166));
        pnlBtnAdj.add(btnAbrirAdjunto); pnlBtnAdj.add(lblRutaAdjunto); pnlArchivoDetalle.add(pnlBtnAdj);
    }

    private JPanel construirVistaEditar() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setBackground(new Color(242, 245, 247));
        JPanel pnlNorte = new JPanel(); pnlNorte.setLayout(new BoxLayout(pnlNorte, BoxLayout.Y_AXIS)); pnlNorte.setBackground(new Color(242, 245, 247)); pnlNorte.setBorder(new EmptyBorder(25, 60, 10, 60));
        JLabel lblTitulo = new JLabel("EDITAR HISTORIA CLÍNICA"); lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28)); lblTitulo.setForeground(new Color(243, 156, 18)); pnlNorte.add(lblTitulo); panel.add(pnlNorte, BorderLayout.NORTH);
        JPanel pnlFondoWeb = new JPanel(new GridBagLayout()); pnlFondoWeb.setBackground(new Color(242, 245, 247));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.NORTH; gbc.insets = new Insets(10, 30, 40, 30);
        pnlContenedorEditar = new JPanel(); pnlContenedorEditar.setLayout(new BoxLayout(pnlContenedorEditar, BoxLayout.Y_AXIS)); pnlContenedorEditar.setBackground(Color.WHITE); pnlContenedorEditar.setMaximumSize(new Dimension(1000, Integer.MAX_VALUE)); pnlContenedorEditar.setBorder(BorderFactory.createCompoundBorder(EstiloRedondeado.borde(new Color(225, 228, 230), 1), new EmptyBorder(20, 40, 30, 40)));
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); pnlToolbar.setBackground(Color.WHITE); pnlToolbar.setMaximumSize(new Dimension(900, 60)); pnlToolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 228, 230))); pnlToolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDiagEdit = crearBotonToggle("+ Diagnóstico"); btnTratEdit = crearBotonToggle("+ Tratamiento"); btnArchivoEdit = crearBotonToggle("+ Archivo");
        pnlToolbar.add(btnDiagEdit); pnlToolbar.add(btnTratEdit); pnlToolbar.add(btnArchivoEdit); pnlContenedorEditar.add(pnlToolbar); pnlContenedorEditar.add(Box.createVerticalStrut(20));
        JPanel pnlEvo = new JPanel(new BorderLayout()); pnlEvo.setBackground(Color.WHITE); pnlEvo.setAlignmentX(Component.LEFT_ALIGNMENT); pnlEvo.setPreferredSize(new Dimension(900, 150)); pnlEvo.setMaximumSize(new Dimension(900, 150));
        JLabel lblEvo = new JLabel("  Escribe aquí la evolución clínica detallada (*Obligatorio):"); lblEvo.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblEvo.setForeground(new Color(100, 110, 115));
        lblContadorEvolucionEdit = new JLabel("0 / 300"); lblContadorEvolucionEdit.setFont(new Font("Segoe UI", Font.PLAIN, 13)); lblContadorEvolucionEdit.setForeground(new Color(108, 117, 125));
        JPanel encabezadoEvo = new JPanel(new BorderLayout()); encabezadoEvo.setOpaque(false); encabezadoEvo.add(lblEvo, BorderLayout.WEST); encabezadoEvo.add(lblContadorEvolucionEdit, BorderLayout.EAST); pnlEvo.add(encabezadoEvo, BorderLayout.NORTH);
        txtEvolucionEdit = new JTextArea(); txtEvolucionEdit.setLineWrap(true); txtEvolucionEdit.setWrapStyleWord(true); txtEvolucionEdit.setFont(new Font("Segoe UI", Font.PLAIN, 15)); txtEvolucionEdit.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        int limiteEvolucion = 300; ((AbstractDocument) txtEvolucionEdit.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException { if (text == null) text = ""; if (fb.getDocument().getLength() + text.length() - length <= limiteEvolucion) { super.replace(fb, offset, length, text, attrs); } else { Toolkit.getDefaultToolkit().beep(); } }
            @Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException { if (string == null) return; if (fb.getDocument().getLength() + string.length() <= limiteEvolucion) { super.insertString(fb, offset, string, attr); } else { Toolkit.getDefaultToolkit().beep(); } }
        });
        txtEvolucionEdit.getDocument().addDocumentListener(new DocumentListener() {
            private void actualizar() {
                int cantidad = txtEvolucionEdit.getDocument().getLength();
                lblContadorEvolucionEdit.setText(cantidad + " / " + limiteEvolucion);
                lblContadorEvolucionEdit.setForeground(cantidad > limiteEvolucion ? new Color(211, 47, 47) : new Color(108, 117, 125));
            }
            @Override public void insertUpdate(DocumentEvent e) { actualizar(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizar(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizar(); }
        });
        JScrollPane scrollEvo = new JScrollPane(txtEvolucionEdit); scrollEvo.setBorder(EstiloRedondeado.borde(new Color(200, 205, 208), 1)); pnlEvo.add(scrollEvo, BorderLayout.CENTER); pnlContenedorEditar.add(pnlEvo); pnlContenedorEditar.add(Box.createVerticalStrut(15));
        construirPanelDiagEdit(); construirPanelTratEdit(); construirPanelArchivoEdit();
        pnlContenedorEditar.add(pnlDiagEdit); pnlContenedorEditar.add(pnlTratEdit); pnlContenedorEditar.add(pnlArchivoEdit);
        pnlFondoWeb.add(pnlContenedorEditar, gbc); JScrollPane scrollGeneral = new JScrollPane(pnlFondoWeb); scrollGeneral.setBorder(null); scrollGeneral.getVerticalScrollBar().setUnitIncrement(20); scrollGeneral.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); panel.add(scrollGeneral, BorderLayout.CENTER);
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 60, 15)); pnlSur.setBackground(Color.WHITE); pnlSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 228, 230)));
        JButton btnVolver = new JButton("Cancelar Edición"); btnVolver.setBackground(Color.WHITE); btnVolver.setForeground(new Color(108, 117, 125)); btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnVolver.setPreferredSize(new Dimension(170, 45)); btnVolver.setBorder(EstiloRedondeado.borde(new Color(200, 205, 208), 1)); btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnVolver.setFocusPainted(false); btnVolver.addActionListener(e -> cardLayout.show(this, "VISTA_TABLA"));
        JButton btnActualizar = new JButton("Actualizar Historia Clínica"); btnActualizar.setBackground(new Color(243, 156, 18)); btnActualizar.setForeground(Color.WHITE); btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 15)); btnActualizar.setPreferredSize(new Dimension(230, 45)); btnActualizar.setBorder(BorderFactory.createEmptyBorder()); btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnActualizar.setFocusPainted(false); btnActualizar.addActionListener(e -> actualizarConsulta());
        pnlSur.add(btnVolver); pnlSur.add(btnActualizar); panel.add(pnlSur, BorderLayout.SOUTH);
        btnDiagEdit.addActionListener(e -> alternarAcordeon(pnlDiagEdit, pnlTratEdit, pnlArchivoEdit, btnDiagEdit, btnTratEdit, btnArchivoEdit, pnlContenedorEditar));
        btnTratEdit.addActionListener(e -> alternarAcordeon(pnlTratEdit, pnlDiagEdit, pnlArchivoEdit, btnTratEdit, btnDiagEdit, btnArchivoEdit, pnlContenedorEditar));
        btnArchivoEdit.addActionListener(e -> alternarAcordeon(pnlArchivoEdit, pnlDiagEdit, pnlTratEdit, btnArchivoEdit, btnDiagEdit, btnTratEdit, pnlContenedorEditar));
        return panel;
    }

    private void construirPanelDiagEdit() {
        pnlDiagEdit = new JPanel(new BorderLayout()); pnlDiagEdit.setBackground(new Color(250, 251, 252)); pnlDiagEdit.setBorder(BorderFactory.createCompoundBorder(EstiloRedondeado.borde(new Color(225, 228, 230), 1), new EmptyBorder(15, 25, 15, 25))); pnlDiagEdit.setAlignmentX(Component.LEFT_ALIGNMENT); pnlDiagEdit.setVisible(false); pnlDiagEdit.setPreferredSize(new Dimension(900, 320)); pnlDiagEdit.setMaximumSize(new Dimension(900, 320));
        JPanel pnlHeader = new JPanel(new BorderLayout()); pnlHeader.setOpaque(false); pnlHeader.setMaximumSize(new Dimension(900, 35));
        JLabel lbl = new JLabel("Sección de Diagnóstico"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 16)); lbl.setForeground(new Color(41, 128, 185)); pnlHeader.add(lbl, BorderLayout.WEST);
        btnAgregarTabDiagEdit = new JButton("+ Añadir Pestaña"); btnAgregarTabDiagEdit.setFont(new Font("Segoe UI", Font.BOLD, 12)); btnAgregarTabDiagEdit.setBackground(Color.WHITE); btnAgregarTabDiagEdit.setForeground(new Color(41, 128, 185)); btnAgregarTabDiagEdit.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnAgregarTabDiagEdit.setFocusPainted(false); btnAgregarTabDiagEdit.setToolTipText("Máximo " + MAX_PESTANAS_POR_SECCION + " diagnósticos por evolución."); btnAgregarTabDiagEdit.addActionListener(e -> agregarNuevaTabEditDiagnostico());
        btnEliminarTabDiagEdit = new JButton("Quitar pestaña"); btnEliminarTabDiagEdit.setFont(new Font("Segoe UI", Font.BOLD, 12)); btnEliminarTabDiagEdit.setBackground(Color.WHITE); btnEliminarTabDiagEdit.setForeground(new Color(211, 47, 47)); btnEliminarTabDiagEdit.setFocusPainted(false); btnEliminarTabDiagEdit.setToolTipText("Solo se pueden quitar pestañas adicionales; la primera se conserva."); btnEliminarTabDiagEdit.addActionListener(e -> eliminarTabEditDiagnostico());
        JPanel accionesTabs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); accionesTabs.setOpaque(false); accionesTabs.add(btnEliminarTabDiagEdit); accionesTabs.add(btnAgregarTabDiagEdit); pnlHeader.add(accionesTabs, BorderLayout.EAST);
        pnlDiagEdit.add(pnlHeader, BorderLayout.NORTH); tabDiagnosticosEdit = new JTabbedPane(); tabDiagnosticosEdit.setFont(new Font("Segoe UI", Font.BOLD, 13)); tabDiagnosticosEdit.setBackground(Color.WHITE); tabDiagnosticosEdit.addChangeListener(e -> actualizarBotonesTabsEdit()); pnlDiagEdit.add(tabDiagnosticosEdit, BorderLayout.CENTER);
    }

    private void construirPanelTratEdit() {
        pnlTratEdit = new JPanel(new BorderLayout()); pnlTratEdit.setBackground(new Color(250, 251, 252)); pnlTratEdit.setBorder(BorderFactory.createCompoundBorder(EstiloRedondeado.borde(new Color(225, 228, 230), 1), new EmptyBorder(15, 25, 15, 25))); pnlTratEdit.setAlignmentX(Component.LEFT_ALIGNMENT); pnlTratEdit.setVisible(false); pnlTratEdit.setPreferredSize(new Dimension(900, 380)); pnlTratEdit.setMaximumSize(new Dimension(900, 380));
        JPanel pnlHeader = new JPanel(new BorderLayout()); pnlHeader.setOpaque(false); pnlHeader.setMaximumSize(new Dimension(900, 35));
        JLabel lbl = new JLabel("Sección de Tratamiento e Indicaciones"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 16)); lbl.setForeground(new Color(41, 128, 185)); pnlHeader.add(lbl, BorderLayout.WEST);
        btnAgregarTabTratEdit = new JButton("+ Añadir Pestaña"); btnAgregarTabTratEdit.setFont(new Font("Segoe UI", Font.BOLD, 12)); btnAgregarTabTratEdit.setBackground(Color.WHITE); btnAgregarTabTratEdit.setForeground(new Color(41, 128, 185)); btnAgregarTabTratEdit.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnAgregarTabTratEdit.setFocusPainted(false); btnAgregarTabTratEdit.setToolTipText("Máximo " + MAX_PESTANAS_POR_SECCION + " tratamientos por evolución."); btnAgregarTabTratEdit.addActionListener(e -> agregarNuevaTabEditTratamiento());
        btnEliminarTabTratEdit = new JButton("Quitar pestaña"); btnEliminarTabTratEdit.setFont(new Font("Segoe UI", Font.BOLD, 12)); btnEliminarTabTratEdit.setBackground(Color.WHITE); btnEliminarTabTratEdit.setForeground(new Color(211, 47, 47)); btnEliminarTabTratEdit.setFocusPainted(false); btnEliminarTabTratEdit.setToolTipText("Solo se pueden quitar pestañas adicionales; la primera se conserva."); btnEliminarTabTratEdit.addActionListener(e -> eliminarTabEditTratamiento());
        JPanel accionesTabs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); accionesTabs.setOpaque(false); accionesTabs.add(btnEliminarTabTratEdit); accionesTabs.add(btnAgregarTabTratEdit); pnlHeader.add(accionesTabs, BorderLayout.EAST);
        pnlTratEdit.add(pnlHeader, BorderLayout.NORTH); tabTratamientosEdit = new JTabbedPane(); tabTratamientosEdit.setFont(new Font("Segoe UI", Font.BOLD, 13)); tabTratamientosEdit.setBackground(Color.WHITE); tabTratamientosEdit.addChangeListener(e -> actualizarBotonesTabsEdit()); pnlTratEdit.add(tabTratamientosEdit, BorderLayout.CENTER);
    }

    private void construirPanelArchivoEdit() {
        pnlArchivoEdit = new JPanel(); pnlArchivoEdit.setLayout(new BoxLayout(pnlArchivoEdit, BoxLayout.Y_AXIS)); pnlArchivoEdit.setBackground(new Color(250, 251, 252)); pnlArchivoEdit.setBorder(BorderFactory.createCompoundBorder(EstiloRedondeado.borde(new Color(225, 228, 230), 1), new EmptyBorder(15, 25, 15, 25))); pnlArchivoEdit.setAlignmentX(Component.LEFT_ALIGNMENT); pnlArchivoEdit.setVisible(false);
        JLabel lbl = new JLabel("Estudios y Archivos Adjuntos"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 16)); lbl.setForeground(new Color(41, 128, 185)); lbl.setAlignmentX(Component.LEFT_ALIGNMENT); pnlArchivoEdit.add(lbl); pnlArchivoEdit.add(Box.createVerticalStrut(10));
        JPanel pnlBtnAdj = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); pnlBtnAdj.setOpaque(false); pnlBtnAdj.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnAdjuntar = new JButton("+ Seleccionar PDFs / Imágenes"); btnAdjuntar.setBackground(Color.WHITE); btnAdjuntar.setForeground(new Color(41, 128, 185)); btnAdjuntar.setFont(new Font("Segoe UI", Font.BOLD, 13)); btnAdjuntar.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnAdjuntar.setFocusPainted(false); btnAdjuntar.setBorder(BorderFactory.createCompoundBorder(EstiloRedondeado.borde(new Color(41, 128, 185), 1), new EmptyBorder(8, 15, 8, 15)));
        btnAdjuntar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(); fileChooser.setMultiSelectionEnabled(true);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File[] seleccionados = fileChooser.getSelectedFiles(); archivosAdjuntosEdit.clear();
                for (File seleccionado : seleccionados) archivosAdjuntosEdit.add(seleccionado);
                lblRutaAdjuntoEdit.setText("   " + archivosAdjuntosEdit.size() + " nuevo(s) archivo(s) listo(s)."); lblRutaAdjuntoEdit.setForeground(new Color(39, 174, 96));
            }
        });
        JButton btnLimpiar = new JButton("Limpiar"); btnLimpiar.setBackground(new Color(231, 76, 60)); btnLimpiar.setForeground(Color.WHITE); btnLimpiar.setFont(new Font("Segoe UI", Font.BOLD, 13)); btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnLimpiar.setFocusPainted(false); btnLimpiar.setBorder(BorderFactory.createEmptyBorder(9, 15, 9, 15));
        btnLimpiar.addActionListener(e -> { archivosAdjuntosEdit.clear(); rutaArchivoAdjuntoEdit = ""; lblRutaAdjuntoEdit.setText("   Ningún archivo adjunto en esta consulta."); lblRutaAdjuntoEdit.setForeground(new Color(149, 165, 166)); });
        lblRutaAdjuntoEdit = new JLabel("   Ningún archivo adjunto."); lblRutaAdjuntoEdit.setFont(new Font("Segoe UI", Font.ITALIC, 16)); lblRutaAdjuntoEdit.setForeground(new Color(149, 165, 166));
        pnlBtnAdj.add(btnAdjuntar); pnlBtnAdj.add(Box.createHorizontalStrut(10)); pnlBtnAdj.add(btnLimpiar); pnlBtnAdj.add(lblRutaAdjuntoEdit); pnlArchivoEdit.add(pnlBtnAdj);
    }

    private void abrirPantallaEdicion() {
        if(datosConsultaActual == null) return;
        idEspecialidadMedicoActual = obtenerIdEspecialidadSesion();
        listaEnfermedades = new CatalogoDAO().obtenerDiagnosticosActivosPorEspecialidad(idEspecialidadMedicoActual);
        listaTratamientos = new CatalogoDAO().obtenerMedicamentosActivosPorEspecialidad(idEspecialidadMedicoActual);
        String evolucionActual = datosConsultaActual[3] != null ? datosConsultaActual[3] : "";
        AbstractDocument documentoEvolucion = (AbstractDocument) txtEvolucionEdit.getDocument();
        DocumentFilter filtroLimite = documentoEvolucion.getDocumentFilter();
        documentoEvolucion.setDocumentFilter(null);
        txtEvolucionEdit.setText(evolucionActual);
        documentoEvolucion.setDocumentFilter(filtroLimite);
        tabDiagnosticosEdit.removeAll(); tabsDiagDataEdit.clear();
        String diagCrudo = datosConsultaActual[4];
        if (diagCrudo == null || diagCrudo.isEmpty() || diagCrudo.equals("No registrado") || diagCrudo.equals("No registrada")) { agregarTabEditDiagnostico(null, null); }
        else {
            String[] diags = diagCrudo.split("\\|\\|");
            for (String d : diags) {
                String limpio = d.replace("[", "").replace("]", "").trim(); String[] partes = limpio.split(" \\| ");
                String diag = partes.length > 0 ? partes[0].trim() : ""; String est = "";
                for(String p : partes) { if(p.trim().startsWith("Est: ")) est = p.replace("Est: ", "").trim(); }
                agregarTabEditDiagnostico(diag, est);
            }
        }
        tabTratamientosEdit.removeAll(); tabsTratDataEdit.clear();
        String tratCrudo = datosConsultaActual[5];
        if (tratCrudo == null || tratCrudo.isEmpty() || tratCrudo.equals("No registrado")) { agregarTabEditTratamiento(null, null, null, null, null, null); }
        else {
            String[] trats = tratCrudo.split("\\|\\|");
            for (String t : trats) {
                String limpio = t.replace("[", "").replace("]", "").trim(); String[] partes = limpio.split(" \\| ");
                String med = partes.length > 0 ? partes[0].trim() : ""; String dosis = "", frec = "", ini = "", fin = "", est = "";
                for(String p : partes) {
                    if(p.trim().startsWith("Dosis: ")) dosis = p.replace("Dosis: ", "").trim();
                    if(p.trim().startsWith("Frec: ")) frec = p.replace("Frec: ", "").trim();
                    if(p.trim().startsWith("Inicio: ")) ini = p.replace("Inicio: ", "").trim();
                    if(p.trim().startsWith("Fin: ")) fin = p.replace("Fin: ", "").trim();
                    if(p.trim().startsWith("Est: ")) est = p.replace("Est: ", "").trim();
                }
                agregarTabEditTratamiento(med, dosis, frec, ini, fin, est);
            }
        }
        rutaArchivoAdjuntoEdit = datosConsultaActual[8] != null ? datosConsultaActual[8] : ""; rutaArchivoOriginalEdit = rutaArchivoAdjuntoEdit; archivosAdjuntosEdit.clear();
        if(!rutaArchivoAdjuntoEdit.isEmpty() && !rutaArchivoAdjuntoEdit.equals("No registrado")) { lblRutaAdjuntoEdit.setText("   Archivos preservados. (Si seleccionás nuevos, se reemplazarán)."); }
        else { lblRutaAdjuntoEdit.setText("   Ningún archivo adjunto en esta consulta."); }
        pnlDiagEdit.setVisible(false); pnlTratEdit.setVisible(false); pnlArchivoEdit.setVisible(false);
        btnDiagEdit.setBackground(Color.WHITE); btnDiagEdit.setForeground(new Color(41, 128, 185)); btnTratEdit.setBackground(Color.WHITE); btnTratEdit.setForeground(new Color(41, 128, 185)); btnArchivoEdit.setBackground(Color.WHITE); btnArchivoEdit.setForeground(new Color(41, 128, 185));
        cardLayout.show(this, "VISTA_EDITAR");
    }

    private void agregarTabEditDiagnostico(String diag, String est) {
        DatosDiagnostico d = new DatosDiagnostico();
        JPanel pnl = new JPanel(); pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS)); pnl.setOpaque(false); pnl.setBorder(new EmptyBorder(15, 10, 10, 10));
        d.cmbBuscar = new JComboBox<>(); d.cmbBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        d.cmbEstado = new JComboBox<>(new String[]{"Agudo", "Crónico", "Resuelto"});
        if (diag != null && !diag.equals("No especificado") && !diag.isEmpty()) { if (!listaEnfermedades.contains(diag)) listaEnfermedades.add(diag); }
        pnl.add(crearBuscadorDinamico("Diagnóstico (opcional):", listaEnfermedades, d.cmbBuscar,
            valor -> new CatalogoDAO().insertarDiagnostico(valor, idEspecialidadMedicoActual), valor -> new CatalogoDAO().eliminarDiagnostico(valor), (viejo, nuevo) -> new CatalogoDAO().editarDiagnostico(viejo, nuevo),
            () -> new CatalogoDAO().obtenerDiagnosticosInactivosPorEspecialidad(idEspecialidadMedicoActual), valor -> new CatalogoDAO().restaurarDiagnostico(valor, idEspecialidadMedicoActual)
        ));
        if (diag != null) d.cmbBuscar.setSelectedItem(diag); if (est != null && !est.equals("-")) d.cmbEstado.setSelectedItem(est);
        JPanel filaF = new JPanel(new GridLayout(1, 1, 20, 0)); filaF.setOpaque(false); filaF.setAlignmentX(Component.LEFT_ALIGNMENT); filaF.setMaximumSize(new Dimension(900, 75));
        filaF.add(crearBloqueInput("Estado:", d.cmbEstado)); pnl.add(filaF);
        tabsDiagDataEdit.add(d); ResponsiveUI.aplicar(pnl); tabDiagnosticosEdit.addTab("Diagnóstico " + tabsDiagDataEdit.size(), pnl); tabDiagnosticosEdit.setSelectedIndex(tabsDiagDataEdit.size() - 1); actualizarBotonesTabsEdit();
    }

    private void agregarNuevaTabEditDiagnostico() {
        if (tabsDiagDataEdit.size() >= MAX_PESTANAS_POR_SECCION) {
            JOptionPane.showMessageDialog(this, "Se permiten hasta " + MAX_PESTANAS_POR_SECCION + " diagnósticos por evolución.", "Límite alcanzado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        agregarTabEditDiagnostico(null, null);
    }

    private void agregarTabEditTratamiento(String med, String dosis, String frec, String ini, String fin, String est) {
        DatosTratamiento t = new DatosTratamiento();
        JPanel pnl = new JPanel(); pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS)); pnl.setOpaque(false); pnl.setBorder(new EmptyBorder(15, 10, 10, 10));
        t.cmbBuscar = new JComboBox<>(); t.cmbBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        t.txtDosis = crearCampoLimitado(30); t.txtDosis.setFont(new Font("Segoe UI", Font.PLAIN, 14)); t.txtDosis.setBorder(bordeSuaveInptus);
        t.cmbFrecuencia = new JComboBox<>(new String[]{"Seleccione frecuencia...", "Única vez", "Cada 4 horas", "Cada 6 horas", "Cada 8 horas", "Cada 12 horas", "Cada 24 horas", "Según necesidad"});
        t.dateInicio = new JDateChooser(); JTextFieldDateEditor editorIni = (JTextFieldDateEditor) t.dateInicio.getDateEditor(); editorIni.setEditable(false); editorIni.setBackground(Color.WHITE);
        t.dateFin = new JDateChooser(); JTextFieldDateEditor editorFin = (JTextFieldDateEditor) t.dateFin.getDateEditor(); editorFin.setEditable(false); editorFin.setBackground(Color.WHITE);
        t.dateInicio.getDateEditor().addPropertyChangeListener(e -> { if ("date".equals(e.getPropertyName()) && e.getNewValue() != null) { t.dateFin.setMinSelectableDate((java.util.Date) e.getNewValue()); } });
        t.cmbEstado = new JComboBox<>(new String[]{"Actual", "Suspendido", "Finalizado"});
        if (med != null && !med.equals("No especificado") && !med.isEmpty()) { if (!listaTratamientos.contains(med)) listaTratamientos.add(med); }
        pnl.add(crearBuscadorDinamico("Medicamento (opcional):", listaTratamientos, t.cmbBuscar,
            valor -> new CatalogoDAO().insertarMedicamento(valor, idEspecialidadMedicoActual), valor -> new CatalogoDAO().eliminarMedicamento(valor), (viejo, nuevo) -> new CatalogoDAO().editarMedicamento(viejo, nuevo),
            () -> new CatalogoDAO().obtenerMedicamentosInactivosPorEspecialidad(idEspecialidadMedicoActual), valor -> new CatalogoDAO().restaurarMedicamento(valor, idEspecialidadMedicoActual)
        ));
        if (med != null) t.cmbBuscar.setSelectedItem(med); if (dosis != null && !dosis.equals("-")) t.txtDosis.setText(dosis); if (frec != null && !frec.equals("-")) t.cmbFrecuencia.setSelectedItem(frec); if (ini != null && !ini.equals("-")) t.dateInicio.setDate(parsearFecha(ini)); if (fin != null && !fin.equals("-")) t.dateFin.setDate(parsearFecha(fin)); if (est != null && !est.equals("-")) t.cmbEstado.setSelectedItem(est);
        JPanel fila1 = new JPanel(new GridLayout(1, 2, 20, 0)); fila1.setOpaque(false); fila1.setAlignmentX(Component.LEFT_ALIGNMENT); fila1.setMaximumSize(new Dimension(900, 75));
        fila1.add(crearBloqueInput("Dosis (*Si indica tratamiento):", t.txtDosis)); fila1.add(crearBloqueInput("Frecuencia (*Si indica tratamiento):", t.cmbFrecuencia)); pnl.add(fila1);
        JPanel fila2 = new JPanel(new GridLayout(1, 3, 20, 0)); fila2.setOpaque(false); fila2.setAlignmentX(Component.LEFT_ALIGNMENT); fila2.setMaximumSize(new Dimension(900, 75));
        fila2.add(crearBloqueInput("Fecha Inicio (*Si indica tratamiento):", t.dateInicio)); fila2.add(crearBloqueInput("Fecha Fin (*Si indica tratamiento):", t.dateFin)); fila2.add(crearBloqueInput("Estado:", t.cmbEstado)); pnl.add(fila2);
        tabsTratDataEdit.add(t); ResponsiveUI.aplicar(pnl); tabTratamientosEdit.addTab("Tratamiento " + tabsTratDataEdit.size(), pnl); tabTratamientosEdit.setSelectedIndex(tabsTratDataEdit.size() - 1); actualizarBotonesTabsEdit();
    }

    private void agregarNuevaTabEditTratamiento() {
        if (tabsTratDataEdit.size() >= MAX_PESTANAS_POR_SECCION) {
            JOptionPane.showMessageDialog(this, "Se permiten hasta " + MAX_PESTANAS_POR_SECCION + " tratamientos por evolución.", "Límite alcanzado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        agregarTabEditTratamiento(null, null, null, null, null, null);
    }

    private void eliminarTabEditDiagnostico() {
        int indice = tabDiagnosticosEdit.getSelectedIndex();
        if (indice <= 0 || tabsDiagDataEdit.size() <= 1) return;
        tabDiagnosticosEdit.removeTabAt(indice);
        tabsDiagDataEdit.remove(indice);
        renumerarTabs(tabDiagnosticosEdit, "Diagnóstico");
        actualizarBotonesTabsEdit();
    }

    private void eliminarTabEditTratamiento() {
        int indice = tabTratamientosEdit.getSelectedIndex();
        if (indice <= 0 || tabsTratDataEdit.size() <= 1) return;
        tabTratamientosEdit.removeTabAt(indice);
        tabsTratDataEdit.remove(indice);
        renumerarTabs(tabTratamientosEdit, "Tratamiento");
        actualizarBotonesTabsEdit();
    }

    private void renumerarTabs(JTabbedPane tabs, String titulo) {
        for (int i = 0; i < tabs.getTabCount(); i++) tabs.setTitleAt(i, titulo + " " + (i + 1));
    }

    private void actualizarBotonesTabsEdit() {
        if (btnEliminarTabDiagEdit != null && tabDiagnosticosEdit != null) {
            btnEliminarTabDiagEdit.setEnabled(tabDiagnosticosEdit.getTabCount() > 1 && tabDiagnosticosEdit.getSelectedIndex() > 0);
            btnAgregarTabDiagEdit.setEnabled(tabDiagnosticosEdit.getTabCount() < MAX_PESTANAS_POR_SECCION);
        }
        if (btnEliminarTabTratEdit != null && tabTratamientosEdit != null) {
            btnEliminarTabTratEdit.setEnabled(tabTratamientosEdit.getTabCount() > 1 && tabTratamientosEdit.getSelectedIndex() > 0);
            btnAgregarTabTratEdit.setEnabled(tabTratamientosEdit.getTabCount() < MAX_PESTANAS_POR_SECCION);
        }
    }

    private void actualizarConsulta() {
        boolean hayError = false;
        for (DatosDiagnostico d : tabsDiagDataEdit) { d.cmbBuscar.setBorder(bordeSuaveInptus); }
        for (DatosTratamiento t : tabsTratDataEdit) { t.cmbBuscar.setBorder(bordeSuaveInptus); t.txtDosis.setBorder(bordeSuaveInptus); t.cmbFrecuencia.setBorder(bordeSuaveInptus); ((JTextFieldDateEditor)t.dateInicio.getDateEditor()).setBorder(BORDE_FECHA_NORMAL); ((JTextFieldDateEditor)t.dateFin.getDateEditor()).setBorder(BORDE_FECHA_NORMAL); }
        txtEvolucionEdit.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); String evolucion = txtEvolucionEdit.getText().trim();
        if (evolucion.isEmpty()) { txtEvolucionEdit.setBorder(BORDE_ERROR); hayError = true; }
        else if (evolucion.length() > 300) {
            txtEvolucionEdit.setBorder(BORDE_ERROR);
            JOptionPane.showMessageDialog(this, "La evolución supera los 300 caracteres. Reducila antes de guardar.", "Texto demasiado largo", JOptionPane.WARNING_MESSAGE);
            hayError = true;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        List<String> diagValidos = new ArrayList<>();
        for (int i = 0; i < tabsDiagDataEdit.size(); i++) {
            DatosDiagnostico d = tabsDiagDataEdit.get(i); String diag = d.cmbBuscar.getSelectedIndex() > 0 ? d.cmbBuscar.getSelectedItem().toString() : "";
            boolean isPestaniaVacia = diag.isEmpty();
            if (!isPestaniaVacia) {
                boolean err = false; if (diag.isEmpty()) { d.cmbBuscar.setBorder(BORDE_ERROR); err = true; }
                if (err) { hayError = true; if (!pnlDiagEdit.isVisible()) alternarAcordeon(pnlDiagEdit, pnlTratEdit, pnlArchivoEdit, btnDiagEdit, btnTratEdit, btnArchivoEdit, pnlContenedorEditar); tabDiagnosticosEdit.setSelectedIndex(i); }
                else { diagValidos.add("[" + seguroSerializado(diag) + " | Est: " + d.cmbEstado.getSelectedItem() + "]"); }
            }
        }
        List<String> tratValidos = new ArrayList<>();
        for (int i = 0; i < tabsTratDataEdit.size(); i++) {
            DatosTratamiento t = tabsTratDataEdit.get(i); String trat = t.cmbBuscar.getSelectedIndex() > 0 ? t.cmbBuscar.getSelectedItem().toString() : ""; String dosis = t.txtDosis.getText().trim();
            boolean isPestaniaVacia = trat.isEmpty() && dosis.isEmpty() && t.cmbFrecuencia.getSelectedIndex() == 0 && t.dateFin.getDate() == null;
            if (!isPestaniaVacia) {
                boolean err = false;
                if (trat.isEmpty()) { t.cmbBuscar.setBorder(BORDE_ERROR); err = true; } if (dosis.isEmpty()) { t.txtDosis.setBorder(BORDE_ERROR); err = true; } if (t.cmbFrecuencia.getSelectedIndex() == 0) { t.cmbFrecuencia.setBorder(BORDE_ERROR); err = true; } if (t.dateInicio.getDate() == null) { ((JTextFieldDateEditor)t.dateInicio.getDateEditor()).setBorder(BORDE_ERROR); err = true; } if (t.dateFin.getDate() == null) { ((JTextFieldDateEditor)t.dateFin.getDateEditor()).setBorder(BORDE_ERROR); err = true; }
                if (err) { hayError = true; if (!pnlTratEdit.isVisible()) alternarAcordeon(pnlTratEdit, pnlDiagEdit, pnlArchivoEdit, btnTratEdit, btnDiagEdit, btnArchivoEdit, pnlContenedorEditar); tabTratamientosEdit.setSelectedIndex(i); }
                else { String fIni = sdf.format(t.dateInicio.getDate()); String fFin = sdf.format(t.dateFin.getDate()); tratValidos.add("[" + seguroSerializado(trat) + " | Dosis: " + seguroSerializado(dosis) + " | Frec: " + t.cmbFrecuencia.getSelectedItem() + " | Inicio: " + fIni + " | Fin: " + fFin + " | Est: " + t.cmbEstado.getSelectedItem() + "]"); }
            }
        }
        if (hayError) { Toolkit.getDefaultToolkit().beep(); return; }
        String diagFinal = diagValidos.isEmpty() ? "No registrado" : String.join("  ||  ", diagValidos); String tratFinal = tratValidos.isEmpty() ? "No registrado" : String.join("  ||  ", tratValidos);
        int idConsulta = Integer.parseInt(datosConsultaActual[9]);
        String rutaAnterior = rutaArchivoOriginalEdit;
        String rutaNueva = rutaArchivoAdjuntoEdit;
        try { if (!archivosAdjuntosEdit.isEmpty()) rutaNueva = ArchivoAdjuntoService.copiar(archivosAdjuntosEdit); }
        catch (Exception e) { JOptionPane.showMessageDialog(this, "No se pudieron copiar los adjuntos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); return; }
        Usuario sesion = mainApp.getUsuarioSesion();
        Integer idPermitido = sesion != null && sesion.esMedico() ? sesion.getId_personal() : null;
        String resultado = new ConsultaDAO().actualizarConsultaSegura(idConsulta, evolucion, diagFinal, tratFinal, rutaNueva, idPermitido);
        if (resultado.equals("OK")) {
            if (!rutaAnterior.equals(rutaNueva)) ArchivoAdjuntoService.eliminar(rutaAnterior);
            JOptionPane.showMessageDialog(this, "¡La historia clínica ha sido actualizada con éxito!", "Actualización Guardada", JOptionPane.INFORMATION_MESSAGE); cargarHistorial(dniActual, nombreActual);
        } else {
            if (!rutaNueva.equals(rutaAnterior)) ArchivoAdjuntoService.eliminar(rutaNueva);
            JOptionPane.showMessageDialog(this, "Error en BD:\n" + resultado, "Reporte de Falla", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alternarAcordeon(JPanel pnlActivar, JPanel pnlOcultar1, JPanel pnlOcultar2, JButton btnActivar, JButton btnApagar1, JButton btnApagar2, JPanel contenedor) {
        boolean estabaVisible = pnlActivar.isVisible(); pnlActivar.setVisible(false); pnlOcultar1.setVisible(false); pnlOcultar2.setVisible(false);
        btnActivar.setBackground(Color.WHITE); btnActivar.setForeground(new Color(41, 128, 185)); btnApagar1.setBackground(Color.WHITE); btnApagar1.setForeground(new Color(41, 128, 185)); btnApagar2.setBackground(Color.WHITE); btnApagar2.setForeground(new Color(41, 128, 185));
        if (!estabaVisible) { pnlActivar.setVisible(true); btnActivar.setBackground(new Color(41, 128, 185)); btnActivar.setForeground(Color.WHITE); }
        if (contenedor == pnlContenedorDetalle) ajustarTarjetaDetalle();
        contenedor.revalidate(); contenedor.repaint();
        SwingUtilities.invokeLater(() -> {
            Container ancestro = SwingUtilities.getAncestorOfClass(JScrollPane.class, contenedor);
            if (ancestro instanceof JScrollPane scroll) scroll.getVerticalScrollBar().setValue(0);
        });
    }

    /** Mantiene el detalle como una tarjeta compacta y adapta su alto al acordeón abierto. */
    private void ajustarTarjetaDetalle() {
        if (pnlContenedorDetalle == null) return;
        Insets margen = pnlContenedorDetalle.getInsets();
        int alto = margen.top + margen.bottom;
        for (Component componente : pnlContenedorDetalle.getComponents()) {
            if (componente.isVisible()) alto += componente.getPreferredSize().height;
        }
        pnlContenedorDetalle.setPreferredSize(new Dimension(1000, Math.max(270, alto)));
        pnlContenedorDetalle.setMinimumSize(new Dimension(760, Math.max(270, alto)));
    }

    private java.util.Date parsearFecha(String str) { try { return new SimpleDateFormat("dd/MM/yyyy").parse(str); } catch (Exception e) { return new java.util.Date(); } }

    private JTextField crearCampoLimitado(int limite) {
        JTextField tf = new JTextField();
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) text = "";
                if (fb.getDocument().getLength() + text.length() - length <= limite) { super.replace(fb, offset, length, text, attrs); } else { Toolkit.getDefaultToolkit().beep(); }
            }
        }); return tf;
    }

    private JPanel crearBuscadorDinamico(String titulo, List<String> diccionario, JComboBox<String> cmbDestino, java.util.function.Function<String, Boolean> accionGuardarBD, java.util.function.Function<String, Boolean> accionEliminarBD, java.util.function.BiFunction<String, String, Boolean> accionEditarBD, java.util.function.Supplier<List<String>> accionListarOcultosBD, java.util.function.Function<String, Boolean> accionRestaurarBD) {
        JPanel pnlPrincipal = new JPanel(); pnlPrincipal.setLayout(new BoxLayout(pnlPrincipal, BoxLayout.Y_AXIS)); pnlPrincipal.setOpaque(false); pnlPrincipal.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(titulo); lbl.setFont(new Font("Segoe UI", Font.BOLD, 15)); lbl.setForeground(new Color(100, 110, 115)); pnlPrincipal.add(lbl); pnlPrincipal.add(Box.createVerticalStrut(4));
        JPanel pnlCards = new JPanel(new CardLayout()); pnlCards.setOpaque(false); pnlCards.setAlignmentX(Component.LEFT_ALIGNMENT); pnlCards.setMaximumSize(new Dimension(900, 45)); pnlCards.setPreferredSize(new Dimension(0, 45));
        final String[] valorAEditar = {null};
        JPanel pnlNormal = new JPanel(new BorderLayout(5, 0)); pnlNormal.setOpaque(false);
        cmbDestino.removeAllItems(); for(String item : diccionario) cmbDestino.addItem(item); EstiloRedondeado.aplicarComboBuscador(cmbDestino); cmbDestino.setFocusable(false); cmbDestino.setRequestFocusEnabled(false);
        JPanel pnlBotonesInline = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); pnlBotonesInline.setOpaque(false);
        JButton btnDelInline = new JButton("Ocultar"); btnDelInline.setFont(new Font("Segoe UI", Font.BOLD, 12)); btnDelInline.setBackground(new Color(231, 76, 60)); btnDelInline.setForeground(Color.WHITE); btnDelInline.setFocusPainted(false); btnDelInline.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnDelInline.setPreferredSize(new Dimension(95, 40));
        JButton btnAddInline = new JButton("Agregar"); btnAddInline.setFont(new Font("Segoe UI", Font.BOLD, 12)); btnAddInline.setBackground(new Color(41, 128, 185)); btnAddInline.setForeground(Color.WHITE); btnAddInline.setFocusPainted(false); btnAddInline.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnAddInline.setPreferredSize(new Dimension(95, 40));
        JButton btnRestaurarInline = new JButton("Restaurar"); btnRestaurarInline.setFont(new Font("Segoe UI", Font.BOLD, 12)); btnRestaurarInline.setBackground(new Color(46, 204, 113)); btnRestaurarInline.setForeground(Color.WHITE); btnRestaurarInline.setFocusPainted(false); btnRestaurarInline.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnRestaurarInline.setPreferredSize(new Dimension(115, 40));
        pnlBotonesInline.add(btnDelInline); pnlBotonesInline.add(btnAddInline); pnlBotonesInline.add(btnRestaurarInline);
        Usuario sesion = mainApp.getUsuarioSesion(); pnlBotonesInline.setVisible(sesion != null && (sesion.esAdministrador() || sesion.esMedico()));
        pnlNormal.add(cmbDestino, BorderLayout.CENTER); pnlNormal.add(pnlBotonesInline, BorderLayout.EAST);
        JPanel pnlNuevo = new JPanel(new BorderLayout(5, 0)); pnlNuevo.setOpaque(false);
        JTextField txtNuevo = new JTextField(); txtNuevo.setFont(new Font("Segoe UI", Font.PLAIN, 14)); txtNuevo.setBorder(bordeSuaveInptus);
        JPanel pnlBotonesAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); pnlBotonesAccion.setOpaque(false);
        JButton btnOk = new JButton("Guardar"); btnOk.setFont(new Font("Segoe UI", Font.BOLD, 12)); btnOk.setBackground(new Color(46, 204, 113)); btnOk.setForeground(Color.WHITE); btnOk.setFocusPainted(false); btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnOk.setPreferredSize(new Dimension(115, 40));
        JButton btnCancel = new JButton("Cancelar"); btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12)); btnCancel.setBackground(new Color(149, 165, 166)); btnCancel.setForeground(Color.WHITE); btnCancel.setFocusPainted(false); btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnCancel.setPreferredSize(new Dimension(115, 40));
        pnlBotonesAccion.add(btnOk); pnlBotonesAccion.add(btnCancel);
        pnlNuevo.add(txtNuevo, BorderLayout.CENTER); pnlNuevo.add(pnlBotonesAccion, BorderLayout.EAST);
        pnlCards.add(pnlNormal, "NORMAL"); pnlCards.add(pnlNuevo, "NUEVO");
        CardLayout cl = (CardLayout) pnlCards.getLayout();
        btnDelInline.addActionListener(e -> { if (!puedeGestionarCatalogos()) { accesoCatalogoDenegado(); return; } String sel = (String) cmbDestino.getSelectedItem(); if (sel != null && !sel.startsWith("Seleccione")) { if (JOptionPane.showConfirmDialog(this, "¿Seguro de ocultar '" + sel + "' del catálogo para futuras atenciones?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { boolean ok = accionEliminarBD != null && Boolean.TRUE.equals(accionEliminarBD.apply(sel)); if (ok) { diccionario.remove(sel); cmbDestino.removeItem(sel); } else JOptionPane.showMessageDialog(this, "No se pudo ocultar el elemento.", "Error", JOptionPane.ERROR_MESSAGE); } } });
        btnAddInline.addActionListener(e -> { if (!puedeGestionarCatalogos()) { accesoCatalogoDenegado(); return; } valorAEditar[0] = null; txtNuevo.setText(""); txtNuevo.setBorder(bordeSuaveInptus); cl.show(pnlCards, "NUEVO"); txtNuevo.requestFocus(); });
        btnRestaurarInline.addActionListener(e -> { if (!puedeGestionarCatalogos()) { accesoCatalogoDenegado(); return; } restaurarElementoCatalogo(diccionario, cmbDestino, accionListarOcultosBD, accionRestaurarBD); });
        btnCancel.addActionListener(e -> cl.show(pnlCards, "NORMAL"));
        btnOk.addActionListener(e -> { String valor = txtNuevo.getText().trim().toUpperCase(); if(!valor.isEmpty()) { if (valorAEditar[0] != null) { String viejo = valorAEditar[0]; boolean ok = accionEditarBD != null && Boolean.TRUE.equals(accionEditarBD.apply(viejo, valor)); if (!ok) { JOptionPane.showMessageDialog(this, "No se pudo editar el catálogo.", "Error", JOptionPane.ERROR_MESSAGE); return; } int idx = diccionario.indexOf(viejo); if (idx != -1) diccionario.set(idx, valor); cmbDestino.removeItem(viejo); cmbDestino.addItem(valor); cmbDestino.setSelectedItem(valor); } else { boolean ok = accionGuardarBD != null && Boolean.TRUE.equals(accionGuardarBD.apply(valor)); if (!ok) { JOptionPane.showMessageDialog(this, "No se pudo agregar el elemento.", "Error", JOptionPane.ERROR_MESSAGE); return; } diccionario.add(valor); cmbDestino.addItem(valor); cmbDestino.setSelectedItem(valor); } cl.show(pnlCards, "NORMAL"); } else { txtNuevo.setBorder(BORDE_ERROR); } });
        pnlPrincipal.add(pnlCards); pnlPrincipal.add(Box.createVerticalStrut(10)); return pnlPrincipal;
    }

    private void restaurarElementoCatalogo(List<String> diccionario, JComboBox<String> cmbDestino, java.util.function.Supplier<List<String>> accionListarOcultosBD, java.util.function.Function<String, Boolean> accionRestaurarBD) {
        List<String> ocultos = accionListarOcultosBD == null ? java.util.Collections.emptyList() : accionListarOcultosBD.get();
        if (ocultos == null || ocultos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay elementos ocultos para restaurar.", "Restaurar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String seleccionado = seleccionarElementoParaRestaurar(ocultos);
        if (seleccionado == null || seleccionado.isBlank()) return;
        boolean restaurado = accionRestaurarBD != null && Boolean.TRUE.equals(accionRestaurarBD.apply(seleccionado));
        if (!restaurado) {
            JOptionPane.showMessageDialog(this, "No se pudo restaurar el elemento.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!diccionario.contains(seleccionado)) diccionario.add(seleccionado);
        cmbDestino.addItem(seleccionado);
        cmbDestino.setSelectedItem(seleccionado);
        JOptionPane.showMessageDialog(this, "Elemento restaurado correctamente.", "Restaurado", JOptionPane.INFORMATION_MESSAGE);
    }

    private String seleccionarElementoParaRestaurar(List<String> ocultos) {
        JTextField campoSeleccionado = new JTextField(ocultos.get(0));
        campoSeleccionado.setEditable(false);
        campoSeleccionado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campoSeleccionado.setBackground(Color.WHITE);
        campoSeleccionado.setBorder(BorderFactory.createCompoundBorder(
                EstiloRedondeado.borde(new Color(80, 150, 240), 2),
                new EmptyBorder(6, 8, 6, 8)));
        campoSeleccionado.setMaximumSize(new Dimension(300, 36));

        JList<String> lista = new JList<>(ocultos.toArray(new String[0]));
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setSelectedIndex(0);
        lista.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lista.setFixedCellHeight(30);
        lista.setBackground(new Color(245, 245, 245));
        lista.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && lista.getSelectedValue() != null) {
                campoSeleccionado.setText(lista.getSelectedValue());
            }
        });

        JScrollPane scrollLista = new JScrollPane(lista);
        scrollLista.setBorder(EstiloRedondeado.borde(new Color(190, 190, 190), 1));
        scrollLista.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollLista.setPreferredSize(new Dimension(300, Math.min(150, Math.max(60, ocultos.size() * 30))));
        scrollLista.setMaximumSize(new Dimension(300, 150));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(5, 5, 0, 5));

        JLabel lbl = new JLabel("Seleccione el elemento a restaurar:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        campoSeleccionado.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lbl);
        panel.add(Box.createVerticalStrut(8));
        panel.add(campoSeleccionado);
        panel.add(Box.createVerticalStrut(4));
        panel.add(scrollLista);
        panel.setPreferredSize(new Dimension(320, 130 + scrollLista.getPreferredSize().height));

        int opcion = JOptionPane.showConfirmDialog(this, panel, "Restaurar elemento",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return opcion == JOptionPane.OK_OPTION ? lista.getSelectedValue() : null;
    }

    private void abrirPantallaDetalle(int fila) {
        if (fila < 0 || fila >= historialCompleto.size()) return;
        datosConsultaActual = historialCompleto.get(fila);
        String fecha = datosConsultaActual[0]; String medico = datosConsultaActual[2]; String evolucion = (datosConsultaActual[3] != null && !datosConsultaActual[3].isEmpty()) ? datosConsultaActual[3] : "Sin evolución registrada."; String diagnosticoCrudo = (datosConsultaActual[4] != null && !datosConsultaActual[4].isEmpty()) ? datosConsultaActual[4] : "No registrada"; String tratamientoCrudo = (datosConsultaActual[5] != null && !datosConsultaActual[5].isEmpty()) ? datosConsultaActual[5] : "No registrado"; String rutaArchivo = datosConsultaActual[8];
        lblSubtituloDetalle.setText("Paciente: " + nombreActual + " | Fecha: " + fecha + " | Atendido por: " + medico);
        txtDetalleEvolucion.setText(evolucion); txtDetalleEvolucion.setCaretPosition(0);
        tabDiagnosticosDetalle.removeAll();
        if (diagnosticoCrudo.equals("No registrado") || diagnosticoCrudo.equals("No registrada")) { tabDiagnosticosDetalle.addTab("Diagnóstico", crearPanelVacio("Sin diagnósticos registrados.")); }
        else { String[] diags = diagnosticoCrudo.split("\\|\\|"); int count = 1; for (String d : diags) { tabDiagnosticosDetalle.addTab("Diagnóstico " + count++, parsearYCrearPanelDiag(d.trim())); } }
        tabTratamientosDetalle.removeAll();
        if (tratamientoCrudo.equals("No registrado")) { tabTratamientosDetalle.addTab("Tratamiento", crearPanelVacio("Sin tratamientos registrados.")); }
        else { String[] trats = tratamientoCrudo.split("\\|\\|"); int count = 1; for (String t : trats) { tabTratamientosDetalle.addTab("Tratamiento " + count++, parsearYCrearPanelTrat(t.trim())); } }
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty() || rutaArchivo.equals("No registrado")) { btnAbrirAdjunto.setEnabled(false); btnAbrirAdjunto.setText("Sin Estudios Adjuntos"); btnAbrirAdjunto.setForeground(Color.GRAY); btnAbrirAdjunto.setBorder(EstiloRedondeado.borde(Color.LIGHT_GRAY, 1)); lblRutaAdjunto.setText("   No hay documentos guardados en esta historia clínica."); }
        else { btnAbrirAdjunto.setEnabled(true); String[] cantArchivos = separarRutasAdjuntos(rutaArchivo); btnAbrirAdjunto.setText("Ver / Descargar Archivo(s)"); btnAbrirAdjunto.setForeground(new Color(41, 128, 185)); btnAbrirAdjunto.setBorder(EstiloRedondeado.borde(new Color(41, 128, 185), 1)); lblRutaAdjunto.setText("   " + cantArchivos.length + " documento(s) disponible(s)."); }
        pnlDiagDetalle.setVisible(false); pnlTratDetalle.setVisible(false); pnlArchivoDetalle.setVisible(false);
        btnDiagDetalle.setBackground(Color.WHITE); btnDiagDetalle.setForeground(new Color(41, 128, 185)); btnTratDetalle.setBackground(Color.WHITE); btnTratDetalle.setForeground(new Color(41, 128, 185)); btnArchivoDetalle.setBackground(Color.WHITE); btnArchivoDetalle.setForeground(new Color(41, 128, 185));
        ajustarTarjetaDetalle();
        cardLayout.show(this, "VISTA_DETALLE");
    }

    private JPanel parsearYCrearPanelDiag(String crudo) {
        String limpio = crudo.replace("[", "").replace("]", ""); String[] partes = limpio.split(" \\| "); String diag = "No especificado", est = "-";
        if(partes.length > 0) diag = partes[0]; for(String p : partes) { if(p.startsWith("Est: ")) est = p.replace("Est: ", ""); if(p.startsWith("Estado: ")) est = p.replace("Estado: ", ""); }
        JPanel pnl = new JPanel(); pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS)); pnl.setOpaque(false); pnl.setBorder(new EmptyBorder(15, 10, 10, 10));
        JTextField txtDiag = crearInputLectura(); txtDiag.setText(diag); JTextField txtEst = crearInputLectura(); txtEst.setText(est);
        pnl.add(crearBloqueInput("Diagnóstico Registrado:", txtDiag)); JPanel filaF = new JPanel(new GridLayout(1, 1, 20, 0)); filaF.setOpaque(false); filaF.setAlignmentX(Component.LEFT_ALIGNMENT); filaF.setMaximumSize(new Dimension(900, 75)); filaF.add(crearBloqueInput("Estado:", txtEst)); pnl.add(filaF); return pnl;
    }

    private JPanel parsearYCrearPanelTrat(String crudo) {
        String limpio = crudo.replace("[", "").replace("]", ""); String[] partes = limpio.split(" \\| "); String med = "No especificado", dosis = "-", frec = "-", ini = "-", fin = "-", est = "-";
        if(partes.length > 0) med = partes[0]; for(String p : partes) { if(p.startsWith("Dosis: ")) dosis = p.replace("Dosis: ", ""); if(p.startsWith("Frec: ")) frec = p.replace("Frec: ", ""); if(p.startsWith("Inicio: ")) ini = p.replace("Inicio: ", ""); if(p.startsWith("Fin: ")) fin = p.replace("Fin: ", ""); if(p.startsWith("Est: ")) est = p.replace("Est: ", ""); if(p.startsWith("Estado: ")) est = p.replace("Estado: ", ""); }
        JPanel pnl = new JPanel(); pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS)); pnl.setOpaque(false); pnl.setBorder(new EmptyBorder(15, 10, 10, 10));
        JTextField txtMed = crearInputLectura(); txtMed.setText(med); JTextField txtDosis = crearInputLectura(); txtDosis.setText(dosis); JTextField txtFrec = crearInputLectura(); txtFrec.setText(frec); JTextField txtIni = crearInputLectura(); txtIni.setText(ini); JTextField txtFin = crearInputLectura(); txtFin.setText(fin); JTextField txtEst = crearInputLectura(); txtEst.setText(est);
        pnl.add(crearBloqueInput("Medicamento / Acción:", txtMed)); JPanel fila1 = new JPanel(new GridLayout(1, 2, 20, 0)); fila1.setOpaque(false); fila1.setAlignmentX(Component.LEFT_ALIGNMENT); fila1.setMaximumSize(new Dimension(900, 75)); fila1.add(crearBloqueInput("Dosis:", txtDosis)); fila1.add(crearBloqueInput("Frecuencia:", txtFrec)); pnl.add(fila1); JPanel fila2 = new JPanel(new GridLayout(1, 3, 20, 0)); fila2.setOpaque(false); fila2.setAlignmentX(Component.LEFT_ALIGNMENT); fila2.setMaximumSize(new Dimension(900, 75)); fila2.add(crearBloqueInput("Fecha Inicio:", txtIni)); fila2.add(crearBloqueInput("Fecha Fin:", txtFin)); fila2.add(crearBloqueInput("Estado:", txtEst)); pnl.add(fila2); return pnl;
    }

    private JButton crearBotonToggle(String texto) { JButton btn = new JButton(texto); btn.setFont(new Font("Segoe UI", Font.BOLD, 13)); btn.setBackground(Color.WHITE); btn.setForeground(new Color(41, 128, 185)); btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn; }

    private JPanel crearBloqueInput(String titulo, JComponent comp) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false); p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(titulo); lbl.setFont(new Font("Segoe UI", Font.BOLD, 15)); lbl.setForeground(new Color(100, 110, 115)); p.add(lbl); p.add(Box.createVerticalStrut(4));
        comp.setAlignmentX(Component.LEFT_ALIGNMENT); comp.setMaximumSize(new Dimension(900, 42)); comp.setPreferredSize(new Dimension(0, 42));
        if (comp instanceof JDateChooser) comp.setFont(new Font("Segoe UI", Font.PLAIN, 14)); else if (comp instanceof JComboBox || comp instanceof JTextField) { comp.setFont(new Font("Segoe UI", Font.PLAIN, 14)); comp.setBackground(Color.WHITE); comp.setBorder(bordeSuaveInptus); }
        p.add(comp); p.add(Box.createVerticalStrut(8)); return p;
    }

    private JTextField crearInputLectura() { JTextField txt = new JTextField(); txt.setEditable(false); txt.setFont(new Font("Segoe UI", Font.PLAIN, 15)); txt.setForeground(new Color(50, 50, 50)); txt.setBackground(new Color(250, 251, 252)); txt.setBorder(BorderFactory.createCompoundBorder(EstiloRedondeado.borde(new Color(215, 220, 225), 1), new EmptyBorder(6, 12, 6, 12))); return txt; }

    private JPanel crearPanelVacio(String mensaje) { JPanel pnl = new JPanel(new BorderLayout()); pnl.setOpaque(false); JLabel lbl = new JLabel(mensaje); lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14)); lbl.setForeground(Color.GRAY); lbl.setHorizontalAlignment(JLabel.CENTER); pnl.add(lbl, BorderLayout.CENTER); return pnl; }

    private void abrirArchivosInteligente() {
        if (datosConsultaActual == null) return;
        String rutasStr = datosConsultaActual[8];
        if (rutasStr == null || rutasStr.trim().isEmpty() || rutasStr.equals("No registrado")) return;

        String[] rutas = separarRutasAdjuntos(rutasStr);
        JPopupMenu popup = new JPopupMenu();
        for (String r : rutas) {
            String ruta = r.trim();
            if (ruta.isEmpty()) continue;
            File f = ArchivoAdjuntoService.resolver(ruta);
            String nombre = f == null ? "Archivo" : f.getName();

            JMenuItem abrir = new JMenuItem("Abrir: " + nombre);
            abrir.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            abrir.setCursor(new Cursor(Cursor.HAND_CURSOR));
            abrir.addActionListener(e -> abrirFisico(ruta));
            popup.add(abrir);

            JMenuItem descargar = new JMenuItem("Descargar: " + nombre);
            descargar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            descargar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            descargar.addActionListener(e -> descargarFisico(ruta));
            popup.add(descargar);
            popup.addSeparator();
        }
        if (popup.getComponentCount() > 0) popup.show(btnAbrirAdjunto, 0, -popup.getPreferredSize().height);
    }

    private void abrirFisico(String ruta) {
        try {
            File archivo = ArchivoAdjuntoService.resolver(ruta);
            if (archivo != null && archivo.exists()) {
                Desktop.getDesktop().open(archivo);
            } else {
                JOptionPane.showMessageDialog(this, "El archivo ya no se encuentra disponible.", "Archivo Inaccesible", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al intentar abrir el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void descargarFisico(String ruta) {
        try {
            File origen = ArchivoAdjuntoService.resolver(ruta);
            if (origen == null || !origen.exists()) {
                JOptionPane.showMessageDialog(this, "El archivo ya no se encuentra disponible.", "Archivo Inaccesible", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(origen.getName()));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            Files.copy(origen.toPath(), chooser.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(this, "Archivo descargado correctamente.", "Descarga completa", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo descargar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String[] separarRutasAdjuntos(String rutas) {
        if (rutas == null || rutas.trim().isEmpty()) return new String[0];
        return rutas.trim().split("\\s*\\|\\s*");
    }

    private void irANuevaConsulta() { if (dniActual != null && !dniActual.isEmpty()) { mainApp.irANuevaConsulta(dniActual); } else { JOptionPane.showMessageDialog(this, "Error: No se ha seleccionado un paciente válido.", "Atención", JOptionPane.WARNING_MESSAGE); } }

    private boolean puedeEditarConsulta(int fila) {
        Usuario sesion = mainApp.getUsuarioSesion();
        if (sesion == null || fila < 0 || fila >= historialCompleto.size()) return false;
        if (sesion.esAdministrador()) return true;
        if (!sesion.esMedico() || sesion.getId_personal() <= 0) return false;

        String[] consulta = historialCompleto.get(fila);
        if (consulta.length <= 10 || consulta[10] == null) return false;
        try {
            return sesion.getId_personal() == Integer.parseInt(consulta[10]);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void abrirHistoriaCompleta() {
        lblHistoriaCompletaPaciente.setText("Paciente: " + nombreActual + "  |  DNI: " + dniActual);
        pnlHistoriaCompletaRegistros.removeAll();
        seleccionHistoriaCompleta.clear();
        if (historialCompleto.isEmpty()) {
            JLabel vacio = new JLabel("El paciente todavía no posee evoluciones activas registradas.");
            vacio.setFont(new Font("Segoe UI", Font.ITALIC, 15));
            vacio.setForeground(Color.GRAY);
            vacio.setBorder(new EmptyBorder(20, 15, 20, 15));
            GridBagConstraints vacioGbc = new GridBagConstraints();
            vacioGbc.gridx = 0; vacioGbc.gridy = 0; vacioGbc.weightx = 1; vacioGbc.weighty = 1;
            vacioGbc.anchor = GridBagConstraints.NORTHWEST;
            pnlHistoriaCompletaRegistros.add(vacio, vacioGbc);
        } else {
            for (int i = 0; i < historialCompleto.size(); i++) {
                GridBagConstraints tarjetaGbc = new GridBagConstraints();
                tarjetaGbc.gridx = i % 2; tarjetaGbc.gridy = i / 2;
                tarjetaGbc.weightx = 0.5; tarjetaGbc.weighty = 0;
                tarjetaGbc.fill = GridBagConstraints.HORIZONTAL; tarjetaGbc.anchor = GridBagConstraints.NORTH;
                tarjetaGbc.insets = new Insets(0, i % 2 == 0 ? 0 : 6, 12, i % 2 == 0 ? 6 : 0);
                pnlHistoriaCompletaRegistros.add(crearTarjetaHistoriaCompleta(historialCompleto.get(i), i), tarjetaGbc);
            }
            GridBagConstraints relleno = new GridBagConstraints();
            relleno.gridx = 0; relleno.gridy = (historialCompleto.size() + 1) / 2;
            relleno.gridwidth = 2; relleno.weightx = 1; relleno.weighty = 1; relleno.fill = GridBagConstraints.BOTH;
            pnlHistoriaCompletaRegistros.add(Box.createGlue(), relleno);
        }
        pnlHistoriaCompletaRegistros.revalidate();
        pnlHistoriaCompletaRegistros.repaint();
        cardLayout.show(this, "VISTA_COMPLETA");
    }

    private void marcarHistorias(boolean seleccionadas) {
        for (JCheckBox check : seleccionHistoriaCompleta) check.setSelected(seleccionadas);
    }

    private JPanel crearTarjetaHistoriaCompleta(String[] registro, int indice) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.setPreferredSize(new Dimension(470, 390));
        tarjeta.setMinimumSize(new Dimension(420, 390));
        tarjeta.setBorder(EstiloRedondeado.borde(new Color(215, 220, 225)));

        JPanel cabecera = new JPanel(new GridBagLayout());
        cabecera.setBackground(new Color(232, 245, 243));
        cabecera.setBorder(new EmptyBorder(10, 14, 10, 14));
        JLabel titulo = new JLabel(valor(registro, 0, "Sin fecha") + " — " + tituloClinico(registro));
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titulo.setForeground(new Color(0, 105, 92));
        // El título puede ceder ancho; el control de exportación debe verse completo.
        titulo.setMinimumSize(new Dimension(0, 24));
        JCheckBox incluir = new JCheckBox("Incluir en PDF", true);
        incluir.setOpaque(false);
        incluir.setFont(new Font("Segoe UI", Font.BOLD, 13));
        incluir.setPreferredSize(new Dimension(135, 26));
        incluir.setMinimumSize(new Dimension(135, 26));
        incluir.setHorizontalAlignment(SwingConstants.RIGHT);
        incluir.setToolTipText("Desmarcá esta evolución si no querés incluirla en el PDF.");
        seleccionHistoriaCompleta.add(incluir);
        GridBagConstraints cabeceraGbc = new GridBagConstraints();
        cabeceraGbc.gridx = 0; cabeceraGbc.gridy = 0; cabeceraGbc.weightx = 1;
        cabeceraGbc.fill = GridBagConstraints.HORIZONTAL; cabeceraGbc.anchor = GridBagConstraints.WEST;
        cabecera.add(titulo, cabeceraGbc);
        cabeceraGbc.gridx = 1; cabeceraGbc.weightx = 0; cabeceraGbc.fill = GridBagConstraints.NONE;
        cabeceraGbc.anchor = GridBagConstraints.EAST; cabeceraGbc.insets = new Insets(0, 8, 0, 0);
        cabecera.add(incluir, cabeceraGbc);
        tarjeta.add(cabecera, BorderLayout.NORTH);

        JPanel datos = new JPanel(new GridBagLayout());
        datos.setBackground(Color.WHITE);
        datos.setBorder(new EmptyBorder(12, 16, 14, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        agregarCampoHistoria(datos, gbc, "Profesional", valor(registro, 2, "No informado"));
        agregarCampoHistoria(datos, gbc, "Evolución", valor(registro, 3, "Sin evolución registrada"));
        agregarCampoHistoria(datos, gbc, "Diagnósticos", limpiarListado(valor(registro, 4, "No registrado")));
        agregarCampoHistoria(datos, gbc, "Tratamientos", limpiarListado(valor(registro, 5, "No registrado")));
        agregarCampoHistoria(datos, gbc, "Estudios / antecedentes", valor(registro, 6, "No registrado"));
        agregarCampoHistoria(datos, gbc, "Próxima cita", valor(registro, 7, "No registrada"));
        agregarCampoHistoria(datos, gbc, "Adjuntos", nombresAdjuntos(valor(registro, 8, "")));
        gbc.gridx = 0; gbc.gridwidth = 2; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        panelRelleno(datos, gbc);
        tarjeta.add(datos, BorderLayout.CENTER);
        return tarjeta;
    }

    private String tituloClinico(String[] registro) {
        String practica = valor(registro, 1, "Consulta oftalmológica");
        String normalizada = practica.toLowerCase();
        if (normalizada.contains("demostr")) return "Consulta oftalmológica";
        if (normalizada.contains("registro") || normalizada.contains("regulariz")) return "Atención clínica";
        return practica.length() <= 28 ? practica : "Consulta oftalmológica";
    }

    private void panelRelleno(JPanel panel, GridBagConstraints gbc) {
        panel.add(Box.createGlue(), gbc);
        gbc.gridwidth = 1;
    }

    private void agregarCampoHistoria(JPanel panel, GridBagConstraints gbc, String etiqueta, String contenido) {
        gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(3, 0, 3, 18);
        JLabel nombre = new JLabel(etiqueta + ":");
        nombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nombre.setForeground(new Color(85, 96, 101));
        panel.add(nombre, gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextArea valor = new JTextArea(contenido == null ? "" : contenido);
        valor.setEditable(false); valor.setOpaque(false); valor.setLineWrap(true); valor.setWrapStyleWord(true);
        valor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valor.setBorder(null);
        panel.add(valor, gbc);
        gbc.gridy++;
    }

    private String valor(String[] registro, int indice, String porDefecto) {
        if (registro.length <= indice || registro[indice] == null || registro[indice].trim().isEmpty()) return porDefecto;
        return registro[indice].trim();
    }

    private String limpiarListado(String valor) {
        return valor.replace("  ||  ", "\n").replace("||", "\n").replace("[", "").replace("]", "");
    }

    private String nombresAdjuntos(String rutas) {
        if (rutas == null || rutas.trim().isEmpty() || rutas.equalsIgnoreCase("No registrado")) return "Sin archivos adjuntos";
        String[] partes = rutas.split("\\s*\\|\\s*");
        List<String> nombres = new ArrayList<>();
        for (String ruta : partes) nombres.add(new File(ruta.trim()).getName());
        return String.join(", ", nombres);
    }

    private static String seguroSerializado(String valor) {
        return valor == null ? "" : valor.replace("|", "¦").replace("[", "(").replace("]", ")");
    }

    private boolean puedeGestionarCatalogos() {
        Usuario sesion = mainApp.getUsuarioSesion();
        return sesion != null && (sesion.esAdministrador() || sesion.esMedico());
    }

    private int obtenerIdEspecialidadSesion() {
        Usuario sesion = mainApp.getUsuarioSesion();
        return sesion != null && sesion.esMedico() ? sesion.getIdEspecialidad() : 0;
    }

    private void accesoCatalogoDenegado() {
        JOptionPane.showMessageDialog(this, "Solo los médicos y administradores pueden modificar los catálogos clínicos.", "Acceso denegado", JOptionPane.WARNING_MESSAGE);
    }

    public void cargarHistorial(String dni, String nombre) {
        this.dniActual = dni; this.nombreActual = nombre; lblNombreDni.setText("Paciente: " + nombre + "  |  DNI: " + dni); modelo.setRowCount(0); cardLayout.show(this, "VISTA_TABLA");
        historialCompleto.clear();
        paginaHistorialActual = 1;
        actualizarControlesPaginacionHistorial();
        tabla.setEnabled(false);
        final int token = ++historialCargaToken;
        Usuario sesion = mainApp.getUsuarioSesion();
        Integer idMedico = sesion != null && sesion.esMedico() ? sesion.getId_personal() : null;
        new SwingWorker<List<String[]>, Void>() {
            @Override protected List<String[]> doInBackground() { return new ConsultaDAO().obtenerHistorialPorDni(dni, idMedico); }
            @Override protected void done() {
                if (token != historialCargaToken) return;
                try {
                    historialCompleto.addAll(get());
                    paginaHistorialActual = 1;
                    cargarPaginaHistorial();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(VerHistoriaMedicaPanel.this, "Error al cargar historial: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally { tabla.setEnabled(true); }
            }
        }.execute();
    }

    private void exportarConsultaActual() { if (datosConsultaActual != null) { try { generarPDFDetalleConsulta(datosConsultaActual); } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error al leer los datos para el PDF: " + e.getMessage()); } } }

    private void exportarHistoriaCompleta() {
        List<String[]> seleccionadas = new ArrayList<>();
        for (int i = 0; i < seleccionHistoriaCompleta.size() && i < historialCompleto.size(); i++) {
            if (seleccionHistoriaCompleta.get(i).isSelected()) seleccionadas.add(historialCompleto.get(i));
        }
        if (seleccionadas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay evoluciones para exportar.", "Historia clínica vacía", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar historia clínica completa");
        selector.setFileFilter(new FileNameExtensionFilter("Documento PDF", "pdf"));
        selector.setSelectedFile(new File("HistoriaClinicaCompleta_" + nombreActual.replaceAll("[^a-zA-Z0-9]+", "_") + ".pdf"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File destino = selector.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".pdf")) destino = new File(destino.getAbsolutePath() + ".pdf");
        try {
            HistoriaClinicaPdfExporter.exportar(destino, nombreActual, dniActual, seleccionadas);
            JOptionPane.showMessageDialog(this, "Historia clínica completa exportada correctamente.", "PDF generado", JOptionPane.INFORMATION_MESSAGE);
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(destino);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo generar el PDF:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generarPDFDetalleConsulta(String[] datos) {
        JFileChooser fileChooser = new JFileChooser(); fileChooser.setDialogTitle("Guardar Informe de Historia Clínica"); fileChooser.setSelectedFile(new java.io.File("HistoriaClinica_" + nombreActual.replace(" ", "_") + "_" + datos[0].replace("/", "-") + ".pdf"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String ruta = fileChooser.getSelectedFile().getAbsolutePath(); if (!ruta.endsWith(".pdf")) ruta += ".pdf";
            com.itextpdf.text.Document documento = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 36, 80);
            try {
                com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(ruta)); writer.setPageEvent(new EventoFooterFirma()); documento.open();
                com.itextpdf.text.Font fontTitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 22, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(0, 105, 92)); com.itextpdf.text.Font fontSubtitulo = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.ITALIC, com.itextpdf.text.BaseColor.GRAY); com.itextpdf.text.Font fontSeccion = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(0, 105, 92)); com.itextpdf.text.Font fontNormal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.DARK_GRAY); com.itextpdf.text.Font fontBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.BLACK);
                com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("CLÍNICA REY SALUD", fontTitulo); titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER); documento.add(titulo);
                com.itextpdf.text.Paragraph tipoDoc = new com.itextpdf.text.Paragraph("REPORTE DE HISTORIA CLÍNICA", fontSubtitulo); tipoDoc.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER); documento.add(tipoDoc);
                documento.add(new com.itextpdf.text.Paragraph("\n")); documento.add(new com.itextpdf.text.pdf.draw.LineSeparator(0.5f, 100f, com.itextpdf.text.BaseColor.LIGHT_GRAY, com.itextpdf.text.Element.ALIGN_CENTER, -1)); documento.add(new com.itextpdf.text.Paragraph("\n"));
                com.itextpdf.text.pdf.PdfPTable tablaInfo = new com.itextpdf.text.pdf.PdfPTable(2); tablaInfo.setWidthPercentage(100); tablaInfo.setSpacingBefore(10f); tablaInfo.setSpacingAfter(20f);
                com.itextpdf.text.pdf.PdfPCell celdaIzq = new com.itextpdf.text.pdf.PdfPCell(); celdaIzq.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); celdaIzq.addElement(new com.itextpdf.text.Paragraph("Paciente: " + nombreActual, fontBold)); celdaIzq.addElement(new com.itextpdf.text.Paragraph("DNI: " + dniActual, fontNormal)); tablaInfo.addCell(celdaIzq);
                com.itextpdf.text.pdf.PdfPCell celdaDer = new com.itextpdf.text.pdf.PdfPCell(); celdaDer.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); celdaDer.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT); com.itextpdf.text.Paragraph pFecha = new com.itextpdf.text.Paragraph("Fecha: " + datos[0], fontBold); pFecha.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT); celdaDer.addElement(pFecha); com.itextpdf.text.Paragraph pMed = new com.itextpdf.text.Paragraph("Médico: " + datos[2], fontNormal); pMed.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT); celdaDer.addElement(pMed); tablaInfo.addCell(celdaDer);
                documento.add(tablaInfo);
                agregarCabeceraSeccion(documento, "EVOLUCIÓN CLÍNICA DETALLADA", fontSeccion); com.itextpdf.text.Paragraph pEvo = new com.itextpdf.text.Paragraph(datos[3] == null ? "No registrado" : datos[3], fontNormal); pEvo.setSpacingBefore(5f); pEvo.setSpacingAfter(15f); documento.add(pEvo);
                agregarCabeceraSeccion(documento, "DIAGNÓSTICOS HALLADOS", fontSeccion); agregarListadoEnColumnasPDF(documento, datos[4], "Diagnóstico", fontBold, fontNormal);
                agregarCabeceraSeccion(documento, "TRATAMIENTOS E INDICACIONES", fontSeccion); agregarListadoEnColumnasPDF(documento, datos[5], "Medicamento", fontBold, fontNormal);
                documento.close(); JOptionPane.showMessageDialog(this, "PDF generado con éxito. Diseño a doble columna aplicado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                try { java.awt.Desktop.getDesktop().open(new java.io.File(ruta)); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "El PDF se generó, pero no se pudo abrir automáticamente: " + ex.getMessage(), "PDF generado", JOptionPane.INFORMATION_MESSAGE); }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error al crear PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void agregarCabeceraSeccion(com.itextpdf.text.Document doc, String titulo, com.itextpdf.text.Font fontTit) throws Exception {
        com.itextpdf.text.pdf.PdfPTable tabla = new com.itextpdf.text.pdf.PdfPTable(1); tabla.setWidthPercentage(100); tabla.setSpacingBefore(10f);
        com.itextpdf.text.pdf.PdfPCell celda = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(titulo, fontTit)); celda.setBorder(com.itextpdf.text.Rectangle.BOTTOM); celda.setBorderColorBottom(new com.itextpdf.text.BaseColor(200, 200, 200)); celda.setBorderWidthBottom(1f); celda.setPaddingBottom(5f); celda.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_LEFT); tabla.addCell(celda); doc.add(tabla);
    }

    private void agregarListadoEnColumnasPDF(com.itextpdf.text.Document doc, String contenidoBruto, String etiquetaPrincipal, com.itextpdf.text.Font fontBold, com.itextpdf.text.Font fontNormal) throws Exception {
        if (contenidoBruto == null || contenidoBruto.trim().isEmpty() || contenidoBruto.equals("No registrado") || contenidoBruto.equals("No registrada")) { com.itextpdf.text.Paragraph pCont = new com.itextpdf.text.Paragraph("No registrado", fontNormal); pCont.setSpacingBefore(5f); pCont.setSpacingAfter(15f); doc.add(pCont); return; }
        String[] elementos = contenidoBruto.split("\\|\\|"); com.itextpdf.text.pdf.PdfPTable tablaContenedor = new com.itextpdf.text.pdf.PdfPTable(2); tablaContenedor.setWidthPercentage(100); tablaContenedor.setWidths(new float[]{50f, 50f}); tablaContenedor.setSpacingBefore(10f); tablaContenedor.setSpacingAfter(10f);
        for (int i = 0; i < elementos.length; i++) {
            String elemento = elementos[i]; String limpio = elemento.replace("[", "").replace("]", "").trim(); String[] partes = limpio.split(" \\| "); if (partes.length == 0) continue;
            com.itextpdf.text.pdf.PdfPTable tablaItem = new com.itextpdf.text.pdf.PdfPTable(2); tablaItem.setWidthPercentage(100); tablaItem.setWidths(new float[]{4f, 6f});
            String valorPrincipal = partes[0].trim(); if (valorPrincipal.matches("^[A-Z][0-9]{2}\\s*-\\s*.*")) { valorPrincipal = valorPrincipal.replaceFirst("^[A-Z][0-9]{2}\\s*-\\s*", ""); } agregarRenglonTabla(tablaItem, etiquetaPrincipal + ":", valorPrincipal, fontBold, fontNormal);
            for (int j = 1; j < partes.length; j++) {
                String p = partes[j].trim(); int idx = p.indexOf(":");
                if(idx != -1) {
                    String etiquetaOriginal = p.substring(0, idx).trim(); String valor = p.substring(idx+1).trim(); String etiqueta = etiquetaOriginal + ":";
                    if (etiquetaOriginal.equals("Inicio")) etiqueta = "Fecha de Inicio:"; else if (etiquetaOriginal.equals("Fin")) etiqueta = "Fecha de Fin:"; else if (etiquetaOriginal.equals("Est")) etiqueta = "Estado:"; else if (etiquetaOriginal.equals("Frec")) etiqueta = "Frecuencia:";
                    agregarRenglonTabla(tablaItem, etiqueta, valor, fontBold, fontNormal);
                } else { agregarRenglonTabla(tablaItem, "", p, fontBold, fontNormal); }
            }
            com.itextpdf.text.pdf.PdfPCell celdaWrapper = new com.itextpdf.text.pdf.PdfPCell(tablaItem); celdaWrapper.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); if (i % 2 == 0) { celdaWrapper.setPaddingRight(15f); } else { celdaWrapper.setPaddingLeft(15f); } celdaWrapper.setPaddingBottom(15f); tablaContenedor.addCell(celdaWrapper);
        }
        if (elementos.length % 2 != 0) { com.itextpdf.text.pdf.PdfPCell celdaVacia = new com.itextpdf.text.pdf.PdfPCell(); celdaVacia.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); tablaContenedor.addCell(celdaVacia); } doc.add(tablaContenedor);
    }

    private void agregarRenglonTabla(com.itextpdf.text.pdf.PdfPTable tabla, String etiqueta, String valor, com.itextpdf.text.Font fontBold, com.itextpdf.text.Font fontNormal) {
        com.itextpdf.text.pdf.PdfPCell celdaEtiq = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(etiqueta, fontBold)); celdaEtiq.setBorder(com.itextpdf.text.Rectangle.BOTTOM); celdaEtiq.setBorderColorBottom(new com.itextpdf.text.BaseColor(235, 238, 240)); celdaEtiq.setBorderWidthBottom(1f); celdaEtiq.setPaddingTop(6f); celdaEtiq.setPaddingBottom(6f); tabla.addCell(celdaEtiq);
        com.itextpdf.text.pdf.PdfPCell celdaVal = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(valor, fontNormal)); celdaVal.setBorder(com.itextpdf.text.Rectangle.BOTTOM); celdaVal.setBorderColorBottom(new com.itextpdf.text.BaseColor(235, 238, 240)); celdaVal.setBorderWidthBottom(1f); celdaVal.setPaddingTop(6f); celdaVal.setPaddingBottom(6f); tabla.addCell(celdaVal);
    }

    class EventoFooterFirma extends com.itextpdf.text.pdf.PdfPageEventHelper {
        com.itextpdf.text.Font fontFirma = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL, com.itextpdf.text.BaseColor.DARK_GRAY);
        @Override public void onEndPage(com.itextpdf.text.pdf.PdfWriter writer, com.itextpdf.text.Document document) {
            com.itextpdf.text.pdf.PdfContentByte cb = writer.getDirectContent();
            com.itextpdf.text.Phrase linea = new com.itextpdf.text.Phrase("_______________________________", fontFirma); com.itextpdf.text.Phrase texto = new com.itextpdf.text.Phrase("Firma y Sello del Profesional", fontFirma);
            com.itextpdf.text.pdf.ColumnText.showTextAligned(cb, com.itextpdf.text.Element.ALIGN_RIGHT, linea, document.right(), document.bottom() - 10, 0); com.itextpdf.text.pdf.ColumnText.showTextAligned(cb, com.itextpdf.text.Element.ALIGN_RIGHT, texto, document.right(), document.bottom() - 25, 0);
        }
    }
}
