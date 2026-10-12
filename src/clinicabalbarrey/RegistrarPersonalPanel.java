package clinicabalbarrey;

import clinicabalbarrey.ConexionBD;
import clinicabalbarrey.dao.RolDAO;
import clinicabalbarrey.security.PasswordUtil;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.*;

public class RegistrarPersonalPanel extends JPanel {
    private MainApplicationView mainApp;
    private JTextField txtDni, txtNombre, txtApellido, txtMatricula, txtTelefono, txtEmail, txtDireccion;
    private JComboBox<String> cmbEspecialidad, cmbRol;

    private JCheckBox chkCrearUsuario;
    private JTextField txtUser;
    private JPasswordField txtPass;

    // Paneles dinámicos
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

    public RegistrarPersonalPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));

        bordeSuaveInptus = BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(200, 205, 208), 1),
            // Menos relleno vertical para que no se corten descendentes como g, q o p.
            new EmptyBorder(5, 12, 5, 12)
        );

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

        JLabel lblTitulo = new JLabel("REGISTRO DE PERSONAL Y HORARIOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(0, 121, 107));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        tarjetaPrincipal.add(lblTitulo);
        JLabel avisoObligatorios = new JLabel("Los campos con * son obligatorios");
        avisoObligatorios.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avisoObligatorios.setForeground(new Color(198, 40, 40));
        avisoObligatorios.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjetaPrincipal.add(Box.createVerticalStrut(5));
        tarjetaPrincipal.add(avisoObligatorios);
        tarjetaPrincipal.add(Box.createVerticalStrut(28));

        JPanel pnlColumnas = new JPanel(new GridLayout(1, 2, 30, 0));
        pnlColumnas.setOpaque(false);
        pnlColumnas.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlColumnas.setMaximumSize(new Dimension(980, 560));

        // COLUMNA IZQUIERDA: DATOS BÁSICOS
        JPanel pnlIzq = crearColumnaFormulario();
        txtDni = crearCampoFiltradoConPlaceholder(10, "[0-9]", "ej: 40123456");
        txtNombre = crearCampoNombresConPlaceholder(40, "ej: Juan");
        txtApellido = crearCampoNombresConPlaceholder(40, "ej: Perez");
        txtTelefono = crearCampoFiltradoConPlaceholder(10, "[0-9]", "ej: 3421234567");

        // Límites amplios para admitir datos reales sin recortar la escritura.
        txtEmail = crearCampoEmailConPlaceholder(100, "ej: juan.perez@clinica.com");
        txtDireccion = crearCampoDireccionConPlaceholder(120, "ej: San Martin 2541");

        pnlIzq.add(crearBloqueInput("DNI (*):", txtDni));
        pnlIzq.add(crearBloqueInput("Nombre (*):", txtNombre));
        pnlIzq.add(crearBloqueInput("Apellido (*):", txtApellido));
        pnlIzq.add(crearBloqueInput("Teléfono (10 números) (*):", txtTelefono));
        pnlIzq.add(crearBloqueInput("Email (Opcional):", txtEmail));
        pnlIzq.add(crearBloqueInput("Dirección (*):", txtDireccion));
        pnlColumnas.add(pnlIzq);

        // COLUMNA DERECHA: SISTEMA
        JPanel pnlDer = crearColumnaFormulario();

        cmbRol = new JComboBox<>(new String[]{"Administrador", "Médico", "Secretaria"});
        cmbRol.setSelectedIndex(-1);
        pnlDer.add(crearBloqueInput("Tipo de personal (*):", cmbRol));

        cmbEspecialidad = new JComboBox<>();
        cmbEspecialidad.setUI(new PopupMasBajoComboBoxUI(8));
        cargarEspecialidades();

        txtMatricula = crearCampoFiltradoConPlaceholder(25, "[a-zA-Z0-9-]", "ej: MP-12345");

        pnlMatricula = crearBloqueInput("Matrícula (*Obligatoria para Médicos):", txtMatricula);
        pnlEspecialidad = crearBloqueInput("Especialidad:", cmbEspecialidad);

        pnlDer.add(pnlMatricula);
        pnlDer.add(pnlEspecialidad);
        pnlDer.add(Box.createVerticalStrut(10));

        chkCrearUsuario = new JCheckBox("Habilitar acceso al sistema (Crear Usuario)");
        chkCrearUsuario.setFont(new Font("Segoe UI", Font.BOLD, 15));
        chkCrearUsuario.setForeground(new Color(0, 121, 107));
        chkCrearUsuario.setBackground(Color.WHITE);
        chkCrearUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDer.add(chkCrearUsuario);
        pnlDer.add(Box.createVerticalStrut(15));

        txtUser = crearCampoUsernameConPlaceholder(30, "ej: juanperez");
        txtPass = crearCampoPassword(64);
        txtPass.setToolTipText("Obligatoria solo si habilitás acceso al sistema");

        txtUser.setEnabled(false);
        txtPass.setEnabled(false);

        pnlDer.add(crearBloqueInput("Nombre de Usuario (obligatorio si habilitás acceso):", txtUser));
        pnlDer.add(crearBloqueInput("Contraseña (obligatoria si habilitás acceso):", txtPass));

        crearPanelHorarios();
        pnlDer.add(crearBloqueHorarios());

        chkCrearUsuario.addActionListener(e -> {
            boolean activo = chkCrearUsuario.isSelected();
            txtUser.setEnabled(activo);
            txtPass.setEnabled(activo);
            if(!activo) {
                txtUser.setBorder(bordeSuaveInptus);
                txtPass.setBorder(bordeSuaveInptus);
            }
            verificarMostrarHorarios();
        });

        cmbRol.addActionListener(e -> verificarMostrarHorarios());

        pnlColumnas.add(pnlDer);
        tarjetaPrincipal.add(pnlColumnas);
        verificarMostrarHorarios();
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

        JButton btnGuardar = new JButton("GUARDAR PERSONAL");
        btnGuardar.setBackground(new Color(0, 150, 136));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGuardar.setPreferredSize(new Dimension(250, 45));
        btnGuardar.setBorder(BorderFactory.createEmptyBorder());
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardar());

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
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(lblTitulo);
        tarjeta.add(Box.createVerticalStrut(25));
        return tarjeta;
    }

    private JPanel crearBloqueInput(String titulo, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean obligatorio = titulo.contains("(*");
        String tituloVisible = titulo.replace("(*):", ":")
                .replace("(*Obligatoria para Médicos):", "(obligatoria para médicos):");
        JLabel lbl = new JLabel(obligatorio
                ? "<html>" + tituloVisible + " <span style='color:#C62828'>*</span></html>"
                : tituloVisible);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(127, 140, 141));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
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
                String horaInicio = cmbInicio[i].getSelectedItem().toString();
                String horaFin = cmbFin[i].getSelectedItem().toString();
                if (horaInicio.compareTo(horaFin) >= 0) {
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
        panelHorarios.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelHorarios.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(0, 150, 136), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panelHorarios.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        panelHorarios.setPreferredSize(new Dimension(560, 245));
        panelHorarios.setMinimumSize(new Dimension(560, 245));
        panelHorarios.setVisible(false);

        JLabel lbl = new JLabel("Horario de Atención del Médico:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(0, 105, 92));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelHorarios.add(lbl);
        panelHorarios.add(Box.createVerticalStrut(15));

        chkDias = new JCheckBox[diasNombres.length];
        cmbInicio = new JComboBox[diasNombres.length];
        cmbFin = new JComboBox[diasNombres.length];

        String[] horas = {"08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"};

        for (int i = 0; i < diasNombres.length; i++) {
            JPanel pDia = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            pDia.setBackground(Color.WHITE);
            pDia.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        boolean esMedico = cmbRol.getSelectedIndex() == 1;
        panelHorarios.setVisible(false);
        if (pnlBotonHorarios != null) pnlBotonHorarios.setVisible(esMedico);

        boolean esAdminOSecretaria = cmbRol.getSelectedIndex() == 0 || cmbRol.getSelectedIndex() == 2;

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

    /** Evita espacio blanco sobrante y conserva un ancho uniforme en todos los roles. */
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

    private void configurarPlaceholder(JTextField tf, String placeholder) {
        tf.setText(placeholder);
        tf.setForeground(Color.GRAY);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setForeground(Color.GRAY);
                    tf.setText(placeholder);
                }
            }
        });
    }

    private JTextField crearCampoUsernameConPlaceholder(int limite, String placeholder) {
        JTextField tf = new JTextField();
        configurarPlaceholder(tf, placeholder);
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) text = "";
                if(tf.getText().equals(placeholder) && text.isEmpty()) {
                    super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
                } else if (tf.getText().equals(placeholder)) {
                    if (!text.contains(" ") && text.matches("[a-zA-Z0-9._-]*") && text.length() <= limite) {
                        super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
                        tf.setForeground(Color.BLACK);
                    } else { Toolkit.getDefaultToolkit().beep(); }
                } else if (text.contains(" ") || !text.matches("[a-zA-Z0-9._-]*")) {
                    Toolkit.getDefaultToolkit().beep(); return;
                } else if (fb.getDocument().getLength() + text.length() - length <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                } else { Toolkit.getDefaultToolkit().beep(); }
                tf.setBorder(bordeSuaveInptus);
            }
        });
        return tf;
    }

    // Método nuevo para limitar los caracteres de la contraseña
    private JPasswordField crearCampoPassword(int limite) {
        JPasswordField pf = new JPasswordField();
        ((AbstractDocument) pf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) text = "";
                if (fb.getDocument().getLength() + text.length() - length <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });
        return pf;
    }

    private JTextField crearCampoFiltradoConPlaceholder(int limite, String regex, String placeholder) {
        JTextField tf = new JTextField();
        configurarPlaceholder(tf, placeholder);
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) text = "";
                if(tf.getText().equals(placeholder) && text.isEmpty()) {
                    super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
                } else if (tf.getText().equals(placeholder)) {
                    if (text.matches(regex + "*") && text.length() <= limite) {
                        super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
                        tf.setForeground(Color.BLACK);
                    } else { Toolkit.getDefaultToolkit().beep(); }
                } else if (text.matches(regex + "*") && fb.getDocument().getLength() + text.length() - length <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                } else { Toolkit.getDefaultToolkit().beep(); }
                tf.setBorder(bordeSuaveInptus);
            }
        });
        return tf;
    }

    private JTextField crearCampoNombresConPlaceholder(int limite, String placeholder) {
        JTextField tf = new JTextField();
        configurarPlaceholder(tf, placeholder);
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) text = "";
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String proposedText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

                if(tf.getText().equals(placeholder) && text.isEmpty()) {
                    super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
                } else if (tf.getText().equals(placeholder)) {
                    if (text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*") && text.length() <= limite && !text.startsWith(" ")) {
                        super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
                        tf.setForeground(Color.BLACK);
                    } else { Toolkit.getDefaultToolkit().beep(); }
                } else if (proposedText.startsWith(" ") || proposedText.contains("  ") || !text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*")) {
                    Toolkit.getDefaultToolkit().beep(); return;
                } else if (proposedText.length() <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                } else { Toolkit.getDefaultToolkit().beep(); }
                tf.setBorder(bordeSuaveInptus);
            }
        });
        return tf;
    }

    private JTextField crearCampoEmailConPlaceholder(int limite, String placeholder) {
        JTextField tf = new JTextField();
        configurarPlaceholder(tf, placeholder);
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) text = "";
                if(tf.getText().equals(placeholder) && text.isEmpty()) {
                    super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
                } else if (tf.getText().equals(placeholder)) {
                    if (!text.contains(" ") && text.length() <= limite) {
                        super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
                        tf.setForeground(Color.BLACK);
                    } else { Toolkit.getDefaultToolkit().beep(); }
                } else if (text.contains(" ")) {
                    Toolkit.getDefaultToolkit().beep(); return;
                } else if (fb.getDocument().getLength() + text.length() - length <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                } else { Toolkit.getDefaultToolkit().beep(); }
                tf.setBorder(bordeSuaveInptus);
            }
        });
        return tf;
    }

    private JTextField crearCampoDireccionConPlaceholder(int limite, String placeholder) {
        JTextField tf = new JTextField();
        configurarPlaceholder(tf, placeholder);
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                replace(fb, offset, 0, string, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) text = "";
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String proposedText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

                if(tf.getText().equals(placeholder) && text.isEmpty()) {
                     super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
                } else if (tf.getText().equals(placeholder)) {
                    if (text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s.,'°-]*") && text.length() <= limite && !text.startsWith(" ")) {
                        super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
                        tf.setForeground(Color.BLACK);
                    } else { Toolkit.getDefaultToolkit().beep(); }
                } else if (!text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s.,'°-]*") || proposedText.startsWith(" ") || proposedText.contains("  ")) {
                    Toolkit.getDefaultToolkit().beep(); return;
                } else if (proposedText.length() <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                } else { Toolkit.getDefaultToolkit().beep(); }
                tf.setBorder(bordeSuaveInptus);
            }
        });
        return tf;
    }

    private String getValorReal(JTextField tf) {
        if(tf.getText().startsWith("ej: ")) return "";
        return tf.getText().trim();
    }

    private void guardar() {
        String dniLimpio = getValorReal(txtDni);
        String nomLimpio = getValorReal(txtNombre).replaceAll("\\s+", " ");
        String apeLimpio = getValorReal(txtApellido).replaceAll("\\s+", " ");
        String telLimpio = getValorReal(txtTelefono);
        String dirLimpia = getValorReal(txtDireccion).replaceAll("\\s+", " ");
        String emailLimpio = getValorReal(txtEmail);

        boolean hayError = false;

        txtDni.setBorder(bordeSuaveInptus);
        txtNombre.setBorder(bordeSuaveInptus);
        txtApellido.setBorder(bordeSuaveInptus);
        txtTelefono.setBorder(bordeSuaveInptus);
        txtEmail.setBorder(bordeSuaveInptus);
        txtDireccion.setBorder(bordeSuaveInptus);
        txtMatricula.setBorder(bordeSuaveInptus);
        txtUser.setBorder(bordeSuaveInptus);
        txtPass.setBorder(bordeSuaveInptus);

        if (dniLimpio.isEmpty() || dniLimpio.length() < 7) { txtDni.setBorder(BORDE_ERROR); hayError = true; }
        if (nomLimpio.isEmpty()) { txtNombre.setBorder(BORDE_ERROR); hayError = true; }
        if (apeLimpio.isEmpty()) { txtApellido.setBorder(BORDE_ERROR); hayError = true; }
        if (telLimpio.isEmpty() || telLimpio.length() < 10) { txtTelefono.setBorder(BORDE_ERROR); hayError = true; }
        if (dirLimpia.isEmpty()) { txtDireccion.setBorder(BORDE_ERROR); hayError = true; }

        // VALIDACIÓN EMAIL (Condicional)
        if (!emailLimpio.isEmpty()) {
            if (!emailLimpio.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                txtEmail.setBorder(BORDE_ERROR);
                hayError = true;
            }
        }

        if (cmbRol.getSelectedIndex() < 0) {
            cmbRol.setBorder(BORDE_ERROR);
            hayError = true;
        }
        boolean esMedico = cmbRol.getSelectedIndex() == 1;

        if (chkCrearUsuario.isSelected()) {
            String passLimpio = new String(txtPass.getPassword()).trim();
            if (getValorReal(txtUser).isEmpty()) { txtUser.setBorder(BORDE_ERROR); hayError = true; }
            if (passLimpio.isEmpty()) { txtPass.setBorder(BORDE_ERROR); hayError = true; }

        }

        if (esMedico) {
            if (getValorReal(txtMatricula).isEmpty()) {
                txtMatricula.setBorder(BORDE_ERROR);
                hayError = true;
            }
            if (cmbEspecialidad.getSelectedIndex() < 0) {
                cmbEspecialidad.setBorder(BORDE_ERROR);
                hayError = true;
            }
            for (int i = 0; i < diasNombres.length; i++) {
                if (chkDias[i].isSelected()) {
                    String horaInicio = cmbInicio[i].getSelectedItem().toString();
                    String horaFin = cmbFin[i].getSelectedItem().toString();
                    if (horaInicio.compareTo(horaFin) >= 0) {
                        JOptionPane.showMessageDialog(this, "Horario inválido en el día " + diasNombres[i] + ".",
                                "Horario incorrecto", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }
        }

        if (hayError) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this,
                    "Completá correctamente los campos marcados.",
                    "Datos obligatorios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombreCompleto = nomLimpio + " " + apeLimpio;
        int indiceEspecialidad = cmbEspecialidad.getSelectedIndex();
        int idEspecialidad = indiceEspecialidad >= 0 && indiceEspecialidad < idsEspecialidades.size()
                ? idsEspecialidades.get(indiceEspecialidad) : 0;

        String sqlPersonal = "INSERT INTO personal (dni, nombreyapellido, matricula, telefono, correoelectronico, direccion, id_especialidad, tipo_personal, estado, fechaingreso) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'activo', CURRENT_DATE)";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try {
                int idPersonalGenerado = 0;
                try (PreparedStatement pst = con.prepareStatement(sqlPersonal, Statement.RETURN_GENERATED_KEYS)) {
                    pst.setString(1, dniLimpio);
                    pst.setString(2, nombreCompleto);
                    if (pnlMatricula.isVisible()) {
                        String matLimpia = getValorReal(txtMatricula);
                        if(matLimpia.isEmpty()) pst.setNull(3, java.sql.Types.VARCHAR);
                        else pst.setString(3, matLimpia);
                    } else { pst.setNull(3, java.sql.Types.VARCHAR); }

                    pst.setLong(4, Long.parseLong(telLimpio));
                    pst.setString(5, emailLimpio); // Guarda el email validado
                    pst.setString(6, dirLimpia);

                    if (idEspecialidad > 0 && esMedico) { pst.setInt(7, idEspecialidad); }
                    else { pst.setNull(7, java.sql.Types.INTEGER); }
                    pst.setString(8, cmbRol.getSelectedItem().toString());

                    pst.executeUpdate();
                    ResultSet rs = pst.getGeneratedKeys();
                    if (rs.next()) { idPersonalGenerado = rs.getInt(1); }
                }

                if (chkCrearUsuario.isSelected() && idPersonalGenerado > 0) {
                    String sqlUsu = "INSERT INTO usuario (username, contrasena, id_personal, id_rol, estado) VALUES (?, ?, ?, ?, 'activo')";
                    try (PreparedStatement pstUsu = con.prepareStatement(sqlUsu)) {
                        pstUsu.setString(1, getValorReal(txtUser));
                        pstUsu.setString(2, PasswordUtil.hash(new String(txtPass.getPassword()).trim().toCharArray()));
                        pstUsu.setInt(3, idPersonalGenerado);
                        pstUsu.setInt(4, RolDAO.obtenerId(con, cmbRol.getSelectedItem().toString()));
                        pstUsu.executeUpdate();
                    }

                }

                if (esMedico && idPersonalGenerado > 0) {
                    String sqlHorario = "INSERT INTO horario (id_personal, diasemana, horainicio, horafin, duracionturnominutos, estado) VALUES (?, ?::dia_semana_enum, ?::time, ?::time, 30, 'activo')";
                    try (PreparedStatement pstHorario = con.prepareStatement(sqlHorario)) {
                        for (int i = 0; i < diasNombres.length; i++) {
                            if (chkDias[i].isSelected()) {
                                pstHorario.setInt(1, idPersonalGenerado);
                                pstHorario.setString(2, diasNombres[i]);
                                pstHorario.setString(3, cmbInicio[i].getSelectedItem().toString() + ":00");
                                pstHorario.setString(4, cmbFin[i].getSelectedItem().toString() + ":00");
                                pstHorario.executeUpdate();
                            }
                        }
                    }
                }
                con.commit();
                JOptionPane.showMessageDialog(this, chkCrearUsuario.isSelected()
                        ? "Personal y acceso al sistema registrados correctamente."
                        : "Personal registrado correctamente, sin acceso al sistema.");

                txtDni.setText(""); txtDni.transferFocusBackward();
                txtNombre.setText(""); txtApellido.setText(""); txtMatricula.setText("");
                txtTelefono.setText(""); txtEmail.setText(""); txtDireccion.setText("");
                chkCrearUsuario.setSelected(false); txtUser.setText(""); txtPass.setText("");
                txtUser.setEnabled(false); txtPass.setEnabled(false); cmbRol.setSelectedIndex(-1);

                for (int i = 0; i < diasNombres.length; i++) {
                    chkDias[i].setSelected(false);
                    cmbInicio[i].setEnabled(false);
                    cmbFin[i].setEnabled(false);
                    cmbFin[i].setSelectedItem("16:00");
                }
                verificarMostrarHorarios();
                mainApp.cambiarPanel("GESTION_PERSONAL");

            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("personal_dni_key") || e.getMessage().contains("llave duplicada")) {
                JOptionPane.showMessageDialog(this, "El DNI ingresado ya se encuentra registrado.", "DNI Duplicado", JOptionPane.WARNING_MESSAGE);
            } else if (e.getMessage().contains("usuario_username_key")) {
                JOptionPane.showMessageDialog(this, "El Nombre de Usuario ya está en uso.", "Usuario Duplicado", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error de Base de Datos:\n" + e.getMessage(), "Falla del Sistema", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean passwordValida(String clave) {
        return clave != null && !clave.trim().isEmpty();
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
