package dao;

import db.ConnectionFactory;
import model.Auditoria;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC para la tabla 'auditoria'.
 * Solo lectura: lista los eventos recientes para informes.
 */
public class JdbcAuditoriaDao implements AuditoriaDao {

    @Override
    public List<Auditoria> listarRecientes(int limit) {
        final String sql = "SELECT * FROM auditoria ORDER BY ts DESC LIMIT ?";
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, limit <= 0 ? 50 : limit);

            try (ResultSet rs = ps.executeQuery()) {
                List<Auditoria> out = new ArrayList<>();
                while (rs.next()) {
                    Auditoria a = new Auditoria();
                    a.setId(rs.getLong("id"));
                    // ts guardado como texto ISO-8601: lo parseamos a LocalDateTime
                    a.setTs(LocalDateTime.parse(rs.getString("ts")));
                    a.setOperadorUsername(rs.getString("operador_username"));
                    a.setTipo(rs.getString("tipo"));
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
        } catch (SQLException e) {
            throw new RuntimeException("Error listando auditoría: " + e.getMessage(), e);
        }
    }
}
