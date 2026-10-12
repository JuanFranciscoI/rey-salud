package clinicabalbarrey;

import clinicabalbarrey.dao.TurnoDAO;
import clinicabalbarrey.dao.TurnoDAO.FiltroHistorial;
import clinicabalbarrey.dao.TurnoDAO.OpcionHistorial;
import clinicabalbarrey.model.Usuario;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/** Pantalla de solo lectura para verificar todos los turnos existentes. */
public class TodosLosTurnosPanel extends JPanel {

    private static final Color VERDE = new Color(0, 128, 111);
    private static final int TAMANIO_PAGINA = 10;

    private final MainApplicationView mainApp;
    private final JDateChooser fechaDesde = new JDateChooser();
    private final JDateChooser fechaHasta = new JDateChooser();
    private final SelectorMultiple<Integer> filtroMedicos = new SelectorMultiple<>("Todos los médicos", true);
    private final SelectorMultiple<Integer> filtroPacientes = new SelectorMultiple<>("Todos los pacientes", true);
    private final SelectorMultiple<Integer> filtroObrasSociales = new SelectorMultiple<>("Todas las obras sociales", true);
    private final SelectorMultiple<String> filtroEstados = new SelectorMultiple<>("Todos los estados", false);
    private final JCheckBox chkPacientesUnicos = new JCheckBox("Ver pacientes únicos");
    private final JLabel lblResultado = new JLabel("Cargando turnos...");
    private final JLabel lblPagina = new JLabel("Página 1 de 1", SwingConstants.CENTER);
    private final JButton btnAnterior = crearBoton("< ANTERIOR", false);
    private final JButton btnSiguiente = crearBoton("SIGUIENTE >", false);
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private JScrollPane scrollTabla;
    private int paginaActual = 1;
    private int totalTurnos;
    private boolean vistaPacientesUnicos;
    private boolean catalogosCargados;
    private boolean catalogosSesionInicializada;
    private Integer idMedicoCatalogos;

    public TodosLosTurnosPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(new Color(242, 245, 247));

        String[] columnas = {"ID", "Fecha", "Hora", "Paciente", "DNI", "Médico",
            "Obra social", "Estado", "Historia clínica"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);
        configurarAtajos();
    }

    public void cargarTurnos() {
        Integer idMedicoSesion = idPersonalPermitido();
        if (!catalogosSesionInicializada || !Objects.equals(idMedicoCatalogos, idMedicoSesion)) {
            // Este panel se reutiliza al cerrar e iniciar sesion. Los catalogos de
            // un administrador no deben quedar visibles cuando ingresa un medico.
            catalogosCargados = false;
            idMedicoCatalogos = idMedicoSesion;
            catalogosSesionInicializada = true;
            filtroMedicos.setEnabled(true);
        }
        paginaActual = 1;
        cargarDatos(!catalogosCargados);
    }

    private JComponent crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout(20, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(22, 28, 20, 28));

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("TODOS LOS TURNOS");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 25));
        titulo.setForeground(new Color(0, 92, 79));
        titulos.add(titulo);
        panel.add(titulos, BorderLayout.NORTH);

        configurarFecha(fechaDesde);
        configurarFecha(fechaHasta);
        fechaDesde.setPreferredSize(new Dimension(145, 38));
        fechaHasta.setPreferredSize(new Dimension(145, 38));
        // Los selectores deben permitir leer nombres completos sin que el popup
        // vuelva a ocupar media pantalla. El desplegable toma este mismo ancho.
        filtroMedicos.configurarAnchos(105, 200);
        filtroPacientes.configurarAnchos(105, 220);
        filtroObrasSociales.configurarAnchos(105, 245);
        filtroEstados.configurarAnchos(105, 180);

        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setOpaque(false);
        agregarCampoFiltro(filtros, "Desde", fechaDesde, 0);
        agregarCampoFiltro(filtros, "Hasta", fechaHasta, 1);
        agregarCampoFiltro(filtros, "Médicos", filtroMedicos, 2);
        agregarCampoFiltro(filtros, "Pacientes", filtroPacientes, 3);
        agregarCampoFiltro(filtros, "Obras sociales", filtroObrasSociales, 4);
        agregarCampoFiltro(filtros, "Estados", filtroEstados, 5);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        acciones.setOpaque(false);
        JButton btnAplicar = crearBoton("APLICAR FILTROS", true);
        fijarTamanioBoton(btnAplicar, 160);
        btnAplicar.addActionListener(e -> aplicarFiltros());
        JButton btnLimpiar = crearBoton("REINICIAR", false);
        fijarTamanioBoton(btnLimpiar, 145);
        btnLimpiar.addActionListener(e -> reiniciarFiltros());
        acciones.add(btnAplicar);
        acciones.add(btnLimpiar);
        chkPacientesUnicos.setOpaque(false);
        chkPacientesUnicos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkPacientesUnicos.setForeground(new Color(0, 92, 79));
        chkPacientesUnicos.setToolTipText("Muestra una sola fila por paciente para comprobar la estadística de pacientes únicos");
        acciones.add(chkPacientesUnicos);

        lblResultado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblResultado.setForeground(new Color(85, 96, 101));
        lblResultado.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel pieControles = new JPanel();
        pieControles.setOpaque(false);
        pieControles.setLayout(new BoxLayout(pieControles, BoxLayout.Y_AXIS));
        acciones.setAlignmentX(Component.CENTER_ALIGNMENT);
        pieControles.add(acciones);
        pieControles.add(Box.createVerticalStrut(5));
        pieControles.add(lblResultado);

        JPanel controles = new JPanel(new BorderLayout(0, 10));
        controles.setOpaque(false);
        controles.add(filtros, BorderLayout.CENTER);
        controles.add(pieControles, BorderLayout.SOUTH);
        panel.add(controles, BorderLayout.CENTER);
        return panel;
    }

    private void agregarCampoFiltro(JPanel panel, String texto, JComponent control, int columna) {
        GridBagConstraints etiqueta = new GridBagConstraints();
        etiqueta.gridx = columna;
        etiqueta.gridy = 0;
        etiqueta.weightx = 0;
        etiqueta.fill = GridBagConstraints.HORIZONTAL;
        etiqueta.anchor = GridBagConstraints.CENTER;
        etiqueta.insets = new Insets(0, columna == 0 ? 0 : 12, 5, 0);
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, etiqueta);

        GridBagConstraints campo = (GridBagConstraints) etiqueta.clone();
        campo.gridy = 1;
        campo.insets = new Insets(0, columna == 0 ? 0 : 12, 0, 0);
        panel.add(control, campo);
    }

    private void configurarFecha(JDateChooser selector) {
        selector.setDateFormatString("dd/MM/yyyy");
        selector.setPreferredSize(new Dimension(135, 38));
        selector.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (selector.getDateEditor() instanceof JTextFieldDateEditor editor) {
            editor.setEditable(false);
            editor.setFocusable(false);
            editor.setBackground(Color.WHITE);
            editor.setToolTipText("Elegí la fecha desde el calendario");
        }
    }

    private void aplicarFiltros() {
        if (fechaDesde.getDate() != null && fechaHasta.getDate() != null
                && fechaDesde.getDate().after(fechaHasta.getDate())) {
            JOptionPane.showMessageDialog(this, "La fecha desde no puede ser posterior a la fecha hasta.",
                    "Fechas inválidas", JOptionPane.WARNING_MESSAGE);
            return;
        }
        paginaActual = 1;
        cargarDatos(false);
    }

    private void reiniciarFiltros() {
        fechaDesde.setDate(null);
        fechaHasta.setDate(null);
        filtroMedicos.limpiarSeleccion();
        filtroPacientes.limpiarSeleccion();
        filtroObrasSociales.limpiarSeleccion();
        filtroEstados.limpiarSeleccion();
        chkPacientesUnicos.setSelected(false);
        paginaActual = 1;
        cargarDatos(false);
    }

    private JComponent crearCuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 0));
        cuerpo.setBackground(new Color(242, 245, 247));

        // La tabla queda anclada al encabezado y ocupa todo el centro de la
        // pantalla. Antes estaba en un bloque centrado verticalmente y por eso,
        // al recalcular el layout, reaparecía el hueco gris de la captura.
        cuerpo.add(crearTabla(), BorderLayout.CENTER);
        cuerpo.add(crearPie(), BorderLayout.SOUTH);
        return cuerpo;
    }

    private JComponent crearTabla() {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.putClientProperty("responsive.filaDinamica", Boolean.TRUE);
        tabla.setRowHeight(38);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(232, 236, 238));
        tabla.setFillsViewportHeight(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = tabla.getTableHeader();
        header.setReorderingAllowed(false);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(VERDE);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 36));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
            tabla.getColumnModel().getColumn(columna).setCellRenderer(centrado);
        }
        TableColumnModel columnas = tabla.getColumnModel();
        int[] anchos = {55, 95, 65, 190, 100, 185, 220, 110, 125};
        for (int i = 0; i < anchos.length; i++) columnas.getColumn(i).setPreferredWidth(anchos[i]);

        scrollTabla = new JScrollPane(tabla);
        scrollTabla.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollTabla.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTabla.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 0, 0),
                BorderFactory.createLineBorder(new Color(215, 221, 224))));
        scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                ajustarAltoFilasTabla();
            }
        });
        modelo.addTableModelListener(e -> ajustarAltoFilasTabla());
        return scrollTabla;
    }

    private void ajustarAltoFilasTabla() {
        if (scrollTabla == null) return;
        SwingUtilities.invokeLater(() -> {
            JViewport viewport = scrollTabla.getViewport();
            int altoDisponible = viewport.getHeight();
            if (altoDisponible <= 0) return;

            int filas = modelo.getRowCount();
            int altoFila = filas >= TAMANIO_PAGINA
                    ? Math.max(34, altoDisponible / TAMANIO_PAGINA)
                    : 38;
            if (tabla.getRowHeight() != altoFila) {
                tabla.setRowHeight(altoFila);
                tabla.revalidate();
                tabla.repaint();
            }
        });
    }

    private JComponent crearPie() {
        JPanel pie = new JPanel(new GridBagLayout());
        pie.setBackground(new Color(242, 245, 247));
        pie.setBorder(new EmptyBorder(4, 28, 8, 28));
        pie.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        JPanel paginas = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        paginas.setOpaque(false);
        btnAnterior.addActionListener(e -> cambiarPagina(-1));
        btnSiguiente.addActionListener(e -> cambiarPagina(1));
        fijarTamanioBoton(btnAnterior, 135);
        fijarTamanioBoton(btnSiguiente, 135);
        lblPagina.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPagina.setPreferredSize(new Dimension(120, 36));
        paginas.add(btnAnterior);
        paginas.add(lblPagina);
        paginas.add(btnSiguiente);
        GridBagConstraints izquierda = new GridBagConstraints();
        izquierda.gridx = 0;
        izquierda.weightx = 1;
        izquierda.fill = GridBagConstraints.HORIZONTAL;
        // Reserva el mismo ancho que la acción derecha para que la paginación
        // quede centrada respecto de toda la pantalla, no del espacio restante.
        pie.add(Box.createRigidArea(new Dimension(210, 38)), izquierda);

        GridBagConstraints centro = new GridBagConstraints();
        centro.gridx = 1;
        centro.weightx = 0;
        centro.anchor = GridBagConstraints.CENTER;
        pie.add(paginas, centro);

        JButton volver = crearBoton("VOLVER A LA AGENDA", false);
        fijarTamanioBoton(volver, 210);
        volver.addActionListener(e -> mainApp.cambiarPanel("AGENDA_TURNOS"));
        GridBagConstraints derecha = new GridBagConstraints();
        derecha.gridx = 2;
        derecha.weightx = 1;
        derecha.fill = GridBagConstraints.HORIZONTAL;
        derecha.anchor = GridBagConstraints.EAST;
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        acciones.setOpaque(false);
        acciones.add(volver);
        pie.add(acciones, derecha);
        return pie;
    }

    private void configurarAtajos() {
        registerKeyboardAction((ActionEvent e) -> mainApp.cambiarPanel("AGENDA_TURNOS"),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void cambiarPagina(int desplazamiento) {
        int paginas = Math.max(1, (int) Math.ceil(totalTurnos / (double) TAMANIO_PAGINA));
        int nueva = paginaActual + desplazamiento;
        if (nueva >= 1 && nueva <= paginas) {
            paginaActual = nueva;
            cargarDatos(false);
        }
    }

    private void cargarDatos(boolean cargarCatalogos) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblResultado.setText("Cargando turnos...");
        btnAnterior.setEnabled(false);
        btnSiguiente.setEnabled(false);
        Integer idPersonal = idPersonalPermitido();
        boolean pacientesUnicos = chkPacientesUnicos.isSelected();
        FiltroHistorial filtro = new FiltroHistorial(
                fechaDesde.getDate(), fechaHasta.getDate(),
                filtroMedicos.valoresParaFiltro(), filtroPacientes.valoresParaFiltro(),
                filtroObrasSociales.valoresParaFiltro(), filtroEstados.valoresParaFiltro(),
                null, idPersonal);

        new SwingWorker<CargaTurnos, Void>() {
            @Override protected CargaTurnos doInBackground() throws Exception {
                TurnoDAO dao = new TurnoDAO();
                dao.marcarAusentesVencidos();
                List<OpcionHistorial> medicos = List.of();
                List<OpcionHistorial> pacientes = List.of();
                List<OpcionHistorial> obrasSociales = List.of();
                List<String> estados = List.of();
                if (cargarCatalogos && !catalogosCargados) {
                    medicos = dao.listarMedicosConTurnos(idPersonal);
                    pacientes = dao.listarPacientesConTurnos(idPersonal);
                    obrasSociales = dao.listarObrasSocialesConTurnos(idPersonal);
                    estados = dao.listarEstadosExistentes(idPersonal);
                }
                int total = pacientesUnicos ? dao.contarPacientesUnicos(filtro) : dao.contarTodosLosTurnos(filtro);
                int paginas = Math.max(1, (int) Math.ceil(total / (double) TAMANIO_PAGINA));
                if (paginaActual > paginas) paginaActual = paginas;
                List<String[]> filas = pacientesUnicos
                        ? dao.listarPacientesUnicos(filtro, TAMANIO_PAGINA, (paginaActual - 1) * TAMANIO_PAGINA)
                        : dao.listarTodosLosTurnos(filtro, TAMANIO_PAGINA, (paginaActual - 1) * TAMANIO_PAGINA);
                return new CargaTurnos(total, filas, medicos, pacientes, obrasSociales, estados, pacientesUnicos);
            }

            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    CargaTurnos carga = get();
                    if (!catalogosCargados) {
                        cargarOpciones(filtroMedicos, carga.medicos);
                        cargarOpciones(filtroPacientes, carga.pacientes);
                        cargarOpciones(filtroObrasSociales, carga.obrasSociales);
                        List<ItemSeleccion<String>> estados = new ArrayList<>();
                        estados.add(new ItemSeleccion<>("__sin_asistencia__", "Sin atención realizada"));
                        for (String estado : carga.estados) {
                            if ("esperando".equalsIgnoreCase(estado)
                                    || "finalizado".equalsIgnoreCase(estado)) continue;
                            estados.add(new ItemSeleccion<>(estado, presentarEstado(estado)));
                        }
                        filtroEstados.setOpciones(estados);
                        filtroEstados.limpiarSeleccion();
                        if (idPersonal != null) {
                            filtroMedicos.seleccionarTodos();
                            filtroMedicos.setEnabled(false);
                        }
                    }
                    catalogosCargados = true;
                    totalTurnos = carga.total;
                    vistaPacientesUnicos = carga.pacientesUnicos;
                    modelo.setRowCount(0);
                    for (String[] fila : carga.filas) {
                        String[] visual = fila.clone();
                        visual[7] = presentarEstado(visual[7]);
                        visual[8] = "Sí".equalsIgnoreCase(visual[8]) ? "Sí" : "No";
                        modelo.addRow(visual);
                    }
                    actualizarPaginacion();
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                    lblResultado.setText("No se pudieron cargar los turnos");
                    JOptionPane.showMessageDialog(TodosLosTurnosPanel.this,
                            "No se pudieron consultar los turnos: " + causa.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void cargarOpciones(SelectorMultiple<Integer> selector,
            List<OpcionHistorial> opciones) {
        List<ItemSeleccion<Integer>> items = new ArrayList<>();
        for (OpcionHistorial opcion : opciones) {
            items.add(new ItemSeleccion<>(opcion.id(), opcion.nombre(), opcion.busqueda()));
        }
        selector.setOpciones(items);
        selector.limpiarSeleccion();
    }

    private void actualizarPaginacion() {
        int paginas = Math.max(1, (int) Math.ceil(totalTurnos / (double) TAMANIO_PAGINA));
        lblPagina.setText("Página " + paginaActual + " de " + paginas);
        if (totalTurnos == 0) {
            lblResultado.setText(vistaPacientesUnicos ? "No se encontraron pacientes únicos" : "No se encontraron turnos");
        } else {
            int desde = ((paginaActual - 1) * TAMANIO_PAGINA) + 1;
            int hasta = Math.min(paginaActual * TAMANIO_PAGINA, totalTurnos);
            lblResultado.setText("Mostrando " + desde + "–" + hasta + " de "
                    + totalTurnos + (vistaPacientesUnicos ? " pacientes únicos" : " turnos"));
        }
        btnAnterior.setEnabled(paginaActual > 1);
        btnSiguiente.setEnabled(paginaActual < paginas);
    }

    private Integer idPersonalPermitido() {
        Usuario usuario = mainApp.getUsuarioSesion();
        if (usuario != null && usuario.esMedico()) {
            return usuario.getId_personal() > 0 ? usuario.getId_personal() : -1;
        }
        return null;
    }

    private static String presentarEstado(String estado) {
        if (estado == null || estado.isBlank()) return "Reservado";
        String texto = estado.trim().toLowerCase(Locale.ROOT);
        if (texto.equals("en consulta")) return "En consulta";
        return texto.substring(0, 1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }

    private static JButton crearBoton(String texto, boolean principal) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setPreferredSize(new Dimension(125, 38));
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBackground(principal ? VERDE : Color.WHITE);
        boton.setForeground(principal ? Color.WHITE : new Color(0, 105, 92));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VERDE), new EmptyBorder(6, 12, 6, 12)));
        return boton;
    }

    private static void fijarTamanioBoton(JButton boton, int ancho) {
        Dimension tamanio = new Dimension(ancho, 38);
        boton.setPreferredSize(tamanio);
        boton.setMinimumSize(tamanio);
        boton.setMaximumSize(tamanio);
        boton.setMargin(new Insets(0, 12, 0, 12));
    }

    private record ItemSeleccion<T>(T valor, String etiqueta, String busqueda) {
        ItemSeleccion(T valor, String etiqueta) { this(valor, etiqueta, etiqueta); }
    }

    /** Selector con casillas para elegir uno, varios, todos o ningún valor. */
    private static final class SelectorMultiple<T> extends JButton {
        private final String textoTodos;
        private final boolean permitirBusqueda;
        private final JPopupMenu popup = new JPopupMenu();
        private final JPanel lista = new JPanel();
        private final JScrollPane scroll = new JScrollPane(lista);
        private final JCheckBox todos = new JCheckBox();
        private final JTextField buscar = new JTextField();
        private final JLabel sinCoincidencias = new JLabel("Sin coincidencias", SwingConstants.CENTER);
        private final List<ItemSeleccion<T>> opciones = new ArrayList<>();
        private final List<JCheckBox> casillas = new ArrayList<>();
        private boolean actualizando;
        private int anchoMinimo = 105;
        private int anchoMaximo = 200;

        private SelectorMultiple(String textoTodos, boolean permitirBusqueda) {
            this.textoTodos = textoTodos;
            this.permitirBusqueda = permitirBusqueda;
            // El texto largo se comunica en tooltip; dentro de un popup angosto
            // "Todos" es claro y no queda truncado.
            todos.setText("Todos");
            todos.setToolTipText(textoTodos);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBackground(Color.WHITE);
            setForeground(new Color(45, 57, 62));
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(166, 177, 182)),
                    new EmptyBorder(0, 9, 0, 7)));
            construirPopup();
            addActionListener(e -> mostrarPopup());
            actualizarTexto();
        }

        private void configurarAnchos(int minimo, int maximo) {
            anchoMinimo = Math.max(90, minimo);
            anchoMaximo = Math.max(anchoMinimo, maximo);
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
                JLabel etiqueta = new JLabel(textoTodos.contains("pacientes")
                        ? "Buscar por nombre o DNI" : "Buscar por nombre");
                etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                etiqueta.setForeground(new Color(85, 96, 101));
                etiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
                etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
                // El buscador se adapta al ancho del botón que abrió este selector.
                buscar.setMinimumSize(new Dimension(60, 30));
                buscar.setPreferredSize(new Dimension(0, 30));
                buscar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                buscar.getDocument().addDocumentListener(new DocumentListener() {
                    @Override public void insertUpdate(DocumentEvent e) { filtrarOpciones(); }
                    @Override public void removeUpdate(DocumentEvent e) { filtrarOpciones(); }
                    @Override public void changedUpdate(DocumentEvent e) { filtrarOpciones(); }
                });
                cabecera.add(etiqueta);
                cabecera.add(Box.createVerticalStrut(2));
                cabecera.add(buscar);
                cabecera.add(Box.createVerticalStrut(5));
            }
            JPanel filaTodos = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            filaTodos.setOpaque(false);
            filaTodos.add(todos);
            cabecera.add(filaTodos);

            lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
            lista.setBackground(Color.WHITE);
            scroll.setBorder(BorderFactory.createLineBorder(new Color(225, 229, 231)));
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUnitIncrement(14);

            JButton listo = crearBoton("LISTO", true);
            listo.setPreferredSize(new Dimension(90, 32));
            listo.addActionListener(e -> popup.setVisible(false));
            JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            pie.setOpaque(false);
            pie.add(listo);

            contenido.add(cabecera, BorderLayout.NORTH);
            contenido.add(scroll, BorderLayout.CENTER);
            contenido.add(pie, BorderLayout.SOUTH);
            popup.setBorder(BorderFactory.createLineBorder(new Color(170, 181, 186)));
            popup.add(contenido);
        }

        private void setOpciones(List<ItemSeleccion<T>> nuevasOpciones) {
            opciones.clear();
            opciones.addAll(nuevasOpciones);
            casillas.clear();
            lista.removeAll();
            actualizando = true;
            for (ItemSeleccion<T> opcion : opciones) {
                JCheckBox casilla = new JCheckBox(etiquetaParaPopup(opcion.etiqueta()));
                casilla.setBackground(Color.WHITE);
                casilla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                casilla.setAlignmentX(Component.LEFT_ALIGNMENT);
                casilla.setBorder(new EmptyBorder(3, 7, 3, 4));
                casilla.setToolTipText(opcion.etiqueta());
                casilla.addActionListener(e -> {
                    if (!actualizando) sincronizarTodos();
                });
                casillas.add(casilla);
                lista.add(casilla);
            }
            sinCoincidencias.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            sinCoincidencias.setForeground(new Color(105, 116, 121));
            sinCoincidencias.setAlignmentX(Component.CENTER_ALIGNMENT);
            sinCoincidencias.setBorder(new EmptyBorder(12, 4, 12, 4));
            sinCoincidencias.setVisible(false);
            lista.add(sinCoincidencias);
            todos.setSelected(false);
            actualizando = false;
            lista.revalidate();
            lista.repaint();
            actualizarTexto();
        }

        private void filtrarOpciones() {
            String texto = buscar.getText().trim().toLowerCase(Locale.ROOT);
            int coincidencias = 0;
            for (int i = 0; i < casillas.size(); i++) {
                boolean coincide = texto.isEmpty()
                        || opciones.get(i).busqueda().toLowerCase(Locale.ROOT).contains(texto);
                casillas.get(i).setVisible(coincide);
                if (coincide) coincidencias++;
            }
            sinCoincidencias.setVisible(!texto.isEmpty() && coincidencias == 0);
            lista.revalidate();
            lista.repaint();
            if (popup.isVisible()) {
                // Cambiar el tamaño del popup puede transferir el foco fuera del campo.
                // Lo devolvemos en el siguiente ciclo de Swing para permitir escribir seguido.
                SwingUtilities.invokeLater(() -> {
                    ajustarTamanoPopup();
                    buscar.requestFocusInWindow();
                    buscar.setCaretPosition(buscar.getDocument().getLength());
                });
            }
        }

        private void seleccionarTodos() {
            actualizando = true;
            todos.setSelected(true);
            for (JCheckBox casilla : casillas) casilla.setSelected(true);
            actualizando = false;
            actualizarTexto();
        }

        private void limpiarSeleccion() {
            actualizando = true;
            todos.setSelected(false);
            for (JCheckBox casilla : casillas) casilla.setSelected(false);
            actualizando = false;
            actualizarTexto();
        }

        private List<T> valoresParaFiltro() {
            int seleccionadas = (int) casillas.stream().filter(JCheckBox::isSelected).count();
            if (seleccionadas == 0 || seleccionadas == casillas.size()) return null;
            List<T> valores = new ArrayList<>();
            for (int i = 0; i < casillas.size(); i++) {
                if (casillas.get(i).isSelected()) valores.add(opciones.get(i).valor());
            }
            return valores;
        }

        private void sincronizarTodos() {
            actualizando = true;
            todos.setSelected(!casillas.isEmpty()
                    && casillas.stream().allMatch(JCheckBox::isSelected));
            actualizando = false;
            actualizarTexto();
        }

        private void actualizarTexto() {
            if (opciones.isEmpty()) {
                mostrarTextoSeleccionado("Cargando...");
                setToolTipText(null);
                return;
            }
            int seleccionadas = (int) casillas.stream().filter(JCheckBox::isSelected).count();
            if (seleccionadas == 0) mostrarTextoSeleccionado("Sin filtro");
            else if (seleccionadas == casillas.size() && casillas.size() == 1)
                mostrarTextoSeleccionado(opciones.get(0).etiqueta());
            else if (seleccionadas == casillas.size()) mostrarTextoSeleccionado("Todos");
            else if (seleccionadas == 1) {
                for (int i = 0; i < casillas.size(); i++) {
                    if (casillas.get(i).isSelected()) {
                        mostrarTextoSeleccionado(opciones.get(i).etiqueta());
                        break;
                    }
                }
            } else {
                mostrarTextoSeleccionado(seleccionadas + " seleccionados");
            }
        }

        /** Evita que Swing reemplace por puntos suspensivos una selección larga. */
        private void mostrarTextoSeleccionado(String texto) {
            int anchoTexto = getFontMetrics(getFont()).stringWidth(texto);
            int ancho = Math.max(anchoMinimo, Math.min(anchoMaximo, anchoTexto + 34));
            int disponible = Math.max(70, ancho - 24);
            if (anchoTexto <= disponible) {
                setText(texto);
                fijarTamanio(ancho, 38);
            } else {
                int mitad = texto.length() / 2;
                int corte = texto.lastIndexOf(' ', mitad);
                if (corte < 8) corte = texto.indexOf(' ', mitad);
                if (corte > 0) {
                    setText("<html><center>" + texto.substring(0, corte)
                            + "<br>" + texto.substring(corte + 1) + "</center></html>");
                    fijarTamanio(ancho, 50);
                } else {
                    setText(texto);
                    fijarTamanio(ancho, 38);
                }
            }
            setToolTipText(texto);
        }

        private void fijarTamanio(int ancho, int alto) {
            Dimension base = new Dimension(ancho, alto);
            putClientProperty("responsive.preferidoBase", new Dimension(base));
            putClientProperty("responsive.minimoBase", new Dimension(base));
            Dimension escalado = new Dimension(ResponsiveUI.px(ancho), ResponsiveUI.px(alto));
            setPreferredSize(escalado);
            setMinimumSize(escalado);
            Container padre = getParent();
            if (padre != null) padre.revalidate();
        }

        private void mostrarPopup() {
            if (!isShowing() || opciones.isEmpty()) return;
            ajustarTamanoPopup();
            popup.show(this, 0, getHeight());
        }

        /** Mantiene el desplegable compacto cuando la búsqueda deja pocos resultados. */
        private void ajustarTamanoPopup() {
            // El botón permanece compacto, pero el desplegable necesita un ancho
            // independiente para no recortar estados, nombres ni obras sociales.
            int anchoBasePopup = permitirBusqueda ? 255 : 210;
            int anchoPopup = Math.max(getWidth(), ResponsiveUI.px(anchoBasePopup));
            long visibles = casillas.stream().filter(Component::isVisible).count();
            int altoContenido = 6;
            for (int i = 0; i < casillas.size(); i++) {
                if (casillas.get(i).isVisible()) altoContenido += altoOpcion(opciones.get(i).etiqueta());
            }
            int altoLista = Math.min(160, Math.max(70, altoContenido));
            Dimension tamanioLista = new Dimension(Math.max(100, anchoPopup - 18), altoLista);
            scroll.setPreferredSize(tamanioLista);
            scroll.setMinimumSize(tamanioLista);
            for (int i = 0; i < casillas.size(); i++) {
                int altoOpcion = altoOpcion(opciones.get(i).etiqueta());
                casillas.get(i).setPreferredSize(new Dimension(tamanioLista.width, altoOpcion));
                casillas.get(i).setMaximumSize(new Dimension(tamanioLista.width, altoOpcion));
            }
            // Incluye buscador, selección global, botón y márgenes sin generar
            // un panel desproporcionado cuando hay muchas opciones.
            int alto = altoLista + (permitirBusqueda ? 146 : 98);
            popup.setPopupSize(anchoPopup, alto);
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

    private static final class CargaTurnos {
        private final int total;
        private final List<String[]> filas;
        private final List<OpcionHistorial> medicos;
        private final List<OpcionHistorial> pacientes;
        private final List<OpcionHistorial> obrasSociales;
        private final List<String> estados;
        private final boolean pacientesUnicos;
        private CargaTurnos(int total, List<String[]> filas,
                List<OpcionHistorial> medicos, List<OpcionHistorial> pacientes,
                List<OpcionHistorial> obrasSociales, List<String> estados, boolean pacientesUnicos) {
            this.total = total;
            this.filas = filas;
            this.medicos = medicos;
            this.pacientes = pacientes;
            this.obrasSociales = obrasSociales;
            this.estados = estados;
            this.pacientesUnicos = pacientesUnicos;
        }
    }
}
