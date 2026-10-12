package clinicabalbarrey;

import clinicabalbarrey.dao.UsuarioDAO;
import clinicabalbarrey.model.Usuario;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class GestionUsuariosPanel extends JPanel {
    private MainApplicationView mainApp;
    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField txtBuscar;
    private final Timer temporizadorBusqueda;
    private int cargaToken;
    private boolean limpiandoBusqueda;

    public GestionUsuariosPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout(0, 25));
        setBackground(new Color(242, 244, 246));
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        JLabel lblTitulo = new JLabel("GESTIÓN DE USUARIOS DEL SISTEMA");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitulo.setForeground(new Color(0, 77, 64));
        pnlTop.add(lblTitulo, BorderLayout.WEST);

        JPanel pnlBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBusqueda.setOpaque(false);
        pnlBusqueda.add(new JLabel("Buscar Usuario:"));

        txtBuscar = new JTextField(15);
        txtBuscar.setPreferredSize(new Dimension(230, 35));
        EstiloRedondeado.aplicarBuscadorTexto(txtBuscar);

        temporizadorBusqueda = new Timer(300, e -> cargarUsuarios(txtBuscar.getText().trim()));
        temporizadorBusqueda.setRepeats(false);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { programarBusqueda(); }
            public void removeUpdate(DocumentEvent e) { programarBusqueda(); }
            public void changedUpdate(DocumentEvent e) { programarBusqueda(); }
        });

        pnlBusqueda.add(txtBuscar);
        pnlTop.add(pnlBusqueda, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"ID", "Nombre de Usuario", "Rol / Cargo", "Nombre Real"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabla = new JTable(modelo);

        // --- ESTILO Y BLOQUEO DE LA TABLA (DISEÑO REY SALUD) ---
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setResizingAllowed(false);
        tabla.getTableHeader().setBackground(new Color(0, 121, 107));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(EstiloRedondeado.fuenteHeaderTabla());
        ((DefaultTableCellRenderer) tabla.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        tabla.setRowHeight(40);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.setSelectionBackground(new Color(178, 223, 219));
        tabla.setSelectionForeground(Color.BLACK);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setCellSelectionEnabled(false);
        tabla.setRowSelectionAllowed(true);
        tabla.setColumnSelectionAllowed(false);
        tabla.setShowGrid(true);
        tabla.setGridColor(new Color(200, 200, 200));

        // Centrado de celdas específicas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tabla.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID
        tabla.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Rol / Cargo

        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(200);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(EstiloRedondeado.borde(new Color(200, 200, 200), 1));
        add(scroll, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        pnlBotones.setOpaque(false);

        JButton btnNuevo = crearBoton("Nuevo Usuario", new Color(0, 150, 136));
        btnNuevo.addActionListener(e -> mainApp.irARegistrarUsuario());

        JButton btnEditar = crearBoton("Editar Datos", new Color(255, 160, 0));
        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int idUser = (int) modelo.getValueAt(fila, 0);
                mainApp.irAEditarUsuario(idUser);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnEliminar = crearBoton("Dar de Baja", new Color(211, 47, 47));
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int idUser = (int) modelo.getValueAt(fila, 0);
                String nombreUser = modelo.getValueAt(fila, 1).toString();
                Usuario sesion = mainApp.getUsuarioSesion();
                if (sesion != null && idUser == sesion.getId_usuario()) {
                    JOptionPane.showMessageDialog(this, "No podés dar de baja tu propia cuenta mientras está iniciada la sesión.", "Operación bloqueada", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (JOptionPane.showConfirmDialog(this, "¿Dar de baja al usuario '" + nombreUser + "'?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    int idSesion = sesion == null ? -1 : sesion.getId_usuario();
                    if (new UsuarioDAO().darDeBajaUsuario(idUser, idSesion)) {
                        JOptionPane.showMessageDialog(this, "Usuario dado de baja exitosamente.");
                        cargarUsuarios();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al dar de baja.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un usuario", "Atención", JOptionPane.WARNING_MESSAGE);
            }
        });

        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnEditar);
        pnlBotones.add(btnEliminar);
        add(pnlBotones, BorderLayout.SOUTH);
    }

    private JButton crearBoton(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        return btn;
    }

    private void programarBusqueda() {
        if (!limpiandoBusqueda) temporizadorBusqueda.restart();
    }

    private void limpiarBusqueda() {
        if (txtBuscar == null) return;
        temporizadorBusqueda.stop();
        limpiandoBusqueda = true;
        try {
            txtBuscar.setText("");
        } finally {
            limpiandoBusqueda = false;
        }
        tabla.clearSelection();
    }

    public void cargarUsuarios() {
        limpiarBusqueda();
        cargarUsuarios("");
    }

    public void cargarUsuarios(String filtroNombre) {
        final int token = ++cargaToken;
        final String filtro = filtroNombre == null ? "" : filtroNombre.toLowerCase();
        new SwingWorker<List<Usuario>, Void>() {
            @Override protected List<Usuario> doInBackground() { return new UsuarioDAO().listarTodos(); }
            @Override protected void done() {
                if (token != cargaToken) return;
                try {
                    modelo.setRowCount(0);
                    for (Usuario u : get()) {
                        String username = u.getUsername() == null ? "" : u.getUsername();
                        if (!filtro.isEmpty() && !username.toLowerCase().contains(filtro)) continue;
                        String nombreReal = (u.getNombre() == null || u.getNombre().isEmpty()) ? "Usuario del Sistema" : u.getNombre() + " " + u.getApellido();
                        modelo.addRow(new Object[]{u.getId_usuario(), username, u.getRol(), nombreReal.trim()});
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(GestionUsuariosPanel.this, "No se pudieron cargar los usuarios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
