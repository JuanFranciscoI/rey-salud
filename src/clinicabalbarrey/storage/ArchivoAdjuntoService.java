package clinicabalbarrey.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ArchivoAdjuntoService {
    private static final Path RAIZ = Path.of("data", "adjuntos").toAbsolutePath().normalize();

    private ArchivoAdjuntoService() {}

    public static String copiar(List<File> archivos) throws IOException {
        if (archivos == null || archivos.isEmpty()) return "";
        Files.createDirectories(RAIZ);
        List<String> rutas = new ArrayList<>();
        try {
            for (File archivo : archivos) {
                if (archivo == null || !archivo.isFile()) {
                    throw new IOException("No se encuentra el archivo seleccionado: " + archivo);
                }
                String nombre = archivo.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
                Path destino = RAIZ.resolve(UUID.randomUUID() + "_" + nombre).normalize();
                if (!destino.startsWith(RAIZ)) throw new IOException("Nombre de archivo inválido");
                Files.copy(archivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
                rutas.add(Path.of("").toAbsolutePath().normalize().relativize(destino).toString());
            }
            return String.join(" | ", rutas);
        } catch (IOException e) {
            eliminar(rutas);
            throw e;
        }
    }

    public static File resolver(String rutaGuardada) {
        if (rutaGuardada == null || rutaGuardada.isBlank()) return null;
        Path ruta = Path.of(rutaGuardada.trim());
        if (!ruta.isAbsolute()) ruta = Path.of("").toAbsolutePath().resolve(ruta);
        return ruta.normalize().toFile();
    }

    public static void eliminar(String rutasGuardadas) {
        if (rutasGuardadas == null || rutasGuardadas.isBlank()) return;
        eliminar(List.of(rutasGuardadas.split("\\s*\\|\\s*")));
    }

    private static void eliminar(List<String> rutas) {
        for (String ruta : rutas) {
            try {
                File archivo = resolver(ruta);
                if (archivo != null && archivo.toPath().toAbsolutePath().normalize().startsWith(RAIZ)) {
                    Files.deleteIfExists(archivo.toPath());
                }
            } catch (Exception ignored) {
                // La limpieza es compensatoria: nunca debe ocultar el error original.
            }
        }
    }
}
