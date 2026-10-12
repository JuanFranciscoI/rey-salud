package clinicabalbarrey.security;

import clinicabalbarrey.model.Usuario;
import java.text.Normalizer;
import java.util.Locale;

/** Reglas de autorización de turnos, independientes de la interfaz. */
public final class PoliticaTurnos {
    private PoliticaTurnos() {}

    public static boolean puedeEditarEstadosClinicos(Usuario usuario) {
        return usuario != null && (usuario.esMedico() || usuario.esAdministrador());
    }

    public static boolean puedeCancelar(Usuario usuario, String estadoActual) {
        return usuario != null
                && (usuario.esMedico() || usuario.esSecretaria() || usuario.esAdministrador())
                && !esDefinitivo(estadoActual);
    }

    public static String validarCambio(Usuario usuario, String estadoActual, String estadoNuevo) {
        if (usuario == null) return "No hay una sesión autenticada.";
        if (usuario.esAdministrador()) return null;
        String actual = normalizar(estadoActual);
        String nuevo = normalizar(estadoNuevo);
        if (esDefinitivo(actual) && !actual.equals(nuevo)) {
            return "El turno está " + estadoActual + " y su estado es definitivo.";
        }
        if (usuario.esMedico()) return null;
        if (usuario.esSecretaria()) {
            if ("cancelado".equals(nuevo)) return null;
            if ("reservado".equals(actual) && "esperando".equals(nuevo)) return null;
            if ("esperando".equals(actual) && "reservado".equals(nuevo)) return null;
            if (("reservado".equals(actual) || "esperando".equals(actual))
                    && "en consulta".equals(nuevo)) return null;
            if ("reservado".equals(actual) && "ausente".equals(nuevo)) return null;
            if (actual.equals(nuevo)) return null;
        }
        return "La Secretaría puede registrar la llegada, iniciar la consulta desde el horario programado, confirmar una ausencia después de 30 minutos, corregir la espera o cancelar turnos futuros abiertos.";
    }

    public static boolean esDefinitivo(String estado) {
        String valor = normalizar(estado);
        return "atendido".equals(valor) || "finalizado".equals(valor) || "ausente".equals(valor);
    }

    private static String normalizar(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }
}
