package clinicabalbarrey;

import clinicabalbarrey.dao.ConsultaDAO;
import clinicabalbarrey.model.Usuario;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

public class MainApplicationView extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private SidebarPanel sidebar;
    private Usuario usuarioSesion;

    private DashboardPanel dashboardPanel;
    private EstadisticasPanel estadisticasPanel;
    private GestionPacientesPanel gestionPacientesPanel;
    private GestionPersonalPanel gestionPersonalPanel;
    private GestionUsuariosPanel gestionUsuariosPanel;
    private RegistrarPacientePanel registrarPacientePanel;
    private EditarPacientePanel editarPacientePanel;
    private NuevaConsultaPanel nuevaConsultaPanel;
    private VerHistoriaMedicaPanel verHistoriaMedicaPanel;
    private RegistrarPersonalPanel registrarPersonalPanel;
    private EditarPersonalPanel editarPersonalPanel;
    private RegistrarUsuarioPanel registrarUsuarioPanel;
    private AgendaTurnosPanel agendaTurnosPanel;
    private TodosLosTurnosPanel todosLosTurnosPanel;
    private ProgramarTurnoPanel programarTurnoPanel;
    private EditarUsuarioPanel editarUsuarioPanel;
    private CalendarioTurnosPanel calendarioTurnosPanel;
    private String panelRetornoRegistroPaciente = "GESTION_PACIENTES";
    private String panelActual = "LOGIN";

    public MainApplicationView() {
        configurarTextosDialogosEnEspanol();
        setTitle("Rey Salud - Gestión Clínica Profesional");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Rectangle areaUtil = ResponsiveUI.getAreaUtil();
        setMinimumSize(new Dimension(
                Math.min(ResponsiveUI.px(960), areaUtil.width),
                Math.min(ResponsiveUI.px(620), areaUtil.height)));
        setSize(Math.min(1280, areaUtil.width), Math.min(800, areaUtil.height));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        sidebar = new SidebarPanel(this);
        ResponsiveUI.aplicar(sidebar);
        sidebar.setVisible(false);
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(new LoginPanel(this), "LOGIN");

        add(contentPanel, BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentMoved(ComponentEvent e) { adaptarAlMonitorActual(); }
            @Override public void componentResized(ComponentEvent e) { adaptarAlMonitorActual(); }
        });
        cambiarPanel("LOGIN");
    }

    private void adaptarAlMonitorActual() {
        if (ResponsiveUI.actualizarPara(getGraphicsConfiguration())) {
            ResponsiveUI.aplicar(sidebar);
            ResponsiveUI.aplicar(contentPanel);
            revalidate();
            repaint();
        }
    }

    private void configurarTextosDialogosEnEspanol() {
        UIManager.put("OptionPane.okButtonText", "Aceptar");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.yesButtonText", "Sí");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("FileChooser.openButtonText", "Abrir");
        UIManager.put("FileChooser.saveButtonText", "Guardar");
        UIManager.put("FileChooser.cancelButtonText", "Cancelar");
    }

    public void cambiarPanel(String nombre) {
        if ("NUEVA_CONSULTA".equals(panelActual)
                && !"NUEVA_CONSULTA".equals(nombre)
                && nuevaConsultaPanel != null
                && nuevaConsultaPanel.tieneHistoriaClinicaPendiente()) {
            JOptionPane.showMessageDialog(this,
                    "Para salir primero tenés que completar y guardar la historia clínica del turno en consulta.",
                    "Historia clínica pendiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String destino = nombre;
        if (!esPanelPermitido(destino)) {
            destino = panelInicialPorRol();
            if (usuarioSesion != null) {
                JOptionPane.showMessageDialog(this, "Tu perfil no tiene permisos para acceder a esa sección.",
                        "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            }
        }

        asegurarPanel(destino);
        cardLayout.show(contentPanel, destino);
        panelActual = destino;

        sidebar.setVisible(!destino.equals("LOGIN"));
        EstiloRedondeado.aplicarBotones(sidebar);
        EstiloRedondeado.aplicarBotones(contentPanel);
        EstiloRedondeado.aplicarTablas(contentPanel);
        ResponsiveUI.aplicar(sidebar);
        ResponsiveUI.aplicar(contentPanel);
        final String panelMostrado = destino;

        SwingUtilities.invokeLater(() -> {
            try {
                if (panelMostrado.equals("DASHBOARD")) {
                    dashboardPanel.cargarConsultasDesdeBD();
                }
                if (panelMostrado.equals("ESTADISTICAS")) estadisticasPanel.cargarEstadisticas();
                if (panelMostrado.equals("GESTION_PACIENTES")) gestionPacientesPanel.cargarPacientes();
                if (panelMostrado.equals("GESTION_PERSONAL")) gestionPersonalPanel.cargarPersonal();
                if (panelMostrado.equals("GESTION_USUARIOS")) gestionUsuariosPanel.cargarUsuarios();
                if (panelMostrado.equals("AGENDA_TURNOS")) agendaTurnosPanel.cargarTurnos();
                if (panelMostrado.equals("TODOS_TURNOS")) todosLosTurnosPanel.cargarTurnos();
                if (panelMostrado.equals("CALENDARIO_TURNOS")) calendarioTurnosPanel.cargarCalendario();
                EstiloRedondeado.aplicarBotones(sidebar);
                EstiloRedondeado.aplicarBotones(contentPanel);
                EstiloRedondeado.aplicarTablas(contentPanel);
                ResponsiveUI.aplicar(sidebar);
                ResponsiveUI.aplicar(contentPanel);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar la sección: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        revalidate();
        repaint();
    }

    private boolean esPanelPermitido(String panel) {
        if ("LOGIN".equals(panel)) return true;
        if (usuarioSesion == null) return false;
        // Se conserva el módulo y sus datos, pero no debe ser accesible desde
        // ningún rol mientras permanezca deshabilitado para la interfaz.
        if ("TODOS_TURNOS".equals(panel)) return false;

        if (usuarioSesion.esAdministrador()) return true;

        if (usuarioSesion.esMedico()) {
            return panel.equals("DASHBOARD") || panel.equals("ESTADISTICAS") || panel.equals("GESTION_PACIENTES")
                    || panel.equals("REGISTRAR_PACIENTE") || panel.equals("EDITAR_PACIENTE")
                    || panel.equals("VER_HISTORIA") || panel.equals("NUEVA_CONSULTA")
                    || panel.equals("AGENDA_TURNOS") || panel.equals("CALENDARIO_TURNOS")
                    || panel.equals("PROGRAMAR_TURNO");
        }

        if (usuarioSesion.esSecretaria()) {
            return panel.equals("GESTION_PACIENTES") || panel.equals("REGISTRAR_PACIENTE")
                    || panel.equals("EDITAR_PACIENTE") || panel.equals("AGENDA_TURNOS")
                    || panel.equals("CALENDARIO_TURNOS") || panel.equals("PROGRAMAR_TURNO");
        }
        return false;
    }

    private String panelInicialPorRol() {
        if (usuarioSesion == null) return "LOGIN";
        return usuarioSesion.esSecretaria() ? "AGENDA_TURNOS" : "DASHBOARD";
    }

    public void aplicarPermisosSidebar() {
        sidebar.aplicarPermisos();
    }

    public void irARegistrarPaciente() {
        panelRetornoRegistroPaciente = "GESTION_PACIENTES";
        cambiarPanel("REGISTRAR_PACIENTE");
    }

    public void irARegistrarPacienteDesdeTurno(String busquedaPaciente) {
        asegurarPanel("REGISTRAR_PACIENTE");
        panelRetornoRegistroPaciente = "PROGRAMAR_TURNO";
        registrarPacientePanel.prepararAltaDesdeTurno(busquedaPaciente);
        cambiarPanel("REGISTRAR_PACIENTE");
    }

    public void irARegistrarPacienteDesdeCalendario() {
        asegurarPanel("REGISTRAR_PACIENTE");
        panelRetornoRegistroPaciente = "CALENDARIO_TURNOS";
        registrarPacientePanel.prepararAltaDesdeTurno("");
        cambiarPanel("REGISTRAR_PACIENTE");
    }

    public void volverDesdeRegistroPaciente() {
        String destino = panelRetornoRegistroPaciente;
        panelRetornoRegistroPaciente = "GESTION_PACIENTES";
        if ("PROGRAMAR_TURNO".equals(destino)) {
            asegurarPanel("PROGRAMAR_TURNO");
            programarTurnoPanel.reanudarTrasAltaPaciente();
            cambiarPanel("PROGRAMAR_TURNO");
        } else {
            cambiarPanel(destino);
        }
    }

    public void irAEditarPaciente(int idPaciente) {
        asegurarPanel("EDITAR_PACIENTE");
        if (editarPacientePanel.prepararEdicion(idPaciente)) {
            cambiarPanel("EDITAR_PACIENTE");
        }
    }

    public void irANuevaConsulta(String dni) {
        irANuevaConsulta(dni, 0);
    }

    public void irANuevaConsulta(String dni, int idTurno) {
        if (usuarioSesion == null || (!usuarioSesion.esMedico() && !usuarioSesion.esAdministrador())) {
            JOptionPane.showMessageDialog(this, "Solo un médico o administrador autenticado puede registrar historias clínicas.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (idTurno > 0) {
            String motivo = new clinicabalbarrey.dao.TurnoDAO()
                    .prepararTurnoParaHistoria(idTurno, usuarioSesion.getId_personal(), usuarioSesion.esAdministrador());
            if (motivo != null) {
                JOptionPane.showMessageDialog(this, motivo, "Acceso a Historia Clínica", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (idTurno > 0 && new ConsultaDAO().existeConsultaActivaPorTurno(idTurno)) {
            JOptionPane.showMessageDialog(this,
                    "Este turno ya tiene una historia clínica cargada.",
                    "Historia existente", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        asegurarPanel("NUEVA_CONSULTA");
        nuevaConsultaPanel.iniciarConsulta(dni, idTurno);
        cambiarPanel("NUEVA_CONSULTA");
    }

    public void irAVerHistoria(String dni, String nombre) {
        if (usuarioSesion != null && usuarioSesion.esSecretaria()) {
            JOptionPane.showMessageDialog(this, "Por normas de confidencialidad, el personal administrativo no tiene acceso a las Historias Clínicas.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        asegurarPanel("VER_HISTORIA");
        verHistoriaMedicaPanel.cargarHistorial(dni, nombre);
        cambiarPanel("VER_HISTORIA");
    }

    public void irARegistrarPersonal() { cambiarPanel("REGISTRAR_PERSONAL"); }

    public void irAEditarPersonal(int idPersonal) {
        asegurarPanel("EDITAR_PERSONAL");
        if (editarPersonalPanel.prepararEdicion(idPersonal)) {
            cambiarPanel("EDITAR_PERSONAL");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la información.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void irARegistrarUsuario() { asegurarPanel("REGISTRAR_USUARIO"); registrarUsuarioPanel.cargarPersonalDisponible(); cambiarPanel("REGISTRAR_USUARIO"); }
    public void irAProgramarTurno() { asegurarPanel("PROGRAMAR_TURNO"); programarTurnoPanel.cargarDatosParaTurno(); cambiarPanel("PROGRAMAR_TURNO"); }
    public void irAEditarUsuario(int idUsuario) { asegurarPanel("EDITAR_USUARIO"); editarUsuarioPanel.prepararEdicion(idUsuario); cambiarPanel("EDITAR_USUARIO"); }

    public void irACalendario() { cambiarPanel("CALENDARIO_TURNOS"); }
    public void irATodosLosTurnos() { cambiarPanel("TODOS_TURNOS"); }

    public void irAProgramarTurnoDesdeCalendario(java.util.Date fecha, String hora, int idMedico, String nombreMedico) {
        asegurarPanel("PROGRAMAR_TURNO");
        programarTurnoPanel.preCargarDatos(fecha, hora, idMedico, nombreMedico);
        cambiarPanel("PROGRAMAR_TURNO");
    }

    public Usuario getUsuarioSesion() { return usuarioSesion; }

    public void cerrarSesion() {
        if (agendaTurnosPanel != null) agendaTurnosPanel.cancelarEdicionPendiente();
        setUsuarioSesion(null);
        cambiarPanel("LOGIN");
    }

    public void setUsuarioSesion(Usuario u) {
        this.usuarioSesion = u;
        if (u != null) new clinicabalbarrey.dao.TurnoDAO().marcarAusentesVencidos();
    }
    public String getNombreUsuarioActual() {
        if (usuarioSesion == null) return "";
        String nombreCompleto = ((usuarioSesion.getNombre() == null ? "" : usuarioSesion.getNombre()) + " "
                + (usuarioSesion.getApellido() == null ? "" : usuarioSesion.getApellido())).trim();
        if (!nombreCompleto.isEmpty()) return nombreCompleto;
        if (usuarioSesion.esAdministrador()) return "Lucas";
        return usuarioSesion.getUsername() == null ? "" : usuarioSesion.getUsername();
    }

    public String getNombreCortoUsuarioActual() {
        if (usuarioSesion == null) return "";
        if (usuarioSesion.esAdministrador()) return "Lucas";
        String nombre = usuarioSesion.getNombre() == null ? "" : usuarioSesion.getNombre().trim();
        if (!nombre.isEmpty()) return nombre;
        return usuarioSesion.getUsername() == null ? "" : usuarioSesion.getUsername();
    }

    public String getRolUsuarioActual() {
        if (usuarioSesion == null) return "";
        if (usuarioSesion.esAdministrador()) return "Administrador";
        if (usuarioSesion.esMedico()) return "Medico";
        if (usuarioSesion.esSecretaria()) return "Secretaria";
        String rol = usuarioSesion.getRol();
        return rol == null || rol.isBlank() ? "Usuario" : rol.trim();
    }

    private void asegurarPanel(String panel) {
        switch (panel) {
            case "DASHBOARD" -> { if (dashboardPanel == null) { dashboardPanel = new DashboardPanel(this); contentPanel.add(dashboardPanel, panel); } }
            case "ESTADISTICAS" -> { if (estadisticasPanel == null) { estadisticasPanel = new EstadisticasPanel(this); contentPanel.add(estadisticasPanel, panel); } }
            case "GESTION_PACIENTES" -> { if (gestionPacientesPanel == null) { gestionPacientesPanel = new GestionPacientesPanel(this); contentPanel.add(gestionPacientesPanel, panel); } }
            case "GESTION_PERSONAL" -> { if (gestionPersonalPanel == null) { gestionPersonalPanel = new GestionPersonalPanel(this); contentPanel.add(gestionPersonalPanel, panel); } }
            case "GESTION_USUARIOS" -> { if (gestionUsuariosPanel == null) { gestionUsuariosPanel = new GestionUsuariosPanel(this); contentPanel.add(gestionUsuariosPanel, panel); } }
            case "REGISTRAR_PACIENTE" -> { if (registrarPacientePanel == null) { registrarPacientePanel = new RegistrarPacientePanel(this); contentPanel.add(registrarPacientePanel, panel); } }
            case "EDITAR_PACIENTE" -> { if (editarPacientePanel == null) { editarPacientePanel = new EditarPacientePanel(this); contentPanel.add(editarPacientePanel, panel); } }
            case "NUEVA_CONSULTA" -> { if (nuevaConsultaPanel == null) { nuevaConsultaPanel = new NuevaConsultaPanel(this); contentPanel.add(nuevaConsultaPanel, panel); } }
            case "VER_HISTORIA" -> { if (verHistoriaMedicaPanel == null) { verHistoriaMedicaPanel = new VerHistoriaMedicaPanel(this); contentPanel.add(verHistoriaMedicaPanel, panel); } }
            case "REGISTRAR_PERSONAL" -> { if (registrarPersonalPanel == null) { registrarPersonalPanel = new RegistrarPersonalPanel(this); contentPanel.add(registrarPersonalPanel, panel); } }
            case "EDITAR_PERSONAL" -> { if (editarPersonalPanel == null) { editarPersonalPanel = new EditarPersonalPanel(this); contentPanel.add(editarPersonalPanel, panel); } }
            case "REGISTRAR_USUARIO" -> { if (registrarUsuarioPanel == null) { registrarUsuarioPanel = new RegistrarUsuarioPanel(this); contentPanel.add(registrarUsuarioPanel, panel); } }
            case "AGENDA_TURNOS" -> { if (agendaTurnosPanel == null) { agendaTurnosPanel = new AgendaTurnosPanel(this); contentPanel.add(agendaTurnosPanel, panel); } }
            case "TODOS_TURNOS" -> { if (todosLosTurnosPanel == null) { todosLosTurnosPanel = new TodosLosTurnosPanel(this); contentPanel.add(todosLosTurnosPanel, panel); } }
            case "PROGRAMAR_TURNO" -> { if (programarTurnoPanel == null) { programarTurnoPanel = new ProgramarTurnoPanel(this); contentPanel.add(programarTurnoPanel, panel); } }
            case "EDITAR_USUARIO" -> { if (editarUsuarioPanel == null) { editarUsuarioPanel = new EditarUsuarioPanel(this); contentPanel.add(editarUsuarioPanel, panel); } }
            case "CALENDARIO_TURNOS" -> { if (calendarioTurnosPanel == null) { calendarioTurnosPanel = new CalendarioTurnosPanel(this); contentPanel.add(calendarioTurnosPanel, panel); } }
            default -> { }
        }
    }

}
