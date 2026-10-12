package clinicabalbarrey;

import clinicabalbarrey.dao.UsuarioDAO;
import clinicabalbarrey.dao.RolDAO;
import clinicabalbarrey.model.Usuario;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

public class EditarUsuarioPanel extends JPanel {
    private MainApplicationView mainApp;
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JComboBox<String> cmbRol;
    private int idUsuarioActual;

    public EditarUsuarioPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 242, 245));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(500, 500));
        card.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(200, 200, 200), 1),
            new EmptyBorder(40, 50, 40, 50)
        ));

        JLabel lblT = new JLabel("EDITAR USUARIO");
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblT.setForeground(new Color(0, 121, 107));
        lblT.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblT);
        card.add(Box.createVerticalStrut(35));

        txtUser = crearCampoFiltrado(20, "[a-zA-Z0-9._-]");

        txtPass = new JPasswordField();
        ((AbstractDocument) txtPass.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (fb.getDocument().getLength() + text.length() - length <= 20) {
                    super.replace(fb, offset, length, text, attrs);
                } else { Toolkit.getDefaultToolkit().beep(); }
            }
        });

        cmbRol = new JComboBox<>(new String[]{"Administrador", "Médico", "Secretaria"});
        ((JLabel)cmbRol.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        agregarBloque(card, "Nombre de Usuario:", txtUser);
        agregarBloque(card, "Contraseña (Dejar en blanco para no cambiarla):", txtPass);
        agregarBloque(card, "Rol / Permisos:", cmbRol);

        card.add(Box.createVerticalStrut(25));

        JButton btnGuardar = new JButton("GUARDAR CAMBIOS");
        btnGuardar.setBackground(new Color(0, 150, 136));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGuardar.setMaximumSize(new Dimension(400, 45));
        btnGuardar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardar());
        card.add(btnGuardar);

        card.add(Box.createVerticalStrut(15));

        JButton btnVolver = new JButton("CANCELAR");
        btnVolver.setBackground(new Color(211, 47, 47));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnVolver.setMaximumSize(new Dimension(400, 40));
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> mainApp.cambiarPanel("GESTION_USUARIOS"));
        card.add(btnVolver);

        add(card);
    }

    // Método que recibe el ID del usuario seleccionado y carga sus datos en las cajas
    public void prepararEdicion(int idUsuario) {
        this.idUsuarioActual = idUsuario;
        Usuario u = new UsuarioDAO().obtenerUsuarioPorId(idUsuario);
        if (u != null) {
            txtUser.setText(u.getUsername());
            String rol = u.getRol();
            cmbRol.setSelectedItem(rol != null && (rol.equalsIgnoreCase("Médico") || rol.equalsIgnoreCase("Medico")) ? "Médico" : rol);
            txtPass.setText(""); // Se deja vacía por seguridad
        } else {
            JOptionPane.showMessageDialog(this, "No se encontraron los datos del usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            mainApp.cambiarPanel("GESTION_USUARIOS");
        }
    }

    private JTextField crearCampoFiltrado(int limite, String regex) {
        JTextField tf = new JTextField();
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches(regex + "*") && fb.getDocument().getLength() + text.length() - length <= limite) {
                    super.replace(fb, offset, length, text, attrs);
                } else { Toolkit.getDefaultToolkit().beep(); }
            }
        });
        return tf;
    }

    private void agregarBloque(JPanel p, String titulo, JComponent comp) {
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.DARK_GRAY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lbl); p.add(Box.createVerticalStrut(5));
        comp.setMaximumSize(new Dimension(400, 40));
        comp.setAlignmentX(Component.CENTER_ALIGNMENT);
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (comp instanceof JTextField) ((JTextField) comp).setHorizontalAlignment(JTextField.CENTER);
        p.add(comp); p.add(Box.createVerticalStrut(15));
    }

    private void guardar() {
        if (txtUser.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de usuario no puede estar vacío.", "Atención", JOptionPane.WARNING_MESSAGE); return;
        }

        Usuario u = new Usuario();
        u.setId_usuario(idUsuarioActual);
        u.setUsername(txtUser.getText().trim());
        String pass = new String(txtPass.getPassword()); // Si está vacía, manda ""
        try {
            u.setId_rol(RolDAO.obtenerId(cmbRol.getSelectedItem().toString()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo resolver el rol seleccionado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (new UsuarioDAO().actualizarUsuario(u, pass)) {
            JOptionPane.showMessageDialog(this, "Datos del usuario actualizados exitosamente.");
            mainApp.cambiarPanel("GESTION_USUARIOS");
        } else {
            JOptionPane.showMessageDialog(this, "Ocurrió un error al actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
