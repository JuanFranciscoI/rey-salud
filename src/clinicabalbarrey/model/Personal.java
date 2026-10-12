package clinicabalbarrey.model;

import java.util.Date;

public class Personal {
    private int idPersonal;
    private String dni;
    private String nombreYApellido; // Con "Y"
    private String matricula;
    private String telefono;
    private String correoElectronico;
    private String direccion;
    private int idEspecialidad;
    private String estado;
    private Date fechaIngreso;
    private String rol;
    private String especialidad;
    private boolean accesoActivo;
    private boolean tieneUsuario;

    public Personal() {}

    public int getIdPersonal() { return idPersonal; }
    public void setIdPersonal(int idPersonal) { this.idPersonal = idPersonal; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombreYApellido() { return nombreYApellido; }
    public void setNombreYApellido(String nombreYApellido) { this.nombreYApellido = nombreYApellido; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public int getIdEspecialidad() { return idEspecialidad; }
    public void setIdEspecialidad(int idEspecialidad) { this.idEspecialidad = idEspecialidad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Date getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(Date fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public boolean isAccesoActivo() { return accesoActivo; }
    public void setAccesoActivo(boolean accesoActivo) { this.accesoActivo = accesoActivo; }
    public boolean isTieneUsuario() { return tieneUsuario; }
    public void setTieneUsuario(boolean tieneUsuario) { this.tieneUsuario = tieneUsuario; }
}
