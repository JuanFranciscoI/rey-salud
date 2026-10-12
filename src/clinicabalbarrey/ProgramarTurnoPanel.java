package clinicabalbarrey;

import clinicabalbarrey.ConexionBD;
import clinicabalbarrey.dao.TurnoDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ProgramarTurnoPanel extends JPanel {
    private MainApplicationView mainApp;

    private int idMedicoPrecargado;
    private String fechaSQLPrecargada;
    private String horaPrecargada;

    private JLabel lblMedico;
    private JLabel lblFechaHora;
    private JLabel lblAyudaPaciente;
    private JTextField txtBuscarPaciente;
    private JButton btnConfirmar;

    private DefaultListModel<String> modeloLista;
    private JList<String> listaPacientes;
    private JScrollPane scrollLista;
    private JPanel pnlBuscadorLista;

    private ArrayList<Integer> idsPacientes = new ArrayList<>();
    private int idPacienteSeleccionado = -1;
    private boolean omitirBusqueda = false;
    private Timer temporizadorBusqueda;
    private int busquedaToken;

    public ProgramarTurnoPanel(MainApplicationView mainApp) {
        this.mainApp = mainApp;
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 242, 245));

        JPanel card = new TarjetaTurno();
        card.setLayout(new BorderLayout(0, 20));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(200, 200, 200), 1),
            new EmptyBorder(32, 42, 28, 42)
        ));
        card.setPreferredSize(new Dimension(610, 680));

        JPanel pnlNorte = new JPanel();
        pnlNorte.setLayout(new BoxLayout(pnlNorte, BoxLayout.Y_AXIS));
        pnlNorte.setOpaque(false);

        IconoCircularTurno iconoCalendario = new IconoCircularTurno();
        iconoCalendario.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlNorte.add(iconoCalendario);
        pnlNorte.add(Box.createVerticalStrut(12));

        JLabel lblTitulo = new JLabel("NUEVO TURNO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(0, 121, 107));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlNorte.add(lblTitulo);
        pnlNorte.add(Box.createVerticalStrut(20));

        JPanel pnlInfo = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlInfo.setOpaque(false);
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(
            EstiloRedondeado.borde(new Color(178, 223, 219), 1),
            new EmptyBorder(16, 24, 16, 24)
        ));

        lblMedico = new JLabel("Profesional: Cargando...");
        lblMedico.setIcon(new IconoMiniTurno("persona"));
        lblMedico.setIconTextGap(14);
        lblMedico.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblMedico.setHorizontalAlignment(SwingConstants.LEFT);

        lblFechaHora = new JLabel("Fecha: Cargando...");
        lblFechaHora.setIcon(new IconoMiniTurno("calendario"));
        lblFechaHora.setIconTextGap(14);
        lblFechaHora.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFechaHora.setForeground(new Color(0, 105, 92));
        lblFechaHora.setHorizontalAlignment(SwingConstants.LEFT);

        pnlInfo.add(lblMedico);
        pnlInfo.add(lblFechaHora);
        pnlNorte.add(pnlInfo);

        card.add(pnlNorte, BorderLayout.NORTH);

        JPanel pnlCentro = new JPanel(new BorderLayout(0, 10));
        pnlCentro.setOpaque(false);

        JPanel pnlCabeceraBusqueda = new JPanel();
        pnlCabeceraBusqueda.setLayout(new BoxLayout(pnlCabeceraBusqueda, BoxLayout.Y_AXIS));
        pnlCabeceraBusqueda.setOpaque(false);

        JLabel lblPasoPaciente = new JLabel("SELECCIONAR PACIENTE");
        lblPasoPaciente.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPasoPaciente.setForeground(new Color(0, 105, 92));
        lblPasoPaciente.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlCabeceraBusqueda.add(lblPasoPaciente);
        pnlCabeceraBusqueda.add(Box.createVerticalStrut(5));

        JLabel lblBuscador = new JLabel("Buscar por DNI, nombre o apellido");
        lblBuscador.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBuscador.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlCabeceraBusqueda.add(lblBuscador);
        pnlCabeceraBusqueda.add(Box.createVerticalStrut(3));

        lblAyudaPaciente = new JLabel("Escribí para ver pacientes y hacé clic sobre uno para seleccionarlo.");
        lblAyudaPaciente.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAyudaPaciente.setForeground(new Color(95, 105, 110));
        lblAyudaPaciente.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlCabeceraBusqueda.add(lblAyudaPaciente);
        pnlCentro.add(pnlCabeceraBusqueda, BorderLayout.NORTH);

        pnlBuscadorLista = new JPanel(new BorderLayout(0, 0));
        pnlBuscadorLista.setOpaque(false);
        pnlBuscadorLista.setPreferredSize(new Dimension(0, 265));
        pnlBuscadorLista.setMinimumSize(new Dimension(0, 265));

        txtBuscarPaciente = new JTextField();
        txtBuscarPaciente.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtBuscarPaciente.setPreferredSize(new Dimension(0, 45));
        txtBuscarPaciente.setHorizontalAlignment(JTextField.LEFT);
        EstiloRedondeado.aplicarBuscadorTexto(txtBuscarPaciente);
        pnlBuscadorLista.add(txtBuscarPaciente, BorderLayout.NORTH);

        modeloLista = new DefaultListModel<>();
        modeloLista.addElement("Escribí para buscar pacientes");
        listaPacientes = new JList<>(modeloLista);
        listaPacientes.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        listaPacientes.setFixedCellHeight(40);
        listaPacientes.setSelectionBackground(new Color(178, 223, 219));
        listaPacientes.setSelectionForeground(Color.BLACK);
        listaPacientes.setCursor(new Cursor(Cursor.HAND_CURSOR));
        listaPacientes.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, false);
                label.setBorder(new EmptyBorder(0, 14, 0, 14));
                label.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 14));
                if (idsPacientes.isEmpty()) {
                    label.setForeground(new Color(115, 125, 130));
                    label.setBackground(Color.WHITE);
                }
                return label;
            }
        });

        scrollLista = new JScrollPane(listaPacientes);
        scrollLista.setBorder(EstiloRedondeado.borde(new Color(150, 150, 150)));
        scrollLista.setPreferredSize(new Dimension(0, 225));
        scrollLista.setMinimumSize(new Dimension(0, 225));
        scrollLista.setVisible(true);

        pnlBuscadorLista.add(scrollLista, BorderLayout.CENTER);

        pnlCentro.add(pnlBuscadorLista, BorderLayout.CENTER);
        card.add(pnlCentro, BorderLayout.CENTER);

        temporizadorBusqueda = new Timer(250, e -> buscarEnVivo());
        temporizadorBusqueda.setRepeats(false);
        txtBuscarPaciente.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { programarBusqueda(); }
            public void removeUpdate(DocumentEvent e) { programarBusqueda(); }
            public void changedUpdate(DocumentEvent e) { programarBusqueda(); }
        });

        listaPacientes.addMouseListener(new MouseAdapter() {
            private void mostrarMenuSiCorresponde(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int indice = listaPacientes.locationToIndex(e.getPoint());
                if (indice >= 0 && listaPacientes.getCellBounds(indice, indice).contains(e.getPoint())) {
                    listaPacientes.setSelectedIndex(indice);
                } else {
                    listaPacientes.clearSelection();
                }
                crearMenuPaciente().show(listaPacientes, e.getX(), e.getY());
            }

            @Override public void mousePressed(MouseEvent e) { mostrarMenuSiCorresponde(e); }
            @Override public void mouseReleased(MouseEvent e) { mostrarMenuSiCorresponde(e); }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) seleccionarPacienteActual();
            }
        });

        txtBuscarPaciente.setComponentPopupMenu(crearMenuPaciente());

        txtBuscarPaciente.addActionListener(e -> {
            if (!idsPacientes.isEmpty()) {
                listaPacientes.setSelectedIndex(0);
                seleccionarPacienteActual();
            }
        });

        JPanel pnlSur = new JPanel(new GridLayout(2, 1, 0, 10));
        pnlSur.setOpaque(false);
        pnlSur.setBorder(new EmptyBorder(15, 0, 0, 0));

        btnConfirmar = new JButton("CONFIRMAR TURNO");
        btnConfirmar.setBackground(new Color(0, 150, 136));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmar.setPreferredSize(new Dimension(0, 45));
        btnConfirmar.setEnabled(false);
        btnConfirmar.addActionListener(e -> guardarTurno());

        JButton btnCancelar = new JButton("CANCELAR Y VOLVER");
        btnCancelar.setBackground(new Color(211, 47, 47));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.setPreferredSize(new Dimension(0, 40));
        btnCancelar.addActionListener(e -> mainApp.cambiarPanel("CALENDARIO_TURNOS"));

        pnlSur.add(btnConfirmar);
        pnlSur.add(btnCancelar);

        card.add(pnlSur, BorderLayout.SOUTH);
        add(card);
    }

    private void programarBusqueda() {
        if (!omitirBusqueda) temporizadorBusqueda.restart();
    }

    private JPopupMenu crearMenuPaciente() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem crearPaciente = new JMenuItem("Crear paciente");
        crearPaciente.setFont(new Font("Segoe UI", Font.BOLD, 14));
        crearPaciente.addActionListener(e ->
                mainApp.irARegistrarPacienteDesdeTurno(txtBuscarPaciente.getText().trim()));
        menu.add(crearPaciente);
        return menu;
    }

    public void reanudarTrasAltaPaciente() {
        txtBuscarPaciente.setEnabled(true);
        idPacienteSeleccionado = -1;
        btnConfirmar.setEnabled(false);
        buscarEnVivo();
        SwingUtilities.invokeLater(() -> txtBuscarPaciente.requestFocusInWindow());
    }

    private void seleccionarPacienteActual() {
        int index = listaPacientes.getSelectedIndex();
        if (index < 0 || index >= idsPacientes.size()) return;
        idPacienteSeleccionado = idsPacientes.get(index);
        temporizadorBusqueda.stop();
        busquedaToken++;
        omitirBusqueda = true;
        txtBuscarPaciente.setText(modeloLista.get(index));
        omitirBusqueda = false;
        lblAyudaPaciente.setText("Paciente seleccionado. Ya podés confirmar el turno.");
        lblAyudaPaciente.setForeground(new Color(0, 121, 107));
        btnConfirmar.setEnabled(true);
        scrollLista.setVisible(true);
        pnlBuscadorLista.revalidate();
        pnlBuscadorLista.repaint();
    }

    public void preCargarDatos(java.util.Date fecha, String hora, int idMedico, String nombreMedico) {
        this.idMedicoPrecargado = idMedico;
        this.horaPrecargada = hora;
        SimpleDateFormat sdfSQL = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfVisual = new SimpleDateFormat("dd/MM/yyyy");
        this.fechaSQLPrecargada = sdfSQL.format(fecha);
        lblMedico.setText("Profesional: " + nombreMedico);
        lblFechaHora.setText(sdfVisual.format(fecha) + " a las " + hora + " hs.");
        txtBuscarPaciente.setEnabled(true);
        SwingUtilities.invokeLater(() -> {
            omitirBusqueda = true;
            txtBuscarPaciente.setText("");
            omitirBusqueda = false;
            modeloLista.clear();
            modeloLista.addElement("Escribí para buscar pacientes");
            idsPacientes.clear();
            idPacienteSeleccionado = -1;
            lblAyudaPaciente.setText("Escribí para ver pacientes y hacé clic sobre uno para seleccionarlo.");
            lblAyudaPaciente.setForeground(new Color(95, 105, 110));
            btnConfirmar.setEnabled(false);
            scrollLista.setVisible(true);
            pnlBuscadorLista.revalidate();
            pnlBuscadorLista.repaint();
        });
    }

    private void buscarEnVivo() {
        if (omitirBusqueda) return;
        String filtro = txtBuscarPaciente.getText().trim();
        if (filtro.isEmpty()) {
            busquedaToken++;
            idPacienteSeleccionado = -1;
            idsPacientes.clear();
            modeloLista.clear();
            modeloLista.addElement("Escribí para buscar pacientes");
            lblAyudaPaciente.setText("Escribí para ver pacientes y hacé clic sobre uno para seleccionarlo.");
            lblAyudaPaciente.setForeground(new Color(95, 105, 110));
            btnConfirmar.setEnabled(false);
            scrollLista.setVisible(true);
            pnlBuscadorLista.revalidate();
            pnlBuscadorLista.repaint();
            return;
        }
        final int token = ++busquedaToken;
        idPacienteSeleccionado = -1;
        btnConfirmar.setEnabled(false);
        idsPacientes.clear();
        modeloLista.clear();
        modeloLista.addElement("Buscando pacientes...");
        lblAyudaPaciente.setText("Buscando coincidencias...");
        lblAyudaPaciente.setForeground(new Color(95, 105, 110));
        scrollLista.setVisible(true);
        new SwingWorker<ArrayList<ResultadoPaciente>, Void>() {
            @Override protected ArrayList<ResultadoPaciente> doInBackground() throws Exception {
                ArrayList<ResultadoPaciente> resultados = new ArrayList<>();
                java.sql.Timestamp fechaHoraBuscada = obtenerFechaHoraPrecargada();
                String sql = "SELECT p.id_paciente,p.dni,p.apellido,p.nombre FROM paciente p "
                        + "WHERE p.estado::text='activo' AND (p.dni ILIKE ? OR p.nombre ILIKE ? OR p.apellido ILIKE ? "
                        + "OR (p.nombre || ' ' || p.apellido) ILIKE ? OR (p.apellido || ' ' || p.nombre) ILIKE ?) "
                        + (fechaHoraBuscada == null ? "" :
                            "AND NOT EXISTS (SELECT 1 FROM turno t WHERE t.id_paciente=p.id_paciente "
                          + "AND t.fechahora=? AND LOWER(COALESCE(t.estado,'')) NOT IN ('cancelado','finalizado','ausente')) ")
                        + "ORDER BY p.apellido,p.nombre LIMIT 15";
                try (Connection con = ConexionBD.getConexion(); PreparedStatement pst = con.prepareStatement(sql)) {
                    String patron = "%" + filtro + "%";
                    for (int i = 1; i <= 5; i++) pst.setString(i, patron);
                    if (fechaHoraBuscada != null) pst.setTimestamp(6, fechaHoraBuscada);
                    try (ResultSet rs = pst.executeQuery()) {
                        while (rs.next()) resultados.add(new ResultadoPaciente(rs.getInt("id_paciente"), rs.getString("dni") + " - " + rs.getString("apellido") + ", " + rs.getString("nombre")));
                    }
                }
                return resultados;
            }
            @Override protected void done() {
                if (token != busquedaToken) return;
                try {
                    modeloLista.clear(); idsPacientes.clear(); idPacienteSeleccionado = -1;
                    for (ResultadoPaciente resultado : get()) { idsPacientes.add(resultado.id()); modeloLista.addElement(resultado.texto()); }
                    if (modeloLista.isEmpty()) {
                        modeloLista.addElement("No se encontraron pacientes");
                        lblAyudaPaciente.setText("No hay coincidencias. Podés crear el paciente con clic derecho.");
                    } else {
                        lblAyudaPaciente.setText("Elegí un paciente de la lista para continuar.");
                    }
                    scrollLista.setVisible(true); pnlBuscadorLista.revalidate(); pnlBuscadorLista.repaint();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ProgramarTurnoPanel.this, "No se pudieron buscar pacientes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private record ResultadoPaciente(int id, String texto) {}

    private java.sql.Timestamp obtenerFechaHoraPrecargada() {
        if (fechaSQLPrecargada == null || fechaSQLPrecargada.isBlank()
                || horaPrecargada == null || horaPrecargada.isBlank()) {
            return null;
        }
        try {
            return java.sql.Timestamp.valueOf(fechaSQLPrecargada + " " + horaPrecargada + ":00");
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void guardarTurno() {
        if (idPacienteSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Debe buscar y seleccionar un paciente haciendo clic en la lista.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fechaHoraUnida = fechaSQLPrecargada + " " + horaPrecargada + ":00";

        TurnoDAO dao = new TurnoDAO();
        String mensajeConflicto;
        try {
            mensajeConflicto = dao.reservarTurno(idPacienteSeleccionado, idMedicoPrecargado,
                    java.sql.Timestamp.valueOf(fechaHoraUnida), "");
        } catch (IllegalArgumentException e) {
            mensajeConflicto = "La fecha u hora seleccionada no es válida.";
        }
        if (mensajeConflicto != null) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, mensajeConflicto, "Conflicto de Agenda", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "¡Turno asignado exitosamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        mainApp.cambiarPanel("CALENDARIO_TURNOS");
    }

    public void cargarDatosParaTurno() {
        lblMedico.setText("No hay datos cargados.");
        lblFechaHora.setText("Vuelva al calendario.");
        omitirBusqueda = true;
        try {
            txtBuscarPaciente.setText("");
        } finally {
            omitirBusqueda = false;
        }
        modeloLista.clear();
        idsPacientes.clear();
        idPacienteSeleccionado = -1;
        lblAyudaPaciente.setText("Volvé al calendario y seleccioná un horario disponible.");
        lblAyudaPaciente.setForeground(new Color(95, 105, 110));
        btnConfirmar.setEnabled(false);
        txtBuscarPaciente.setEnabled(false);
    }

    private static class TarjetaTurno extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 121, 107));
            g2.fillRoundRect(0, 0, getWidth(), 10, 16, 16);
            g2.fillRect(0, 6, getWidth(), 6);
            g2.dispose();
        }
    }

    private static class IconoCircularTurno extends JComponent {
        IconoCircularTurno() {
            setPreferredSize(new Dimension(62, 62));
            setMaximumSize(new Dimension(62, 62));
            setMinimumSize(new Dimension(62, 62));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(224, 242, 241));
            g2.fillOval(4, 4, 54, 54);
            new IconoMiniTurno("calendario", 30, new Color(0, 137, 123)).paintIcon(this, g2, 16, 16);
            g2.dispose();
        }
    }

    private static class IconoMiniTurno implements Icon {
        private final String tipo;
        private final int tamano;
        private final Color color;

        IconoMiniTurno(String tipo) {
            this(tipo, 20, new Color(0, 150, 136));
        }

        IconoMiniTurno(String tipo, int tamano, Color color) {
            this.tipo = tipo;
            this.tamano = tamano;
            this.color = color;
        }

        @Override public int getIconWidth() { return tamano; }
        @Override public int getIconHeight() { return tamano; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(2f, tamano / 12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if ("persona".equals(tipo)) {
                int cabeza = Math.max(6, tamano / 3);
                g2.fillOval(x + tamano / 2 - cabeza / 2, y + 2, cabeza, cabeza);
                g2.fillRoundRect(x + tamano / 4, y + tamano / 2, tamano / 2, tamano / 3, 8, 8);
            } else {
                int margen = tamano / 6;
                g2.drawRoundRect(x + margen, y + margen + 2, tamano - margen * 2, tamano - margen * 2, 4, 4);
                g2.drawLine(x + margen, y + tamano / 3, x + tamano - margen, y + tamano / 3);
                g2.drawLine(x + tamano / 3, y + margen - 1, x + tamano / 3, y + margen + 5);
                g2.drawLine(x + (tamano * 2) / 3, y + margen - 1, x + (tamano * 2) / 3, y + margen + 5);
            }
            g2.dispose();
        }
    }
}
