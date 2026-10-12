package clinicabalbarrey;

import clinicabalbarrey.dao.CatalogoDAO;
import clinicabalbarrey.dao.ConsultaDAO;
import clinicabalbarrey.dao.TurnoDAO;
import clinicabalbarrey.ConexionBD;
import clinicabalbarrey.model.Usuario;
import clinicabalbarrey.storage.ArchivoAdjuntoService;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class NuevaConsultaPanel extends JPanel {
    private static final int MAX_PESTANAS_POR_SECCION = 5;
    private MainApplicationView mainApp;
    private JLabel lblPacienteInfo;
    private String dniPacienteActual;
    private String nombrePacienteActual;
    private int idTurnoActual;
    private int idPersonalHistoriaActual;
    private int idEspecialidadMedicoActual;
    private boolean historiaClinicaEnCurso;

    private JPanel pnlContenedorCentral;
    private JTextArea txtEvolucion;
    private JLabel lblContadorEvolucion;

    private JButton btnDiagnostico;
    private JButton btnTratamiento;
    private JButton btnArchivo;

    private JPanel pnlDiagnostico;
    private JTabbedPane tabDiagnosticos;
    private JButton btnAgregarTabDiag;
    private JButton btnEliminarTabDiag;
    private List<DatosDiagnostico> tabsDiagData = new ArrayList<>();

    private JPanel pnlTratamiento;
    private JTabbedPane tabTratamientos;
    private JButton btnAgregarTabTrat;
    private JButton btnEliminarTabTrat;
    private List<DatosTratamiento> tabsTratData = new ArrayList<>();

    private JPanel pnlArchivo;
    private String rutaArchivoAdjunto = "";
    private JLabel lblRutaAdjunto;
    private ArrayList<File> archivosAdjuntos = new ArrayList<>();

    private Border bordeSuaveInptus;
    private final Border BORDE_ERROR = EstiloRedondeado.borde(Color.RED, 2);
    private final Border BORDE_FECHA_NORMAL = UIManager.getBorder("TextField.border");

    private List<String> listaEnfermedades = new ArrayList<>();
    private List<String> listaTratamientos = new ArrayList<>();

    class DatosDiagnostico {
        JComboBox<String> cmbBuscar;
        JComboBox<String> cmbEstado;
    }

    class DatosTratamiento {
        JComboBox<String> cmbBuscar;
        JTextField txtDosis;
        JComboBox<String> cmbFrecuencia;
        JDateChooser dateInicio;
        JDateChooser dateFin;
        JComboBox<String> cmbEstado;
    }

    public NuevaConsultaPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(new Color(242, 245, 247));

        listaEnfermedades = new CatalogoDAO().obtenerDiagnosticosActivosPorEspecialidad(idEspecialidadMedicoActual);
        listaTratamientos = new CatalogoDAO().obtenerMedicamentosActivosPorEspecialidad(idEspecialidadMedicoActual);

        bordeSuaveInptus = BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(200, 205, 208), 1),
            new EmptyBorder(8, 12, 8, 12)
        );

        JPanel pnlNorte = new JPanel();
        pnlNorte.setLayout(new BoxLayout(pnlNorte, BoxLayout.Y_AXIS));
        pnlNorte.setBackground(new Color(242, 245, 247));
        pnlNorte.setBorder(new EmptyBorder(25, 60, 10, 60));

        JLabel lblTitulo = new JLabel("NUEVA EVOLUCIÓN CLÍNICA");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(0, 105, 92));
        pnlNorte.add(lblTitulo);

        lblPacienteInfo = new JLabel("Paciente: Seleccionando...");
        lblPacienteInfo.setFont(new Font("Segoe UI", Font.ITALIC, 20));
        lblPacienteInfo.setForeground(new Color(108, 117, 125));
        pnlNorte.add(lblPacienteInfo);
        add(pnlNorte, BorderLayout.NORTH);

        JPanel pnlFondoWeb = new JPanel(new GridBagLayout());
        pnlFondoWeb.setBackground(new Color(242, 245, 247));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(10, 60, 40, 60);

        pnlContenedorCentral = new JPanel();
        pnlContenedorCentral.setLayout(new BoxLayout(pnlContenedorCentral, BoxLayout.Y_AXIS));
        pnlContenedorCentral.setBackground(Color.WHITE);
        pnlContenedorCentral.setMaximumSize(new Dimension(1000, Integer.MAX_VALUE));
        pnlContenedorCentral.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(225, 228, 230), 1),
            new EmptyBorder(20, 40, 30, 40)
        ));

        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlToolbar.setBackground(Color.WHITE);
        pnlToolbar.setMaximumSize(new Dimension(900, 60));
        pnlToolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 228, 230)));
        pnlToolbar.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnDiagnostico = crearBotonToggle("+ Diagnóstico");
        btnTratamiento = crearBotonToggle("+ Tratamiento");
        btnArchivo = crearBotonToggle("+ Archivo");

        pnlToolbar.add(btnDiagnostico);
        pnlToolbar.add(btnTratamiento);
        pnlToolbar.add(btnArchivo);
        pnlContenedorCentral.add(pnlToolbar);
        pnlContenedorCentral.add(Box.createVerticalStrut(20));

        JPanel pnlEvolucion = new JPanel(new BorderLayout());
        pnlEvolucion.setBackground(Color.WHITE);
        pnlEvolucion.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlEvolucion.setPreferredSize(new Dimension(900, 150));
        pnlEvolucion.setMaximumSize(new Dimension(900, 150));

        JLabel lblEvo = new JLabel("  Escribe aquí la evolución clínica detallada (*Obligatorio):");
        lblEvo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblEvo.setForeground(new Color(100, 110, 115));
        lblContadorEvolucion = new JLabel("0 / 300");
        lblContadorEvolucion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblContadorEvolucion.setForeground(new Color(108, 117, 125));
        JPanel encabezadoEvolucion = new JPanel(new BorderLayout());
        encabezadoEvolucion.setOpaque(false);
        encabezadoEvolucion.add(lblEvo, BorderLayout.WEST);
        encabezadoEvolucion.add(lblContadorEvolucion, BorderLayout.EAST);
        pnlEvolucion.add(encabezadoEvolucion, BorderLayout.NORTH);

        txtEvolucion = new JTextArea();
        txtEvolucion.setLineWrap(true);
        txtEvolucion.setWrapStyleWord(true);
        txtEvolucion.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtEvolucion.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int limiteEvolucion = 300;
        ((AbstractDocument) txtEvolucion.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) text = "";
                if (fb.getDocument().getLength() + text.length() - length <= limiteEvolucion) {
                    super.replace(fb, offset, length, text, attrs);
                } else { Toolkit.getDefaultToolkit().beep(); }
            }
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                if (fb.getDocument().getLength() + string.length() <= limiteEvolucion) {
                    super.insertString(fb, offset, string, attr);
                } else { Toolkit.getDefaultToolkit().beep(); }
            }
        });
        txtEvolucion.getDocument().addDocumentListener(new DocumentListener() {
            private void actualizar() { lblContadorEvolucion.setText(txtEvolucion.getDocument().getLength() + " / " + limiteEvolucion); }
            @Override public void insertUpdate(DocumentEvent e) { actualizar(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizar(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizar(); }
        });

        JScrollPane scrollEvo = new JScrollPane(txtEvolucion);
        scrollEvo.setBorder(EstiloRedondeado.borde(new Color(200, 205, 208), 1));
        pnlEvolucion.add(scrollEvo, BorderLayout.CENTER);

        pnlContenedorCentral.add(pnlEvolucion);
        pnlContenedorCentral.add(Box.createVerticalStrut(15));

        construirPanelDiagnostico();
        construirPanelTratamiento();
        construirPanelArchivo();

        pnlContenedorCentral.add(pnlDiagnostico);
        pnlContenedorCentral.add(pnlTratamiento);
        pnlContenedorCentral.add(pnlArchivo);

        pnlFondoWeb.add(pnlContenedorCentral, gbc);

        JScrollPane scrollGeneral = new JScrollPane(pnlFondoWeb);
        scrollGeneral.setBorder(null);
        scrollGeneral.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollGeneral, BorderLayout.CENTER);

        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 60, 15));
        pnlSur.setBackground(Color.WHITE);
        pnlSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 228, 230)));

        JButton btnGuardar = new JButton("Guardar Historia Clínica");
        btnGuardar.setBackground(new Color(41, 128, 185));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnGuardar.setPreferredSize(new Dimension(230, 45));
        btnGuardar.setBorder(BorderFactory.createEmptyBorder());
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> guardarConsulta());

        pnlSur.add(btnGuardar);
        add(pnlSur, BorderLayout.SOUTH);

        btnDiagnostico.addActionListener(e -> alternarAcordeon(pnlDiagnostico, btnDiagnostico));
        btnTratamiento.addActionListener(e -> alternarAcordeon(pnlTratamiento, btnTratamiento));
        btnArchivo.addActionListener(e -> alternarAcordeon(pnlArchivo, btnArchivo));
    }

    private JTextField crearCampoLimitado(int limite) {
        JTextField tf = new JTextField();
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) text = "";
                if (fb.getDocument().getLength() + text.length() - length <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                } else { Toolkit.getDefaultToolkit().beep(); }
            }
        });
        return tf;
    }

    private JPanel crearBuscadorDinamico(String titulo, List<String> diccionario, JComboBox<String> cmbDestino,
            java.util.function.Function<String, Boolean> accionGuardarBD,
            java.util.function.Function<String, Boolean> accionEliminarBD,
            java.util.function.BiFunction<String, String, Boolean> accionEditarBD,
            java.util.function.Supplier<List<String>> accionListarOcultosBD,
            java.util.function.Function<String, Boolean> accionRestaurarBD) {

        JPanel pnlPrincipal = new JPanel();
        pnlPrincipal.setLayout(new BoxLayout(pnlPrincipal, BoxLayout.Y_AXIS));
        pnlPrincipal.setOpaque(false);
        pnlPrincipal.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(100, 110, 115));
        pnlPrincipal.add(lbl);
        pnlPrincipal.add(Box.createVerticalStrut(4));

        JPanel pnlCards = new JPanel(new CardLayout());
        pnlCards.setOpaque(false);
        pnlCards.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlCards.setMaximumSize(new Dimension(900, 45));
        pnlCards.setPreferredSize(new Dimension(0, 45));

        final String[] valorAEditar = {null};

        JPanel pnlNormal = new JPanel(new BorderLayout(5, 0));
        pnlNormal.setOpaque(false);

        cmbDestino.removeAllItems();
        for(String item : diccionario) { cmbDestino.addItem(item); }
        cmbDestino.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbDestino.setBackground(Color.WHITE);
        cmbDestino.setBorder(bordeSuaveInptus);
        // Evita que Swing pinte todo el valor en azul al activar una pestaña
        // creada mientras la pantalla ya está visible.
        cmbDestino.setFocusable(false);
        cmbDestino.setRequestFocusEnabled(false);
        configurarPopupCombo(cmbDestino);

        JPanel pnlBotonesInline = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlBotonesInline.setOpaque(false);

        JButton btnDelInline = new JButton("Ocultar");
        btnDelInline.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelInline.setBackground(new Color(231, 76, 60));
        btnDelInline.setForeground(Color.WHITE);
        btnDelInline.setFocusPainted(false);
        btnDelInline.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelInline.setPreferredSize(new Dimension(95, 40));
        EstiloRedondeado.aplicarBotonPeligro(btnDelInline);

        JButton btnEditInline = new JButton("Editar");

        JButton btnAddInline = new JButton("Agregar");
        btnAddInline.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddInline.setBackground(new Color(0, 128, 112));
        btnAddInline.setForeground(Color.WHITE);
        btnAddInline.setFocusPainted(false);
        btnAddInline.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddInline.setPreferredSize(new Dimension(95, 40));
        EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(btnAddInline);

        JButton btnRestaurarInline = new JButton("Restaurar");
        btnRestaurarInline.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRestaurarInline.setBackground(new Color(0, 128, 112));
        btnRestaurarInline.setForeground(Color.WHITE);
        btnRestaurarInline.setFocusPainted(false);
        btnRestaurarInline.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRestaurarInline.setPreferredSize(new Dimension(115, 40));
        EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(btnRestaurarInline);

        pnlBotonesInline.add(btnDelInline);
        pnlBotonesInline.add(btnAddInline);
        pnlBotonesInline.add(btnRestaurarInline);
        Usuario sesion = mainApp.getUsuarioSesion();
        pnlBotonesInline.setVisible(sesion != null && (sesion.esAdministrador() || sesion.esMedico()));

        pnlNormal.add(cmbDestino, BorderLayout.CENTER);
        pnlNormal.add(pnlBotonesInline, BorderLayout.EAST);

        JPanel pnlNuevo = new JPanel(new BorderLayout(5, 0));
        pnlNuevo.setOpaque(false);

        JTextField txtNuevo = new JTextField();
        txtNuevo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNuevo.setBorder(bordeSuaveInptus);

        JPanel pnlBotonesAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlBotonesAccion.setOpaque(false);

        JButton btnOk = new JButton("Guardar");
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOk.setBackground(new Color(46, 204, 113));
        btnOk.setForeground(Color.WHITE);
        btnOk.setFocusPainted(false);
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOk.setPreferredSize(new Dimension(115, 40));
        EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(btnOk);

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancel.setBackground(new Color(0, 128, 112));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.setPreferredSize(new Dimension(115, 40));
        EstiloRedondeado.aplicarBotonPrincipalSiCorresponde(btnCancel);

        pnlBotonesAccion.add(btnOk);
        pnlBotonesAccion.add(btnCancel);

        pnlNuevo.add(txtNuevo, BorderLayout.CENTER);
        pnlNuevo.add(pnlBotonesAccion, BorderLayout.EAST);

        pnlCards.add(pnlNormal, "NORMAL");
        pnlCards.add(pnlNuevo, "NUEVO");

        CardLayout cl = (CardLayout) pnlCards.getLayout();

        btnDelInline.addActionListener(e -> {
            if (!puedeGestionarCatalogos()) { accesoCatalogoDenegado(); return; }
            String seleccionado = (String) cmbDestino.getSelectedItem();
            if (seleccionado != null && !seleccionado.startsWith("Seleccione")) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de ocultar '" + seleccionado + "' del catálogo para futuras atenciones?",
                    "Confirmar Ocultamiento", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean guardado = accionEliminarBD != null && Boolean.TRUE.equals(accionEliminarBD.apply(seleccionado));
                    if (guardado) {
                        diccionario.remove(seleccionado);
                        cmbDestino.removeItem(seleccionado);
                    } else JOptionPane.showMessageDialog(this, "No se pudo ocultar el elemento en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccioná un elemento válido para ocultar.", "Atención", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnEditInline.addActionListener(e -> {
            if (!puedeGestionarCatalogos()) { accesoCatalogoDenegado(); return; }
            String seleccionado = (String) cmbDestino.getSelectedItem();
            if (seleccionado != null && !seleccionado.startsWith("Seleccione")) {
                valorAEditar[0] = seleccionado; txtNuevo.setText(seleccionado);
                txtNuevo.setBorder(bordeSuaveInptus); cl.show(pnlCards, "NUEVO"); txtNuevo.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Seleccioná un elemento válido para editar.", "Atención", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnAddInline.addActionListener(e -> {
            if (!puedeGestionarCatalogos()) { accesoCatalogoDenegado(); return; }
            valorAEditar[0] = null; txtNuevo.setText("");
            txtNuevo.setBorder(bordeSuaveInptus); cl.show(pnlCards, "NUEVO"); txtNuevo.requestFocus();
        });

        btnRestaurarInline.addActionListener(e -> {
            if (!puedeGestionarCatalogos()) { accesoCatalogoDenegado(); return; }
            restaurarElementoCatalogo(diccionario, cmbDestino, accionListarOcultosBD, accionRestaurarBD);
        });

        btnCancel.addActionListener(e -> cl.show(pnlCards, "NORMAL"));

        btnOk.addActionListener(e -> {
            String valor = txtNuevo.getText().trim().toUpperCase();
            if(!valor.isEmpty()) {
                if (valorAEditar[0] != null) {
                    String viejo = valorAEditar[0];
                    boolean guardado = accionEditarBD != null && Boolean.TRUE.equals(accionEditarBD.apply(viejo, valor));
                    if (!guardado) { JOptionPane.showMessageDialog(this, "No se pudo editar el catálogo.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                    int idx = diccionario.indexOf(viejo);
                    if (idx != -1) diccionario.set(idx, valor);
                    cmbDestino.removeItem(viejo); cmbDestino.addItem(valor); cmbDestino.setSelectedItem(valor);
                } else {
                    boolean guardado = accionGuardarBD != null && Boolean.TRUE.equals(accionGuardarBD.apply(valor));
                    if (!guardado) { JOptionPane.showMessageDialog(this, "No se pudo agregar el elemento al catálogo.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                    diccionario.add(valor);
                    cmbDestino.addItem(valor); cmbDestino.setSelectedItem(valor);
                }
                cl.show(pnlCards, "NORMAL");
            } else { txtNuevo.setBorder(BORDE_ERROR); }
        });

        pnlPrincipal.add(pnlCards);
        pnlPrincipal.add(Box.createVerticalStrut(10));

        return pnlPrincipal;
    }

    private void restaurarElementoCatalogo(List<String> diccionario, JComboBox<String> cmbDestino,
            java.util.function.Supplier<List<String>> accionListarOcultosBD,
            java.util.function.Function<String, Boolean> accionRestaurarBD) {
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

    private void construirPanelDiagnostico() {
        pnlDiagnostico = new JPanel();
        pnlDiagnostico.setLayout(new BoxLayout(pnlDiagnostico, BoxLayout.Y_AXIS));
        pnlDiagnostico.setBackground(new Color(250, 251, 252));
        pnlDiagnostico.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(225, 228, 230), 1),
            new EmptyBorder(15, 25, 15, 25)
        ));
        pnlDiagnostico.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDiagnostico.setVisible(false);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setMaximumSize(new Dimension(900, 35));

        JLabel lbl = new JLabel("Sección de Diagnóstico");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(41, 128, 185));
        pnlHeader.add(lbl, BorderLayout.WEST);

        btnAgregarTabDiag = new JButton("+ Añadir Pestaña");
        btnAgregarTabDiag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAgregarTabDiag.setBackground(Color.WHITE);
        btnAgregarTabDiag.setForeground(new Color(41, 128, 185));
        btnAgregarTabDiag.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAgregarTabDiag.setFocusPainted(false);
        btnAgregarTabDiag.setToolTipText("Máximo " + MAX_PESTANAS_POR_SECCION + " diagnósticos por evolución.");
        btnAgregarTabDiag.addActionListener(e -> agregarPestaniaDiagnostico());
        btnEliminarTabDiag = new JButton("Quitar pestaña");
        btnEliminarTabDiag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEliminarTabDiag.setBackground(Color.WHITE);
        btnEliminarTabDiag.setForeground(new Color(211, 47, 47));
        btnEliminarTabDiag.setFocusPainted(false);
        btnEliminarTabDiag.setToolTipText("Solo se pueden quitar pestañas adicionales; la primera se conserva.");
        btnEliminarTabDiag.addActionListener(e -> eliminarPestaniaDiagnostico());
        JPanel accionesTabs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accionesTabs.setOpaque(false);
        accionesTabs.add(btnEliminarTabDiag);
        accionesTabs.add(btnAgregarTabDiag);
        pnlHeader.add(accionesTabs, BorderLayout.EAST);

        pnlDiagnostico.add(pnlHeader);
        pnlDiagnostico.add(Box.createVerticalStrut(10));

        tabDiagnosticos = new JTabbedPane();
        tabDiagnosticos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabDiagnosticos.setBackground(Color.WHITE);
        tabDiagnosticos.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabDiagnosticos.setPreferredSize(new Dimension(900, 235));
        tabDiagnosticos.setMaximumSize(new Dimension(900, 235));
        tabDiagnosticos.addChangeListener(e -> actualizarBotonesPestanas());
        pnlDiagnostico.add(tabDiagnosticos);

        agregarPestaniaDiagnostico();
    }

    private void agregarPestaniaDiagnostico() {
        if (tabsDiagData.size() >= MAX_PESTANAS_POR_SECCION) {
            JOptionPane.showMessageDialog(this, "Se permiten hasta " + MAX_PESTANAS_POR_SECCION + " diagnósticos por evolución.", "Límite alcanzado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DatosDiagnostico d = new DatosDiagnostico();

        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(15, 10, 10, 10));

        d.cmbBuscar = new JComboBox<>();
        d.cmbBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        d.cmbEstado = new JComboBox<>(new String[]{"Agudo", "Crónico", "Resuelto"});

        pnl.add(crearBuscadorDinamico("Diagnóstico (opcional):", listaEnfermedades, d.cmbBuscar,
            valor -> new CatalogoDAO().insertarDiagnostico(valor, idEspecialidadMedicoActual),
            valor -> new CatalogoDAO().eliminarDiagnostico(valor),
            (viejo, nuevo) -> new CatalogoDAO().editarDiagnostico(viejo, nuevo),
            () -> new CatalogoDAO().obtenerDiagnosticosInactivosPorEspecialidad(idEspecialidadMedicoActual),
            valor -> new CatalogoDAO().restaurarDiagnostico(valor, idEspecialidadMedicoActual)
        ));

        JPanel filaF = new JPanel(new GridLayout(1, 1, 20, 0));
        filaF.setOpaque(false);
        filaF.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaF.setMaximumSize(new Dimension(900, 82));
        filaF.add(crearBloqueInput("Estado:", d.cmbEstado));

        pnl.add(filaF);

        tabsDiagData.add(d);
        // Las pestañas añadidas luego de mostrar la ventana también deben pasar
        // por la misma escala responsive que la pestaña inicial.
        ResponsiveUI.aplicar(pnl);
        tabDiagnosticos.addTab("Diagnóstico " + tabsDiagData.size(), pnl);
        tabDiagnosticos.setSelectedIndex(tabsDiagData.size() - 1);
        actualizarBotonesPestanas();
    }

    private void eliminarPestaniaDiagnostico() {
        int indice = tabDiagnosticos.getSelectedIndex();
        if (indice <= 0 || tabsDiagData.size() <= 1) return;
        tabDiagnosticos.removeTabAt(indice);
        tabsDiagData.remove(indice);
        renumerarPestanas(tabDiagnosticos, "Diagnóstico");
        actualizarBotonesPestanas();
    }

    private void construirPanelTratamiento() {
        pnlTratamiento = new JPanel();
        pnlTratamiento.setLayout(new BoxLayout(pnlTratamiento, BoxLayout.Y_AXIS));
        pnlTratamiento.setBackground(new Color(250, 251, 252));
        pnlTratamiento.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(225, 228, 230), 1),
            new EmptyBorder(15, 25, 15, 25)
        ));
        pnlTratamiento.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlTratamiento.setVisible(false);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setMaximumSize(new Dimension(900, 35));

        JLabel lbl = new JLabel("Sección de Tratamiento e Indicaciones");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(41, 128, 185));
        pnlHeader.add(lbl, BorderLayout.WEST);

        btnAgregarTabTrat = new JButton("+ Añadir Pestaña");
        btnAgregarTabTrat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAgregarTabTrat.setBackground(Color.WHITE);
        btnAgregarTabTrat.setForeground(new Color(41, 128, 185));
        btnAgregarTabTrat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAgregarTabTrat.setFocusPainted(false);
        btnAgregarTabTrat.setToolTipText("Máximo " + MAX_PESTANAS_POR_SECCION + " tratamientos por evolución.");
        btnAgregarTabTrat.addActionListener(e -> agregarPestaniaTratamiento());
        btnEliminarTabTrat = new JButton("Quitar pestaña");
        btnEliminarTabTrat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEliminarTabTrat.setBackground(Color.WHITE);
        btnEliminarTabTrat.setForeground(new Color(211, 47, 47));
        btnEliminarTabTrat.setFocusPainted(false);
        btnEliminarTabTrat.setToolTipText("Solo se pueden quitar pestañas adicionales; la primera se conserva.");
        btnEliminarTabTrat.addActionListener(e -> eliminarPestaniaTratamiento());
        JPanel accionesTabs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accionesTabs.setOpaque(false);
        accionesTabs.add(btnEliminarTabTrat);
        accionesTabs.add(btnAgregarTabTrat);
        pnlHeader.add(accionesTabs, BorderLayout.EAST);

        pnlTratamiento.add(pnlHeader);
        pnlTratamiento.add(Box.createVerticalStrut(10));

        tabTratamientos = new JTabbedPane();
        tabTratamientos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabTratamientos.setBackground(Color.WHITE);
        tabTratamientos.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabTratamientos.setPreferredSize(new Dimension(900, 380));
        tabTratamientos.setMaximumSize(new Dimension(900, 380));
        tabTratamientos.addChangeListener(e -> actualizarBotonesPestanas());
        pnlTratamiento.add(tabTratamientos);

        agregarPestaniaTratamiento();
    }

    private void agregarPestaniaTratamiento() {
        if (tabsTratData.size() >= MAX_PESTANAS_POR_SECCION) {
            JOptionPane.showMessageDialog(this, "Se permiten hasta " + MAX_PESTANAS_POR_SECCION + " tratamientos por evolución.", "Límite alcanzado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DatosTratamiento t = new DatosTratamiento();

        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(15, 10, 10, 10));

        t.cmbBuscar = new JComboBox<>();
        t.cmbBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        t.txtDosis = crearCampoLimitado(30);
        t.txtDosis.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.txtDosis.setBorder(bordeSuaveInptus);

        t.cmbFrecuencia = new JComboBox<>(new String[]{
            "Seleccione frecuencia...", "Única vez", "Cada 4 horas", "Cada 6 horas",
            "Cada 8 horas", "Cada 12 horas", "Cada 24 horas", "Según necesidad"
        });

        java.util.Date hoy = new java.util.Date();

        t.dateInicio = new JDateChooser(hoy);
        t.dateInicio.setMinSelectableDate(hoy);
        JTextFieldDateEditor editorIniTrat = (JTextFieldDateEditor) t.dateInicio.getDateEditor();
        editorIniTrat.setEditable(false);
        editorIniTrat.setBackground(Color.WHITE);

        t.dateFin = new JDateChooser();
        t.dateFin.setMinSelectableDate(hoy);
        JTextFieldDateEditor editorFinTrat = (JTextFieldDateEditor) t.dateFin.getDateEditor();
        editorFinTrat.setEditable(false);
        editorFinTrat.setBackground(Color.WHITE);

        t.dateInicio.getDateEditor().addPropertyChangeListener(e -> {
            if ("date".equals(e.getPropertyName()) && e.getNewValue() != null) {
                t.dateFin.setMinSelectableDate((java.util.Date) e.getNewValue());
            }
        });

        t.cmbEstado = new JComboBox<>(new String[]{"Actual", "Suspendido", "Finalizado"});

        pnl.add(crearBuscadorDinamico("Medicamento (opcional):", listaTratamientos, t.cmbBuscar,
            valor -> new CatalogoDAO().insertarMedicamento(valor, idEspecialidadMedicoActual),
            valor -> new CatalogoDAO().eliminarMedicamento(valor),
            (viejo, nuevo) -> new CatalogoDAO().editarMedicamento(viejo, nuevo),
            () -> new CatalogoDAO().obtenerMedicamentosInactivosPorEspecialidad(idEspecialidadMedicoActual),
            valor -> new CatalogoDAO().restaurarMedicamento(valor, idEspecialidadMedicoActual)
        ));

        JPanel fila1 = new JPanel(new GridLayout(1, 2, 20, 0));
        fila1.setOpaque(false);
        fila1.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila1.setMaximumSize(new Dimension(900, 82));
        fila1.add(crearBloqueInput("Dosis (*Si indica tratamiento):", t.txtDosis));
        fila1.add(crearBloqueInput("Frecuencia (*Si indica tratamiento):", t.cmbFrecuencia));
        pnl.add(fila1);

        JPanel fila2 = new JPanel(new GridLayout(1, 3, 20, 0));
        fila2.setOpaque(false);
        fila2.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila2.setMaximumSize(new Dimension(900, 82));
        fila2.add(crearBloqueInput("Fecha Inicio (*Si indica tratamiento):", t.dateInicio));
        fila2.add(crearBloqueInput("Fecha Fin (*Si indica tratamiento):", t.dateFin));
        fila2.add(crearBloqueInput("Estado:", t.cmbEstado));
        pnl.add(fila2);

        tabsTratData.add(t);
        ResponsiveUI.aplicar(pnl);
        tabTratamientos.addTab("Tratamiento " + tabsTratData.size(), pnl);
        tabTratamientos.setSelectedIndex(tabsTratData.size() - 1);
        actualizarBotonesPestanas();
    }

    private void eliminarPestaniaTratamiento() {
        int indice = tabTratamientos.getSelectedIndex();
        if (indice <= 0 || tabsTratData.size() <= 1) return;
        tabTratamientos.removeTabAt(indice);
        tabsTratData.remove(indice);
        renumerarPestanas(tabTratamientos, "Tratamiento");
        actualizarBotonesPestanas();
    }

    private void renumerarPestanas(JTabbedPane tabs, String titulo) {
        for (int i = 0; i < tabs.getTabCount(); i++) tabs.setTitleAt(i, titulo + " " + (i + 1));
    }

    private void actualizarBotonesPestanas() {
        if (btnEliminarTabDiag != null && tabDiagnosticos != null) {
            btnEliminarTabDiag.setEnabled(tabDiagnosticos.getTabCount() > 1 && tabDiagnosticos.getSelectedIndex() > 0);
            btnAgregarTabDiag.setEnabled(tabDiagnosticos.getTabCount() < MAX_PESTANAS_POR_SECCION);
        }
        if (btnEliminarTabTrat != null && tabTratamientos != null) {
            btnEliminarTabTrat.setEnabled(tabTratamientos.getTabCount() > 1 && tabTratamientos.getSelectedIndex() > 0);
            btnAgregarTabTrat.setEnabled(tabTratamientos.getTabCount() < MAX_PESTANAS_POR_SECCION);
        }
    }

    private void construirPanelArchivo() {
        pnlArchivo = new JPanel();
        pnlArchivo.setLayout(new BoxLayout(pnlArchivo, BoxLayout.Y_AXIS));
        pnlArchivo.setBackground(new Color(250, 251, 252));
        pnlArchivo.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(225, 228, 230), 1),
            new EmptyBorder(15, 25, 15, 25)
        ));
        pnlArchivo.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlArchivo.setVisible(false);

        JLabel lbl = new JLabel("Estudios y Archivos Adjuntos");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(41, 128, 185));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlArchivo.add(lbl);
        pnlArchivo.add(Box.createVerticalStrut(10));

        JPanel pnlBtnAdj = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBtnAdj.setOpaque(false);
        pnlBtnAdj.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnAdjuntar = new JButton("+ Seleccionar PDFs / Imágenes");
        btnAdjuntar.setBackground(Color.WHITE);
        btnAdjuntar.setForeground(new Color(41, 128, 185));
        btnAdjuntar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdjuntar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdjuntar.setFocusPainted(false);
        btnAdjuntar.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(41, 128, 185), 1),
            new EmptyBorder(8, 15, 8, 15)
        ));
        btnAdjuntar.addActionListener(e -> adjuntarArchivosMultiples());

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setBackground(new Color(231, 76, 60));
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.setBorder(BorderFactory.createEmptyBorder(9, 15, 9, 15));
        btnLimpiar.addActionListener(e -> {
            archivosAdjuntos.clear();
            actualizarEtiquetaArchivos();
        });

        lblRutaAdjunto = new JLabel("   Ningún archivo seleccionado.");
        lblRutaAdjunto.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lblRutaAdjunto.setForeground(new Color(149, 165, 166));

        pnlBtnAdj.add(btnAdjuntar);
        pnlBtnAdj.add(Box.createHorizontalStrut(10));
        pnlBtnAdj.add(btnLimpiar);
        pnlBtnAdj.add(lblRutaAdjunto);
        pnlArchivo.add(pnlBtnAdj);
    }

    private void alternarAcordeon(JPanel panelDestino, JButton botonDestino) {
        boolean estabaVisible = panelDestino.isVisible();
        pnlDiagnostico.setVisible(false);
        pnlTratamiento.setVisible(false);
        pnlArchivo.setVisible(false);

        btnDiagnostico.setBackground(Color.WHITE); btnDiagnostico.setForeground(new Color(41, 128, 185));
        btnTratamiento.setBackground(Color.WHITE); btnTratamiento.setForeground(new Color(41, 128, 185));
        btnArchivo.setBackground(Color.WHITE); btnArchivo.setForeground(new Color(41, 128, 185));

        if (!estabaVisible) {
            panelDestino.setVisible(true);
            botonDestino.setBackground(new Color(41, 128, 185));
            botonDestino.setForeground(Color.WHITE);
        }
        pnlContenedorCentral.revalidate();
        pnlContenedorCentral.repaint();
    }

    private JButton crearBotonToggle(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(41, 128, 185));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void configurarPopupCombo(JComboBox<?> combo) {
        EstiloRedondeado.aplicarComboBuscador(combo);
        int filas = Math.max(1, Math.min(8, Math.max(combo.getItemCount(), 1)));
        combo.setMaximumRowCount(filas);
    }

    private void estilizarComboFormulario(JComboBox<?> combo) {
        EstiloRedondeado.aplicarComboBuscador(combo);
        combo.setPreferredSize(new Dimension(0, 44));
        combo.setMaximumSize(new Dimension(900, 44));
        configurarPopupCombo(combo);
    }

    private void estilizarFechaFormulario(JDateChooser fecha) {
        fecha.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fecha.setPreferredSize(new Dimension(0, 44));
        fecha.setMaximumSize(new Dimension(900, 44));
        JTextFieldDateEditor editor = (JTextFieldDateEditor) fecha.getDateEditor();
        editor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editor.setBackground(Color.WHITE);
        editor.setBorder(bordeSuaveInptus);
        editor.setPreferredSize(new Dimension(0, 44));
    }

    private JPanel crearBloqueInput(String titulo, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(100, 110, 115));
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));

        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(900, 44));
        comp.setPreferredSize(new Dimension(0, 44));

        if (comp instanceof JDateChooser) {
            estilizarFechaFormulario((JDateChooser) comp);
        } else if (comp instanceof JComboBox) {
            estilizarComboFormulario((JComboBox<?>) comp);
        }

        p.add(comp);
        p.add(Box.createVerticalStrut(10));
        return p;
    }

    private static class PopupMasBajoComboBoxUI extends BasicComboBoxUI {
        @Override
        protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
                @Override
                protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
                    int rowHeight = Math.max(25, comboBox.getFontMetrics(comboBox.getFont()).getHeight() + 7);
                    int rows = Math.max(1, Math.min(comboBox.getMaximumRowCount(), comboBox.getItemCount()));
                    int alto = (rowHeight * rows) + 2;
                    return super.computePopupBounds(px, py + 8, pw, alto);
                }
            };
        }
    }

    private void adjuntarArchivosMultiples() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Estudios Médicos");
        fileChooser.setMultiSelectionEnabled(true);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Documentos e Imágenes", "pdf", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] seleccionados = fileChooser.getSelectedFiles();
            for (File f : seleccionados) {
                if (!archivosAdjuntos.contains(f)) { archivosAdjuntos.add(f); }
            }
            actualizarEtiquetaArchivos();
        }
    }

    private void actualizarEtiquetaArchivos() {
        if (archivosAdjuntos.isEmpty()) {
            lblRutaAdjunto.setText("   Ningún archivo seleccionado.");
            lblRutaAdjunto.setToolTipText(null);
            lblRutaAdjunto.setForeground(new Color(149, 165, 166));
            rutaArchivoAdjunto = "";
        } else {
            lblRutaAdjunto.setText("   " + archivosAdjuntos.size() + " archivo(s) listo(s) para subir.");
            StringBuilder nombres = new StringBuilder("<html><b>Archivos Seleccionados:</b><br>");
            for (int i = 0; i < archivosAdjuntos.size(); i++) {
                nombres.append("- ").append(archivosAdjuntos.get(i).getName()).append("<br>");
            }
            nombres.append("</html>");

            lblRutaAdjunto.setToolTipText(nombres.toString());
            lblRutaAdjunto.setForeground(new Color(39, 174, 96));
            rutaArchivoAdjunto = "";
        }
    }

    public void iniciarConsulta(String dni) {
        iniciarConsulta(dni, 0);
    }

    public void iniciarConsulta(String dni, int idTurno) {
        this.dniPacienteActual = dni;
        this.idTurnoActual = idTurno;
        this.historiaClinicaEnCurso = idTurno > 0;

        String nombreRealPaciente = "Paciente no encontrado";
        try (Connection cn = ConexionBD.getConexion();
             PreparedStatement pst = cn.prepareStatement(
                     "SELECT TRIM(CONCAT_WS(' ', nombre, apellido)) AS nombre_completo "
                     + "FROM paciente WHERE TRIM(dni) = TRIM(?) AND estado = 'activo'")) {
            pst.setString(1, dni);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    nombreRealPaciente = rs.getString("nombre_completo");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el encabezado del paciente: " + e.getMessage());
        }

        nombrePacienteActual = nombreRealPaciente;
        lblPacienteInfo.setText("Paciente: " + nombreRealPaciente + " (DNI: " + dni + ")");

        Usuario medico = mainApp.getUsuarioSesion();
        idPersonalHistoriaActual = medico != null ? medico.getId_personal() : 0;
        idEspecialidadMedicoActual = medico != null ? medico.getIdEspecialidad() : 0;
        if (idTurno > 0) {
            TurnoDAO.DatosHistoriaTurno datosTurno = new TurnoDAO().obtenerDatosHistoriaTurno(idTurno);
            if (datosTurno != null) {
                idPersonalHistoriaActual = datosTurno.idPersonal();
                idEspecialidadMedicoActual = datosTurno.idEspecialidad();
            }
        }
        listaEnfermedades = new CatalogoDAO().obtenerDiagnosticosActivosPorEspecialidad(idEspecialidadMedicoActual);
        listaTratamientos = new CatalogoDAO().obtenerMedicamentosActivosPorEspecialidad(idEspecialidadMedicoActual);

        txtEvolucion.setText("");
        txtEvolucion.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pnlDiagnostico.setVisible(false);
        pnlTratamiento.setVisible(false);
        pnlArchivo.setVisible(false);
        btnDiagnostico.setBackground(Color.WHITE); btnDiagnostico.setForeground(new Color(41, 128, 185));
        btnTratamiento.setBackground(Color.WHITE); btnTratamiento.setForeground(new Color(41, 128, 185));
        btnArchivo.setBackground(Color.WHITE); btnArchivo.setForeground(new Color(41, 128, 185));

        tabDiagnosticos.removeAll();
        tabsDiagData.clear();
        agregarPestaniaDiagnostico();

        tabTratamientos.removeAll();
        tabsTratData.clear();
        agregarPestaniaTratamiento();

        archivosAdjuntos.clear();
        actualizarEtiquetaArchivos();
    }

    private void resetearBordesFormulario() {
        for (DatosDiagnostico d : tabsDiagData) {
            d.cmbBuscar.setBorder(bordeSuaveInptus);
        }
        for (DatosTratamiento t : tabsTratData) {
            t.cmbBuscar.setBorder(bordeSuaveInptus);
            t.txtDosis.setBorder(bordeSuaveInptus);
            t.cmbFrecuencia.setBorder(bordeSuaveInptus);
            ((JTextFieldDateEditor)t.dateInicio.getDateEditor()).setBorder(BORDE_FECHA_NORMAL);
            ((JTextFieldDateEditor)t.dateFin.getDateEditor()).setBorder(BORDE_FECHA_NORMAL);
        }
    }

    private void guardarConsulta() {
        boolean hayError = false;
        resetearBordesFormulario();

        txtEvolucion.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String evolucion = txtEvolucion.getText().trim();
        if (evolucion.isEmpty()) {
            txtEvolucion.setBorder(BORDE_ERROR);
            hayError = true;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        List<String> diagValidos = new ArrayList<>();
        for (int i = 0; i < tabsDiagData.size(); i++) {
            DatosDiagnostico d = tabsDiagData.get(i);
            String diag = d.cmbBuscar.getSelectedIndex() > 0 ? d.cmbBuscar.getSelectedItem().toString() : "";

            boolean isPestaniaVacia = diag.isEmpty();

            if (!isPestaniaVacia) {
                boolean err = false;
                if (diag.isEmpty()) { d.cmbBuscar.setBorder(BORDE_ERROR); err = true; }

                if (err) {
                    hayError = true;
                    if (!pnlDiagnostico.isVisible()) alternarAcordeon(pnlDiagnostico, btnDiagnostico);
                    tabDiagnosticos.setSelectedIndex(i);
                } else {
                    diagValidos.add("[" + seguroSerializado(diag) + " | Est: " + d.cmbEstado.getSelectedItem() + "]");
                }
            }
        }

        List<String> tratValidos = new ArrayList<>();
        for (int i = 0; i < tabsTratData.size(); i++) {
            DatosTratamiento t = tabsTratData.get(i);
            String trat = t.cmbBuscar.getSelectedIndex() > 0 ? t.cmbBuscar.getSelectedItem().toString() : "";
            String dosis = t.txtDosis.getText().trim();

            boolean isPestaniaVacia = trat.isEmpty() && dosis.isEmpty() && t.cmbFrecuencia.getSelectedIndex() == 0 && t.dateFin.getDate() == null;

            if (!isPestaniaVacia) {
                boolean err = false;
                if (trat.isEmpty()) { t.cmbBuscar.setBorder(BORDE_ERROR); err = true; }
                if (dosis.isEmpty()) { t.txtDosis.setBorder(BORDE_ERROR); err = true; }
                if (t.cmbFrecuencia.getSelectedIndex() == 0) { t.cmbFrecuencia.setBorder(BORDE_ERROR); err = true; }
                if (t.dateInicio.getDate() == null) { ((JTextFieldDateEditor)t.dateInicio.getDateEditor()).setBorder(BORDE_ERROR); err = true; }
                if (t.dateFin.getDate() == null) { ((JTextFieldDateEditor)t.dateFin.getDateEditor()).setBorder(BORDE_ERROR); err = true; }

                if (err) {
                    hayError = true;
                    if (!pnlTratamiento.isVisible()) alternarAcordeon(pnlTratamiento, btnTratamiento);
                    tabTratamientos.setSelectedIndex(i);
                } else {
                    String fIni = sdf.format(t.dateInicio.getDate());
                    String fFin = sdf.format(t.dateFin.getDate());
                    tratValidos.add("[" + seguroSerializado(trat) + " | Dosis: " + seguroSerializado(dosis) + " | Frec: " + t.cmbFrecuencia.getSelectedItem() + " | Inicio: " + fIni + " | Fin: " + fFin + " | Est: " + t.cmbEstado.getSelectedItem() + "]");
                }
            }
        }

        if (hayError) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        String diagFinal = diagValidos.isEmpty() ? "No registrado" : String.join("  ||  ", diagValidos);
        String tratFinal = tratValidos.isEmpty() ? "No registrado" : String.join("  ||  ", tratValidos);

        Usuario medico = mainApp.getUsuarioSesion();
        if (medico == null) {
            JOptionPane.showMessageDialog(this, "ERROR: La sesión expiró.", "Falla de Sesión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idPersonalReal = idPersonalHistoriaActual;
        if (idPersonalReal <= 0) {
            JOptionPane.showMessageDialog(this, "Error Crítico: No se pudo identificar al médico asignado al turno.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (idTurnoActual <= 0) {
            JOptionPane.showMessageDialog(this,
                    "La historia clínica debe cargarse desde un turno de la agenda.",
                    "Turno requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            rutaArchivoAdjunto = ArchivoAdjuntoService.copiar(archivosAdjuntos);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudieron copiar los archivos adjuntos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String resultado = new ConsultaDAO().guardarConsultaSegura(
            dniPacienteActual, idPersonalReal, idTurnoActual, "Evolución Clínica",
            evolucion, diagFinal, tratFinal, "Ver notas de evolución", "", rutaArchivoAdjunto
        );

        if (resultado.equals("OK")) {
            historiaClinicaEnCurso = false;
            JOptionPane.showMessageDialog(this,
                    "¡Evolución guardada exitosamente!\nEl turno fue marcado automáticamente como Atendido.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            volverAlHistorialPaciente();
        } else {
            ArchivoAdjuntoService.eliminar(rutaArchivoAdjunto);
            JOptionPane.showMessageDialog(this, "Error en BD:\n" + resultado, "Reporte de Falla", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void volverAlHistorialPaciente() {
        if (dniPacienteActual == null || dniPacienteActual.isBlank()) {
            mainApp.cambiarPanel("GESTION_PACIENTES");
            return;
        }
        mainApp.irAVerHistoria(dniPacienteActual,
                nombrePacienteActual == null ? "Paciente" : nombrePacienteActual);
    }

    public boolean tieneHistoriaClinicaPendiente() {
        return historiaClinicaEnCurso && idTurnoActual > 0;
    }

    private static String seguroSerializado(String valor) {
        return valor == null ? "" : valor.replace("|", "¦").replace("[", "(").replace("]", ")");
    }

    private boolean puedeGestionarCatalogos() {
        Usuario sesion = mainApp.getUsuarioSesion();
        return sesion != null && (sesion.esAdministrador() || sesion.esMedico());
    }

    private void accesoCatalogoDenegado() {
        JOptionPane.showMessageDialog(this, "Solo los médicos y administradores pueden modificar los catálogos clínicos.", "Acceso denegado", JOptionPane.WARNING_MESSAGE);
    }
}
