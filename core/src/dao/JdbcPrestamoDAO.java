package dao;

import db.ConnectionFactory;
import model.Prestamo;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC de Préstamos con transacciones.
 * Registra auditoría en la misma transacción.
 */
public class JdbcPrestamoDAO implements PrestamoDao {

    @Override
    public long prestar(Prestamo p) {
        String insertPrestamo = """
          INSERT INTO prestamo (libro_codigo, operador_username, destinatario, cantidad,
                                fecha_prestamo, fecha_vencimiento, estado)
          VALUES (?,?,?,?,?,?, 'ABIERTO')
        """;
        try (Connection cn = ConnectionFactory.getConnection()) {
            cn.setAutoCommit(false);
            try {
                if (!libroActivo(cn, p.getLibroCodigo()))
                    throw new RuntimeException("El libro está desactivado");
                if (!hayStockSuficiente(cn, p.getLibroCodigo(), p.getCantidad()))
                    throw new RuntimeException("Stock insuficiente");

                long id;
                try (PreparedStatement ps = cn.prepareStatement(insertPrestamo, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, p.getLibroCodigo());
                    ps.setString(2, p.getOperadorUsername());
                    ps.setString(3, p.getDestinatario());
                    ps.setInt(4, p.getCantidad());
                    ps.setString(5, p.getFechaPrestamo().toString());
                    ps.setString(6, p.getFechaVencimiento().toString());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); id = rs.getLong(1); }
                }

                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE libro SET stock = stock - ? WHERE codigo=? AND stock >= ?")) {
                    ps.setInt(1, p.getCantidad());
                    ps.setString(2, p.getLibroCodigo());
                    ps.setInt(3, p.getCantidad());
                    if (ps.executeUpdate() == 0) throw new RuntimeException("No se pudo descontar stock");
                }

                insertAudit(cn, p.getOperadorUsername(), "PRESTAR", p.getLibroCodigo(), id,
                        p.getCantidad(), p.getDestinatario(), "vencimiento=" + p.getFechaVencimiento());

                cn.commit();
                return id;
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally { cn.setAutoCommit(true); }
        } catch (SQLException e) {
            throw new RuntimeException("Error prestando: " + e.getMessage(), e);
        }
    }

    @Override
    public void devolver(long idPrestamo) {
        try (Connection cn = ConnectionFactory.getConnection()) {
            cn.setAutoCommit(false);
            try {
                int cant; String codigo; String operador; String destinatario;
                try (PreparedStatement ps = cn.prepareStatement(
                        "SELECT cantidad, libro_codigo, operador_username, destinatario " +
                        "FROM prestamo WHERE id=? AND estado='ABIERTO'")) {
                    ps.setLong(1, idPrestamo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new RuntimeException("Préstamo no abierto o inexistente");
                        cant = rs.getInt(1);
                        codigo = rs.getString(2);
                        operador = rs.getString(3);
                        destinatario = rs.getString(4);
                    }
                }

                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE prestamo SET estado='DEVUELTO', fecha_devolucion=? WHERE id=?")) {
                    ps.setString(1, LocalDateTime.now().toString());
                    ps.setLong(2, idPrestamo);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE libro SET stock = stock + ? WHERE codigo=?")) {
                    ps.setInt(1, cant);
                    ps.setString(2, codigo);
                    ps.executeUpdate();
                }

                insertAudit(cn, operador, "DEVOLVER", codigo, idPrestamo, cant, destinatario, null);
                cn.commit();
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally { cn.setAutoCommit(true); }
        } catch (SQLException e) {
            throw new RuntimeException("Error devolviendo: " + e.getMessage(), e);
        }
    }

    @Override
    public void renovar(long idPrestamo, int dias) {
        if (dias <= 0) throw new IllegalArgumentException("Días inválidos");
        try (Connection cn = ConnectionFactory.getConnection()) {
            cn.setAutoCommit(false);
            try {
                String operador, codigo, destinatario;
                try (PreparedStatement ps = cn.prepareStatement(
                        "SELECT operador_username, libro_codigo, destinatario FROM prestamo WHERE id=? AND estado='ABIERTO'")) {
                    ps.setLong(1, idPrestamo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new RuntimeException("Préstamo no abierto o inexistente");
                        operador = rs.getString(1);
                        codigo = rs.getString(2);
                        destinatario = rs.getString(3);
                    }
                }

                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE prestamo SET fecha_vencimiento = date(fecha_vencimiento, '+'||?||' day') " +
                        "WHERE id=? AND estado='ABIERTO'")) {
                    ps.setInt(1, dias);
                    ps.setLong(2, idPrestamo);
                    if (ps.executeUpdate() == 0) throw new RuntimeException("No se pudo renovar");
                }

                insertAudit(cn, operador, "RENOVAR", codigo, idPrestamo, null, destinatario, "+" + dias + "d");
                cn.commit();
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally { cn.setAutoCommit(true); }
        } catch (SQLException e) {
            throw new RuntimeException("Error renovando: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Prestamo> abiertos(String filtro) {
        String sql = """
        SELECT p.* FROM prestamo p
        JOIN libro l ON l.codigo = p.libro_codigo
        WHERE p.estado='ABIERTO' AND (
              ? IS NULL OR ?='' OR
              l.titulo LIKE '%'||?||'%' OR l.autor LIKE '%'||?||'%' OR p.destinatario LIKE '%'||?||'%'
        )
        ORDER BY p.fecha_vencimiento ASC
        """;
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i=1;i<=5;i++) ps.setString(i, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                List<Prestamo> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando abiertos: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Prestamo> historico(LocalDate desde, LocalDate hasta, String filtro) {
        String sql = """
        SELECT p.* FROM prestamo p
        JOIN libro l ON l.codigo = p.libro_codigo
        WHERE (? IS NULL OR date(p.fecha_prestamo) >= ?)
          AND (? IS NULL OR date(p.fecha_prestamo) <= ?)
          AND (? IS NULL OR ?='' OR
               l.titulo LIKE '%'||?||'%' OR l.autor LIKE '%'||?||'%' OR p.destinatario LIKE '%'||?||'%')
        ORDER BY p.fecha_prestamo DESC
        """;
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, desde==null?null:desde.toString());
            ps.setString(2, desde==null?null:desde.toString());
            ps.setString(3, hasta==null?null:hasta.toString());
            ps.setString(4, hasta==null?null:hasta.toString());
            for (int i=5;i<=9;i++) ps.setString(i, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                List<Prestamo> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando histórico: " + e.getMessage(), e);
        }
    }

    // ===== helpers =====
    private boolean libroActivo(Connection cn, String codigo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT activo FROM libro WHERE codigo=?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getBoolean(1); }
        }
    }
    private boolean hayStockSuficiente(Connection cn, String codigo, int cant) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT stock FROM libro WHERE codigo=?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) >= cant; }
        }
    }
    private void insertAudit(Connection cn, String op, String tipo, String libro, Long prestamoId,
                             Integer cantidad, String dest, String detalle) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO auditoria (ts, operador_username, tipo, libro_codigo, prestamo_id, cantidad, destinatario, detalle) " +
                "VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setString(2, op);
            ps.setString(3, tipo);
            ps.setString(4, libro);
            if (prestamoId == null) ps.setNull(5, Types.INTEGER); else ps.setLong(5, prestamoId);
            if (cantidad   == null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, cantidad);
            ps.setString(7, dest);
            ps.setString(8, detalle);
            ps.executeUpdate();
        }
    }
    private Prestamo map(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setId(rs.getLong("id"));
        p.setLibroCodigo(rs.getString("libro_codigo"));
        p.setOperadorUsername(rs.getString("operador_username"));
        p.setDestinatario(rs.getString("destinatario"));
        p.setCantidad(rs.getInt("cantidad"));
        p.setFechaPrestamo(LocalDateTime.parse(rs.getString("fecha_prestamo")));
        p.setFechaVencimiento(LocalDate.parse(rs.getString("fecha_vencimiento")));
        String fdev = rs.getString("fecha_devolucion");
        p.setFechaDevolucion(fdev==null?null:LocalDateTime.parse(fdev));
        p.setEstado(Prestamo.Estado.valueOf(rs.getString("estado")));
        return p;
    }
}
