package clinicabalbarrey.model;

import java.sql.Date;

public class Paciente {
    private int id_paciente;
    private String dni;
    private String nombre;
    private String apellido;
    private Date fechaNacimiento;
    private String sexo;
    private String email;
    private String telefono;
    private String direccion;
    private String estado;

    // --- NUEVOS CAMPOS ---
    private int idObraSocial;
    private String obraSocialNombre; // Para mostrar en las tablas
    private String numeroAfiliado;

    public Paciente() {}

    public int getId_paciente() { return id_paciente; }
    public void setId_paciente(int id_paciente) { this.id_paciente = id_paciente; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public Date getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getIdObraSocial() { return idObraSocial; }
    public void setIdObraSocial(int idObraSocial) { this.idObraSocial = idObraSocial; }

    public String getObraSocialNombre() { return obraSocialNombre; }
    public void setObraSocialNombre(String obraSocialNombre) { this.obraSocialNombre = obraSocialNombre; }

    public String getNumeroAfiliado() { return numeroAfiliado; }
    public void setNumeroAfiliado(String numeroAfiliado) { this.numeroAfiliado = numeroAfiliado; }
}