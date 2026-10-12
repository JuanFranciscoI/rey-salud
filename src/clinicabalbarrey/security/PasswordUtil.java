package clinicabalbarrey.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 210_000;
    private static final int SAL_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String hash(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        byte[] salt = new byte[SAL_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = derivar(password, salt, ITERACIONES);
        return PREFIJO + "$" + ITERACIONES + "$"
                + Base64.getEncoder().withoutPadding().encodeToString(salt) + "$"
                + Base64.getEncoder().withoutPadding().encodeToString(derived);
    }

    public static boolean verificar(char[] password, String almacenada) {
        if (password == null || almacenada == null || almacenada.isBlank()) return false;
        if (!esHash(almacenada)) {
            return MessageDigest.isEqual(
                    new String(password).getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    almacenada.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        try {
            String[] partes = almacenada.split("\\$", -1);
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] actual = derivar(password, salt, iteraciones);
            return MessageDigest.isEqual(esperado, actual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean esHash(String valor) {
        return valor != null && valor.startsWith(PREFIJO + "$") && valor.split("\\$", -1).length == 4;
    }

    private static byte[] derivar(char[] password, byte[] salt, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iteraciones, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo proteger la contraseña", e);
        } finally {
            spec.clearPassword();
        }
    }
}
