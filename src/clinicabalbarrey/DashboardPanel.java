package clinicabalbarrey;

import clinicabalbarrey.dao.ConsultaDAO;
import clinicabalbarrey.model.Usuario;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class DashboardPanel extends JPanel {

    private static final String TODAS_OBRAS_SOCIALES = "Todas las obras sociales";

    private MainApplicationView mainApp;
    private JTable tablaUltimasConsultas;
    private DefaultTableModel modeloTabla;

    private JComboBox<String> cmbMes;
    private JComboBox<String> cmbAnio;
    private JPanel pnlEstadisticasAdmin;
    private JDateChooser dateDesde;
    private JDateChooser dateHasta;
    private JTable tablaObrasSociales;
    private DefaultTableModel modeloObrasSociales;
    private JLabel lblTotalPacientes;
    private JLabel lblTotalAtenciones;
    private JLabel lblObrasSociales;
    private JLabel lblObraTop;
    private JLabel lblPacientesObraSeleccionada;
    private JComboBox<String> cmbObraSocialIndicador;
    private boolean actualizandoComboObrasSociales;
    private boolean actualizandoFiltrosFecha;
    private boolean mostrarColumnaMedico;
    private Date fechaMinimaConsultas;
    private List<ConsultaDAO.EstadisticaObraSocial> estadisticasObrasActuales = new ArrayList<>();
    private List<String> obrasSocialesDisponiblesCache = new ArrayList<>();

    // Listas
    private List<String[]> listaConsultasBD = new ArrayList<>();
    private List<String[]> listaFiltrada = new ArrayList<>();

    // Paginación
    private int paginaActual = 1;
    private int registrosPorPagina = 6;
    private JLabel lblPaginacion;
    private JButton btnAnt;
    private JButton btnSig;
    private int cargaToken;
    private int estadisticasToken;

    private record ResumenDashboard(List<ConsultaDAO.EstadisticaObraSocial> obrasSociales,
                                    ConsultaDAO.ResumenAtenciones resumen,
                                    List<String> obrasSocialesDisponibles) {}

    public DashboardPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        this.mostrarColumnaMedico = mainApp.getUsuarioSesion() == null || !mainApp.getUsuarioSesion().esMedico();
        setLayout(new BorderLayout());
        setBackground(new Color(242, 244, 246));

        JPanel pnlNorte = new JPanel(new BorderLayout());
        pnlNorte.setBackground(new Color(242, 244, 246));
        pnlNorte.setBorder(new EmptyBorder(18, 40, 8, 40));

        JPanel pnlTitulos = new JPanel();
        pnlTitulos.setLayout(new BoxLayout(pnlTitulos, BoxLayout.Y_AXIS));
        pnlTitulos.setOpaque(false);

        JLabel lblTitulo = new JLabel("REPORTES");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitulo.setForeground(new Color(0, 77, 64));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlTitulos.add(lblTitulo);


        JLabel lblSubtitulo = new JLabel("Historial clínico consolidado al día de hoy");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        lblSubtitulo.setForeground(new Color(108, 117, 125));
        lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);


        pnlNorte.add(pnlTitulos, BorderLayout.WEST);

        JPanel pnlCajaFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlCajaFiltros.setBackground(Color.WHITE);
        pnlCajaFiltros.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(225, 228, 230), 1),
            new EmptyBorder(0, 10, 0, 10)
        ));

        String[] meses = {"Todo el año", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        cmbMes = new JComboBox<>(meses);
        estilizarComboBoxModerdno(cmbMes);
        cmbMes.setPreferredSize(new Dimension(150, 38));
        cmbMes.setSelectedIndex(0);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int anioActual = cal.get(java.util.Calendar.YEAR);
        String[] anios = new String[11];
        for (int i = 0; i < anios.length; i++) {
            anios[i] = String.valueOf(anioActual - 5 + i);
        }
        cmbAnio = new JComboBox<>(anios);
        estilizarComboBoxModerdno(cmbAnio);
        cmbAnio.setPreferredSize(new Dimension(110, 38));
        cmbAnio.setSelectedItem(String.valueOf(cal.get(java.util.Calendar.YEAR)));

        JLabel lblFechaFiltro = new JLabel("Filtrar Fecha:");
        lblFechaFiltro.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFechaFiltro.setForeground(new Color(100, 110, 115));

        dateDesde = crearSelectorFecha();
        dateHasta = crearSelectorFecha();
        inicializarRangoEstadisticas();
        configurarValidacionRangoFechas();

        JButton btnAplicarFiltroFecha = new JButton("Aplicar");
        btnAplicarFiltroFecha.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAplicarFiltroFecha.setForeground(Color.WHITE);
        btnAplicarFiltroFecha.setBackground(new Color(0, 150, 136));
        btnAplicarFiltroFecha.setFocusPainted(false);
        btnAplicarFiltroFecha.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAplicarFiltroFecha.setPreferredSize(new Dimension(92, 36));
        EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(btnAplicarFiltroFecha);
        btnAplicarFiltroFecha.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAplicarFiltroFecha.setPreferredSize(new Dimension(92, 36));
        btnAplicarFiltroFecha.addActionListener(e -> aplicarDashboardCompleto());

        pnlCajaFiltros.add(lblFechaFiltro);
        pnlCajaFiltros.add(new JLabel("Desde:"));
        pnlCajaFiltros.add(dateDesde);
        pnlCajaFiltros.add(new JLabel("Hasta:"));
        pnlCajaFiltros.add(dateHasta);
        pnlCajaFiltros.add(btnAplicarFiltroFecha);

        JPanel pnlWrapperFiltro = new JPanel(new BorderLayout());
        pnlWrapperFiltro.setOpaque(false);
        pnlWrapperFiltro.add(pnlCajaFiltros, BorderLayout.SOUTH);

        pnlNorte.add(pnlWrapperFiltro, BorderLayout.EAST);
        add(pnlNorte, BorderLayout.NORTH);

        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.setBackground(new Color(242, 244, 246));
        pnlCentro.setBorder(new EmptyBorder(0, 40, 0, 40));
        // Inicializa los datos auxiliares que reutiliza el reporte, pero el bloque
        // redundante de "Indicadores del período" ya no se muestra en pantalla.
        crearPanelEstadisticasAdmin();

        String[] columnas = mostrarColumnaMedico
                ? new String[]{"FECHA", "PACIENTE", "M\u00c9DICO TRATANTE", "OBRA SOCIAL", "RESUMEN DE DIAGN\u00d3STICOS", "RESUMEN DE TRATAMIENTOS"}
                : new String[]{"FECHA", "PACIENTE", "OBRA SOCIAL", "RESUMEN DE DIAGN\u00d3STICOS", "RESUMEN DE TRATAMIENTOS"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaUltimasConsultas = new JTable(modeloTabla);

        tablaUltimasConsultas.setRowHeight(50);
        tablaUltimasConsultas.putClientProperty("responsive.filaDinamica", Boolean.TRUE);
        tablaUltimasConsultas.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaUltimasConsultas.setSelectionBackground(new Color(240, 248, 255));
        tablaUltimasConsultas.setSelectionForeground(Color.BLACK);
        tablaUltimasConsultas.setRowSelectionAllowed(false);
        tablaUltimasConsultas.setColumnSelectionAllowed(false);
        tablaUltimasConsultas.setCellSelectionEnabled(false);
        tablaUltimasConsultas.setFocusable(false);
        tablaUltimasConsultas.setShowHorizontalLines(true);
        tablaUltimasConsultas.setShowVerticalLines(false);
        tablaUltimasConsultas.setGridColor(new Color(230, 235, 240));
        tablaUltimasConsultas.setIntercellSpacing(new Dimension(0, 0));
        tablaUltimasConsultas.setFillsViewportHeight(true);

        JTableHeader header = tablaUltimasConsultas.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setBackground(new Color(0, 121, 107));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        tablaUltimasConsultas.setDefaultRenderer(Object.class, new RenderizadorPersonalizado());

        tablaUltimasConsultas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        configurarAnchosTablaActividad();

        JScrollPane scrollTabla = new JScrollPane(tablaUltimasConsultas);
        scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        scrollTabla.setViewportBorder(BorderFactory.createEmptyBorder());
        scrollTabla.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        scrollTabla.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int availableHeight = scrollTabla.getViewport().getHeight();
                if (availableHeight > 0) {
                    int optimalRowHeight = availableHeight / registrosPorPagina;
                    tablaUltimasConsultas.setRowHeight(Math.max(optimalRowHeight, 32));
                }
            }
        });

        JPanel pnlTablaWrapper = new JPanel(new BorderLayout());
        pnlTablaWrapper.setBackground(Color.WHITE);
        pnlTablaWrapper.setBorder(BorderFactory.createLineBorder(new Color(205, 214, 218), 1));
        pnlTablaWrapper.add(crearEncabezadoActividad(), BorderLayout.NORTH);
        pnlTablaWrapper.add(scrollTabla, BorderLayout.CENTER);

        pnlCentro.add(pnlTablaWrapper, BorderLayout.CENTER);

        JPanel pnlPaginacion = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pnlPaginacion.setBackground(new Color(242, 244, 246));
        pnlPaginacion.setBorder(new EmptyBorder(10, 0, 15, 0));

        btnAnt = new JButton("< Anterior");
        estilizarBotonPaginacion(btnAnt);
        btnAnt.addActionListener(e -> {
            if (paginaActual > 1) {
                paginaActual--;
                actualizarVistaTabla();
            }
        });

        lblPaginacion = new JLabel("Página 1 de 1");
        lblPaginacion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPaginacion.setForeground(Color.DARK_GRAY);

        btnSig = new JButton("Siguiente >");
        estilizarBotonPaginacion(btnSig);
        btnSig.addActionListener(e -> {
            int totalPaginas = (int) Math.ceil((double) listaFiltrada.size() / registrosPorPagina);
            if (paginaActual < totalPaginas) {
                paginaActual++;
                actualizarVistaTabla();
            }
        });

        pnlPaginacion.add(btnAnt);
        pnlPaginacion.add(lblPaginacion);
        pnlPaginacion.add(btnSig);
        pnlCentro.add(pnlPaginacion, BorderLayout.SOUTH);

        add(pnlCentro, BorderLayout.CENTER);

    }

    private void configurarAnchosTablaActividad() {
        if (tablaUltimasConsultas == null || tablaUltimasConsultas.getColumnModel().getColumnCount() == 0) return;

        tablaUltimasConsultas.getColumnModel().getColumn(0).setMinWidth(110);
        tablaUltimasConsultas.getColumnModel().getColumn(0).setPreferredWidth(115);
        tablaUltimasConsultas.getColumnModel().getColumn(1).setMinWidth(140);
        tablaUltimasConsultas.getColumnModel().getColumn(1).setPreferredWidth(190);
        if (mostrarColumnaMedico) {
            tablaUltimasConsultas.getColumnModel().getColumn(2).setMinWidth(150);
            tablaUltimasConsultas.getColumnModel().getColumn(2).setPreferredWidth(180);
            tablaUltimasConsultas.getColumnModel().getColumn(3).setMinWidth(190);
            tablaUltimasConsultas.getColumnModel().getColumn(3).setPreferredWidth(230);
            tablaUltimasConsultas.getColumnModel().getColumn(4).setMinWidth(190);
            tablaUltimasConsultas.getColumnModel().getColumn(4).setPreferredWidth(260);
            tablaUltimasConsultas.getColumnModel().getColumn(5).setMinWidth(200);
            tablaUltimasConsultas.getColumnModel().getColumn(5).setPreferredWidth(270);
        } else {
            tablaUltimasConsultas.getColumnModel().getColumn(2).setPreferredWidth(220);
            tablaUltimasConsultas.getColumnModel().getColumn(3).setPreferredWidth(330);
            tablaUltimasConsultas.getColumnModel().getColumn(4).setPreferredWidth(330);
        }
    }

    private JPanel crearEncabezadoActividad() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 18, 10, 18));

        JLabel titulo = new JLabel("Actividad clínica reciente");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(new Color(0, 77, 64));
        JButton btnExportar = new JButton("Exportar PDF");
        btnExportar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExportar.setPreferredSize(new Dimension(135, 34));
        btnExportar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(btnExportar);
        btnExportar.addActionListener(e -> exportarActividadPdf());

        panel.add(titulo, BorderLayout.WEST);
        panel.add(btnExportar, BorderLayout.EAST);
        return panel;
    }

    private void exportarActividadPdf() {
        if (listaFiltrada.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay actividad para exportar con los filtros actuales.",
                    "Sin datos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar reporte de actividad clínica");
        selector.setSelectedFile(new File("actividad-clinica.pdf"));
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Documento PDF", "pdf"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File destino = selector.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".pdf")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".pdf");
        }
        if (destino.exists() && JOptionPane.showConfirmDialog(this,
                "El archivo ya existe. ¿Desea reemplazarlo?", "Confirmar reemplazo",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;

        try {
            ActividadClinicaPdfExporter.exportar(destino, dateDesde.getDate(), dateHasta.getDate(),
                    new ArrayList<>(listaFiltrada), mostrarColumnaMedico);
            JOptionPane.showMessageDialog(this, "PDF guardado correctamente en:\n" + destino.getAbsolutePath(),
                    "Exportación completada", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo generar el PDF: " + ex.getMessage(),
                    "Error de exportación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel crearPanelEstadisticasAdmin() {
        pnlEstadisticasAdmin = new JPanel(new BorderLayout(0, 14));
        pnlEstadisticasAdmin.setOpaque(false);
        pnlEstadisticasAdmin.setBorder(new EmptyBorder(0, 0, 14, 0));
        pnlEstadisticasAdmin.setVisible(true);

        JPanel pnlSuperior = new JPanel(new BorderLayout());
        pnlSuperior.setOpaque(false);

        JLabel lblTituloStats = new JLabel("ESTADÍSTICAS ADMINISTRATIVAS");
        lblTituloStats.setText("Indicadores del período");
        lblTituloStats.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTituloStats.setForeground(new Color(0, 77, 64));
        pnlSuperior.add(lblTituloStats, BorderLayout.WEST);

        JPanel pnlContenido = new JPanel(new BorderLayout(15, 0));
        pnlContenido.setOpaque(false);

        JPanel pnlTarjetas = new JPanel(new GridLayout(1, 3, 16, 0));
        pnlTarjetas.setOpaque(false);
        lblTotalPacientes = new JLabel("0");
        lblTotalAtenciones = new JLabel("0");
        lblObrasSociales = new JLabel("0");
        lblObraTop = new JLabel("-");
        lblPacientesObraSeleccionada = new JLabel("0");
        cmbObraSocialIndicador = new JComboBox<>();
        cmbObraSocialIndicador.setPreferredSize(new Dimension(360, 38));
        estilizarComboBoxObraSocial(cmbObraSocialIndicador);
        cmbObraSocialIndicador.addActionListener(e -> {
            if (!actualizandoComboObrasSociales) aplicarFiltros();
        });
        pnlTarjetas.add(crearTarjetaEstadistica("TURNOS ATENDIDOS", lblTotalPacientes, "Turnos atendidos en el período.", new Color(0, 121, 107)));
        pnlTarjetas.add(crearTarjetaEstadistica("ATENDIDOS POR OBRA SOCIAL SELECCIONADA", lblPacientesObraSeleccionada, "Atenciones con esa cobertura en el per\u00edodo.", new Color(41, 128, 185)));
        pnlTarjetas.add(crearTarjetaConComboObraSocial());

        modeloObrasSociales = new DefaultTableModel(new String[]{"Obra Social", "Pacientes", "Atenciones"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaObrasSociales = new JTable(modeloObrasSociales);
        tablaObrasSociales.setRowHeight(30);
        tablaObrasSociales.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaObrasSociales.setSelectionBackground(new Color(232, 245, 242));
        tablaObrasSociales.setSelectionForeground(Color.BLACK);
        tablaObrasSociales.setRowSelectionAllowed(false);
        tablaObrasSociales.setColumnSelectionAllowed(false);
        tablaObrasSociales.setCellSelectionEnabled(false);
        tablaObrasSociales.setFocusable(false);
        tablaObrasSociales.setShowGrid(true);
        tablaObrasSociales.setGridColor(new Color(225, 228, 230));
        tablaObrasSociales.getTableHeader().setReorderingAllowed(false);
        tablaObrasSociales.getTableHeader().setBackground(new Color(0, 121, 107));
        tablaObrasSociales.getTableHeader().setForeground(Color.WHITE);
        tablaObrasSociales.getTableHeader().setFont(EstiloRedondeado.fuenteHeaderTabla());
        ((DefaultTableCellRenderer) tablaObrasSociales.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(JLabel.CENTER);
        tablaObrasSociales.getColumnModel().getColumn(1).setCellRenderer(centrado);
        tablaObrasSociales.getColumnModel().getColumn(2).setCellRenderer(centrado);

        pnlContenido.add(pnlTarjetas, BorderLayout.CENTER);

        pnlEstadisticasAdmin.add(pnlSuperior, BorderLayout.NORTH);
        pnlEstadisticasAdmin.add(pnlContenido, BorderLayout.CENTER);
        return pnlEstadisticasAdmin;
    }

    private JDateChooser crearSelectorFecha() {
        JDateChooser selector = new JDateChooser();
        selector.setDateFormatString("dd/MM/yyyy");
        selector.setPreferredSize(new Dimension(120, 34));
        selector.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        selector.setMaxSelectableDate(new Date());
        if (selector.getDateEditor().getUiComponent() instanceof JTextField campoFecha) {
            campoFecha.setEditable(false);
            campoFecha.setFocusable(false);
            campoFecha.setBackground(Color.WHITE);
            campoFecha.setToolTipText("Seleccione la fecha desde el botón del calendario");
        }
        return selector;
    }

    private void inicializarRangoEstadisticas() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        dateDesde.setDate(cal.getTime());
        dateHasta.setDate(new Date());
    }

    private void configurarValidacionRangoFechas() {
        dateDesde.getDateEditor().addPropertyChangeListener("date", e -> ajustarRangoFechas());
        dateHasta.getDateEditor().addPropertyChangeListener("date", e -> ajustarRangoFechas());
        ajustarRangoFechas();
    }

    private void ajustarRangoFechas() {
        if (actualizandoFiltrosFecha || dateDesde == null || dateHasta == null) return;
        actualizandoFiltrosFecha = true;
        try {
            Date hoy = normalizarFecha(new Date(), false);
            Date minimo = fechaMinimaConsultas != null ? normalizarFecha(fechaMinimaConsultas, false) : null;
            dateDesde.setMinSelectableDate(minimo);
            dateDesde.setMaxSelectableDate(hoy);
            dateHasta.setMaxSelectableDate(hoy);

            Date desde = dateDesde.getDate();
            Date hasta = dateHasta.getDate();

            if (minimo != null && desde != null && normalizarFecha(desde, false).before(minimo)) {
                dateDesde.setDate(minimo);
                desde = minimo;
            }
            if (desde != null && normalizarFecha(desde, false).after(hoy)) {
                dateDesde.setDate(hoy);
                desde = hoy;
            }
            if (hasta != null && normalizarFecha(hasta, false).after(hoy)) {
                dateHasta.setDate(hoy);
                hasta = hoy;
            }

            if (desde != null) {
                dateHasta.setMinSelectableDate(normalizarFecha(desde, false));
            } else {
                dateHasta.setMinSelectableDate(minimo);
            }
            if (desde != null && hasta != null && normalizarFecha(hasta, false).before(normalizarFecha(desde, false))) {
                dateHasta.setDate(desde);
            }
        } finally {
            actualizandoFiltrosFecha = false;
        }
    }

    private void actualizarFechaMinimaDesdeConsultas() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date minima = null;

        for (String[] fila : listaConsultasBD) {
            if (fila.length == 0 || fila[0] == null || fila[0].trim().isEmpty()) {
                continue;
            }
            try {
                Date fecha = normalizarFecha(sdf.parse(fila[0].trim()), false);
                if (minima == null || fecha.before(minima)) {
                    minima = fecha;
                }
            } catch (Exception ignored) {
                // Si una fecha viene con formato inválido, se ignora.
            }
        }

        fechaMinimaConsultas = minima;
        if (fechaMinimaConsultas != null) {
            actualizandoFiltrosFecha = true;
            try {
                dateDesde.setMinSelectableDate(fechaMinimaConsultas);
                Date desdeActual = dateDesde.getDate();
                if (desdeActual == null || normalizarFecha(desdeActual, false).before(fechaMinimaConsultas)) {
                    dateDesde.setDate(fechaMinimaConsultas);
                }
            } finally {
                actualizandoFiltrosFecha = false;
            }
        }
        ajustarRangoFechas();
    }

    private JPanel crearTarjetaEstadistica(String titulo, JLabel valor, String descripcion, Color color) {
        JPanel tarjeta = new JPanel(new BorderLayout(10, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint fondo = new GradientPaint(0, 0, Color.WHITE, getWidth(), getHeight(), new Color(245, 250, 249));
                g2.setPaint(fondo);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(new Color(225, 228, 230));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setBorder(new EmptyBorder(12, 16, 12, 16));
        tarjeta.setPreferredSize(new Dimension(0, 108));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(new Color(75, 85, 92));

        valor.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valor.setForeground(color);
        valor.setToolTipText(titulo);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(6));
        textos.add(valor);

        tarjeta.add(textos, BorderLayout.CENTER);
        return tarjeta;
    }

    private JButton crearBotonFlechaCombo() {
        return new JButton() {
            {
                setPreferredSize(new Dimension(44, 34));
                setBorder(BorderFactory.createEmptyBorder());
                setFocusPainted(false);
                setContentAreaFilled(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 150, 136));
                g2.fillRoundRect(5, 4, getWidth() - 10, getHeight() - 8, 10, 10);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2 + 1;
                Polygon flecha = new Polygon();
                flecha.addPoint(cx - 5, cy - 3);
                flecha.addPoint(cx + 5, cy - 3);
                flecha.addPoint(cx, cy + 4);
                g2.setColor(Color.WHITE);
                g2.fillPolygon(flecha);
                g2.dispose();
            }
        };
    }

    private JPanel crearTarjetaConComboObraSocial() {
        JPanel tarjeta = new JPanel(new BorderLayout(10, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint fondo = new GradientPaint(0, 0, Color.WHITE, getWidth(), getHeight(), new Color(245, 250, 249));
                g2.setPaint(fondo);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(new Color(225, 228, 230));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(new Color(93, 64, 55, 28));
                g2.fillOval(getWidth() - 58, getHeight() - 58, 42, 42);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setBorder(new EmptyBorder(12, 16, 12, 16));
        tarjeta.setPreferredSize(new Dimension(0, 108));

        JLabel lblTitulo = new JLabel("OBRA SOCIAL");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(new Color(75, 85, 92));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        cmbObraSocialIndicador.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbObraSocialIndicador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(9));
        textos.add(cmbObraSocialIndicador);

        tarjeta.add(textos, BorderLayout.CENTER);
        return tarjeta;
    }

    private void estilizarBotonPaginacion(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(0, 121, 107));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton boton = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color borde = boton.isEnabled() ? new Color(0, 121, 107) : new Color(190, 200, 205);
                Color fondo = boton.isEnabled()
                        ? (boton.getModel().isPressed() ? new Color(232, 245, 242) : Color.WHITE)
                        : new Color(245, 247, 248);
                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 12, 12);
                g2.setColor(borde);
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 12, 12);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    private void estilizarComboBoxModerdno(JComboBox<String> cmb) {
        cmb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cmb.setForeground(new Color(60, 60, 60));
        cmb.setBackground(new Color(245, 247, 250));
        cmb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cmb.setMaximumRowCount(5);
        cmb.setBorder(EstiloRedondeado.borde(new Color(225, 228, 230), 1));

        cmb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton("▼");
                button.setFont(new Font("Segoe UI", Font.BOLD, 10));
                button.setForeground(new Color(150, 160, 170));
                button.setBackground(new Color(245, 247, 250));
                button.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                button.setFocusPainted(false);
                button.setContentAreaFilled(false);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return button;
            }
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 247, 250));
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                g2.dispose();
            }
        });

        cmb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, false, false);
                label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                if (isSelected && index >= 0) {
                    label.setBackground(new Color(41, 128, 185));
                    label.setForeground(Color.WHITE);
                    label.setOpaque(true);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(Color.DARK_GRAY);
                    label.setOpaque(true);
                }
                return label;
            }
        });
    }

    private void estilizarComboBoxObraSocial(JComboBox<String> cmb) {
        cmb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cmb.setForeground(new Color(55, 65, 72));
        cmb.setBackground(Color.WHITE);
        cmb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cmb.setMaximumRowCount(7);
        cmb.setFocusable(false);
        cmb.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(0, 150, 136), 1),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));

        cmb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return crearBotonFlechaCombo();
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.WHITE);
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                g2.dispose();
            }
        });

        cmb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, false, false);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setVerticalAlignment(SwingConstants.CENTER);
                label.setBorder(index < 0
                        ? BorderFactory.createEmptyBorder(0, 12, 0, 12)
                        : BorderFactory.createEmptyBorder(7, 12, 7, 12));
                label.setOpaque(true);
                if (isSelected && index >= 0) {
                    label.setBackground(new Color(0, 150, 136));
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(55, 65, 72));
                }
                return label;
            }
        });
    }

    class RenderizadorPersonalizado extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(JLabel.CENTER);
            setVerticalAlignment(JLabel.CENTER);
            setHorizontalTextPosition(JLabel.CENTER);
            setVerticalTextPosition(JLabel.CENTER);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            if (value != null) {
                boolean esObraSocial = "OBRA SOCIAL".equals(table.getColumnName(column));
                String contenido = value.toString()
                        .replaceAll("(?i)</?(?:html|div)(?:\\s+[^>]*)?>", "").trim();
                if (esObraSocial) {
                    if (contenido.toUpperCase().startsWith("OSECAC")
                            && contenido.toLowerCase().contains("empleados de comercio")) {
                        contenido = "OSECAC<br>Empleados de Comercio";
                    } else {
                        contenido = contenido.replace(" / ", " /<br>")
                                .replace(" (", "<br>(");
                    }
                }
                boolean variasLineas = contenido.toLowerCase().contains("<br>");
                if (variasLineas) {
                    String seguro = contenido.replaceAll("(?i)<br\\s*/?>", "__SALTO_LINEA__")
                            .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                            .replace("__SALTO_LINEA__", "<br>");
                    setText("<html><center>" + seguro + "</center></html>");
                } else {
                    setText(contenido.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim());
                }
            }

            if (column == 0) {
                c.setFont(new Font("Segoe UI", Font.BOLD, 14));
                c.setForeground(isSelected ? new Color(41, 128, 185) : new Color(50, 50, 50));
            } else {
                c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                c.setForeground(isSelected ? Color.BLACK : new Color(80, 80, 80));
            }

            c.setBackground(isSelected ? new Color(240, 248, 255) : Color.WHITE);

            if (value != null) {
                String textoPlano = value.toString().replaceAll("<[^>]*>", " ").replaceAll(" +", " ").trim();
                setToolTipText(textoPlano);
            }
            return c;
        }
    }

    public void cargarConsultasDesdeBD() {
        Usuario sesion = mainApp.getUsuarioSesion();
        actualizarColumnasActividadPorRol(sesion);
        boolean mostrarEstadisticas = sesion == null || !sesion.esSecretaria();
        if (pnlEstadisticasAdmin != null) {
            pnlEstadisticasAdmin.setVisible(mostrarEstadisticas);
        }
        if (mostrarEstadisticas) {
            cargarObrasSocialesDisponiblesSiHaceFalta();
        } else {
            limpiarEstadisticasAdmin();
        }

        Integer idPersonal = sesion != null && sesion.esMedico() ? sesion.getId_personal() : null;
        if (sesion != null && sesion.esMedico() && (idPersonal == null || idPersonal <= 0)) {
            listaConsultasBD.clear(); aplicarFiltros(); return;
        }
        final int token = ++cargaToken;
        new SwingWorker<List<String[]>, Void>() {
            @Override protected List<String[]> doInBackground() { return new ConsultaDAO().obtenerConsultasDashboard(idPersonal); }
            @Override protected void done() {
                if (token != cargaToken) return;
                try {
                    listaConsultasBD.clear();
                    listaConsultasBD.addAll(get());
                    actualizarFechaMinimaDesdeConsultas();
                    aplicarFiltros();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DashboardPanel.this, "No se pudo cargar el dashboard: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void actualizarColumnasActividadPorRol(Usuario sesion) {
        boolean debeMostrarMedico = sesion == null || !sesion.esMedico();
        if (debeMostrarMedico == mostrarColumnaMedico || modeloTabla == null) {
            return;
        }

        mostrarColumnaMedico = debeMostrarMedico;
        String[] columnas = mostrarColumnaMedico
                ? new String[]{"FECHA", "PACIENTE", "M\u00c9DICO TRATANTE", "OBRA SOCIAL", "RESUMEN DE DIAGN\u00d3STICOS", "RESUMEN DE TRATAMIENTOS"}
                : new String[]{"FECHA", "PACIENTE", "OBRA SOCIAL", "RESUMEN DE DIAGN\u00d3STICOS", "RESUMEN DE TRATAMIENTOS"};
        modeloTabla.setColumnIdentifiers(columnas);
        configurarAnchosTablaActividad();
    }

    private void cargarObrasSocialesDisponiblesSiHaceFalta() {
        if (!obrasSocialesDisponiblesCache.isEmpty()) return;
        Usuario sesion = mainApp.getUsuarioSesion();
        Integer idPersonal = sesion != null && sesion.esMedico() ? sesion.getId_personal() : null;
        new SwingWorker<List<String>, Void>() {
            @Override protected List<String> doInBackground() {
                return new ConsultaDAO().obtenerObrasSocialesDisponibles(idPersonal);
            }

            @Override protected void done() {
                try {
                    obrasSocialesDisponiblesCache = get();
                    actualizarComboObrasSociales();
                    actualizarCantidadObraSeleccionada();
                } catch (Exception e) {
                    System.err.println("No se pudieron cargar las obras sociales: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void actualizarComboObrasSociales() {
        if (cmbObraSocialIndicador == null) return;
        String seleccionAnterior = cmbObraSocialIndicador.getSelectedItem() != null
                ? cmbObraSocialIndicador.getSelectedItem().toString()
                : TODAS_OBRAS_SOCIALES;

        actualizandoComboObrasSociales = true;
        try {
            cmbObraSocialIndicador.removeAllItems();
            List<String> disponibles = obrasSocialesDisponiblesCache;
            if (disponibles == null || disponibles.isEmpty()) {
                disponibles = new ArrayList<>();
                disponibles.add("Particular / Sin Obra Social");
            }
            cmbObraSocialIndicador.addItem(TODAS_OBRAS_SOCIALES);
            for (String obra : disponibles) cmbObraSocialIndicador.addItem(obra);
            seleccionarObraSocialCombo(seleccionAnterior);
        } finally {
            actualizandoComboObrasSociales = false;
        }
    }

    private void cargarEstadisticasAdmin() {
        if (dateDesde == null || dateHasta == null) return;
        Date desdeUtil = dateDesde.getDate();
        Date hastaUtil = dateHasta.getDate();
        if (desdeUtil == null || hastaUtil == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una fecha desde y una fecha hasta para ver las estadísticas.", "Fechas requeridas", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (desdeUtil.after(hastaUtil)) {
            JOptionPane.showMessageDialog(this, "La fecha Desde no puede ser posterior a la fecha Hasta.", "Rango inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.sql.Date desdeSql = new java.sql.Date(desdeUtil.getTime());
        java.sql.Date hastaSql = new java.sql.Date(hastaUtil.getTime());
        Usuario sesion = mainApp.getUsuarioSesion();
        Integer idPersonal = sesion != null && sesion.esMedico() ? sesion.getId_personal() : null;
        final int token = ++estadisticasToken;

        new SwingWorker<ResumenDashboard, Void>() {
            @Override protected ResumenDashboard doInBackground() {
                ConsultaDAO dao = new ConsultaDAO();
                return new ResumenDashboard(
                        dao.obtenerPacientesAtendidosPorObraSocial(desdeSql, hastaSql, idPersonal),
                        dao.obtenerResumenAtenciones(desdeSql, hastaSql, idPersonal),
                        dao.obtenerObrasSocialesDisponibles(idPersonal)
                );
            }

            @Override protected void done() {
                if (token != estadisticasToken) return;
                try {
                    actualizarEstadisticasAdmin(get(), desdeUtil, hastaUtil);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DashboardPanel.this, "No se pudieron cargar las estadísticas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void actualizarEstadisticasAdmin(ResumenDashboard datos, Date desde, Date hasta) {
        if (modeloObrasSociales == null) return;
        modeloObrasSociales.setRowCount(0);

        List<ConsultaDAO.EstadisticaObraSocial> estadisticas = datos.obrasSociales();
        ConsultaDAO.ResumenAtenciones resumen = datos.resumen();
        String obraTop = "-";
        int maxPacientes = -1;
        String seleccionAnterior = cmbObraSocialIndicador != null && cmbObraSocialIndicador.getSelectedItem() != null
                ? cmbObraSocialIndicador.getSelectedItem().toString()
                : TODAS_OBRAS_SOCIALES;

        estadisticasObrasActuales = new ArrayList<>(estadisticas);

        if (cmbObraSocialIndicador != null) {
            actualizandoComboObrasSociales = true;
            try {
                cmbObraSocialIndicador.removeAllItems();
                cmbObraSocialIndicador.addItem(TODAS_OBRAS_SOCIALES);
                List<String> disponibles = datos.obrasSocialesDisponibles();
                if (disponibles == null || disponibles.isEmpty()) {
                    cmbObraSocialIndicador.addItem("Particular / Sin Obra Social");
                } else {
                    for (String obra : disponibles) cmbObraSocialIndicador.addItem(obra);
                }
                seleccionarObraSocialCombo(seleccionAnterior);
            } finally {
                actualizandoComboObrasSociales = false;
            }
        }

        for (ConsultaDAO.EstadisticaObraSocial item : estadisticas) {
            modeloObrasSociales.addRow(new Object[]{item.obraSocial(), item.pacientesAtendidos(), item.atenciones()});
            if (item.pacientesAtendidos() > maxPacientes) {
                maxPacientes = item.pacientesAtendidos();
                obraTop = item.obraSocial();
            }
        }

        lblTotalPacientes.setText(String.valueOf(resumen.pacientesAtendidos()));
        lblTotalAtenciones.setText("0");
        if (lblObrasSociales != null) lblObrasSociales.setText("0");
        lblObraTop.setText(acortarTexto(obraTop, 24));
        lblObraTop.setToolTipText(obraTop);
        actualizarCantidadObraSeleccionada();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        pnlEstadisticasAdmin.setToolTipText("Estadísticas calculadas desde " + sdf.format(desde) + " hasta " + sdf.format(hasta));
    }

    private void seleccionarObraSocialCombo(String nombre) {
        if (cmbObraSocialIndicador == null || cmbObraSocialIndicador.getItemCount() == 0) return;
        String buscada = nombre == null || nombre.isBlank() ? TODAS_OBRAS_SOCIALES : nombre;
        for (int i = 0; i < cmbObraSocialIndicador.getItemCount(); i++) {
            String item = cmbObraSocialIndicador.getItemAt(i);
            if (item != null && item.equalsIgnoreCase(buscada)) {
                cmbObraSocialIndicador.setSelectedIndex(i);
                return;
            }
        }
        cmbObraSocialIndicador.setSelectedIndex(0);
    }

    private void actualizarCantidadObraSeleccionada() {
        if (lblPacientesObraSeleccionada == null || cmbObraSocialIndicador == null) return;
        Object seleccion = cmbObraSocialIndicador.getSelectedItem();
        String obraSeleccionada = seleccion == null ? "" : seleccion.toString();
        if (esTodasLasObrasSociales(obraSeleccionada)) {
            int total = estadisticasObrasActuales.stream().mapToInt(ConsultaDAO.EstadisticaObraSocial::pacientesAtendidos).sum();
            lblPacientesObraSeleccionada.setText(String.valueOf(total));
            lblPacientesObraSeleccionada.setToolTipText("Pacientes atendidos con todas las obras sociales");
            return;
        }
        int cantidad = 0;
        for (ConsultaDAO.EstadisticaObraSocial item : estadisticasObrasActuales) {
            if (item.obraSocial() != null && item.obraSocial().equalsIgnoreCase(obraSeleccionada)) {
                cantidad = item.pacientesAtendidos();
                break;
            }
        }
        lblPacientesObraSeleccionada.setText(String.valueOf(cantidad));
        lblPacientesObraSeleccionada.setToolTipText("Pacientes atendidos con " + obraSeleccionada);
    }

    private boolean esTodasLasObrasSociales(String obra) {
        return obra == null || obra.isBlank() || TODAS_OBRAS_SOCIALES.equalsIgnoreCase(obra.trim());
    }

    private void limpiarEstadisticasAdmin() {
        if (modeloObrasSociales != null) modeloObrasSociales.setRowCount(0);
        if (lblTotalPacientes != null) lblTotalPacientes.setText("0");
        if (lblTotalAtenciones != null) lblTotalAtenciones.setText("0");
        if (lblObrasSociales != null) lblObrasSociales.setText("0");
        if (lblObraTop != null) lblObraTop.setText("-");
        if (lblPacientesObraSeleccionada != null) lblPacientesObraSeleccionada.setText("0");
        estadisticasObrasActuales.clear();
    }

    private String acortarTexto(String texto, int maximo) {
        return texto == null ? "" : texto;
    }

    private void aplicarDashboardCompleto() {
        cargarObrasSocialesDisponiblesSiHaceFalta();
        aplicarFiltros();
    }

    private void sincronizarRangoDesdeMesAnio() {
        if (cmbMes == null || cmbAnio == null || cmbAnio.getSelectedItem() == null || dateDesde == null || dateHasta == null) return;
        actualizandoFiltrosFecha = true;
        try {
            int anio = Integer.parseInt(cmbAnio.getSelectedItem().toString());
            int mes = cmbMes.getSelectedIndex();
            Calendar cal = Calendar.getInstance();
            if (mes <= 0) {
                cal.set(anio, Calendar.JANUARY, 1);
                dateDesde.setDate(cal.getTime());
                cal.set(anio, Calendar.DECEMBER, 31);
                dateHasta.setDate(cal.getTime().after(new Date()) ? new Date() : cal.getTime());
            } else {
                cal.set(anio, mes - 1, 1);
                dateDesde.setDate(cal.getTime());
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                dateHasta.setDate(cal.getTime().after(new Date()) ? new Date() : cal.getTime());
            }
        } catch (NumberFormatException ignored) {
            // Si el año no se puede leer, se conservan las fechas actuales.
        } finally {
            actualizandoFiltrosFecha = false;
        }
    }

    private Date normalizarFecha(Date fecha, boolean finDelDia) {
        if (fecha == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        if (finDelDia) {
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal.getTime();
    }

    private void aplicarFiltros() {
        if (modeloTabla == null) return;

        listaFiltrada.clear();
        List<String[]> filasPorFecha = new ArrayList<>();
        Date desde = normalizarFecha(dateDesde != null ? dateDesde.getDate() : null, false);
        Date hasta = normalizarFecha(dateHasta != null ? dateHasta.getDate() : null, true);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String obraSeleccionada = cmbObraSocialIndicador != null && cmbObraSocialIndicador.getSelectedItem() != null
                ? cmbObraSocialIndicador.getSelectedItem().toString()
                : TODAS_OBRAS_SOCIALES;

        for (String[] fila : listaConsultasBD) {
            String fechaFila = fila[0] != null ? fila[0].trim() : "";
            boolean pasaFiltroFecha = true;
            if (desde != null && hasta != null) {
                try {
                    Date fechaConsulta = normalizarFecha(sdf.parse(fechaFila), false);
                    pasaFiltroFecha = !fechaConsulta.before(desde) && !fechaConsulta.after(hasta);
                } catch (Exception e) {
                    pasaFiltroFecha = false;
                }
            }

            if (pasaFiltroFecha) {
                filasPorFecha.add(fila);
                if (esTodasLasObrasSociales(obraSeleccionada) || obtenerObraSocialFila(fila).equalsIgnoreCase(obraSeleccionada)) {
                    listaFiltrada.add(fila);
                }
            }
        }

        paginaActual = 1;
        actualizarEstadisticasDesdeFiltroActual(filasPorFecha);
        actualizarVistaTabla();
    }

    private void actualizarEstadisticasDesdeFiltroActual(List<String[]> filasDelPeriodo) {
        if (lblTotalPacientes == null) return;
        if (modeloObrasSociales != null) modeloObrasSociales.setRowCount(0);

        Map<String, Integer> atencionesPorObra = new HashMap<>();

        for (String[] fila : filasDelPeriodo) {
            String obra = obtenerObraSocialFila(fila);
            atencionesPorObra.put(obra, atencionesPorObra.getOrDefault(obra, 0) + 1);
        }

        estadisticasObrasActuales = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : atencionesPorObra.entrySet()) {
            String obra = entry.getKey();
            int atenciones = entry.getValue();
            ConsultaDAO.EstadisticaObraSocial estadistica = new ConsultaDAO.EstadisticaObraSocial(obra, atenciones, atenciones);
            estadisticasObrasActuales.add(estadistica);
            if (modeloObrasSociales != null) modeloObrasSociales.addRow(new Object[]{obra, atenciones, atenciones});
        }

        lblTotalPacientes.setText(String.valueOf(filasDelPeriodo.size()));
        actualizarCantidadObraSeleccionada();
    }

    private String obtenerObraSocialFila(String[] fila) {
        return fila != null && fila.length > 6 && fila[6] != null && !fila[6].trim().isEmpty()
                ? fila[6].trim()
                : "Particular / Sin Obra Social";
    }

    private void actualizarVistaTabla() {
        modeloTabla.setRowCount(0);

        int totalRegistros = listaFiltrada.size();
        int totalPaginas = (int) Math.ceil((double) totalRegistros / registrosPorPagina);
        if (totalPaginas == 0) totalPaginas = 1;

        lblPaginacion.setText("Página " + paginaActual + " de " + totalPaginas);
        btnAnt.setEnabled(paginaActual > 1);
        btnSig.setEnabled(paginaActual < totalPaginas);

        int inicio = (paginaActual - 1) * registrosPorPagina;
        int fin = Math.min(inicio + registrosPorPagina, totalRegistros);

        for (int i = inicio; i < fin; i++) {
            String[] fila = listaFiltrada.get(i);
            if (mostrarColumnaMedico) {
                modeloTabla.addRow(new Object[]{
                    fila.length > 0 ? fila[0] : "",
                    fila.length > 1 ? fila[1] : "",
                    fila.length > 2 ? fila[2] : "",
                    fila.length > 6 ? fila[6] : "Particular / Sin Obra Social",
                    fila.length > 3 ? fila[3] : "",
                    fila.length > 4 ? fila[4] : ""
                });
            } else {
                modeloTabla.addRow(new Object[]{
                    fila.length > 0 ? fila[0] : "",
                    fila.length > 1 ? fila[1] : "",
                    fila.length > 6 ? fila[6] : "Particular / Sin Obra Social",
                    fila.length > 3 ? fila[3] : "",
                    fila.length > 4 ? fila[4] : ""
                });
            }
        }
    }
}
