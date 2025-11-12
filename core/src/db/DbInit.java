package db;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import util.HashUtil;

import java.sql.*;

/**
 * DbInit
 * ------
 * - Crea tablas: libro, usuario, prestamo, auditoria.
 * - Activa FOREIGN KEYS (SQLite).
 * - Seed: 2 libros y 1 admin (admin / admin123) si faltan.
 */
public class DbInit {

    public static void ensureInit() {
        try (Connection cn = ConnectionFactory.getConnection()) {

            // Activar FK por conexión (en SQLite es por-conn)
            try (Statement s = cn.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON;");
            }

            // ===== Tabla: LIBRO =====
            final String ddlLibro = """
                CREATE TABLE IF NOT EXISTS libro (
                  codigo     VARCHAR(20) PRIMARY KEY,
                  titulo     VARCHAR(200) NOT NULL,
                  autor      VARCHAR(120) NOT NULL,
                  categoria  VARCHAR(80)  NOT NULL,
                  editorial  VARCHAR(120),
                  anio       INTEGER CHECK(anio >= 0),
                  stock      INTEGER NOT NULL CHECK(stock >= 0),
                  activo     INTEGER NOT NULL DEFAULT 1
                );
                """;
            try (Statement s = cn.createStatement()) { s.execute(ddlLibro); }

            // ===== Tabla: USUARIO =====
            final String ddlUsuario = """
                CREATE TABLE IF NOT EXISTS usuario (
                  id            VARCHAR(50) PRIMARY KEY,
                  nombre        VARCHAR(120) NOT NULL,
                  username      VARCHAR(80)  NOT NULL UNIQUE,
                  password_hash VARCHAR(64)  NOT NULL,
                  salt          VARCHAR(64)  NOT NULL,
                  rol           VARCHAR(20)  NOT NULL
                );
                """;
            try (Statement s = cn.createStatement()) { s.execute(ddlUsuario); }

            // ===== Tabla: PRESTAMO + índices =====
            final String ddlPrestamo = """
                CREATE TABLE IF NOT EXISTS prestamo (
                  id                INTEGER PRIMARY KEY AUTOINCREMENT,
                  libro_codigo      VARCHAR(20)  NOT NULL,
                  operador_username VARCHAR(80)  NOT NULL,
                  destinatario      VARCHAR(120) NOT NULL,
                  cantidad          INTEGER      NOT NULL CHECK(cantidad > 0),
                  fecha_prestamo    DATETIME     NOT NULL,
                  fecha_vencimiento DATE         NOT NULL,
                  fecha_devolucion  DATETIME,
                  estado            VARCHAR(12)  NOT NULL CHECK(estado IN ('ABIERTO','DEVUELTO')),
                  FOREIGN KEY (libro_codigo)      REFERENCES libro(codigo)     ON UPDATE CASCADE ON DELETE RESTRICT,
                  FOREIGN KEY (operador_username) REFERENCES usuario(username) ON UPDATE CASCADE ON DELETE RESTRICT
                );
                CREATE INDEX IF NOT EXISTS ix_prestamo_libro       ON prestamo(libro_codigo);
                CREATE INDEX IF NOT EXISTS ix_prestamo_estado      ON prestamo(estado);
                CREATE INDEX IF NOT EXISTS ix_prestamo_vencimiento ON prestamo(fecha_vencimiento);
                """;
            try (Statement s = cn.createStatement()) { s.execute(ddlPrestamo); }

            // ===== Tabla: AUDITORIA + índices =====
            final String ddlAuditoria = """
                CREATE TABLE IF NOT EXISTS auditoria (
                  id                INTEGER PRIMARY KEY AUTOINCREMENT,
                  ts                DATETIME     NOT NULL,
                  operador_username VARCHAR(80)  NOT NULL,
                  tipo              VARCHAR(24)  NOT NULL,   -- PRESTAR, DEVOLVER, DESACTIVAR_LIBRO, ACTIVAR_LIBRO, RENOVAR
                  libro_codigo      VARCHAR(20),
                  prestamo_id       INTEGER,
                  cantidad          INTEGER,
                  destinatario      VARCHAR(120),
                  detalle           VARCHAR(255),
                  FOREIGN KEY (operador_username) REFERENCES usuario(username),
                  FOREIGN KEY (libro_codigo)      REFERENCES libro(codigo),
                  FOREIGN KEY (prestamo_id)       REFERENCES prestamo(id)
                );
                CREATE INDEX IF NOT EXISTS ix_auditoria_ts   ON auditoria(ts);
                CREATE INDEX IF NOT EXISTS ix_auditoria_tipo ON auditoria(tipo);
                """;
            try (Statement s = cn.createStatement()) { s.execute(ddlAuditoria); }

            // ===== Seed de libros (si tabla vacía) =====
            boolean librosVacios = true;
            try (Statement s = cn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM libro")) {
                if (rs.next()) librosVacios = rs.getInt(1) == 0;
            }
            if (librosVacios) {
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO libro (codigo, titulo, autor, categoria, editorial, anio, stock, activo) VALUES (?,?,?,?,?,?,?,?)")) {
                    ps.setString(1, "L001"); ps.setString(2, "El Quijote"); ps.setString(3, "Miguel de Cervantes");
                    ps.setString(4, "Novela"); ps.setString(5, "Acme"); ps.setInt(6, 2005); ps.setInt(7, 4); ps.setInt(8, 1); ps.executeUpdate();
                    ps.setString(1, "L002"); ps.setString(2, "Clean Code"); ps.setString(3, "Robert C. Martin");
                    ps.setString(4, "Programación"); ps.setString(5, "Prentice Hall"); ps.setInt(6, 2008); ps.setInt(7, 2); ps.setInt(8, 1); ps.executeUpdate();
                }
            }

            // ===== Seed admin (si no existe) =====
            boolean hayAdmin = false;
            try (PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM usuario WHERE username = ?")) {
                ps.setString(1, "admin");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) hayAdmin = rs.getInt(1) > 0;
                }
            }
            if (!hayAdmin) {
                String salt = HashUtil.newSaltHex(16);
                String hash = HashUtil.sha256Hex(salt, "admin123"); // contraseña de seed
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO usuario (id, nombre, username, password_hash, salt, rol) VALUES (?,?,?,?,?,?)")) {
                    ps.setString(1, "admin");           // id fijo simple
                    ps.setString(2, "Administrador");
                    ps.setString(3, "admin");
                    ps.setString(4, hash);
                    ps.setString(5, salt);
                    ps.setString(6, "ADMIN");
                    ps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error inicializando la base: " + e.getMessage(), e);
        }
    }
}
