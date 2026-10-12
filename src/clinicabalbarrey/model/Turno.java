package clinicabalbarrey.model;

import java.sql.Timestamp;

public class Turno {
    private int idTurno;
    private Timestamp fechaHora;
    private String estadoTurno;
    private String observaciones;
    private int idPaciente;
    private int idPersonal;

    // Variables extra para mostrar en la tabla (no están en tu BD, pero sirven para la vista)
    private String pacienteNombreCompleto;
    private String medicoNombreCompleto;

    public Turno() {}

    public int getIdTurno() { return idTurno; }
    public void setIdTurno(int idTurno) { this.idTurno = idTurno; }

    public Timestamp getFechaHora() { return fechaHora; }
    public void setFechaHora(Timestamp fechaHora) { this.fechaHora = fechaHora; }

    public String getEstadoTurno() { return estadoTurno; }
    public void setEstadoTurno(String estadoTurno) { this.estadoTurno = estadoTurno; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }

    public int getIdPersonal() { return idPersonal; }
    public void setIdPersonal(int idPersonal) { this.idPersonal = idPersonal; }

    public String getPacienteNombreCompleto() { return pacienteNombreCompleto; }
    public void setPacienteNombreCompleto(String p) { this.pacienteNombreCompleto = p; }

    public String getMedicoNombreCompleto() { return medicoNombreCompleto; }
    public void setMedicoNombreCompleto(String m) { this.medicoNombreCompleto = m; }
}