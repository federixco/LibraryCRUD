package db;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * -----------------
 * Proveedor central de conexiones JDBC a una base SQLite embebida.
 *
 * ¿Qué hace?
 *   - Resuelve la ubicación del archivo de base en: ${user.home}/.biblioteca/biblioteca.db
 *   - Crea la carpeta y el archivo si no existen (idempotente).
 *   - Expone una URL JDBC absoluta y métodos utilitarios para conexión y diagnóstico.
 *
 * Decisiones:
 *   - Ubicación en el home del usuario para evitar problemas de permisos en carpetas del proyecto.
 *   - Inicialización estática (eager): al cargar la clase se garantiza que la ruta existe.
 *   - Errores de E/S al preparar la ruta se elevan como RuntimeException (falla temprana y explícita).
 *
 * Colabora con:
 *   - Driver JDBC de SQLite (org.sqlite.JDBC) disponible en el classpath.
 */
public class ConnectionFactory {

    /** Nombre del archivo de base dentro del directorio de la app. */
    private static final String DB_FILE_NAME = "biblioteca.db";

    /** Ruta absoluta del archivo .db una vez resuelta. */
    private static Path DB_PATH;

    /** URL JDBC absoluta: jdbc:sqlite:/ruta/completa/biblioteca.db */
    private static String SQLITE_URL;

    // ========== Inicialización estática ==========
    static {
        // 1) Resolver carpeta de la app dentro del HOME del usuario
        String home = System.getProperty("user.home");
        Path appDir = Paths.get(home, ".biblioteca");
        try {
            // Crea la carpeta (y padres) si no existe. Idempotente si ya está creada.
            Files.createDirectories(appDir);
        } catch (IOException e) {
            // Falla temprana: no podemos garantizar persistencia sin directorio
            throw new RuntimeException("No se pudo crear la carpeta: " + appDir, e);
        }

        // 2) Resolver la ruta del archivo de base de datos
        DB_PATH = appDir.resolve(DB_FILE_NAME);
        try {
            // “Tocar” el archivo: si no existe, se crea vacío.
            if (!Files.exists(DB_PATH)) {
                Files.createFile(DB_PATH);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el archivo de base: " + DB_PATH, e);
        }

        // 3) Construir URL JDBC absoluta para SQLite
        SQLITE_URL = "jdbc:sqlite:" + DB_PATH.toAbsolutePath();

        // 4) Logs de diagnóstico (útiles en setup; se pueden comentar en producción)
        System.out.println("[ConnectionFactory] DB_PATH  = " + DB_PATH.toAbsolutePath());
        System.out.println("[ConnectionFactory] JDBC URL = " + SQLITE_URL);
    }

    /**
     * Abre y devuelve una conexión JDBC a la base SQLite.
     * <p>
     * Uso típico con try-with-resources:
     * <pre>
     * try (Connection cn = ConnectionFactory.getConnection()) {
     *     // usar cn ...
     * }
     * </pre>
     *
     * @return {@link Connection} abierta contra la URL configurada.
     * @throws SQLException si el DriverManager no puede abrir la conexión (ruta/permiso/driver).
     */
    public static Connection getConnection() throws SQLException {
        // No cacheamos conexiones: cada llamada devuelve una conexión fresca.
        // Esto evita problemas de concurrencia y cerrados accidentales.
        return DriverManager.getConnection(SQLITE_URL);
    }

    /**
     * Devuelve la ruta absoluta del archivo .db (para mostrar en mensajes de error o soporte).
     *
     * @return {@link Path} absoluto del archivo SQLite.
     */
    public static Path dbPath() {
        return DB_PATH.toAbsolutePath();
    }
}
