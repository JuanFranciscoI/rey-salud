package clinicabalbarrey;

import clinicabalbarrey.dao.UsuarioDAO;
import clinicabalbarrey.model.Usuario;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginPanel extends JPanel {
    private MainApplicationView mainApp;
    private JTextField txtUser;
    private JPasswordField txtPass;

    public LoginPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new GridBagLayout());
        setBackground(new Color(0, 77, 64));

        JPanel card = new TarjetaLoginPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(500, 550));
        card.setBorder(new EmptyBorder(60, 50, 60, 50));

        JLabel lblT = new JLabel("SISTEMA CLÍNICO");
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblT.setForeground(new Color(0, 121, 107));
        lblT.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblT);
        card.add(Box.createVerticalStrut(50));

        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblUser.setForeground(Color.DARK_GRAY);
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblUser);
        card.add(Box.createVerticalStrut(10));

        txtUser = new JTextField();
        txtUser.setMaximumSize(new Dimension(350, 45));
        txtUser.setHorizontalAlignment(JTextField.CENTER);
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        // --- ARREGLO 1: ENTER EN EL USUARIO ---
        txtUser.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    iniciarSesion();
                }
            }
        });

        card.add(txtUser);
        card.add(Box.createVerticalStrut(30));

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPass.setForeground(Color.DARK_GRAY);
        lblPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblPass);
        card.add(Box.createVerticalStrut(10));

        txtPass = new JPasswordField();
        txtPass.setMaximumSize(new Dimension(350, 45));
        txtPass.setHorizontalAlignment(JTextField.CENTER);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        // --- ARREGLO 2: ENTER EN LA CONTRASEÑA ---
        txtPass.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    iniciarSesion();
                }
            }
        });

        card.add(txtPass);
        card.add(Box.createVerticalStrut(50));

        JButton btnLogin = new JButton("INICIAR SESIÓN");
        btnLogin.setBackground(new Color(0, 150, 136));
        btnLogin.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton boton = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(0, 121, 107);
                Color fondo = boton.getModel().isPressed()
                        ? base.darker()
                        : boton.getModel().isRollover() ? new Color(0, 137, 123) : base;
                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 14, 14);
                g2.dispose();
                super.paint(g, c);
            }
        });
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnLogin.setMaximumSize(new Dimension(350, 50));
        btnLogin.setPreferredSize(new Dimension(350, 50));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setOpaque(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> iniciarSesion());
        card.add(btnLogin);

        add(card);
    }

    private static class TarjetaLoginPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D.Float tarjeta = new RoundRectangle2D.Float(
                    1,
                    1,
                    getWidth() - 3,
                    getHeight() - 3,
                    16,
                    16
            );
            g2.setColor(Color.WHITE);
            g2.fill(tarjeta);
            g2.setColor(new Color(150, 150, 150));
            g2.setStroke(new BasicStroke(1.4f));
            g2.draw(tarjeta);
            g2.dispose();

            super.paintComponent(g);
        }
    }

    private void iniciarSesion() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese su usuario y contraseña.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario u = new UsuarioDAO().validarLogin(user, pass);
        if (u != null) {
            mainApp.setUsuarioSesion(u);
            mainApp.aplicarPermisosSidebar();

            // REDIRECCIÓN INTELIGENTE SEGÚN EL ROL
            if (u.esSecretaria()) {
                mainApp.cambiarPanel("AGENDA_TURNOS"); // Va directo a los turnos
            } else {
                mainApp.cambiarPanel("DASHBOARD"); // Médicos y Admin van al Dashboard
            }

            txtUser.setText("");
            txtPass.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
        }
    }
}
