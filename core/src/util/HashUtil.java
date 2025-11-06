package util;



import java.security.MessageDigest;
import java.security.SecureRandom;



/**
 * HashUtil
 * - Genera salt aleatoria.
 * - Calcula hash SHA-256 (hex) de (salt + password).
 */




public final class HashUtil {
    private static final SecureRandom RNG = new SecureRandom();
    private HashUtil(){}

    public static String newSaltHex(int bytes) {
        byte[] salt = new byte[bytes];
        RNG.nextBytes(salt);
        return toHex(salt);
    }

    public static String sha256Hex(String saltHex, String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(fromHex(saltHex));
            md.update(plain.getBytes("UTF-8"));
            return toHex(md.digest());
        } catch (Exception e) {
            throw new RuntimeException("No se pudo calcular hash", e);
        }
    }

    public static boolean verify(String saltHex, String plain, String expectedHex) {
        return sha256Hex(saltHex, plain).equalsIgnoreCase(expectedHex);
    }

    // utils hex
    private static String toHex(byte[] b){
        StringBuilder sb = new StringBuilder(b.length*2);
        for (byte x: b) sb.append(String.format("%02x", x));
        return sb.toString();
    }
    private static byte[] fromHex(String s){
        int len = s.length(); byte[] out = new byte[len/2];
        for (int i=0;i<len;i+=2) out[i/2]=(byte)Integer.parseInt(s.substring(i,i+2),16);
        return out;
    }
}