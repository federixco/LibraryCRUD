package db;

import java.sql.*;

public class DbInit {

    public static void ensureInit() {
        try (Connection cn = ConnectionFactory.getConnection()) {
            // Comprobación rápida
            try (Statement s = cn.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON;");
            }

            final String ddl = """
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
            try (Statement s = cn.createStatement()) {
                s.execute(ddl);
            }

            boolean vacia = true;
            try (Statement s = cn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM libro")) {
                if (rs.next()) vacia = rs.getInt(1) == 0;
            }

            if (vacia) {
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO libro (codigo, titulo, autor, categoria, editorial, anio, stock, activo) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                    ps.setString(1, "L001");
                    ps.setString(2, "El Quijote");
                    ps.setString(3, "Miguel de Cervantes");
                    ps.setString(4, "Novela");
                    ps.setString(5, "Acme");
                    ps.setInt(6, 2005);
                    ps.setInt(7, 4);
                    ps.setInt(8, 1);
                    ps.executeUpdate();

                    ps.setString(1, "L002");
                    ps.setString(2, "Clean Code");
                    ps.setString(3, "Robert C. Martin");
                    ps.setString(4, "Programación");
                    ps.setString(5, "Prentice Hall");
                    ps.setInt(6, 2008);
                    ps.setInt(7, 2);
                    ps.setInt(8, 1);
                    ps.executeUpdate();
                }
            }

            // Log de OK (podés quitarlo)
            // System.out.println("DB lista. Registros: " + (vacia ? 2 : "existentes"));

        } catch (SQLException e) {
            throw new RuntimeException("Error inicializando la base: " + e.getMessage(), e);
        }
    }
}