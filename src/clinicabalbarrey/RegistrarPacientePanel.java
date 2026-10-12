package clinicabalbarrey;

import clinicabalbarrey.ConexionBD;
import clinicabalbarrey.dao.PacienteDAO;
import clinicabalbarrey.model.Paciente;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

public class RegistrarPacientePanel extends JPanel {
    private MainApplicationView mainApp;
    private JTextField txtDni, txtNombre, txtApellido, txtTelefono, txtEmail, txtDireccion, txtNroAfiliado;
    private JDateChooser dateChooserNac;
    private JComboBox<String> cmbObraSocial, cmbSexo;
    private ArrayList<Integer> idsObrasSociales = new ArrayList<>();

    private final Border BORDE_NORMAL = UIManager.getBorder("TextField.border");
    private final Border BORDE_ERROR = EstiloRedondeado.borde(Color.RED, 2);
    private Border bordeSuaveInptus;

    public RegistrarPacientePanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(new Color(242, 245, 247));

        bordeSuaveInptus = BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(200, 205, 208), 1),
            new EmptyBorder(6, 12, 6, 12)
        );

        // --- CUERPO CENTRAL: CONTENEDOR WEB EXPANSIVO ---
        JPanel pnlFondoWeb = new JPanel(new GridBagLayout());
        pnlFondoWeb.setBackground(new Color(242, 245, 247));
        pnlFondoWeb.setBorder(new EmptyBorder(10, 30, 25, 30));

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new GridBagLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setPreferredSize(new Dimension(900, 560));
        tarjeta.setMinimumSize(new Dimension(820, 540));
        tarjeta.setMaximumSize(new Dimension(900, 560));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(225, 228, 230), 1),
            new EmptyBorder(26, 50, 30, 50)
        ));

        JPanel contenidoFormulario = new JPanel();
        contenidoFormulario.setLayout(new BoxLayout(contenidoFormulario, BoxLayout.Y_AXIS));
        contenidoFormulario.setOpaque(false);

        JLabel lblTitulo = new JLabel("REGISTRAR NUEVO PACIENTE");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(0, 105, 92));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel pnlEncabezado = new JPanel();
        pnlEncabezado.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlEncabezado.setOpaque(false);
        pnlEncabezado.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlEncabezado.add(lblTitulo);
        contenidoFormulario.add(pnlEncabezado);
        contenidoFormulario.add(Box.createVerticalStrut(24));

        // Inicialización de componentes
        txtDni = crearCampoFiltradoConPlaceholder(8, "[0-9]", "Ej: 40123456");
        txtNombre = crearCampoNombresConPlaceholder(30, "Ej: Alexis");
        txtApellido = crearCampoNombresConPlaceholder(30, "Ej: Balbarrey");

        dateChooserNac = new JDateChooser();
        dateChooserNac.setDateFormatString("dd/MM/yyyy");
        dateChooserNac.setMaxSelectableDate(new java.util.Date());
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.YEAR, -120);
        dateChooserNac.setMinSelectableDate(cal.getTime());
        JTextFieldDateEditor editorFecha = (JTextFieldDateEditor) dateChooserNac.getDateEditor();
        editorFecha.setEditable(false);
        editorFecha.setBackground(Color.WHITE);

        cmbSexo = new JComboBox<>(new String[]{"Femenino", "Masculino", "Otro"});

        txtTelefono = crearCampoFiltradoConPlaceholder(10, "[0-9]", "Ej: 3426280060");
        txtEmail = crearCampoEmailConPlaceholder(100, "Ej: alexis@gmail.com");
        txtDireccion = crearCampoDireccionConPlaceholder(100, "Ej: Velez Sarsfield 3254");
        txtNroAfiliado = crearCampoFiltradoConPlaceholder(30, "[a-zA-Z0-9-]", "Ej: 12345678-01"); // Aumenté el límite a 30 por las dudas

        cmbObraSocial = new JComboBox<>();
        cargarObrasSociales();

        cmbObraSocial.addActionListener(e -> {
            boolean esParticular = cmbObraSocial.getSelectedIndex() == 0;
            txtNroAfiliado.setEnabled(!esParticular);
            if(esParticular) {
                txtNroAfiliado.setText("");
                txtNroAfiliado.setBorder(bordeSuaveInptus);
            }
        });
        txtNroAfiliado.setEnabled(cmbObraSocial.getSelectedIndex() != 0);

        // --- CONSTRUCCIÓN DE LA TARJETA ---
        Dimension altoFilaSimple = new Dimension(1000, 75);
        int separacionFilas = 12;

        JPanel fNombreApellido = new JPanel(new GridLayout(1, 2, 30, 0));
        fNombreApellido.setOpaque(false);
        fNombreApellido.setAlignmentX(Component.LEFT_ALIGNMENT);
        fNombreApellido.setMaximumSize(altoFilaSimple);
        fNombreApellido.add(crearBloqueInput("Nombre (*):", txtNombre));
        fNombreApellido.add(crearBloqueInput("Apellido (*):", txtApellido));
        contenidoFormulario.add(fNombreApellido);
        contenidoFormulario.add(Box.createVerticalStrut(separacionFilas));

        JPanel fDniSexo = new JPanel(new GridLayout(1, 2, 30, 0));
        fDniSexo.setOpaque(false);
        fDniSexo.setAlignmentX(Component.LEFT_ALIGNMENT);
        fDniSexo.setMaximumSize(altoFilaSimple);
        fDniSexo.add(crearBloqueInput("DNI (*):", txtDni));
        fDniSexo.add(crearBloqueInput("Género (*):", cmbSexo));
        contenidoFormulario.add(fDniSexo);
        contenidoFormulario.add(Box.createVerticalStrut(separacionFilas));

        JPanel fNacTel = new JPanel(new GridLayout(1, 2, 30, 0));
        fNacTel.setOpaque(false);
        fNacTel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fNacTel.setMaximumSize(altoFilaSimple);
        fNacTel.add(crearBloqueInput("Fecha de Nacimiento (*):", dateChooserNac));
        fNacTel.add(crearBloqueInput("Teléfono (*):", txtTelefono));
        contenidoFormulario.add(fNacTel);
        contenidoFormulario.add(Box.createVerticalStrut(separacionFilas));

        JPanel fContacto = new JPanel(new GridLayout(1, 2, 30, 0));
        fContacto.setOpaque(false);
        fContacto.setAlignmentX(Component.LEFT_ALIGNMENT);
        fContacto.setMaximumSize(altoFilaSimple);
        fContacto.add(crearBloqueInput("Dirección de Email (Opcional):", txtEmail));
        fContacto.add(crearBloqueInput("Dirección (*):", txtDireccion));
        contenidoFormulario.add(fContacto);
        contenidoFormulario.add(Box.createVerticalStrut(separacionFilas));

        JPanel fObra = new JPanel(new GridLayout(1, 2, 30, 0));
        fObra.setOpaque(false);
        fObra.setAlignmentX(Component.LEFT_ALIGNMENT);
        fObra.setMaximumSize(altoFilaSimple);
        fObra.add(crearBloqueInput("Cobertura / Obra Social (*):", cmbObraSocial));
        fObra.add(crearBloqueInput("Nro. de Afiliado (*Si posee cobertura):", txtNroAfiliado));
        contenidoFormulario.add(fObra);

        GridBagConstraints gbcFormulario = new GridBagConstraints();
        gbcFormulario.gridx = 0;
        gbcFormulario.gridy = 0;
        gbcFormulario.weightx = 1;
        gbcFormulario.weighty = 1;
        gbcFormulario.fill = GridBagConstraints.HORIZONTAL;
        gbcFormulario.anchor = GridBagConstraints.CENTER;
        tarjeta.add(contenidoFormulario, gbcFormulario);

        GridBagConstraints gbcTarjeta = new GridBagConstraints();
        gbcTarjeta.gridx = 0; gbcTarjeta.gridy = 0; gbcTarjeta.weightx = 1; gbcTarjeta.weighty = 1;
        gbcTarjeta.fill = GridBagConstraints.NONE; gbcTarjeta.anchor = GridBagConstraints.CENTER;
        pnlFondoWeb.add(tarjeta, gbcTarjeta);

        JScrollPane scrollGeneral = new JScrollPane(pnlFondoWeb);
        scrollGeneral.setBorder(null);
        scrollGeneral.getVerticalScrollBar().setUnitIncrement(16);
        scrollGeneral.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollGeneral, BorderLayout.CENTER);

        // --- BOTONES ---
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 15));
        pnlSur.setBackground(Color.WHITE);
        pnlSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 228, 230)));

        JButton btnVolver = new JButton("Cancelar");
        EstiloRedondeado.aplicarBotonPeligro(btnVolver);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolver.setPreferredSize(new Dimension(150, 45));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setFocusPainted(false);
        btnVolver.addActionListener(e -> mainApp.volverDesdeRegistroPaciente());

        JButton btnRegistrar = new JButton("Guardar Paciente");
        btnRegistrar.setBackground(new Color(41, 128, 185));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnRegistrar.setPreferredSize(new Dimension(200, 45));
        btnRegistrar.setBorder(BorderFactory.createEmptyBorder());
        btnRegistrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.addActionListener(e -> guardarNuevo());

        pnlSur.add(btnVolver);
        pnlSur.add(btnRegistrar);
        add(pnlSur, BorderLayout.SOUTH);
    }

    private JPanel crearFila() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        return p;
    }

    public void prepararAltaDesdeTurno(String busquedaPaciente) {
        String texto = busquedaPaciente == null ? "" : busquedaPaciente.trim();
        if (texto.matches("\\d{7,8}")) {
            txtDni.setText(texto);
            SwingUtilities.invokeLater(() -> txtNombre.requestFocusInWindow());
        } else {
            SwingUtilities.invokeLater(() -> txtDni.requestFocusInWindow());
        }
    }

    private JPanel crearBloqueInput(String titulo, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 110, 115));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(6));

        if (comp instanceof JTextField || comp instanceof JComboBox) {
            comp.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            comp.setBackground(Color.WHITE);
            comp.setBorder(bordeSuaveInptus);
        }
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMinimumSize(new Dimension(0, 48));
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        comp.setPreferredSize(new Dimension(0, 48));
        p.add(comp);
        return p;
    }

    private void cargarObrasSociales() {
        idsObrasSociales.add(0);
        cmbObraSocial.addItem("Particular / Sin Obra Social");
        try (Connection con = ConexionBD.getConexion(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_obrasocial, nombre FROM obrasocial WHERE estado = 'activo'")) {
            while (rs.next()) {
                idsObrasSociales.add(rs.getInt("id_obrasocial"));
                cmbObraSocial.addItem(rs.getString("nombre"));
            }
        } catch (Exception e) {
            System.err.println("No se pudieron cargar las obras sociales: " + e.getMessage());
        }
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
                    if (text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s.,'-]*") && text.length() <= limite && !text.startsWith(" ")) {
                        super.replace(fb, 0, fb.getDocument().getLength(), text, attrs);
                        tf.setForeground(Color.BLACK);
                    } else { Toolkit.getDefaultToolkit().beep(); }
                } else if (!text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s.,'-]*") || proposedText.startsWith(" ") || proposedText.contains("  ")) {
                    Toolkit.getDefaultToolkit().beep(); return;
                } else if (proposedText.length() <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                } else { Toolkit.getDefaultToolkit().beep(); }
                tf.setBorder(bordeSuaveInptus);
            }
        });
        return tf;
    }

    // Método que ignora los placeholders ("Ej: ...") al momento de guardar
    private String getValorReal(JTextField tf) {
        if (Color.GRAY.equals(tf.getForeground())) return "";
        return tf.getText().trim();
    }

    private void guardarNuevo() {
        String dniLimpio = getValorReal(txtDni);
        String nomLimpio = getValorReal(txtNombre);
        String apeLimpio = getValorReal(txtApellido);
        String telLimpio = getValorReal(txtTelefono);
        String dirLimpia = getValorReal(txtDireccion);
        String nroAfiliadoLimpio = getValorReal(txtNroAfiliado);
        String emailLimpio = getValorReal(txtEmail);
        java.util.Date fechaNac = dateChooserNac.getDate();

        boolean hayError = false;

        // Reset de bordes
        txtDni.setBorder(bordeSuaveInptus);
        txtNombre.setBorder(bordeSuaveInptus);
        txtApellido.setBorder(bordeSuaveInptus);
        txtTelefono.setBorder(bordeSuaveInptus);
        txtEmail.setBorder(bordeSuaveInptus);
        txtDireccion.setBorder(bordeSuaveInptus);
        txtNroAfiliado.setBorder(bordeSuaveInptus);
        ((JTextFieldDateEditor)dateChooserNac.getDateEditor()).setBorder(BORDE_NORMAL);

        // Validaciones vacías estándar
        if (dniLimpio.isEmpty() || dniLimpio.length() < 7) { txtDni.setBorder(BORDE_ERROR); hayError = true; }
        if (nomLimpio.isEmpty()) { txtNombre.setBorder(BORDE_ERROR); hayError = true; }
        if (apeLimpio.isEmpty()) { txtApellido.setBorder(BORDE_ERROR); hayError = true; }
        if (telLimpio.isEmpty() || telLimpio.length() < 10) { txtTelefono.setBorder(BORDE_ERROR); hayError = true; }
        if (dirLimpia.isEmpty()) { txtDireccion.setBorder(BORDE_ERROR); hayError = true; }
        if (fechaNac == null) { ((JTextFieldDateEditor)dateChooserNac.getDateEditor()).setBorder(BORDE_ERROR); hayError = true; }

        // VALIDACIÓN DE EMAIL CONDICIONAL:
        // Si el usuario escribió algo, obligamos a que tenga un formato de email válido.
        if (!emailLimpio.isEmpty()) {
            if (!emailLimpio.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                txtEmail.setBorder(BORDE_ERROR);
                hayError = true;
            }
        }

        int idxObra = cmbObraSocial.getSelectedIndex();

        // Validación Obra Social
        if (idxObra != 0 && nroAfiliadoLimpio.isEmpty()) {
            txtNroAfiliado.setBorder(BORDE_ERROR);
            hayError = true;
        }

        if(hayError) { Toolkit.getDefaultToolkit().beep(); return; }

        Paciente p = new Paciente();
        p.setDni(dniLimpio);
        p.setNombre(nomLimpio);
        p.setApellido(apeLimpio);
        p.setFechaNacimiento(new java.sql.Date(fechaNac.getTime()));
        p.setSexo(cmbSexo.getSelectedItem().toString());
        p.setTelefono(telLimpio);
        p.setEmail(emailLimpio);
        p.setDireccion(dirLimpia);

        p.setIdObraSocial(idsObrasSociales.get(idxObra));

        if(idxObra == 0) {
            p.setNumeroAfiliado(null);
        } else {
             p.setNumeroAfiliado(nroAfiliadoLimpio);
        }

        try {
            new PacienteDAO().registrarPaciente(p);
            JOptionPane.showMessageDialog(this, "Paciente registrado correctamente.");

            txtDni.setText(""); txtDni.transferFocusBackward();
            txtNombre.setText(""); txtApellido.setText("");
            dateChooserNac.setDate(null); cmbSexo.setSelectedIndex(0);
            txtTelefono.setText(""); txtEmail.setText(""); txtDireccion.setText("");
            txtNroAfiliado.setText(""); cmbObraSocial.setSelectedIndex(0);

            mainApp.volverDesdeRegistroPaciente();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar: " + e.getMessage(), "Error en BD", JOptionPane.ERROR_MESSAGE);
        }
    }
}
