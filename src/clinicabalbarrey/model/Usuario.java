package clinicabalbarrey.model;

import java.text.Normalizer;
import java.util.Locale;

public class Usuario {
    private int id_usuario;
    private String username;
    private int id_personal;
    private int id_rol;
    private String nombre;
    private String apellido;
    private String dni;
    private String matricula;
    private String rol;

    // --- NUEVOS CAMPOS DE PERSONAL ---
    private String telefono;
    private String email;
    private String direccion;
    private int idEspecialidad;

    public Usuario() {}

    // --- GETTERS Y SETTERS ---
    public int getId_usuario() { return id_usuario; }
    public void setId_usuario(int id_usuario) { this.id_usuario = id_usuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getId_personal() { return id_personal; }
    public void setId_personal(int id_personal) { this.id_personal = id_personal; }

    public int getId_rol() { return id_rol; }
    public void setId_rol(int id_rol) { this.id_rol = id_rol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean esAdministrador() {
        String valor = rolNormalizado();
        return valor.equals("administrador") || valor.equals("admin") || id_rol == 1;
    }
    public boolean esMedico() { return rolNormalizado().equals("medico"); }
    public boolean esSecretaria() { return rolNormalizado().startsWith("secretari"); }

    private String rolNormalizado() {
        String valor = rol == null ? "" : rol;
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public int getIdEspecialidad() { return idEspecialidad; }
    public void setIdEspecialidad(int idEspecialidad) { this.idEspecialidad = idEspecialidad; }
}
