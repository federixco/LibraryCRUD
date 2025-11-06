package db;

import util.HashUtil;

import java.sql.*;

/**
 * DbInit
 * - Crea tablas libro y usuario.
 * - Seed: 2 libros y 1 admin (admin / admin123).
 */
public class DbInit {

    public static void ensureInit() {
        try (Connection cn = ConnectionFactory.getConnection()) {
            try (Statement s = cn.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON;");
            }

            // Tabla libro (igual que antes)
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

            // Tabla usuario
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

            // Seed libros si vacío
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

            // Seed admin si no existe
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