package db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ConnectionFactory
 * -----------------
 * Usa SQLite en: ~/.biblioteca/biblioteca.db
 * - Crea la carpeta si no existe.
 * - “Toca” el archivo si no existe.
 * - Expone la ruta real mediante dbPath().
 */
public class ConnectionFactory {

    private static final String DB_FILE_NAME = "biblioteca.db";
    private static Path DB_PATH;
    private static String SQLITE_URL;

    static {
        // Carpeta de la app en el home
        String home = System.getProperty("user.home");
        Path appDir = Paths.get(home, ".biblioteca");
        try {
            Files.createDirectories(appDir);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta: " + appDir, e);
        }

        // Archivo de la base
        DB_PATH = appDir.resolve(DB_FILE_NAME);
        try {
            if (!Files.exists(DB_PATH)) {
                Files.createFile(DB_PATH); // crea archivo vacío
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el archivo de base: " + DB_PATH, e);
        }

        // URL absoluta de SQLite
        SQLITE_URL = "jdbc:sqlite:" + DB_PATH.toAbsolutePath();

        // Logs útiles (podés comentarlos luego)
        System.out.println("[ConnectionFactory] DB_PATH  = " + DB_PATH.toAbsolutePath());
        System.out.println("[ConnectionFactory] JDBC URL = " + SQLITE_URL);
    }

    /** Abre y devuelve la conexión JDBC. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(SQLITE_URL);
    }

    /** Devuelve la ruta absoluta del archivo .db (para mostrar en errores). */
    public static Path dbPath() {
        return DB_PATH.toAbsolutePath();
    }
}