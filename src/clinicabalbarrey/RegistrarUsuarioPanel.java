package clinicabalbarrey;

import clinicabalbarrey.dao.RolDAO;
import clinicabalbarrey.dao.UsuarioDAO;
import clinicabalbarrey.model.Usuario;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

public class RegistrarUsuarioPanel extends JPanel {
    private final MainApplicationView mainApp;
    private final JTextField txtUser;
    private final JPasswordField txtPass;
    private final JComboBox<String> cmbRol;
    private final JComboBox<String> cmbPersonal;
    private final JButton btnGuardar;
    private final UsuarioDAO dao = new UsuarioDAO();
    private List<Usuario> personalDisponible = new ArrayList<>();

    public RegistrarUsuarioPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 242, 245));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(560, 590));
        card.setBorder(BorderFactory.createCompoundBorder(EstiloRedondeado.borde(new Color(200, 200, 200)), new EmptyBorder(35, 50, 35, 50)));

        JLabel titulo = new JLabel("NUEVO USUARIO DEL SISTEMA");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(new Color(0, 121, 107));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titulo);
        card.add(Box.createVerticalStrut(25));

        cmbPersonal = new JComboBox<>();
        txtUser = crearCampoFiltrado(40, "[a-zA-Z0-9._-]");
        txtPass = new JPasswordField();
        ((AbstractDocument) txtPass.getDocument()).setDocumentFilter(new LimiteFilter(64));
        cmbRol = new JComboBox<>(new String[]{"Administrador", "Médico", "Secretaria"});

        agregarBloque(card, "Personal asociado:", cmbPersonal);
        agregarBloque(card, "Nombre de usuario:", txtUser);
        agregarBloque(card, "Contraseña (mínimo 8 caracteres):", txtPass);
        agregarBloque(card, "Rol / permisos:", cmbRol);
        card.add(Box.createVerticalStrut(20));

        btnGuardar = new JButton("GUARDAR USUARIO");
        btnGuardar.setBackground(new Color(0, 150, 136));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGuardar.setMaximumSize(new Dimension(420, 45));
        btnGuardar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGuardar.addActionListener(e -> guardar());
        card.add(btnGuardar);
        card.add(Box.createVerticalStrut(15));

        JButton volver = new JButton("CANCELAR Y VOLVER");
        volver.setBackground(new Color(211, 47, 47));
        volver.setForeground(Color.WHITE);
        volver.setFont(new Font("Segoe UI", Font.BOLD, 14));
        volver.setMaximumSize(new Dimension(420, 40));
        volver.setAlignmentX(Component.CENTER_ALIGNMENT);
        volver.addActionListener(e -> mainApp.cambiarPanel("GESTION_USUARIOS"));
        card.add(volver);
        add(card);
    }

    public void cargarPersonalDisponible() {
        personalDisponible = dao.listarPersonalSinUsuario();
        cmbPersonal.removeAllItems();
        for (Usuario u : personalDisponible) {
            cmbPersonal.addItem((u.getNombre() + " " + u.getApellido()).trim() + " — DNI " + u.getDni());
        }
        btnGuardar.setEnabled(!personalDisponible.isEmpty());
        if (personalDisponible.isEmpty()) cmbPersonal.addItem("No hay personal sin cuenta de acceso");
    }

    private void guardar() {
        String usuario = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());
        if (personalDisponible.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero registrá una persona sin cuenta de acceso.", "Sin personal disponible", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (usuario.isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresá un usuario y una contraseña.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Usuario personal = personalDisponible.get(cmbPersonal.getSelectedIndex());
            int idRol = RolDAO.obtenerId(cmbRol.getSelectedItem().toString());
            if (!dao.crearCuentaAcceso(personal.getId_personal(), usuario, password, idRol)) {
                throw new Exception("No se pudo crear la cuenta. Verificá que el usuario no esté repetido.");
            }
            JOptionPane.showMessageDialog(this, "Usuario creado exitosamente.");
            txtUser.setText("");
            txtPass.setText("");
            mainApp.cambiarPanel("GESTION_USUARIOS");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField crearCampoFiltrado(int limite, String regex) {
        JTextField campo = new JTextField();
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches(regex + "*") && fb.getDocument().getLength() + text.length() - length <= limite) super.replace(fb, offset, length, text, attrs);
            }
        });
        return campo;
    }

    private void agregarBloque(JPanel panel, String texto, JComponent componente) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        componente.setMaximumSize(new Dimension(420, 40));
        componente.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(componente);
        panel.add(Box.createVerticalStrut(15));
    }

    private static class LimiteFilter extends DocumentFilter {
        private final int limite;
        LimiteFilter(int limite) { this.limite = limite; }
        @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (fb.getDocument().getLength() + text.length() - length <= limite) super.replace(fb, offset, length, text, attrs);
        }
    }
}
