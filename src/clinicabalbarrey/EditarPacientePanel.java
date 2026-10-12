package clinicabalbarrey;

import clinicabalbarrey.ConexionBD;
import clinicabalbarrey.dao.PacienteDAO;
import clinicabalbarrey.model.Paciente;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

public class EditarPacientePanel extends JPanel {
    private MainApplicationView mainApp;
    private JTextField txtDni, txtNombre, txtApellido, txtTelefono, txtEmail, txtDireccion, txtNroAfiliado;
    private JDateChooser dateChooserNac;
    private JComboBox<String> cmbObraSocial, cmbSexo;
    private int idPacienteActual;
    private ArrayList<Integer> idsObrasSociales = new ArrayList<>();

    private final Border BORDE_NORMAL = UIManager.getBorder("TextField.border");
    private final Border BORDE_ERROR = EstiloRedondeado.borde(Color.RED, 2);
    private Border bordeSuaveInptus;

    public EditarPacientePanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(new Color(242, 245, 247));

        bordeSuaveInptus = BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(200, 205, 208), 1),
            new EmptyBorder(6, 12, 6, 12)
        );

        JPanel pnlFondoWeb = new JPanel(new GridBagLayout());
        pnlFondoWeb.setBackground(new Color(242, 245, 247));
        pnlFondoWeb.setBorder(new EmptyBorder(10, 30, 25, 30));

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setPreferredSize(new Dimension(900, 560));
        tarjeta.setMinimumSize(new Dimension(820, 540));
        tarjeta.setMaximumSize(new Dimension(900, 560));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(225, 228, 230), 1),
            new EmptyBorder(26, 50, 30, 50)
        ));

        JLabel lblTitulo = new JLabel("EDITAR DATOS DEL PACIENTE");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(0, 105, 92));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel pnlEncabezado = new JPanel();
        pnlEncabezado.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlEncabezado.setOpaque(false);
        pnlEncabezado.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlEncabezado.add(lblTitulo);
        tarjeta.add(pnlEncabezado);
        tarjeta.add(Box.createVerticalStrut(24));

        txtDni = crearCampoFiltrado(8, "[0-9]");
        txtNombre = crearCampoNombres(30);
        txtApellido = crearCampoNombres(30);

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
        txtTelefono = crearCampoFiltrado(10, "[0-9]");
        txtEmail = crearCampoEmail(100);
        txtDireccion = crearCampoDireccion(100);
        txtNroAfiliado = crearCampoFiltrado(30, "[a-zA-Z0-9-]");

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

        Dimension altoFilaSimple = new Dimension(1000, 75);
        int separacionFilas = 12;

        JPanel fNombreApellido = new JPanel(new GridLayout(1, 2, 30, 0));
        fNombreApellido.setOpaque(false);
        fNombreApellido.setAlignmentX(Component.LEFT_ALIGNMENT);
        fNombreApellido.setMaximumSize(altoFilaSimple);
        fNombreApellido.add(crearBloqueInput("Nombre (*):", txtNombre));
        fNombreApellido.add(crearBloqueInput("Apellido (*):", txtApellido));
        tarjeta.add(fNombreApellido);
        tarjeta.add(Box.createVerticalStrut(separacionFilas));

        JPanel fDniSexo = new JPanel(new GridLayout(1, 2, 30, 0));
        fDniSexo.setOpaque(false);
        fDniSexo.setAlignmentX(Component.LEFT_ALIGNMENT);
        fDniSexo.setMaximumSize(altoFilaSimple);
        fDniSexo.add(crearBloqueInput("DNI (*):", txtDni));
        fDniSexo.add(crearBloqueInput("Género (*):", cmbSexo));
        tarjeta.add(fDniSexo);
        tarjeta.add(Box.createVerticalStrut(separacionFilas));

        JPanel fNacTel = new JPanel(new GridLayout(1, 2, 30, 0));
        fNacTel.setOpaque(false);
        fNacTel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fNacTel.setMaximumSize(altoFilaSimple);
        fNacTel.add(crearBloqueInput("Fecha de Nacimiento (*):", dateChooserNac));
        fNacTel.add(crearBloqueInput("Teléfono (*):", txtTelefono));
        tarjeta.add(fNacTel);
        tarjeta.add(Box.createVerticalStrut(separacionFilas));

        JPanel fContacto = new JPanel(new GridLayout(1, 2, 30, 0));
        fContacto.setOpaque(false);
        fContacto.setAlignmentX(Component.LEFT_ALIGNMENT);
        fContacto.setMaximumSize(altoFilaSimple);
        fContacto.add(crearBloqueInput("Dirección de Email (Opcional):", txtEmail));
        fContacto.add(crearBloqueInput("Dirección (*):", txtDireccion));
        tarjeta.add(fContacto);
        tarjeta.add(Box.createVerticalStrut(separacionFilas));

        JPanel fObra = new JPanel(new GridLayout(1, 2, 30, 0));
        fObra.setOpaque(false);
        fObra.setAlignmentX(Component.LEFT_ALIGNMENT);
        fObra.setMaximumSize(altoFilaSimple);
        fObra.add(crearBloqueInput("Cobertura / Obra Social (*):", cmbObraSocial));
        fObra.add(crearBloqueInput("Nro. de Afiliado:", txtNroAfiliado));
        tarjeta.add(fObra);

        tarjeta.add(Box.createVerticalGlue());

        GridBagConstraints gbcTarjeta = new GridBagConstraints();
        gbcTarjeta.gridx = 0; gbcTarjeta.gridy = 0; gbcTarjeta.weightx = 1; gbcTarjeta.weighty = 1;
        gbcTarjeta.fill = GridBagConstraints.NONE; gbcTarjeta.anchor = GridBagConstraints.CENTER;
        pnlFondoWeb.add(tarjeta, gbcTarjeta);

        JScrollPane scroll = new JScrollPane(pnlFondoWeb);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // BOTONES
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 15));
        pnlSur.setBackground(Color.WHITE);
        pnlSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 228, 230)));

        JButton btnVolver = new JButton("Cancelar");
        btnVolver.setBackground(Color.WHITE);
        btnVolver.setForeground(new Color(108, 117, 125));
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolver.setPreferredSize(new Dimension(150, 45));
        btnVolver.setBorder(EstiloRedondeado.borde(new Color(200, 205, 208), 1));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setFocusPainted(false);
        btnVolver.addActionListener(e -> mainApp.cambiarPanel("GESTION_PACIENTES"));

        JButton btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.setBackground(new Color(41, 128, 185));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnGuardar.setPreferredSize(new Dimension(200, 45));
        btnGuardar.setBorder(BorderFactory.createEmptyBorder());
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> guardar());

        pnlSur.add(btnVolver);
        pnlSur.add(btnGuardar);
        add(pnlSur, BorderLayout.SOUTH);
    }

    private JPanel crearFila() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        return p;
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

    public boolean prepararEdicion(int idPaciente) {
        this.idPacienteActual = idPaciente;
        Paciente p = new PacienteDAO().obtenerPorId(idPaciente);

        if (p != null) {
            txtDni.setBorder(bordeSuaveInptus);
            txtNombre.setBorder(bordeSuaveInptus);
            txtApellido.setBorder(bordeSuaveInptus);
            txtTelefono.setBorder(bordeSuaveInptus);
            txtDireccion.setBorder(bordeSuaveInptus);
            ((JTextFieldDateEditor)dateChooserNac.getDateEditor()).setBorder(BORDE_NORMAL);

            txtDni.setText(p.getDni());
            txtNombre.setText(p.getNombre());
            txtApellido.setText(p.getApellido());
            if (p.getFechaNacimiento() != null) { dateChooserNac.setDate(p.getFechaNacimiento()); }
            else { dateChooserNac.setDate(null); }

            if (p.getSexo() != null && !p.getSexo().equals("Otro")) { cmbSexo.setSelectedItem(p.getSexo()); }
            else { cmbSexo.setSelectedIndex(0); }

            txtTelefono.setText(p.getTelefono() != null ? p.getTelefono() : "");
            txtEmail.setText(p.getEmail() != null ? p.getEmail() : "");
            txtDireccion.setText(p.getDireccion() != null ? p.getDireccion() : "");
            txtNroAfiliado.setText(p.getNumeroAfiliado() != null && !p.getNumeroAfiliado().equals("-") ? p.getNumeroAfiliado() : "");

            if (p.getIdObraSocial() > 0 && idsObrasSociales.contains(p.getIdObraSocial())) {
                cmbObraSocial.setSelectedIndex(idsObrasSociales.indexOf(p.getIdObraSocial()));
            } else { cmbObraSocial.setSelectedIndex(0); }

            txtNroAfiliado.setEnabled(cmbObraSocial.getSelectedIndex() != 0);
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró el paciente.", "Error", JOptionPane.ERROR_MESSAGE);
            mainApp.cambiarPanel("GESTION_PACIENTES");
            return false;
        }
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
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String proposedText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                if (!text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s.,'-]*") || proposedText.startsWith(" ") || proposedText.contains("  ")) {
                    Toolkit.getDefaultToolkit().beep(); return;
                }
                if (proposedText.length() <= limite) {
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
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String proposedText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                if (proposedText.startsWith(" ") || proposedText.contains("  ") || !text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*")) {
                    Toolkit.getDefaultToolkit().beep(); return;
                }
                if (proposedText.length() <= limite) {
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

    private void guardar() {
        String dniLimpio = txtDni.getText().trim();
        String nomLimpio = txtNombre.getText().trim();
        String apeLimpio = txtApellido.getText().trim();
        String telLimpio = txtTelefono.getText().trim();
        String dirLimpia = txtDireccion.getText().trim();
        java.util.Date fechaNac = dateChooserNac.getDate();

        boolean hayError = false;

        txtDni.setBorder(bordeSuaveInptus);
        txtNombre.setBorder(bordeSuaveInptus);
        txtApellido.setBorder(bordeSuaveInptus);
        txtTelefono.setBorder(bordeSuaveInptus);
        txtEmail.setBorder(bordeSuaveInptus);
        txtDireccion.setBorder(bordeSuaveInptus);
        txtNroAfiliado.setBorder(bordeSuaveInptus);
        ((JTextFieldDateEditor)dateChooserNac.getDateEditor()).setBorder(BORDE_NORMAL);

        if (dniLimpio.isEmpty() || dniLimpio.length() < 7) { txtDni.setBorder(BORDE_ERROR); hayError = true; }
        if (nomLimpio.isEmpty()) { txtNombre.setBorder(BORDE_ERROR); hayError = true; }
        if (apeLimpio.isEmpty()) { txtApellido.setBorder(BORDE_ERROR); hayError = true; }
        if (telLimpio.isEmpty() || telLimpio.length() < 10) { txtTelefono.setBorder(BORDE_ERROR); hayError = true; }
        if (dirLimpia.isEmpty()) { txtDireccion.setBorder(BORDE_ERROR); hayError = true; }
        if (fechaNac == null) { ((JTextFieldDateEditor)dateChooserNac.getDateEditor()).setBorder(BORDE_ERROR); hayError = true; }
        String emailLimpio = txtEmail.getText().trim();
        if (!emailLimpio.isEmpty() && !emailLimpio.matches("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,}$")) {
            txtEmail.setBorder(BORDE_ERROR);
            hayError = true;
        }
        int idxObra = cmbObraSocial.getSelectedIndex();
        if (idxObra > 0 && txtNroAfiliado.getText().trim().isEmpty()) {
            txtNroAfiliado.setBorder(BORDE_ERROR);
            hayError = true;
        }

        if (hayError) { Toolkit.getDefaultToolkit().beep(); return; }

        Paciente p = new Paciente();
        p.setId_paciente(idPacienteActual);
        p.setDni(dniLimpio);
        p.setNombre(nomLimpio);
        p.setApellido(apeLimpio);
        p.setFechaNacimiento(new java.sql.Date(fechaNac.getTime()));
        p.setSexo(cmbSexo.getSelectedItem().toString());
        p.setTelefono(telLimpio);
        p.setEmail(emailLimpio);
        p.setDireccion(dirLimpia);

        p.setIdObraSocial(idsObrasSociales.get(idxObra));

        if(idxObra == 0) { p.setNumeroAfiliado(null); }
        else {
             String nro = txtNroAfiliado.getText().trim();
             p.setNumeroAfiliado(nro);
        }

        try {
            new PacienteDAO().actualizarPaciente(p, idPacienteActual);
            JOptionPane.showMessageDialog(this, "Datos actualizados correctamente.");
            mainApp.cambiarPanel("GESTION_PACIENTES");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage(), "Error en BD", JOptionPane.ERROR_MESSAGE);
        }
    }
}
