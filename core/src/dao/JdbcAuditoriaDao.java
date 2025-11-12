package dao;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import db.ConnectionFactory;
import model.Auditoria;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC del {@link AuditoriaDao} para SQLite.
 *
 * ¿Qué hace?
 *   - Lee eventos de la tabla auditoria para mostrarlos en informes/consultas.
 *
 * Decisiones:
 *   - Solo lectura (no inserta ni borra).
 *   - Ordena por ts DESC en la consulta.
 *   - Maneja NULL correctamente en columnas opcionales (prestamo_id, cantidad).
 *   - Envuelve excepciones SQL en RuntimeException para no ensuciar firmas con checked exceptions.
 *
 * Colabora con:
 *   - {@link db.ConnectionFactory} para abrir conexiones.
 *   - {@link model.Auditoria} como DTO de salida.
 */


public class JdbcAuditoriaDao implements AuditoriaDao {

    /**
     * Lista los eventos de auditoría más recientes.
     *
     * @param limit cantidad máxima de filas a devolver; si es <= 0 se usa un valor por defecto (50).
     * @return lista de {@link Auditoria} ordenada de más nuevo a más viejo.
     *
     * Contrato/Reglas:
     *  - El orden es DESC por <code>ts</code>.
     *  - La implementación NO cierra la app ni altera autocommit; usa try-with-resources.
     *  - Los campos opcionales (prestamo_id, cantidad, detalle, destinatario, libro_codigo) pueden venir nulos.
     *  - Se asume que <code>ts</code> está guardado en formato ISO-8601 y se parsea con {@link LocalDateTime#parse(CharSequence)}.
     */
    
    @Override
    public List<Auditoria> listarRecientes(int limit) {
        // 1) SQL: ordenar por ts DESC y limitar cantidad
        final String sql = "SELECT * FROM auditoria ORDER BY ts DESC LIMIT ?";

        // 2) Abrir conexión y preparar statement
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 3) Parámetros (usar default 50 si el caller pasa <= 0)
            ps.setInt(1, (limit <= 0) ? 50 : limit);

            // 4) Ejecutar consulta y mapear ResultSet -> List<Auditoria>
            try (ResultSet rs = ps.executeQuery()) {
                List<Auditoria> out = new ArrayList<>();

                while (rs.next()) {
                    Auditoria a = new Auditoria();

                    // Campos obligatorios
                    a.setId(rs.getLong("id"));
                    // ts guardado como texto ISO-8601: parse a LocalDateTime
                    a.setTs(LocalDateTime.parse(rs.getString("ts")));
                    a.setOperadorUsername(rs.getString("operador_username"));
                    a.setTipo(rs.getString("tipo"));

                    // Campos opcionales (pueden ser NULL)
                    a.setLibroCodigo(rs.getString("libro_codigo"));

                    long pid = rs.getLong("prestamo_id");
                    a.setPrestamoId(rs.wasNull() ? null : pid);

                    int cant = rs.getInt("cantidad");
                    a.setCantidad(rs.wasNull() ? null : cant);

                    a.setDestinatario(rs.getString("destinatario"));
                    a.setDetalle(rs.getString("detalle"));

                    out.add(a);
                }
                return out;
            }

        // 5) Manejo de errores de acceso a datos
        } catch (SQLException e) {
            throw new RuntimeException("Error listando auditoría: " + e.getMessage(), e);
        }
    }
}
