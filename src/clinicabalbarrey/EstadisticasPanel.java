package clinicabalbarrey;

import clinicabalbarrey.dao.EstadisticasDAO;
import clinicabalbarrey.dao.EstadisticasDAO.Filtro;
import clinicabalbarrey.dao.EstadisticasDAO.Opcion;
import clinicabalbarrey.dao.EstadisticasDAO.Punto;
import clinicabalbarrey.dao.EstadisticasDAO.Resultado;
import clinicabalbarrey.model.Usuario;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Estadísticas de turnos con filtros múltiples aplicados íntegramente en PostgreSQL. */
public class EstadisticasPanel extends JPanel {
    private static final Color VERDE = new Color(0, 121, 107);
    private static final Color VERDE_OSCURO = new Color(0, 77, 64);
    private static final Color FONDO = new Color(242, 244, 246);
    private static final Color BORDE = new Color(218, 224, 227);

    private final MainApplicationView mainApp;
    private final EstadisticasDAO dao = new EstadisticasDAO();
    private final JDateChooser desde = new JDateChooser();
    private final JDateChooser hasta = new JDateChooser();
    private final SelectorMultiple<Integer> medicos = new SelectorMultiple<>("Todos los médicos", true);
    private final SelectorMultiple<Integer> pacientes = new SelectorMultiple<>("Todos los pacientes", true);
    private final SelectorMultiple<String> obrasSociales = new SelectorMultiple<>("Todas las obras sociales", true);
    private final SelectorMultiple<String> estadosTurno = new SelectorMultiple<>("Todos los estados", false);
    private final JComboBox<String> valores = new JComboBox<>(new String[]{"Cantidad de turnos", "Pacientes únicos", "Porcentaje"});
    private final JComboBox<String> agruparPor = new JComboBox<>(new String[]{
        "Total", "Médico", "Paciente", "Obra social", "Estado", "Mes"
    });
    private final JLabel ayuda = new JLabel("Los campos en «Sin filtro» no limitan la búsqueda.");
    private final JLabel seleccionActual = new JLabel("Selección actual: cargando opciones...");
    private final JLabel tituloResultado = new JLabel("Cantidad de turnos por estado");
    private final JLabel detalleResultado = new JLabel("Completá los tres pasos y presioná Generar estadística.");
    private final JLabel contadorResultado = new JLabel("0 turnos encontrados");
    private final JPanel resumenCategorias = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final GraficoObrasSociales graficoPrincipal = new GraficoObrasSociales();
    private final JButton aplicar = new JButton("Generar estadística");
    private final JButton restablecer = new JButton("Reiniciar filtros");
    private final JButton exportar = new JButton("Exportar a Excel");
    private List<String[]> datosFiltrados = new ArrayList<>();
    private Map<String, String> filtrosAplicados = new LinkedHashMap<>();
    private String[] encabezadosExportacion = {"Resultado", "Cantidad de turnos"};
    private List<String[]> resultadosExportacion = new ArrayList<>();
    private boolean opcionesCargadas;
    private boolean actualizandoAgrupaciones;

    public EstadisticasPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(FONDO);
        JPanel pagina = new PaginaDesplazable(new BorderLayout(0, 12));
        pagina.setBackground(FONDO);
        pagina.setBorder(new EmptyBorder(18, 30, 18, 30));
        pagina.add(crearEncabezado(), BorderLayout.NORTH);
        pagina.add(crearCentro(), BorderLayout.CENTER);
        pagina.add(crearPie(), BorderLayout.SOUTH);
        // Altura lógica suficiente para que el gráfico nunca sea comprimido.
        // En pantallas bajas la página completa se recorre verticalmente.
        pagina.setPreferredSize(new Dimension(1000, 1120));

        JScrollPane desplazamientoPagina = new JScrollPane(pagina,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        desplazamientoPagina.setBorder(null);
        desplazamientoPagina.getViewport().setBackground(FONDO);
        desplazamientoPagina.getVerticalScrollBar().setUnitIncrement(24);
        add(desplazamientoPagina, BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("ESTADÍSTICAS");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(VERDE_OSCURO);
        titulos.add(titulo);
        panel.add(titulos, BorderLayout.NORTH);
        panel.add(crearConfiguracion(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearConfiguracion() {
        inicializarControles();
        JPanel tarjeta = new JPanel(new BorderLayout(0, 6));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), new EmptyBorder(8, 11, 7, 11)));

        JPanel introduccion = new JPanel();
        introduccion.setOpaque(false);
        introduccion.setLayout(new BoxLayout(introduccion, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("ARMÁ TU ESTADÍSTICA EN 3 PASOS");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titulo.setForeground(VERDE_OSCURO);
        JLabel explicacion = new JLabel("Elegí el período, indicá qué turnos querés incluir y definí cómo querés verlos.");
        explicacion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        explicacion.setForeground(new Color(70, 84, 90));
        introduccion.add(titulo);
        introduccion.add(Box.createVerticalStrut(2));
        introduccion.add(explicacion);

        JPanel periodo = new JPanel(new GridLayout(2, 1, 0, 7));
        periodo.setOpaque(false);
        periodo.add(campo("Fecha desde", desde, 126));
        periodo.add(campo("Fecha hasta", hasta, 126));

        JPanel filtros = new JPanel(new GridLayout(2, 2, 9, 7));
        filtros.setOpaque(false);
        filtros.add(campo("Médicos", medicos, 200));
        filtros.add(campo("Paciente", pacientes, 200));
        filtros.add(campo("Obras sociales", obrasSociales, 210));
        filtros.add(campo("Estados del turno", estadosTurno, 180));

        JPanel presentacion = new JPanel(new GridLayout(2, 1, 0, 7));
        presentacion.setOpaque(false);
        presentacion.add(campo("Qué valor mostrar", valores, 150));
        presentacion.add(campo("Separar el resultado por", agruparPor, 190));

        JPanel pasos = new JPanel(new GridLayout(1, 3, 9, 0));
        pasos.setOpaque(false);
        pasos.add(crearPaso("1", "Elegí el período",
                "Define desde qué fecha y hasta qué fecha buscar.", periodo));
        pasos.add(crearPaso("2", "Filtrá los turnos",
                "Combiná los criterios que necesites.", filtros));
        pasos.add(crearPaso("3", "Elegí cómo ver el resultado",
                "Cantidad o porcentaje, y qué nombres mostrar en el gráfico.", presentacion));

        configurarBotonPrincipal(aplicar);
        configurarBotonSecundario(restablecer);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        acciones.setOpaque(false);
        acciones.add(aplicar);
        acciones.add(restablecer);

        tarjeta.add(introduccion, BorderLayout.NORTH);
        tarjeta.add(pasos, BorderLayout.CENTER);
        tarjeta.add(acciones, BorderLayout.SOUTH);
        aplicar.addActionListener(e -> aplicarFiltros());
        restablecer.addActionListener(e -> reiniciarFiltros());
        return tarjeta;
    }

    private JPanel crearPaso(String numero, String titulo, String descripcion, JPanel contenido) {
        JPanel paso = new JPanel(new BorderLayout(0, 5));
        paso.setBackground(new Color(248, 250, 250));
        paso.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 222, 221)),
                new EmptyBorder(6, 8, 7, 8)));

        JPanel cabecera = new JPanel(new BorderLayout(8, 0));
        cabecera.setOpaque(false);
        JLabel indicador = new JLabel(numero, SwingConstants.CENTER);
        indicador.setOpaque(true);
        indicador.setBackground(VERDE);
        indicador.setForeground(Color.WHITE);
        indicador.setFont(new Font("Segoe UI", Font.BOLD, 14));
        indicador.setPreferredSize(new Dimension(25, 25));

        JLabel texto = new JLabel("<html><b>" + titulo + "</b><br><span style='color:#526066'>"
                + descripcion + "</span></html>");
        texto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        texto.setForeground(new Color(50, 65, 70));
        cabecera.add(indicador, BorderLayout.WEST);
        cabecera.add(texto, BorderLayout.CENTER);
        paso.add(cabecera, BorderLayout.NORTH);
        paso.add(contenido, BorderLayout.CENTER);
        return paso;
    }

    private void inicializarControles() {
        configurarFecha(desde);
        configurarFecha(hasta);
        establecerRangoInicial();
        List<ItemSeleccion<String>> estados = new ArrayList<>();
        estados.add(new ItemSeleccion<>("__sin_asistencia__", "Sin atención realizada"));
        for (String estado : List.of("atendido", "ausente", "cancelado", "reservado")) {
            estados.add(new ItemSeleccion<>(estado, capitalizar(estado)));
        }
        estadosTurno.setOpciones(estados);
        estadosTurno.limpiarSeleccion();
        valores.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        agruparPor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        medicos.setToolTipText("Opcional: elegí uno, varios o todos los médicos");
        pacientes.setToolTipText("Opcional: elegí pacientes concretos o dejalo sin filtro");
        obrasSociales.setToolTipText("Opcional: elegí una, varias o todas las obras sociales");
        estadosTurno.setToolTipText("Opcional: elegí uno o varios estados del turno");
        valores.setToolTipText("Cantidad de turnos cuenta cada cita; Pacientes únicos cuenta a cada persona una sola vez; Porcentaje muestra la proporción de cada grupo");
        agruparPor.setToolTipText("Define qué categorías o nombres aparecen en el gráfico");
        valores.addActionListener(e -> actualizarAgrupacionesDisponibles());
        actualizarAgrupacionesDisponibles();
        medicos.setEnabled(false);
        pacientes.setEnabled(false);
        obrasSociales.setEnabled(false);
    }

    private JPanel campo(String nombre, JComponent control, int ancho) {
        JPanel campo = new JPanel();
        campo.setOpaque(false);
        campo.setLayout(new BoxLayout(campo, BoxLayout.Y_AXIS));
        JLabel etiqueta = new JLabel(nombre);
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        etiqueta.setForeground(new Color(65, 77, 83));
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension tamano = new Dimension(ancho, 31);
        control.setPreferredSize(tamano);
        control.setMinimumSize(new Dimension(90, 31));
        control.setMaximumSize(new Dimension(Integer.MAX_VALUE, 31));
        control.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.add(etiqueta);
        campo.add(Box.createVerticalStrut(3));
        campo.add(control);
        return campo;
    }

    private void configurarFecha(JDateChooser selector) {
        selector.setDateFormatString("dd/MM/yyyy");
        // Las reservas futuras también deben poder analizarse (por ejemplo,
        // para conocer los turnos pendientes de mañana).
        selector.setMaxSelectableDate(null);
        selector.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (selector.getDateEditor() instanceof JTextFieldDateEditor editor) {
            editor.setEditable(false);
            editor.setFocusable(false);
            editor.setToolTipText("Elegí la fecha desde el calendario");
        }
    }

    private void establecerRangoInicial() {
        Calendar calendario = Calendar.getInstance();
        calendario.set(Calendar.DAY_OF_YEAR, 1);
        desde.setDate(calendario.getTime());
        hasta.setDate(new Date());
    }

    private void configurarBotonPrincipal(JButton boton) {
        Dimension tamano = new Dimension(180, 34);
        boton.setPreferredSize(tamano);
        boton.setMinimumSize(tamano);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBackground(VERDE);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void configurarBotonSecundario(JButton boton) {
        boton.setPreferredSize(new Dimension(140, 34));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBackground(Color.WHITE);
        boton.setForeground(VERDE_OSCURO);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createLineBorder(new Color(143, 180, 174)));
        boton.setToolTipText("Restaurar el período y las selecciones iniciales");
    }

    /** Evita combinaciones redundantes como «pacientes únicos por paciente». */
    private void actualizarAgrupacionesDisponibles() {
        if (actualizandoAgrupaciones) return;
        actualizandoAgrupaciones = true;
        try {
            String seleccion = (String) agruparPor.getSelectedItem();
            boolean unicos = "Pacientes únicos".equals(valores.getSelectedItem());
            String[] opciones = unicos
                    ? new String[]{"Total", "Médico", "Obra social", "Estado", "Mes"}
                    : new String[]{"Total", "Médico", "Paciente", "Obra social", "Estado", "Mes"};
            agruparPor.setModel(new DefaultComboBoxModel<>(opciones));
            if (seleccion != null && java.util.Arrays.asList(opciones).contains(seleccion)) {
                agruparPor.setSelectedItem(seleccion);
            } else {
                agruparPor.setSelectedItem("Total");
            }
            agruparPor.setToolTipText(unicos
                    ? "Cada paciente se cuenta una sola vez; podés separarlos por médico, obra social, estado o mes"
                    : "Define qué categorías o nombres aparecen en el gráfico");
        } finally {
            actualizandoAgrupaciones = false;
        }
    }

    private void reiniciarFiltros() {
        establecerRangoInicial();
        if (medicos.isEnabled()) medicos.limpiarSeleccion();
        pacientes.limpiarSeleccion();
        obrasSociales.limpiarSeleccion();
        estadosTurno.limpiarSeleccion();
        valores.setSelectedIndex(0);
        agruparPor.setSelectedItem("Total");
        if (opcionesCargadas) aplicarFiltros();
    }

    private JPanel crearCentro() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(crearSeccionGrafico(tituloResultado, detalleResultado, graficoPrincipal), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearSeccionGrafico(JLabel titulo, JLabel detalle, GraficoObrasSociales grafico) {
        JPanel seccion = new JPanel(new BorderLayout(0, 10));
        seccion.setOpaque(false);
        seccion.setPreferredSize(new Dimension(1000, 680));
        seccion.setMinimumSize(new Dimension(700, 620));
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titulo.setForeground(VERDE_OSCURO);
        detalle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detalle.setForeground(new Color(70, 84, 90));
        contadorResultado.setFont(new Font("Segoe UI", Font.BOLD, 20));
        contadorResultado.setForeground(VERDE);
        JPanel resumen = new JPanel(new BorderLayout(18, 6));
        resumen.setBackground(Color.WHITE);
        resumen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), new EmptyBorder(14, 16, 12, 16)));
        JPanel resultadoPrincipal = new JPanel();
        resultadoPrincipal.setOpaque(false);
        resultadoPrincipal.setLayout(new BoxLayout(resultadoPrincipal, BoxLayout.Y_AXIS));
        resultadoPrincipal.add(titulo);
        resultadoPrincipal.add(Box.createVerticalStrut(3));
        resultadoPrincipal.add(contadorResultado);
        resumen.add(resultadoPrincipal, BorderLayout.WEST);
        resumen.add(detalle, BorderLayout.SOUTH);
        grafico.setPreferredSize(new Dimension(1100, 520));
        grafico.setMinimumSize(new Dimension(700, 500));
        JScrollPane desplazamiento = new JScrollPane(grafico,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        desplazamiento.setPreferredSize(new Dimension(1000, 530));
        desplazamiento.setMinimumSize(new Dimension(700, 510));
        desplazamiento.setBorder(BorderFactory.createLineBorder(BORDE));
        desplazamiento.getVerticalScrollBar().setUnitIncrement(20);
        desplazamiento.getViewport().setBackground(Color.WHITE);
        seccion.add(resumen, BorderLayout.NORTH);
        seccion.add(desplazamiento, BorderLayout.CENTER);
        return seccion;
    }
    private JPanel crearPie() {
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pie.setOpaque(false);
        pie.setVisible(false);
        exportar.setPreferredSize(new Dimension(180, 42));
        exportar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exportar.setBackground(VERDE);
        exportar.setForeground(Color.WHITE);
        exportar.setFocusPainted(false);
        exportar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportar.setToolTipText("Exportar únicamente los resultados seleccionados");
        exportar.addActionListener(e -> exportar());
        pie.add(exportar);
        return pie;
    }

    public void cargarEstadisticas() {
        Usuario usuario = mainApp.getUsuarioSesion();
        if (usuario != null && usuario.esSecretaria()) {
            JOptionPane.showMessageDialog(this, "Secretaría no tiene acceso a Estadísticas.",
                    "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ayuda.setText("Cargando opciones...");
        aplicar.setEnabled(false);
        new SwingWorker<Void, Void>() {
            List<Opcion> listaMedicos;
            List<Opcion> listaPacientes;
            List<String> listaObras;
            @Override protected Void doInBackground() {
                listaMedicos = dao.listarMedicos();
                listaPacientes = dao.listarPacientes();
                listaObras = dao.listarObrasSociales();
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    cargarOpciones(listaMedicos, listaPacientes, listaObras, usuario);
                    opcionesCargadas = true;
                    aplicarFiltros();
                } catch (Exception ex) {
                    mostrarError(ex);
                } finally {
                    aplicar.setEnabled(true);
                }
            }
        }.execute();
    }

    private void cargarOpciones(List<Opcion> listaMedicos, List<Opcion> listaPacientes,
                                List<String> listaObras, Usuario usuario) {
        List<ItemSeleccion<Integer>> opcionesMedicos = new ArrayList<>();
        if (usuario != null && usuario.esMedico()) {
            opcionesMedicos.add(new ItemSeleccion<>(usuario.getId_personal(), mainApp.getNombreUsuarioActual()));
            medicos.setOpciones(opcionesMedicos);
            medicos.setEnabled(false);
        } else {
            for (Opcion opcion : listaMedicos) opcionesMedicos.add(new ItemSeleccion<>(opcion.id(), opcion.nombre()));
            medicos.setOpciones(opcionesMedicos);
            medicos.limpiarSeleccion();
            medicos.setEnabled(true);
        }
        List<ItemSeleccion<Integer>> opcionesPacientes = new ArrayList<>();
        for (Opcion opcion : listaPacientes) {
            opcionesPacientes.add(new ItemSeleccion<>(opcion.id(), opcion.nombre(), opcion.busqueda()));
        }
        pacientes.setOpciones(opcionesPacientes);
        pacientes.limpiarSeleccion();
        pacientes.setEnabled(true);
        List<ItemSeleccion<String>> opcionesObras = new ArrayList<>();
        for (String nombre : listaObras) opcionesObras.add(new ItemSeleccion<>(nombre, nombre));
        obrasSociales.setOpciones(opcionesObras);
        obrasSociales.limpiarSeleccion();
        obrasSociales.setEnabled(true);
    }

    private void aplicarFiltros() {
        if (!opcionesCargadas) return;
        Date fechaDesde = desde.getDate();
        Date fechaHasta = hasta.getDate();
        if (fechaDesde == null || fechaHasta == null) {
            JOptionPane.showMessageDialog(this, "Seleccioná las fechas desde y hasta.", "Estadísticas", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fechaDesde.after(fechaHasta)) {
            JOptionPane.showMessageDialog(this, "La fecha desde no puede ser posterior a la fecha hasta.", "Estadísticas", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Usuario usuario = mainApp.getUsuarioSesion();
        List<Integer> idsMedicos = usuario != null && usuario.esMedico()
                ? List.of(usuario.getId_personal()) : medicos.valoresParaFiltro();
        Filtro filtro = new Filtro(fechaDesde, fechaHasta, idsMedicos, pacientes.valoresParaFiltro(),
                obrasSociales.valoresParaFiltro(), estadosTurno.valoresParaFiltro());

        aplicar.setEnabled(false);
        restablecer.setEnabled(false);
        exportar.setEnabled(false);
        aplicar.setText("Consultando...");
        ayuda.setText("Consultando la combinación seleccionada...");
        new SwingWorker<Resultado, Void>() {
            @Override protected Resultado doInBackground() {
                new clinicabalbarrey.dao.TurnoDAO().marcarAusentesVencidos();
                return dao.consultar(filtro);
            }
            @Override protected void done() {
                try {
                    mostrar(get(), filtro);
                } catch (Exception ex) {
                    mostrarError(ex);
                } finally {
                    aplicar.setText("Generar estadística");
                    aplicar.setEnabled(true);
                    restablecer.setEnabled(true);
                    exportar.setEnabled(true);
                }
            }
        }.execute();
    }

    private void mostrar(Resultado resultado, Filtro filtro) {
        datosFiltrados = new ArrayList<>(resultado.filas());
        actualizarVistas(resultado);

        Usuario usuario = mainApp.getUsuarioSesion();
        String descripcionMedicos = usuario != null && usuario.esMedico()
                ? mainApp.getNombreUsuarioActual() : medicos.descripcionSeleccion();
        String descripcionPacientes = pacientes.descripcionSeleccion();
        String descripcionObras = obrasSociales.descripcionSeleccion();
        String descripcionEstados = estadosTurno.descripcionSeleccion();
        seleccionActual.setText("Consulta: Médicos: " + descripcionMedicos
                + "   |   Pacientes: " + descripcionPacientes
                + "   |   Obras sociales: " + descripcionObras
                + "   |   Estados: " + descripcionEstados
                + "   |   Turnos encontrados: " + resultado.filas().size());
        seleccionActual.setToolTipText("<html><b>Médicos:</b> " + descripcionMedicos
                + "<br><b>Pacientes:</b> " + descripcionPacientes
                + "<br><b>Obras sociales:</b> " + descripcionObras
                + "<br><b>Estados:</b> " + descripcionEstados + "</html>");
        int cantidad = resultado.filas().size();
        List<String> filtrosVisibles = new ArrayList<>();
        if (filtro.idsMedicos() != null) filtrosVisibles.add("Médicos: " + descripcionMedicos);
        if (filtro.idsPacientes() != null) filtrosVisibles.add("Pacientes: " + descripcionPacientes);
        if (filtro.obrasSociales() != null) filtrosVisibles.add("Obras sociales: " + descripcionObras);
        if (filtro.estados() != null) filtrosVisibles.add("Estados: " + descripcionEstados);
        String filtrosActivos = filtrosVisibles.isEmpty()
                ? "Sin filtros adicionales"
                : "<b>Filtros:</b> " + String.join(" &nbsp;·&nbsp; ", filtrosVisibles);
        detalleResultado.setText("<html><b>Período:</b> " + formato(filtro.desde())
                + " al " + formato(filtro.hasta()) + "<br>" + filtrosActivos + "</html>");

        filtrosAplicados = new LinkedHashMap<>();
        filtrosAplicados.put("Fecha desde", formato(filtro.desde()));
        filtrosAplicados.put("Fecha hasta", formato(filtro.hasta()));
        filtrosAplicados.put("Médicos", descripcionMedicos);
        filtrosAplicados.put("Pacientes", descripcionPacientes);
        filtrosAplicados.put("Obras sociales", descripcionObras);
        filtrosAplicados.put("Estados del turno", descripcionEstados);
        filtrosAplicados.put("Valores mostrados", (String) valores.getSelectedItem());
        filtrosAplicados.put("Resultado agrupado por", (String) agruparPor.getSelectedItem());
        filtrosAplicados.put("Turnos filtrados", String.valueOf(cantidad));
        filtrosAplicados.put("Pacientes únicos", String.valueOf(contarPacientesUnicos(resultado.filas())));

        if (resultado.filas().isEmpty()) {
            ayuda.setText("No hubo coincidencias. Probá quitar algún criterio o ampliar el período.");
            JOptionPane.showMessageDialog(this,
                    "No existen turnos que coincidan con la combinación seleccionada.",
                    "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
        } else {
            ayuda.setText("Se encontraron " + resultado.filas().size()
                    + " turnos que cumplen simultáneamente todos los filtros seleccionados.");
        }
    }

    private void actualizarVistas(Resultado resultado) {
        int turnosFiltrados = resultado.filas().size();
        boolean pacientesUnicos = "Pacientes únicos".equals(valores.getSelectedItem());
        int totalFiltrado = pacientesUnicos ? contarPacientesUnicos(resultado.filas()) : turnosFiltrados;
        contadorResultado.setText(totalFiltrado + (pacientesUnicos
                ? (totalFiltrado == 1 ? " paciente único" : " pacientes únicos")
                : (totalFiltrado == 1 ? " turno encontrado" : " turnos encontrados")));
        String agrupacion = (String) agruparPor.getSelectedItem();
        boolean esTotal = "Total".equals(agrupacion);
        boolean porcentaje = "Porcentaje".equals(valores.getSelectedItem());
        boolean lineas = "Mes".equals(agrupacion);
        String estadoUnico = resultado.porEstado().size() == 1
                ? resultado.porEstado().get(0).etiqueta() : null;
        int basePorcentaje = porcentaje && "Estado".equals(agrupacion)
                ? resultado.filasBaseEstado().size() : turnosFiltrados;
        LinkedHashMap<String, Integer> datos;

        switch (agrupacion) {
            case "Médico" -> datos = pacientesUnicos ? agruparPacientesUnicosPorCampo(resultado.filas(), 2) : aMapa(resultado.porMedico());
            case "Paciente" -> datos = pacientesUnicos ? agruparPacientesUnicosPorCampo(resultado.filas(), 1) : agruparPorCampo(resultado.filas(), 1);
            case "Obra social" -> datos = pacientesUnicos ? agruparPacientesUnicosPorCampo(resultado.filas(), 4) : aMapa(resultado.porObraSocial());
            case "Estado" -> datos = pacientesUnicos ? agruparPacientesUnicosPorCampo(resultado.filas(), 5) : aMapa(resultado.porEstado());
            case "Mes" -> datos = pacientesUnicos ? agruparPacientesUnicosPorMes(resultado.filas()) : agruparPorMes(resultado.filas());
            default -> {
                datos = new LinkedHashMap<>();
                datos.put("Total", totalFiltrado);
            }
        }

        String nombreAgrupacion = switch (agrupacion) {
            case "Médico" -> "médico";
            case "Paciente" -> "paciente";
            case "Obra social" -> "obra social";
            case "Estado" -> "estado del turno";
            case "Mes" -> "mes";
            default -> "";
        };
        if (esTotal) {
            tituloResultado.setText(porcentaje ? "Porcentaje total de la consulta"
                    : pacientesUnicos ? "Pacientes únicos de la consulta" : "Resultado de la consulta");
            if (porcentaje) {
                contadorResultado.setText((turnosFiltrados == 0 ? "0%" : "100%") + " ("
                        + turnosFiltrados + (turnosFiltrados == 1 ? " turno encontrado)" : " turnos encontrados)"));
            }
        } else if (lineas && estadoUnico != null) {
            tituloResultado.setText(pacientesUnicos ? "Pacientes únicos por mes" : tituloMensual(estadoUnico));
            contadorResultado.setText("TOTAL DEL PERÍODO: " + totalFiltrado + " "
                    + nombreResultado(estadoUnico, totalFiltrado));
        } else if (lineas) {
            tituloResultado.setText("Cantidad de turnos por mes");
        } else if ("Paciente".equals(agrupacion) && estadoUnico != null && !porcentaje) {
            tituloResultado.setText(tituloPorPaciente(estadoUnico));
        } else {
            tituloResultado.setText((porcentaje ? "Porcentaje" : pacientesUnicos ? "Pacientes únicos" : "Cantidad de turnos")
                    + " por " + nombreAgrupacion);
        }
        if (estadosTurno.valoresParaFiltro() != null
                && estadosTurno.valoresParaFiltro().contains("__sin_asistencia__") && !porcentaje) {
            tituloResultado.setText("Turnos sin atención realizada");
            contadorResultado.setText(totalFiltrado + (totalFiltrado == 1
                    ? " turno sin atención" : " turnos sin atención"));
        }
        if (porcentaje && !esTotal) {
            contadorResultado.setText("Base de cálculo: " + basePorcentaje
                    + (basePorcentaje == 1 ? " turno = 100%" : " turnos = 100%"));
        }
        if (porcentaje && "Estado".equals(agrupacion) && basePorcentaje != totalFiltrado) {
            int porcentajeFiltrado = porcentaje(totalFiltrado, basePorcentaje);
            contadorResultado.setText("RESULTADO: " + totalFiltrado + " DE " + basePorcentaje
                    + " TURNOS (" + porcentajeFiltrado + "%)");
        }
        LinkedHashMap<String, Integer> datosMostrados =
                ajustarPorcentaje(datos, basePorcentaje, porcentaje);
        prepararResultadosExportacion(agrupacion, datos, datosMostrados, porcentaje, pacientesUnicos);
        actualizarResumenCategorias(datos, porcentaje);
        graficoPrincipal.setPorcentajes(porcentaje);
        graficoPrincipal.setDatos(datosMostrados, lineas);
    }

    private void actualizarResumenCategorias(LinkedHashMap<String, Integer> datos,
            boolean porcentaje) {
        resumenCategorias.removeAll();
        resumenCategorias.revalidate();
        resumenCategorias.repaint();
    }

    private void prepararResultadosExportacion(String agrupacion,
            LinkedHashMap<String, Integer> cantidades,
            LinkedHashMap<String, Integer> datosMostrados,
            boolean porcentaje, boolean pacientesUnicos) {
        String primeraColumna = switch (agrupacion) {
            case "Médico" -> "Médico";
            case "Paciente" -> "Paciente";
            case "Obra social" -> "Obra social";
            case "Estado" -> "Estado del turno";
            case "Mes" -> "Mes";
            default -> "Resultado";
        };
        encabezadosExportacion = porcentaje
                ? new String[]{primeraColumna, "Cantidad de turnos", "Porcentaje"}
                : new String[]{primeraColumna, pacientesUnicos ? "Pacientes únicos" : "Cantidad de turnos"};
        resultadosExportacion = new ArrayList<>();
        cantidades.forEach((etiqueta, cantidad) -> {
            if (porcentaje) {
                resultadosExportacion.add(new String[]{etiqueta, String.valueOf(cantidad),
                    datosMostrados.getOrDefault(etiqueta, 0) + "%"});
            } else {
                resultadosExportacion.add(new String[]{etiqueta, String.valueOf(cantidad)});
            }
        });
    }

    private LinkedHashMap<String, Integer> ajustarPorcentaje(LinkedHashMap<String, Integer> datos,
            int total, boolean usarPorcentaje) {
        if (!usarPorcentaje) return datos;
        return calcularPorcentajesCoherentes(datos, total);
    }

    /**
     * Distribuye los decimales por resto mayor. Así, cuando las categorías
     * cubren todo el resultado, los porcentajes enteros suman exactamente 100.
     */
    private LinkedHashMap<String, Integer> calcularPorcentajesCoherentes(
            LinkedHashMap<String, Integer> datos, int total) {
        LinkedHashMap<String, Integer> resultado = new LinkedHashMap<>();
        if (total <= 0 || datos.isEmpty()) {
            datos.keySet().forEach(etiqueta -> resultado.put(etiqueta, 0));
            return resultado;
        }

        Map<String, Double> restos = new LinkedHashMap<>();
        int sumaCantidades = 0;
        int sumaEnteros = 0;
        for (Map.Entry<String, Integer> entrada : datos.entrySet()) {
            int cantidad = Math.max(0, entrada.getValue());
            double exacto = cantidad * 100.0 / total;
            int entero = (int) Math.floor(exacto);
            resultado.put(entrada.getKey(), entero);
            restos.put(entrada.getKey(), exacto - entero);
            sumaCantidades += cantidad;
            sumaEnteros += entero;
        }

        int objetivo = (int) Math.round(sumaCantidades * 100.0 / total);
        List<String> orden = new ArrayList<>(datos.keySet());
        orden.sort((a, b) -> Double.compare(restos.get(b), restos.get(a)));
        int unidades = Math.max(0, objetivo - sumaEnteros);
        for (int i = 0; i < unidades && i < orden.size(); i++) {
            String etiqueta = orden.get(i);
            resultado.put(etiqueta, resultado.get(etiqueta) + 1);
        }
        return resultado;
    }

    private LinkedHashMap<String, Integer> agruparPorMes(List<String[]> filas) {
        Map<String, Integer> ordenados = new TreeMap<>();
        for (String[] fila : filas) {
            String[] partes = fila[0].split("/");
            if (partes.length != 3) continue;
            String clave = partes[2] + partes[1];
            ordenados.merge(clave, 1, Integer::sum);
        }
        LinkedHashMap<String, Integer> resultado = new LinkedHashMap<>();
        ordenados.forEach((clave, cantidad) ->
                resultado.put(nombreMes(clave), cantidad));
        return resultado;
    }

    private int contarPacientesUnicos(List<String[]> filas) {
        return (int) filas.stream().map(fila -> fila[6]).filter(id -> id != null && !id.isBlank()).distinct().count();
    }

    private LinkedHashMap<String, Integer> agruparPacientesUnicosPorCampo(List<String[]> filas, int indiceEtiqueta) {
        Map<String, Set<String>> pacientesPorGrupo = new LinkedHashMap<>();
        for (String[] fila : filas) {
            pacientesPorGrupo.computeIfAbsent(fila[indiceEtiqueta], clave -> new java.util.HashSet<>()).add(fila[6]);
        }
        return ordenarPorCantidad(pacientesPorGrupo);
    }

    private LinkedHashMap<String, Integer> agruparPacientesUnicosPorMes(List<String[]> filas) {
        Map<String, Set<String>> pacientesPorMes = new TreeMap<>();
        for (String[] fila : filas) {
            String[] partes = fila[0].split("/");
            if (partes.length == 3) pacientesPorMes.computeIfAbsent(partes[2] + partes[1], clave -> new java.util.HashSet<>()).add(fila[6]);
        }
        LinkedHashMap<String, Integer> resultado = new LinkedHashMap<>();
        pacientesPorMes.forEach((clave, pacientes) -> resultado.put(nombreMes(clave), pacientes.size()));
        return resultado;
    }

    private LinkedHashMap<String, Integer> ordenarPorCantidad(Map<String, Set<String>> pacientesPorGrupo) {
        LinkedHashMap<String, Integer> resultado = new LinkedHashMap<>();
        pacientesPorGrupo.entrySet().stream()
                .sorted((a, b) -> {
                    int comparacion = Integer.compare(b.getValue().size(), a.getValue().size());
                    return comparacion != 0 ? comparacion : a.getKey().compareToIgnoreCase(b.getKey());
                })
                .forEach(entrada -> resultado.put(entrada.getKey(), entrada.getValue().size()));
        return resultado;
    }

    private LinkedHashMap<String, Integer> agruparPorCampo(List<String[]> filas, int indice) {
        Map<String, Long> cantidades = filas.stream()
                .collect(Collectors.groupingBy(fila -> fila[indice], LinkedHashMap::new, Collectors.counting()));
        LinkedHashMap<String, Integer> resultado = new LinkedHashMap<>();
        cantidades.entrySet().stream()
                .sorted((a, b) -> {
                    int porCantidad = Long.compare(b.getValue(), a.getValue());
                    return porCantidad != 0 ? porCantidad : a.getKey().compareToIgnoreCase(b.getKey());
                })
                .forEach(e -> resultado.put(e.getKey(), e.getValue().intValue()));
        return resultado;
    }

    private String tituloMensual(String estado) {
        return switch (estado.toLowerCase()) {
            case "ausente" -> "Cantidad de ausencias por mes";
            case "atendido" -> "Cantidad de turnos atendidos por mes";
            case "cancelado" -> "Cantidad de cancelaciones por mes";
            case "reservado" -> "Cantidad de turnos reservados por mes";
            case "esperando" -> "Cantidad de turnos en espera por mes";
            case "en consulta" -> "Cantidad de turnos en consulta por mes";
            default -> "Cantidad de turnos por mes";
        };
    }

    private String tituloPorPaciente(String estado) {
        return switch (estado.toLowerCase()) {
            case "ausente" -> "Cantidad de ausencias por paciente";
            case "atendido" -> "Cantidad de turnos atendidos por paciente";
            case "cancelado" -> "Cantidad de cancelaciones por paciente";
            case "reservado" -> "Cantidad de turnos reservados por paciente";
            default -> "Cantidad de turnos por paciente";
        };
    }

    private String nombreResultado(String estado, int cantidad) {
        boolean singular = cantidad == 1;
        return switch (estado.toLowerCase()) {
            case "ausente" -> singular ? "AUSENCIA" : "AUSENCIAS";
            case "atendido" -> singular ? "TURNO ATENDIDO" : "TURNOS ATENDIDOS";
            case "cancelado" -> singular ? "CANCELACIÓN" : "CANCELACIONES";
            case "reservado" -> singular ? "TURNO RESERVADO" : "TURNOS RESERVADOS";
            case "esperando" -> singular ? "TURNO EN ESPERA" : "TURNOS EN ESPERA";
            case "en consulta" -> singular ? "TURNO EN CONSULTA" : "TURNOS EN CONSULTA";
            default -> singular ? "TURNO" : "TURNOS";
        };
    }

    private String nombreMes(String clave) {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        int mes = Integer.parseInt(clave.substring(4, 6));
        return meses[mes - 1] + " " + clave.substring(0, 4);
    }

    private int porcentaje(int cantidad, int total) {
        return total == 0 ? 0 : (int) Math.round(cantidad * 100.0 / total);
    }

    private LinkedHashMap<String, Integer> aMapa(List<Punto> puntos) {
        LinkedHashMap<String, Integer> datos = new LinkedHashMap<>();
        for (Punto punto : puntos) datos.put(punto.etiqueta(), punto.valor());
        return datos;
    }

    private void exportar() {
        if (datosFiltrados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay resultados para exportar.", "Exportar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("estadisticas_turnos.xlsx"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".xlsx")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".xlsx");
        }
        try {
            TurnosExcelExporter.exportar(archivo, encabezadosExportacion,
                    resultadosExportacion, filtrosAplicados);
            JOptionPane.showMessageDialog(this, "Archivo exportado correctamente:\n" + archivo.getAbsolutePath());
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private String formato(Date fecha) {
        return new SimpleDateFormat("dd/MM/yyyy").format(fecha);
    }

    private static String capitalizar(String texto) {
        return texto == null || texto.isBlank() ? ""
                : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private void mostrarError(Exception error) {
        Throwable causa = error;
        while (causa.getCause() != null) causa = causa.getCause();
        ayuda.setText("No se pudo completar la consulta.");
        JOptionPane.showMessageDialog(this, causa.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private record ItemSeleccion<T>(T valor, String etiqueta, String busqueda) {
        ItemSeleccion(T valor, String etiqueta) { this(valor, etiqueta, etiqueta); }
    }

    private static class SelectorMultiple<T> extends JButton {
        private final String textoTodos;
        private final JPopupMenu popup = new JPopupMenu();
        private final JPanel lista = new JPanel();
        private final JScrollPane scroll = new JScrollPane(lista);
        private final JCheckBox todos = new JCheckBox("Todos");
        private final JTextField buscar = new JTextField();
        private final List<ItemSeleccion<T>> opciones = new ArrayList<>();
        private final List<JCheckBox> casillas = new ArrayList<>();
        private final boolean permitirBusqueda;
        private boolean actualizando;

        SelectorMultiple(String textoTodos, boolean permitirBusqueda) {
            this.textoTodos = textoTodos;
            this.permitirBusqueda = permitirBusqueda;
            todos.setText("Todos");
            todos.setToolTipText(textoTodos);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setOpaque(true);
            setContentAreaFilled(true);
            setBackground(VERDE);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(VERDE_OSCURO), new EmptyBorder(0, 9, 0, 7)));
            construirPopup();
            addActionListener(e -> mostrarPopup());
            actualizarTexto();
        }

        private void construirPopup() {
            JPanel contenido = new JPanel(new BorderLayout(0, 5));
            contenido.setBackground(Color.WHITE);
            contenido.setBorder(new EmptyBorder(8, 9, 8, 9));
            todos.setBackground(Color.WHITE);
            todos.setFont(new Font("Segoe UI", Font.BOLD, 13));
            todos.addActionListener(e -> {
                if (actualizando) return;
                actualizando = true;
                for (JCheckBox casilla : casillas) casilla.setSelected(todos.isSelected());
                actualizando = false;
                actualizarTexto();
            });
            JPanel cabecera = new JPanel();
            cabecera.setOpaque(false);
            cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
            if (permitirBusqueda) {
                JLabel etiquetaBuscar = new JLabel(textoTodos.contains("pacientes")
                        ? "Buscar por nombre o DNI" : "Buscar por nombre");
                etiquetaBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                etiquetaBuscar.setForeground(new Color(85, 96, 101));
                buscar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                buscar.getDocument().addDocumentListener(new DocumentListener() {
                    @Override public void insertUpdate(DocumentEvent e) { filtrarOpciones(); }
                    @Override public void removeUpdate(DocumentEvent e) { filtrarOpciones(); }
                    @Override public void changedUpdate(DocumentEvent e) { filtrarOpciones(); }
                });
                cabecera.add(etiquetaBuscar);
                cabecera.add(Box.createVerticalStrut(2));
                cabecera.add(buscar);
                cabecera.add(Box.createVerticalStrut(5));
            }
            cabecera.add(todos);
            lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
            lista.setBackground(Color.WHITE);
            scroll.setBorder(BorderFactory.createLineBorder(new Color(225, 229, 231)));
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUnitIncrement(14);
            JButton listo = new JButton("Listo");
            listo.setBackground(VERDE);
            listo.setForeground(Color.WHITE);
            listo.setFocusPainted(false);
            listo.addActionListener(e -> popup.setVisible(false));
            JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pie.setOpaque(false);
            pie.add(listo);
            contenido.add(cabecera, BorderLayout.NORTH);
            contenido.add(scroll, BorderLayout.CENTER);
            contenido.add(pie, BorderLayout.SOUTH);
            popup.setBorder(BorderFactory.createLineBorder(new Color(170, 181, 186)));
            popup.add(contenido);
        }

        void setOpciones(List<ItemSeleccion<T>> nuevasOpciones) {
            opciones.clear();
            opciones.addAll(nuevasOpciones);
            casillas.clear();
            lista.removeAll();
            actualizando = true;
            for (ItemSeleccion<T> opcion : opciones) {
                JCheckBox casilla = new JCheckBox(etiquetaParaPopup(opcion.etiqueta()), true);
                casilla.setBackground(Color.WHITE);
                casilla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                casilla.setAlignmentX(Component.LEFT_ALIGNMENT);
                casilla.setToolTipText(opcion.etiqueta());
                casilla.addActionListener(e -> {
                    if (!actualizando) sincronizarTodos();
                });
                casillas.add(casilla);
                lista.add(casilla);
            }
            todos.setSelected(!casillas.isEmpty());
            actualizando = false;
            lista.revalidate();
            lista.repaint();
            actualizarTexto();
        }

        private void filtrarOpciones() {
            if (!permitirBusqueda) return;
            String texto = buscar.getText().trim().toLowerCase();
            for (int i = 0; i < casillas.size(); i++) {
                casillas.get(i).setVisible(texto.isEmpty()
                        || opciones.get(i).busqueda().toLowerCase().contains(texto));
            }
            lista.revalidate();
            lista.repaint();
        }

        void seleccionarTodos() {
            actualizando = true;
            todos.setSelected(true);
            for (JCheckBox casilla : casillas) casilla.setSelected(true);
            actualizando = false;
            actualizarTexto();
        }

        void limpiarSeleccion() {
            actualizando = true;
            todos.setSelected(false);
            for (JCheckBox casilla : casillas) casilla.setSelected(false);
            actualizando = false;
            actualizarTexto();
        }

        List<T> valoresParaFiltro() {
            long seleccionadas = casillas.stream().filter(JCheckBox::isSelected).count();
            if (seleccionadas == 0 || seleccionadas == casillas.size()) return null;
            List<T> valores = new ArrayList<>();
            for (int i = 0; i < casillas.size(); i++) {
                if (casillas.get(i).isSelected()) valores.add(opciones.get(i).valor());
            }
            return valores;
        }

        String descripcionSeleccion() {
            long seleccionadas = casillas.stream().filter(JCheckBox::isSelected).count();
            if (seleccionadas == 0) return "Sin filtro";
            if (seleccionadas == casillas.size()) return textoTodos;
            List<String> etiquetas = new ArrayList<>();
            for (int i = 0; i < casillas.size(); i++) {
                if (casillas.get(i).isSelected()) etiquetas.add(opciones.get(i).etiqueta());
            }
            return etiquetas.stream().collect(Collectors.joining(", "));
        }

        int cantidadSeleccionada() {
            return (int) casillas.stream().filter(JCheckBox::isSelected).count();
        }

        private void sincronizarTodos() {
            actualizando = true;
            todos.setSelected(!casillas.isEmpty() && casillas.stream().allMatch(JCheckBox::isSelected));
            actualizando = false;
            actualizarTexto();
        }

        private void actualizarTexto() {
            if (opciones.isEmpty()) {
                setText("Cargando...");
                return;
            }
            int seleccionadas = (int) casillas.stream().filter(JCheckBox::isSelected).count();
            if (todos.isSelected() && casillas.size() == 1) setText(opciones.get(0).etiqueta());
            else if (todos.isSelected()) setText("Todos");
            else if (seleccionadas == 0) setText("Sin filtro");
            else if (seleccionadas == 1) {
                int indice = -1;
                for (int i = 0; i < casillas.size(); i++) if (casillas.get(i).isSelected()) { indice = i; break; }
                setText(opciones.get(indice).etiqueta());
            } else setText(seleccionadas + " seleccionados");
            setToolTipText(descripcionSeleccion());
        }

        private void mostrarPopup() {
            if (!isShowing() || opciones.isEmpty()) return;
            int anchoPopup = Math.max(360, getWidth());
            long visibles = casillas.stream().filter(Component::isVisible).count();
            int altoContenido = 6;
            for (int i = 0; i < casillas.size(); i++) {
                if (casillas.get(i).isVisible()) altoContenido += altoOpcion(opciones.get(i).etiqueta());
            }
            int altoLista = Math.min(230, Math.max(90, altoContenido));
            Dimension tamanioLista = new Dimension(Math.max(100, anchoPopup - 18), altoLista);
            scroll.setPreferredSize(tamanioLista);
            scroll.setMinimumSize(tamanioLista);
            for (int i = 0; i < casillas.size(); i++) {
                int altoOpcion = altoOpcion(opciones.get(i).etiqueta());
                casillas.get(i).setPreferredSize(new Dimension(tamanioLista.width, altoOpcion));
                casillas.get(i).setMaximumSize(new Dimension(tamanioLista.width, altoOpcion));
            }
            popup.setPopupSize(anchoPopup, altoLista + (permitirBusqueda ? 146 : 98));
            popup.show(this, 0, getHeight());
        }

        private int altoOpcion(String etiqueta) {
            return etiqueta != null && etiqueta.length() > 27 ? 44 : 28;
        }

        private String etiquetaParaPopup(String etiqueta) {
            if (etiqueta == null || etiqueta.length() <= 27) return etiqueta;
            int corte = etiqueta.lastIndexOf(' ', 27);
            if (corte < 12) corte = 27;
            return "<html>" + etiqueta.substring(0, corte) + "<br>" + etiqueta.substring(corte).trim() + "</html>";
        }
    }
    /** Mantiene el ancho de la ventana y habilita desplazamiento sólo vertical. */
    private static final class PaginaDesplazable extends JPanel implements Scrollable {
        PaginaDesplazable(BorderLayout layout) {
            super(layout);
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return Math.max(120, r.height - 48);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private static class GraficoObrasSociales extends JPanel implements Scrollable {
        private static final Color[] COLORES = {
            new Color(0, 121, 107), new Color(25, 118, 210), new Color(245, 124, 0),
            new Color(123, 31, 162), new Color(198, 40, 40), new Color(46, 125, 50),
            new Color(0, 137, 123), new Color(94, 53, 177)
        };
        private final LinkedHashMap<String, LinkedHashMap<String, Integer>> datos = new LinkedHashMap<>();
        private boolean lineas;
        private boolean porcentajes;
        private boolean barrasHorizontales;

        GraficoObrasSociales() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(230, 233, 235)));
        }

        void setDatos(LinkedHashMap<String, Integer> nuevosDatos, boolean mostrarLineas) {
            LinkedHashMap<String, LinkedHashMap<String, Integer>> unaSerie = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entrada : nuevosDatos.entrySet()) {
                LinkedHashMap<String, Integer> valor = new LinkedHashMap<>();
                valor.put("Total", entrada.getValue());
                unaSerie.put(entrada.getKey(), valor);
            }
            setSeries(unaSerie, mostrarLineas);
        }

        void setSeries(LinkedHashMap<String, LinkedHashMap<String, Integer>> nuevosDatos,
                       boolean mostrarLineas) {
            datos.clear();
            for (Map.Entry<String, LinkedHashMap<String, Integer>> entrada : nuevosDatos.entrySet()) {
                datos.put(entrada.getKey(), new LinkedHashMap<>(entrada.getValue()));
            }
            lineas = mostrarLineas;
            // Con muchas categorías, las barras verticales hacen ilegibles los
            // nombres: se cambia a barras horizontales con desplazamiento vertical.
            barrasHorizontales = !lineas && datos.size() > 12;
            int altura = barrasHorizontales ? Math.max(520, datos.size() * 38 + 48) : 520;
            setPreferredSize(new Dimension(1100, altura));
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(20, visibleRect.width - 20);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return !barrasHorizontales;
        }

        void setPorcentajes(boolean porcentajes) {
            this.porcentajes = porcentajes;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (datos.isEmpty()) {
                g2.setColor(new Color(115, 123, 128));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String mensaje = "Sin resultados para la selección actual";
                g2.drawString(mensaje, Math.max(20,
                        (getWidth() - g2.getFontMetrics().stringWidth(mensaje)) / 2), getHeight() / 2);
                g2.dispose();
                return;
            }

            List<String> nombresSeries = new ArrayList<>(datos.values().iterator().next().keySet());
            if (barrasHorizontales) {
                dibujarBarrasHorizontales(g2, nombresSeries);
                g2.dispose();
                return;
            }
            boolean mostrarLeyenda = nombresSeries.size() > 1;
            int izquierda = 62;
            int derecha = 28;
            int arriba = mostrarLeyenda ? 64 : 34;
            int abajo = 96;
            int anchoGrafico = Math.max(100, getWidth() - izquierda - derecha);
            int altoGrafico = Math.max(100, getHeight() - arriba - abajo);
            int baseY = arriba + altoGrafico;
            int maximo = 1;
            for (Map<String, Integer> categoria : datos.values()) {
                for (Integer valor : categoria.values()) maximo = Math.max(maximo, valor);
            }

            if (mostrarLeyenda) dibujarLeyenda(g2, nombresSeries);
            dibujarEscala(g2, izquierda, arriba, anchoGrafico, altoGrafico, baseY, maximo);

            int cantidadCategorias = datos.size();
            double espacio = anchoGrafico / (double) Math.max(1, cantidadCategorias);
            if (lineas) {
                dibujarLineas(g2, nombresSeries, izquierda, espacio, altoGrafico, baseY, maximo);
            } else {
                dibujarBarras(g2, nombresSeries, izquierda, espacio, altoGrafico, baseY, maximo);
            }

            int indice = 0;
            for (String categoria : datos.keySet()) {
                int centroX = izquierda + (int) Math.round((indice + 0.5) * espacio);
                dibujarEtiqueta(g2, categoria, centroX, baseY + 20,
                        Math.max(20, (int) espacio - 8), espacio < 55 ? 4 : 3);
                indice++;
            }
            g2.dispose();
        }

        private void dibujarBarras(Graphics2D g2, List<String> series, int izquierda,
                                   double espacio, int alto, int baseY, int maximo) {
            int cantidadSeries = Math.max(1, series.size());
            int anchoGrupo = Math.max(4, (int) (espacio * 0.72));
            int anchoBarra = Math.max(2, Math.min(55, anchoGrupo / cantidadSeries - 2));
            int indiceCategoria = 0;
            for (Map<String, Integer> categoria : datos.values()) {
                int centro = izquierda + (int) Math.round((indiceCategoria + 0.5) * espacio);
                int anchoReal = anchoBarra * cantidadSeries;
                int inicio = centro - anchoReal / 2;
                for (int s = 0; s < series.size(); s++) {
                    int valor = categoria.getOrDefault(series.get(s), 0);
                    int altura = (int) Math.round(alto * (valor / (double) maximo));
                    int x = inicio + s * anchoBarra;
                    int y = baseY - altura;
                    g2.setColor(COLORES[s % COLORES.length]);
                    g2.fillRoundRect(x, y, Math.max(2, anchoBarra - 2), Math.max(2, altura), 7, 7);
                    if (valor > 0) dibujarValor(g2, valor, x + (anchoBarra - 3) / 2, y);
                }
                indiceCategoria++;
            }
        }

        /** Vista legible para listados largos, por ejemplo al agrupar por paciente. */
        private void dibujarBarrasHorizontales(Graphics2D g2, List<String> series) {
            int maximo = 1;
            for (Map<String, Integer> categoria : datos.values()) {
                for (Integer valor : categoria.values()) maximo = Math.max(maximo, valor);
            }
            int izquierdo = Math.min(290, Math.max(190, getWidth() / 4));
            int derecho = 70;
            int anchoBarras = Math.max(120, getWidth() - izquierdo - derecho);
            int altoFila = 34;
            int yInicial = 28;
            int indice = 0;
            for (Map.Entry<String, LinkedHashMap<String, Integer>> entrada : datos.entrySet()) {
                int y = yInicial + indice * altoFila;
                if (indice % 2 == 0) {
                    g2.setColor(new Color(248, 250, 250));
                    g2.fillRect(4, y - 16, getWidth() - 8, altoFila);
                }
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.setColor(new Color(55, 65, 70));
                g2.drawString(textoAcotado(g2.getFontMetrics(), entrada.getKey(), izquierdo - 20), 14, y + 4);

                int cantidadSeries = Math.max(1, series.size());
                int altoBarra = Math.max(7, 18 / cantidadSeries);
                int desplazamiento = -(cantidadSeries * altoBarra + (cantidadSeries - 1) * 2) / 2;
                for (int s = 0; s < series.size(); s++) {
                    int valor = entrada.getValue().getOrDefault(series.get(s), 0);
                    int ancho = (int) Math.round(anchoBarras * (valor / (double) maximo));
                    int yBarra = y + desplazamiento + s * (altoBarra + 2);
                    g2.setColor(COLORES[s % COLORES.length]);
                    g2.fillRoundRect(izquierdo, yBarra, Math.max(1, ancho), altoBarra, altoBarra, altoBarra);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g2.setColor(new Color(42, 52, 57));
                    String textoValor = valor + (porcentajes ? "%" : "");
                    g2.drawString(textoValor, Math.min(getWidth() - 48, izquierdo + ancho + 8), yBarra + altoBarra - 1);
                }
                indice++;
            }
        }

        private String textoAcotado(FontMetrics metricas, String texto, int anchoMaximo) {
            return texto == null ? "" : texto;
        }

        private void dibujarLineas(Graphics2D g2, List<String> series, int izquierda,
                                   double espacio, int alto, int baseY, int maximo) {
            for (int s = 0; s < series.size(); s++) {
                int anteriorX = -1;
                int anteriorY = -1;
                int indiceCategoria = 0;
                Color colorSerie = COLORES[s % COLORES.length];
                for (Map<String, Integer> categoria : datos.values()) {
                    int valor = categoria.getOrDefault(series.get(s), 0);
                    int x = izquierda + (int) Math.round((indiceCategoria + 0.5) * espacio);
                    int y = baseY - (int) Math.round(alto * (valor / (double) maximo));
                    g2.setColor(colorSerie);
                    g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    if (anteriorX >= 0) g2.drawLine(anteriorX, anteriorY, x, y);
                    g2.fillOval(x - 5, y - 5, 10, 10);
                    dibujarValor(g2, valor, x, y - (s % 2) * 15);
                    anteriorX = x;
                    anteriorY = y;
                    indiceCategoria++;
                }
            }
        }

        private void dibujarLeyenda(Graphics2D g2, List<String> series) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            int x = 66;
            int y = 26;
            for (int i = 0; i < series.size(); i++) {
                String nombre = series.get(i);
                int ancho = g2.getFontMetrics().stringWidth(nombre) + 30;
                if (x + ancho > getWidth() - 25) {
                    x = 66;
                    y += 20;
                }
                g2.setColor(COLORES[i % COLORES.length]);
                g2.fillRoundRect(x, y - 10, 16, 8, 4, 4);
                g2.setColor(new Color(55, 65, 70));
                g2.drawString(nombre, x + 21, y - 2);
                x += ancho;
            }
        }

        private void dibujarEscala(Graphics2D g2, int izquierda, int arriba, int ancho,
                                   int alto, int baseY, int maximo) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            int divisiones = Math.min(4, maximo);
            for (int i = 0; i <= divisiones; i++) {
                int valor = (int) Math.round(maximo * (i / (double) divisiones));
                int y = baseY - (int) Math.round(alto * (i / (double) divisiones));
                g2.setColor(new Color(232, 235, 237));
                g2.drawLine(izquierda, y, izquierda + ancho, y);
                g2.setColor(new Color(95, 105, 110));
                String texto = String.valueOf(valor);
                g2.drawString(texto, izquierda - 10 - g2.getFontMetrics().stringWidth(texto), y + 4);
            }
            g2.setColor(new Color(196, 203, 207));
            g2.drawLine(izquierda, arriba, izquierda, baseY);
            g2.drawLine(izquierda, baseY, izquierda + ancho, baseY);
        }

        private void dibujarValor(Graphics2D g2, int valor, int centroX, int y) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(new Color(42, 52, 57));
            String texto = valor + (porcentajes ? "%" : "");
            int x = centroX - g2.getFontMetrics().stringWidth(texto) / 2;
            g2.drawString(texto, x, Math.max(18, y - 9));
        }

        private void dibujarEtiqueta(Graphics2D g2, String texto, int centroX, int yInicial,
                                     int anchoMaximo, int maximoLineas) {
            int tamanoFuente = anchoMaximo < 40 ? 9 : anchoMaximo < 65 ? 10 : 12;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, tamanoFuente));
            g2.setColor(new Color(55, 65, 70));
            List<String> lineasTexto = dividirEnLineas(g2.getFontMetrics(), texto, anchoMaximo, maximoLineas);
            int y = yInicial;
            for (String linea : lineasTexto) {
                int x = centroX - g2.getFontMetrics().stringWidth(linea) / 2;
                g2.drawString(linea, Math.max(4, x), y);
                y += g2.getFontMetrics().getHeight();
            }
        }

        private List<String> dividirEnLineas(FontMetrics metricas, String texto,
                                             int anchoMaximo, int maximoLineas) {
            List<String> resultado = new ArrayList<>();
            if (texto == null || texto.isBlank()) return resultado;
            String[] palabras = texto.trim().split("\\s+");
            StringBuilder actual = new StringBuilder();
            for (String palabra : palabras) {
                String candidato = actual.length() == 0 ? palabra : actual + " " + palabra;
                if (metricas.stringWidth(candidato) <= anchoMaximo || actual.length() == 0) {
                    actual.setLength(0);
                    actual.append(candidato);
                } else {
                    resultado.add(actual.toString());
                    actual.setLength(0);
                    actual.append(palabra);
                }
            }
            if (actual.length() > 0) resultado.add(actual.toString());
            return resultado;
        }
    }
}
