package util;

import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Utilidad de hashing para contraseñas.
 *
 * ¿Qué hace?
 *  - Genera salts criptográficamente seguros.
 *  - Calcula SHA-256(salt || password) y lo devuelve en HEX.
 *  - Verifica comparando hashes (case-insensitive).
 *
 * Decisiones:
 *  - {@link SecureRandom} único y estático para generar salts de alta entropía.
 *  - Codificación HEX simple para almacenar en texto.
 *  - Se usa UTF-8 al convertir el password a bytes.
 *
 * WARNING (seguridad):
 *  - SHA-256 con salt es aceptable para trabajos académicos, pero en producción
 *    conviene usar funciones de derivación de clave con factor de costo
 *    (bcrypt/argon2/scrypt/PBKDF2).
 */
public final class HashUtil {

    /** Fuente de aleatoriedad criptográficamente segura para salts. */
    private static final SecureRandom RNG = new SecureRandom();

    /** Clase de utilidades: constructor privado para evitar instanciación. */
    private HashUtil() {}

    /**
     * Genera un salt aleatorio y lo retorna en HEX.
     *
     * @param bytes longitud del salt en bytes (p.ej. 16 → 128 bits).
     * @return cadena HEX (minúsculas) del salt.
     */
    public static String newSaltHex(int bytes) {
        byte[] salt = new byte[bytes];
        RNG.nextBytes(salt);            // llena con entropía del sistema
        return toHex(salt);             // lo exponemos en formato legible/almacenable
    }

    /**
     * Calcula SHA-256( saltHex || plain ) y devuelve el resultado en HEX.
     *
     * @param saltHex salt en HEX.
     * @param plain   contraseña en claro.
     * @return hash en HEX (minúsculas).
     */
    public static String sha256Hex(String saltHex, String plain) {
        try {
            // 1) Preparar MessageDigest con SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 2) md.update(bytes del salt) + md.update(bytes del password)
            md.update(fromHex(saltHex));
            md.update(plain.getBytes("UTF-8"));

            // 3) Calcular digest y devolver en HEX
            return toHex(md.digest());
        } catch (Exception e) {
            throw new RuntimeException("No se pudo calcular hash", e);
        }
    }

    /**
     * Verifica si el hash generado con (saltHex, plain) coincide con expectedHex.
     *
     * @param saltHex     salt en HEX.
     * @param plain       contraseña ingresada (en claro).
     * @param expectedHex hash esperado (HEX).
     * @return true si coincide, false caso contrario.
     */
    public static boolean verify(String saltHex, String plain, String expectedHex) {
        // equalsIgnoreCase por si el hash se almacenó en mayúsculas/minúsculas
        return sha256Hex(saltHex, plain).equalsIgnoreCase(expectedHex);
    }

    // ===================== Helpers HEX =====================

    /** Convierte bytes → HEX en minúsculas. */
    private static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    /**
     * Convierte HEX → bytes.
     * Asume longitud par y caracteres válidos [0-9a-fA-F].
     */
    private static byte[] fromHex(String s) {
        int len = s.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) Integer.parseInt(s.substring(i, i + 2), 16);
        }
        return out;
    }
}
