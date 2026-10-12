package clinicabalbarrey;

import clinicabalbarrey.dao.PersonalDAO;
import clinicabalbarrey.dao.RolDAO;
import clinicabalbarrey.model.Personal;
import clinicabalbarrey.model.Usuario;
import clinicabalbarrey.security.PasswordUtil;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.*;

public class EditarPersonalPanel extends JPanel {
    private MainApplicationView mainApp;
    private int idPersonalActual;
    private boolean usuarioExistenteActual;

    private JTextField txtDni, txtNombre, txtApellido, txtMatricula, txtTelefono, txtEmail, txtDireccion, txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbEspecialidad, cmbPermisos;
    private JCheckBox chkHabilitarAcceso;

    private JPanel tarjetaPrincipal;
    private JPanel pnlMatricula;
    private JPanel pnlEspecialidad;
    private JPanel pnlBotonHorarios;
    private JPanel panelHorarios;
    private JButton btnConfigurarHorarios;
    private JCheckBox[] chkDias;
    private JComboBox<String>[] cmbInicio;
    private JComboBox<String>[] cmbFin;
    private String[] diasNombres = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes"};
    private ArrayList<Integer> idsEspecialidades = new ArrayList<>();

    private final Border BORDE_NORMAL = UIManager.getBorder("TextField.border");
    private final Border BORDE_ERROR = EstiloRedondeado.borde(Color.RED, 2);
    private Border bordeSuaveInptus;

    public EditarPersonalPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(new Color(242, 245, 247));

        bordeSuaveInptus = BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(200, 205, 208), 1),
            // Conserva completa la parte inferior de j, g, p y q.
            new EmptyBorder(5, 12, 5, 12)
        );

        JPanel pnlNorte = new JPanel();
        pnlNorte.setLayout(new BoxLayout(pnlNorte, BoxLayout.Y_AXIS));
        pnlNorte.setBackground(new Color(242, 245, 247));
        pnlNorte.setBorder(new EmptyBorder(25, 40, 10, 40));

        JLabel lblT = new JLabel("EDITAR DATOS DEL PERSONAL");
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblT.setForeground(new Color(0, 121, 107));
        lblT.setAlignmentX(Component.LEFT_ALIGNMENT); // ALINEACIÓN FIJADA
        pnlNorte.add(lblT);
        // El titulo queda dentro de la tarjeta principal, igual que en registrar personal.

        JPanel pnlCentro = new JPanel(new GridBagLayout());
        pnlCentro.setBackground(new Color(242, 245, 247));
        pnlCentro.setBorder(new EmptyBorder(32, 30, 32, 30));

        tarjetaPrincipal = new JPanel();
        tarjetaPrincipal.setLayout(new BoxLayout(tarjetaPrincipal, BoxLayout.Y_AXIS));
        tarjetaPrincipal.setBackground(Color.WHITE);
        tarjetaPrincipal.setPreferredSize(new Dimension(920, 590));
        tarjetaPrincipal.setMaximumSize(new Dimension(920, 590));
        tarjetaPrincipal.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(225, 228, 230), 1),
            new EmptyBorder(30, 50, 36, 50)
        ));

        JLabel lblTituloTarjeta = new JLabel("EDITAR DATOS DEL PERSONAL");
        lblTituloTarjeta.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTituloTarjeta.setForeground(new Color(0, 121, 107));
        lblTituloTarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTituloTarjeta.setHorizontalAlignment(SwingConstants.CENTER);
        tarjetaPrincipal.add(lblTituloTarjeta);
        tarjetaPrincipal.add(Box.createVerticalStrut(28));

        JPanel pnlColumnas = new JPanel(new GridLayout(1, 2, 30, 0));
        pnlColumnas.setOpaque(false);
        pnlColumnas.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlColumnas.setMaximumSize(new Dimension(980, 560));

        // COLUMNA IZQUIERDA
        JPanel pnlIzq = crearColumnaFormulario();
        txtDni = crearCampoFiltrado(8, "[0-9]");
        txtNombre = crearCampoNombres(40);
        txtApellido = crearCampoNombres(40);
        txtTelefono = crearCampoFiltrado(10, "[0-9]");
        txtEmail = crearCampoEmail(100);
        txtDireccion = crearCampoDireccion(120);

        pnlIzq.add(crearBloqueInput("DNI (*):", txtDni));
        pnlIzq.add(crearBloqueInput("Nombre (*):", txtNombre));
        pnlIzq.add(crearBloqueInput("Apellido (*):", txtApellido));
        pnlIzq.add(crearBloqueInput("Teléfono (10 números) (*):", txtTelefono));
        pnlIzq.add(crearBloqueInput("Email (Opcional):", txtEmail));
        pnlIzq.add(crearBloqueInput("Dirección (*):", txtDireccion));
        pnlColumnas.add(pnlIzq);

        // COLUMNA DERECHA
        JPanel pnlDer = crearColumnaFormulario();

        cmbPermisos = new JComboBox<>(new String[]{"Administrador", "Médico", "Secretaria"});
        pnlDer.add(crearBloqueInput("Tipo de personal (*):", cmbPermisos));

        cmbEspecialidad = new JComboBox<>();
        cmbEspecialidad.setUI(new PopupMasBajoComboBoxUI(8));
        cargarEspecialidades();

        txtMatricula = crearCampoFiltrado(15, "[a-zA-Z0-9-]");

        pnlMatricula = crearBloqueInput("Matrícula (*Obligatoria para Médicos):", txtMatricula);
        pnlEspecialidad = crearBloqueInput("Especialidad:", cmbEspecialidad);

        pnlDer.add(pnlMatricula);
        pnlDer.add(pnlEspecialidad);
        pnlDer.add(Box.createVerticalStrut(10));

        chkHabilitarAcceso = new JCheckBox("Habilitar acceso al sistema");
        chkHabilitarAcceso.setToolTipText("Crear, editar o deshabilitar el usuario de acceso");
        chkHabilitarAcceso.setFont(new Font("Segoe UI", Font.BOLD, 15));
        chkHabilitarAcceso.setForeground(new Color(0, 121, 107));
        chkHabilitarAcceso.setBackground(Color.WHITE);
        chkHabilitarAcceso.setAlignmentX(Component.LEFT_ALIGNMENT); // ALINEACIÓN FIJADA
        pnlDer.add(chkHabilitarAcceso);
        pnlDer.add(Box.createVerticalStrut(15));

        txtUsername = crearCampoUsername(30);
        txtPassword = crearCampoPassword(64);
        txtPassword.setToolTipText("Dejá en blanco si no querés cambiarla");

        crearPanelHorarios();

        chkHabilitarAcceso.addActionListener(e -> {
            boolean sel = chkHabilitarAcceso.isSelected();
            txtUsername.setEnabled(sel);
            txtPassword.setEnabled(sel);
            if(!sel) {
                txtUsername.setBorder(bordeSuaveInptus);
                txtPassword.setBorder(bordeSuaveInptus);
            }
            verificarMostrarHorarios();
        });

        cmbPermisos.addActionListener(e -> verificarMostrarHorarios());

        pnlDer.add(crearBloqueInput("Nombre de Usuario:", txtUsername));
        pnlDer.add(crearBloqueInput("Contraseña (Dejar en blanco para no cambiar):", txtPassword));
        pnlDer.add(crearBloqueHorarios());

        pnlColumnas.add(pnlDer);
        tarjetaPrincipal.add(pnlColumnas);
        ajustarTarjetaAlContenido();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        pnlCentro.add(tarjetaPrincipal, gbc);

        JScrollPane scroll = new JScrollPane(pnlCentro);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(new Color(242, 245, 247));
        add(scroll, BorderLayout.CENTER);

        // --- BOTONES ---
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 40, 15));
        pnlSur.setBackground(Color.WHITE);
        pnlSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 228, 230)));

        JButton btnVolver = new JButton("CANCELAR Y VOLVER");
        EstiloRedondeado.aplicarBotonPeligro(btnVolver);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnVolver.setPreferredSize(new Dimension(200, 45));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> mainApp.cambiarPanel("GESTION_PERSONAL"));

        JButton btnGuardar = new JButton("GUARDAR CAMBIOS");
        btnGuardar.setBackground(new Color(0, 150, 136));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGuardar.setPreferredSize(new Dimension(250, 45));
        btnGuardar.setBorder(BorderFactory.createEmptyBorder());
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardarCambios());

        pnlSur.add(btnVolver);
        pnlSur.add(btnGuardar);
        add(pnlSur, BorderLayout.SOUTH);
    }

    private JPanel crearColumnaFormulario() {
        JPanel columna = new JPanel();
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.setOpaque(false);
        columna.setAlignmentX(Component.LEFT_ALIGNMENT);
        return columna;
    }

    private JPanel crearTarjeta(String titulo) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(225, 228, 230), 1),
            new EmptyBorder(25, 30, 20, 30)
        ));
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(0, 105, 92));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT); // ALINEACIÓN FIJADA
        tarjeta.add(lblTitulo);
        tarjeta.add(Box.createVerticalStrut(25));
        return tarjeta;
    }

    private JPanel crearBloqueInput(String titulo, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT); // ALINEACIÓN FIJADA

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(127, 140, 141));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT); // ALINEACIÓN FIJADA
        p.add(lbl);
        p.add(Box.createVerticalStrut(8));

        if (comp instanceof JTextField || comp instanceof JComboBox || comp instanceof JPasswordField) {
            comp.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            comp.setBackground(Color.WHITE);
            if (comp instanceof JComboBox) {
                comp.setBorder(BorderFactory.createCompoundBorder(
                    EstiloRedondeado.borde(new Color(200, 205, 208), 1),
                    new EmptyBorder(4, 12, 4, 12)
                ));
            } else {
                comp.setBorder(bordeSuaveInptus);
            }
            if(comp instanceof JPasswordField) {
                comp.addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) { comp.setBorder(bordeSuaveInptus); }
                });
            }
        }
        comp.setAlignmentX(Component.LEFT_ALIGNMENT); // ALINEACIÓN FIJADA
        int alto = comp instanceof JComboBox ? 42 : 44;
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));
        comp.setPreferredSize(new Dimension(0, alto));
        p.add(comp);
        p.add(Box.createVerticalStrut(20));
        return p;
    }

    private JPanel crearBloqueHorarios() {
        pnlBotonHorarios = new JPanel();
        pnlBotonHorarios.setLayout(new BoxLayout(pnlBotonHorarios, BoxLayout.Y_AXIS));
        pnlBotonHorarios.setOpaque(false);
        pnlBotonHorarios.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlBotonHorarios.setVisible(false);

        JLabel lbl = new JLabel("Horario de atención del médico:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(127, 140, 141));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnConfigurarHorarios = new JButton("CONFIGURAR HORARIOS");
        btnConfigurarHorarios.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfigurarHorarios.setForeground(Color.WHITE);
        btnConfigurarHorarios.setBackground(new Color(0, 150, 136));
        btnConfigurarHorarios.setBorder(BorderFactory.createEmptyBorder());
        btnConfigurarHorarios.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfigurarHorarios.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnConfigurarHorarios.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnConfigurarHorarios.setPreferredSize(new Dimension(0, 42));
        btnConfigurarHorarios.addActionListener(e -> mostrarPopupHorarios());

        pnlBotonHorarios.add(lbl);
        pnlBotonHorarios.add(Box.createVerticalStrut(8));
        pnlBotonHorarios.add(btnConfigurarHorarios);
        pnlBotonHorarios.add(Box.createVerticalStrut(20));
        return pnlBotonHorarios;
    }

    private void mostrarPopupHorarios() {
        boolean[] diasPrevios = new boolean[diasNombres.length];
        String[] iniciosPrevios = new String[diasNombres.length];
        String[] finesPrevios = new String[diasNombres.length];
        for (int i = 0; i < diasNombres.length; i++) {
            diasPrevios[i] = chkDias[i].isSelected();
            iniciosPrevios[i] = cmbInicio[i].getSelectedItem().toString();
            finesPrevios[i] = cmbFin[i].getSelectedItem().toString();
        }

        panelHorarios.setVisible(true);
        while (true) {
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    panelHorarios,
                    "Configurar horarios del médico",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (opcion != JOptionPane.OK_OPTION) {
                restaurarHorarios(diasPrevios, iniciosPrevios, finesPrevios);
                panelHorarios.setVisible(false);
                return;
            }
            if (validarHorariosSeleccionados(true)) {
                panelHorarios.setVisible(false);
                return;
            }
        }
    }

    private void restaurarHorarios(boolean[] dias, String[] inicios, String[] fines) {
        for (int i = 0; i < diasNombres.length; i++) {
            chkDias[i].setSelected(dias[i]);
            cmbInicio[i].setSelectedItem(inicios[i]);
            cmbFin[i].setSelectedItem(fines[i]);
            cmbInicio[i].setEnabled(dias[i]);
            cmbFin[i].setEnabled(dias[i]);
        }
    }

    private boolean validarHorariosSeleccionados(boolean mostrarAviso) {
        for (int i = 0; i < diasNombres.length; i++) {
            if (chkDias[i].isSelected()) {
                if (cmbInicio[i].getSelectedItem().toString().compareTo(cmbFin[i].getSelectedItem().toString()) >= 0) {
                    if (mostrarAviso) {
                        JOptionPane.showMessageDialog(this, "Horario inválido en el día " + diasNombres[i] + ".", "Horario Incorrecto", JOptionPane.WARNING_MESSAGE);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private void crearPanelHorarios() {
        panelHorarios = new JPanel();
        panelHorarios.setLayout(new BoxLayout(panelHorarios, BoxLayout.Y_AXIS));
        panelHorarios.setBackground(Color.WHITE);
        panelHorarios.setAlignmentX(Component.LEFT_ALIGNMENT); // ALINEACIÓN FIJADA
        panelHorarios.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(0, 150, 136), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panelHorarios.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        panelHorarios.setPreferredSize(new Dimension(560, 250));
        panelHorarios.setMinimumSize(new Dimension(560, 250));
        panelHorarios.setVisible(false);

        JLabel lbl = new JLabel("Horario de Atención del Médico:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(0, 105, 92));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT); // ALINEACIÓN FIJADA
        panelHorarios.add(lbl);
        panelHorarios.add(Box.createVerticalStrut(15));

        chkDias = new JCheckBox[diasNombres.length];
        cmbInicio = new JComboBox[diasNombres.length];
        cmbFin = new JComboBox[diasNombres.length];

        String[] horas = {"08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"};

        for (int i = 0; i < diasNombres.length; i++) {
            JPanel pDia = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            pDia.setBackground(Color.WHITE);
            pDia.setAlignmentX(Component.LEFT_ALIGNMENT); // ALINEACIÓN FIJADA
            pDia.setMaximumSize(new Dimension(520, 34));

            chkDias[i] = new JCheckBox(diasNombres[i]);
            chkDias[i].setPreferredSize(new Dimension(105, 25));
            chkDias[i].setBackground(Color.WHITE);

            cmbInicio[i] = new JComboBox<>(horas);
            cmbFin[i] = new JComboBox<>(horas);
            cmbFin[i].setSelectedItem("16:00");
            Dimension comboHorario = new Dimension(105, 28);
            cmbInicio[i].setPreferredSize(comboHorario);
            cmbInicio[i].setMinimumSize(comboHorario);
            cmbFin[i].setPreferredSize(comboHorario);
            cmbFin[i].setMinimumSize(comboHorario);

            cmbInicio[i].setEnabled(false);
            cmbFin[i].setEnabled(false);

            final int index = i;
            chkDias[i].addActionListener(e -> {
                boolean sel = chkDias[index].isSelected();
                cmbInicio[index].setEnabled(sel);
                cmbFin[index].setEnabled(sel);
            });

            pDia.add(chkDias[i]);
            pDia.add(new JLabel("De:")); pDia.add(cmbInicio[i]);
            pDia.add(new JLabel(" A:")); pDia.add(cmbFin[i]);

            panelHorarios.add(pDia);
        }
    }

    private void verificarMostrarHorarios() {
        boolean esMedico = cmbPermisos.getSelectedIndex() == 1;
        panelHorarios.setVisible(false);
        if (pnlBotonHorarios != null) pnlBotonHorarios.setVisible(esMedico);

        boolean esAdminOSecretaria = cmbPermisos.getSelectedIndex() == 0 || cmbPermisos.getSelectedIndex() == 2;

        pnlMatricula.setVisible(esMedico);
        pnlEspecialidad.setVisible(esMedico);

        if (esAdminOSecretaria) {
            txtMatricula.setText("");
            txtMatricula.setBorder(bordeSuaveInptus);
            cmbEspecialidad.setSelectedIndex(-1);
        }
        ajustarTarjetaAlContenido();
        revalidate();
        repaint();
    }

    /** Ajusta la altura de la tarjeta a los campos realmente visibles del rol editado. */
    private void ajustarTarjetaAlContenido() {
        if (tarjetaPrincipal == null) return;
        tarjetaPrincipal.setPreferredSize(null);
        tarjetaPrincipal.revalidate();
        int alto = Math.max(560, tarjetaPrincipal.getPreferredSize().height);
        Dimension tamanio = new Dimension(920, alto);
        tarjetaPrincipal.setPreferredSize(tamanio);
        tarjetaPrincipal.setMaximumSize(tamanio);
    }

    private void cargarEspecialidades() {
        try (Connection con = ConexionBD.getConexion(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_especialidad, nombre FROM especialidad ORDER BY nombre")) {
            while (rs.next()) {
                String nombre = rs.getString("nombre");
                if (esEspecialidadDeRol(nombre)) continue;
                idsEspecialidades.add(rs.getInt("id_especialidad"));
                cmbEspecialidad.addItem(nombre);
            }
            if (idsEspecialidades.isEmpty()) {
                idsEspecialidades.add(0);
                cmbEspecialidad.addItem("Faltan Especialidades en BD");
            }
            int filas = Math.max(1, Math.min(6, cmbEspecialidad.getItemCount()));
            cmbEspecialidad.setMaximumRowCount(filas);
            cmbEspecialidad.setUI(new PopupMasBajoComboBoxUI(filas));
        } catch (Exception e) {
            System.err.println("No se pudieron cargar las especialidades: " + e.getMessage());
        }
    }

    private boolean esEspecialidadDeRol(String nombre) {
        if (nombre == null) return false;
        String normalizado = nombre.toLowerCase();
        return normalizado.contains("secretar") || normalizado.contains("recep");
    }

    private JTextField crearCampoUsername(int limite) {
        JTextField tf = new JTextField();
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.contains(" ") || !text.matches("[a-zA-Z0-9._-]*")) {
                    Toolkit.getDefaultToolkit().beep(); return;
                }
                if (fb.getDocument().getLength() + text.length() - length <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                    tf.setBorder(bordeSuaveInptus);
                } else { Toolkit.getDefaultToolkit().beep(); }
            }
        });
        return tf;
    }

    private JPasswordField crearCampoPassword(int limite) {
        JPasswordField campo = new JPasswordField();
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void replace(FilterBypass fb, int offset, int length, String texto,
                    AttributeSet attrs) throws BadLocationException {
                if (texto == null) texto = "";
                if (fb.getDocument().getLength() + texto.length() - length <= limite) {
                    super.replace(fb, offset, length, texto, attrs);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });
        return campo;
    }

    private boolean passwordValida(String clave) {
        return clave != null && !clave.trim().isEmpty();
    }

    private JTextField crearCampoEmail(int limite) {
        JTextField tf = new JTextField();
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.contains(" ")) { Toolkit.getDefaultToolkit().beep(); return; }
                if (fb.getDocument().getLength() + text.length() - length <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                    tf.setBorder(bordeSuaveInptus);
                } else { Toolkit.getDefaultToolkit().beep(); }
            }
        });
        return tf;
    }

    private JTextField crearCampoDireccion(int limite) {
        JTextField tf = new JTextField();
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (!text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s.,'°-]*")) {
                    Toolkit.getDefaultToolkit().beep(); return;
                }
                if (fb.getDocument().getLength() + text.length() - length <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                    tf.setBorder(bordeSuaveInptus);
                } else { Toolkit.getDefaultToolkit().beep(); }
            }
        });
        return tf;
    }

    private JTextField crearCampoNombres(int limite) {
        JTextField tf = new JTextField();
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (!text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*")) {
                    Toolkit.getDefaultToolkit().beep(); return;
                }
                if (fb.getDocument().getLength() + text.length() - length <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                    tf.setBorder(bordeSuaveInptus);
                } else { Toolkit.getDefaultToolkit().beep(); }
            }
        });
        return tf;
    }

    private JTextField crearCampoFiltrado(int limite, String regex) {
        JTextField tf = new JTextField();
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches(regex + "*") && fb.getDocument().getLength() + text.length() - length <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                    tf.setBorder(bordeSuaveInptus);
                } else { Toolkit.getDefaultToolkit().beep(); }
            }
        });
        return tf;
    }

    public boolean prepararEdicion(int idPersonal) {
        try {
            PersonalDAO dao = new PersonalDAO();
            Personal p = dao.buscarPorId(idPersonal);

            if (p != null) {
                this.idPersonalActual = idPersonal;

                txtDni.setBorder(bordeSuaveInptus);
                txtNombre.setBorder(bordeSuaveInptus);
                txtApellido.setBorder(bordeSuaveInptus);
                txtTelefono.setBorder(bordeSuaveInptus);
                txtDireccion.setBorder(bordeSuaveInptus);
                txtUsername.setBorder(bordeSuaveInptus);
                txtPassword.setBorder(bordeSuaveInptus);

                txtDni.setText(p.getDni());
                String[] nomApe = p.getNombreYApellido().split(" ", 2);
                txtNombre.setText(nomApe[0]);
                txtApellido.setText(nomApe.length > 1 ? nomApe[1] : "");

                txtMatricula.setText(p.getMatricula() != null ? p.getMatricula() : "");
                txtTelefono.setText(p.getTelefono());
                txtEmail.setText(p.getCorreoElectronico() != null ? p.getCorreoElectronico() : "");
                txtDireccion.setText(p.getDireccion() != null ? p.getDireccion() : "");

                for (int i = 0; i < idsEspecialidades.size(); i++) {
                    if (p.getIdEspecialidad() == idsEspecialidades.get(i)) {
                        cmbEspecialidad.setSelectedIndex(i);
                        break;
                    }
                }

                chkHabilitarAcceso.setSelected(false);
                usuarioExistenteActual = false;
                txtUsername.setEnabled(false);
                txtPassword.setEnabled(false);
                if (p.getRol() != null) cmbPermisos.setSelectedItem(p.getRol());
                txtUsername.setText("");
                txtPassword.setText("");
                for (int i = 0; i < diasNombres.length; i++) {
                    chkDias[i].setSelected(false);
                    cmbInicio[i].setEnabled(false);
                    cmbFin[i].setEnabled(false);
                }

                try (Connection con = ConexionBD.getConexion();
                     PreparedStatement pst = con.prepareStatement("SELECT u.username, u.id_rol, r.nombre AS rol FROM usuario u JOIN rol r ON r.id_rol=u.id_rol WHERE u.id_personal = ? AND u.estado::text = 'activo'")) {
                    pst.setInt(1, idPersonal);
                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        usuarioExistenteActual = true;
                        chkHabilitarAcceso.setSelected(true);
                        txtUsername.setEnabled(true);
                        txtPassword.setEnabled(true);
                        txtUsername.setText(rs.getString("username"));
                        int idRol = rs.getInt("id_rol");
                        if (idRol == 2) cmbPermisos.setSelectedItem("Médico");
                        else if (idRol == 3) cmbPermisos.setSelectedItem("Secretaria");
                        else cmbPermisos.setSelectedItem("Administrador");

                        if (idRol == 2) {
                            String sqlHor = "SELECT diasemana, horainicio, horafin FROM horario WHERE id_personal = ? AND estado::text = 'activo'";
                            try (PreparedStatement pstHor = con.prepareStatement(sqlHor)) {
                                pstHor.setInt(1, idPersonal);
                                ResultSet rsHor = pstHor.executeQuery();
                                while (rsHor.next()) {
                                    String diaBD = rsHor.getString("diasemana");
                                    String hIn = rsHor.getString("horainicio").substring(0, 5);
                                    String hOut = rsHor.getString("horafin").substring(0, 5);

                                    for (int i = 0; i < diasNombres.length; i++) {
                                        if (diasNombres[i].equalsIgnoreCase(diaBD)) {
                                            chkDias[i].setSelected(true);
                                            cmbInicio[i].setEnabled(true);
                                            cmbFin[i].setEnabled(true);
                                            cmbInicio[i].setSelectedItem(hIn);
                                            cmbFin[i].setSelectedItem(hOut);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                verificarMostrarHorarios();
                return true;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el personal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private void guardarCambios() {
        String dniLimpio = txtDni.getText().trim();
        String nomLimpio = txtNombre.getText().trim().replaceAll("\\s+", " ");
        String apeLimpio = txtApellido.getText().trim().replaceAll("\\s+", " ");
        String telLimpio = txtTelefono.getText().trim();
        String dirLimpia = txtDireccion.getText().trim().replaceAll("\\s+", " ");

        boolean hayError = false;

        txtDni.setBorder(bordeSuaveInptus);
        txtNombre.setBorder(bordeSuaveInptus);
        txtApellido.setBorder(bordeSuaveInptus);
        txtTelefono.setBorder(bordeSuaveInptus);
        txtDireccion.setBorder(bordeSuaveInptus);
        txtMatricula.setBorder(bordeSuaveInptus);
        txtUsername.setBorder(bordeSuaveInptus);
        txtPassword.setBorder(bordeSuaveInptus);

        if (dniLimpio.isEmpty() || dniLimpio.length() < 7) { txtDni.setBorder(BORDE_ERROR); hayError = true; }
        if (nomLimpio.isEmpty()) { txtNombre.setBorder(BORDE_ERROR); hayError = true; }
        if (apeLimpio.isEmpty()) { txtApellido.setBorder(BORDE_ERROR); hayError = true; }
        if (telLimpio.isEmpty() || telLimpio.length() < 10) { txtTelefono.setBorder(BORDE_ERROR); hayError = true; }
        if (dirLimpia.isEmpty()) { txtDireccion.setBorder(BORDE_ERROR); hayError = true; }
        String emailLimpio = txtEmail.getText().trim();
        if (!emailLimpio.isEmpty() && !emailLimpio.matches("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,}$")) {
            txtEmail.setBorder(BORDE_ERROR);
            hayError = true;
        }

        boolean esMedico = cmbPermisos.getSelectedIndex() == 1;

        if (chkHabilitarAcceso.isSelected()) {
            if (txtUsername.getText().trim().isEmpty()) { txtUsername.setBorder(BORDE_ERROR); hayError = true; }
            String clave = new String(txtPassword.getPassword()).trim();
            if (!usuarioExistenteActual && !passwordValida(clave)) {
                txtPassword.setBorder(BORDE_ERROR);
                hayError = true;
            }

            if (esMedico) {
                if (txtMatricula.getText().trim().isEmpty()) {
                    txtMatricula.setBorder(BORDE_ERROR);
                    hayError = true;
                }
                if (cmbEspecialidad.getSelectedIndex() < 0) {
                    cmbEspecialidad.setBorder(BORDE_ERROR);
                    hayError = true;
                }
                for (int i = 0; i < diasNombres.length; i++) {
                    if (chkDias[i].isSelected()) {
                        if (cmbInicio[i].getSelectedItem().toString().compareTo(cmbFin[i].getSelectedItem().toString()) >= 0) {
                        JOptionPane.showMessageDialog(this, "Horario inválido en el día " + diasNombres[i] + ".", "Horario Incorrecto", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }
                }
            }
        }
        Usuario sesion = mainApp.getUsuarioSesion();
        if (!chkHabilitarAcceso.isSelected() && sesion != null && sesion.getId_personal() == idPersonalActual) {
            JOptionPane.showMessageDialog(this, "No podés deshabilitar tu propia cuenta mientras está iniciada la sesión.", "Operación bloqueada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (hayError) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        int indiceEspecialidad = cmbEspecialidad.getSelectedIndex();
        int idEspecialidad = indiceEspecialidad >= 0 && indiceEspecialidad < idsEspecialidades.size()
                ? idsEspecialidades.get(indiceEspecialidad) : 0;

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try {
            int idRolSeleccionado = chkHabilitarAcceso.isSelected()
                    ? RolDAO.obtenerId(con, cmbPermisos.getSelectedItem().toString()) : 0;
            String sqlPer = "UPDATE personal SET dni=?, nombreyapellido=?, matricula=?, telefono=?, correoelectronico=?, direccion=?, id_especialidad=?, tipo_personal=? WHERE id_personal=?";
            try (PreparedStatement pst = con.prepareStatement(sqlPer)) {
                pst.setString(1, dniLimpio);
                pst.setString(2, nomLimpio + " " + apeLimpio);
                pst.setString(3, txtMatricula.getText().trim());
                pst.setLong(4, Long.parseLong(telLimpio));
                pst.setString(5, emailLimpio);
                pst.setString(6, dirLimpia);

                if (idEspecialidad > 0 && esMedico) pst.setInt(7, idEspecialidad);
                else pst.setNull(7, java.sql.Types.INTEGER);

                pst.setString(8, cmbPermisos.getSelectedItem().toString());
                pst.setInt(9, idPersonalActual);
                pst.executeUpdate();
            }

            if (chkHabilitarAcceso.isSelected()) {
                boolean existeUsu = false;
                try (PreparedStatement checkUsu = con.prepareStatement("SELECT id_usuario FROM usuario WHERE id_personal = ?")) {
                    checkUsu.setInt(1, idPersonalActual);
                    ResultSet rs = checkUsu.executeQuery();
                    if (rs.next()) existeUsu = true;
                }

                String pass = new String(txtPassword.getPassword()).trim();

                if (existeUsu) {
                    String sqlUpdUsu = "UPDATE usuario SET username=?, id_rol=?, estado='activo' " + (!pass.isEmpty() ? ", contrasena=? " : "") + "WHERE id_personal=?";
                    try (PreparedStatement pstUpd = con.prepareStatement(sqlUpdUsu)) {
                        pstUpd.setString(1, txtUsername.getText().trim());
                        pstUpd.setInt(2, idRolSeleccionado);
                        if (!pass.isEmpty()) {
                            pstUpd.setString(3, PasswordUtil.hash(pass.toCharArray()));
                            pstUpd.setInt(4, idPersonalActual);
                        } else {
                            pstUpd.setInt(3, idPersonalActual);
                        }
                        pstUpd.executeUpdate();
                    }
                } else {
                    if (pass.isEmpty()) throw new SQLException("La contraseña inicial es obligatoria.");
                    String sqlInsUsu = "INSERT INTO usuario (username, contrasena, id_personal, id_rol, estado) VALUES (?, ?, ?, ?, 'activo')";
                    try (PreparedStatement pstIns = con.prepareStatement(sqlInsUsu)) {
                        pstIns.setString(1, txtUsername.getText().trim());
                        pstIns.setString(2, PasswordUtil.hash(pass.toCharArray()));
                        pstIns.setInt(3, idPersonalActual);
                        pstIns.setInt(4, idRolSeleccionado);
                        pstIns.executeUpdate();
                    }
                }

            } else {
                try (PreparedStatement pstDisUsu = con.prepareStatement("UPDATE usuario SET estado='inactivo' WHERE id_personal = ?")) {
                    pstDisUsu.setInt(1, idPersonalActual);
                    pstDisUsu.executeUpdate();
                }
            }

            try (PreparedStatement pstBajaHor = con.prepareStatement("UPDATE horario SET estado = 'inactivo' WHERE id_personal = ? AND estado = 'activo'")) {
                pstBajaHor.setInt(1, idPersonalActual);
                pstBajaHor.executeUpdate();
            }
            if (esMedico) {
                String sqlHorario = "INSERT INTO horario (id_personal, diasemana, horainicio, horafin, duracionturnominutos, estado) VALUES (?, ?::dia_semana_enum, ?::time, ?::time, 30, 'activo')";
                try (PreparedStatement pstHor = con.prepareStatement(sqlHorario)) {
                    for (int i = 0; i < diasNombres.length; i++) {
                        if (chkDias[i].isSelected()) {
                            pstHor.setInt(1, idPersonalActual);
                            pstHor.setString(2, diasNombres[i]);
                            pstHor.setString(3, cmbInicio[i].getSelectedItem().toString() + ":00");
                            pstHor.setString(4, cmbFin[i].getSelectedItem().toString() + ":00");
                            pstHor.executeUpdate();
                        }
                    }
                }
            }

            con.commit();
            JOptionPane.showMessageDialog(this, "Datos actualizados correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            mainApp.cambiarPanel("GESTION_PERSONAL");

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("usuario_username_key") || e.getMessage().contains("llave duplicada")) {
                JOptionPane.showMessageDialog(this, "El DNI o el Nombre de Usuario ingresado ya está en uso.", "Dato Duplicado", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar:\n" + e.getMessage(), "Error en BD", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class PopupMasBajoComboBoxUI extends BasicComboBoxUI {
        private final int maxRows;

        PopupMasBajoComboBoxUI(int maxRows) {
            this.maxRows = maxRows;
        }

        @Override
        protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
                @Override
                protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
                    int rowHeight = Math.max(24, comboBox.getFontMetrics(comboBox.getFont()).getHeight() + 6);
                    int rows = Math.max(1, Math.min(maxRows, comboBox.getItemCount()));
                    int alto = (rowHeight * rows) + 2;
                    return super.computePopupBounds(px, py + 6, pw, alto);
                }
            };
        }
    }
}
