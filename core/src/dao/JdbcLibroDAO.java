package dao;

import db.ConnectionFactory;
import model.Libro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC de LibroDao para SQLite.
 * Estructura esperada de la tabla 'libro':
 *   codigo TEXT PK,
 *   titulo TEXT,
 *   autor TEXT,
 *   categoria TEXT,
 *   editorial TEXT,
 *   anio INTEGER,
 *   stock INTEGER,
 *   activo INTEGER (0/1)
 */
public class JdbcLibroDAO implements LibroDao {

    @Override
    public void crear(Libro l) {
        final String sql = """
                INSERT INTO libro
                  (codigo, titulo, autor, categoria, editorial, anio, stock, activo)
                VALUES (?,?,?,?,?,?,?,?)
                """;
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, l.getCodigo());
            ps.setString(2, l.getTitulo());
            ps.setString(3, l.getAutor());
            ps.setString(4, l.getCategoria());
            ps.setString(5, l.getEditorial());
            ps.setInt(6, l.getAnio());
            ps.setInt(7, l.getStock());
            ps.setBoolean(8, l.isActivo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creando libro: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Libro l) {
        final String sql = """
                UPDATE libro SET
                   titulo=?, autor=?, categoria=?, editorial=?, anio=?, stock=?, activo=?
                WHERE codigo=?
                """;
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getCategoria());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getAnio());
            ps.setInt(6, l.getStock());
            ps.setBoolean(7, l.isActivo());
            ps.setString(8, l.getCodigo());
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("No existe el libro con código: " + l.getCodigo());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando libro: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(String codigo) {
        final String sql = "DELETE FROM libro WHERE codigo=?";
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("No existe el libro con código: " + codigo);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando libro: " + e.getMessage(), e);
        }
    }

    @Override
    public Libro leerPorCodigo(String codigo) {
        final String sql = "SELECT * FROM libro WHERE codigo=?";
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo libro: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Libro> listar(String filtroTexto) {
        final String sql = """
                SELECT * FROM libro
                WHERE (? IS NULL OR ? = '' OR
                       titulo    LIKE '%'||?||'%' OR
                       autor     LIKE '%'||?||'%' OR
                       categoria LIKE '%'||?||'%')
                ORDER BY titulo COLLATE NOCASE
                """;
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 5 placeholders → 5 setString
            ps.setString(1, filtroTexto); // ? IS NULL
            ps.setString(2, filtroTexto); // ? = ''
            ps.setString(3, filtroTexto); // titulo LIKE
            ps.setString(4, filtroTexto); // autor LIKE
            ps.setString(5, filtroTexto); // categoria LIKE

            try (ResultSet rs = ps.executeQuery()) {
                List<Libro> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando libros: " + e.getMessage(), e);
        }
    }

    // ================== Baja lógica / Reglas con préstamos ==================

    @Override
    public void setActivo(String codigo, boolean activo) {
        final String sql = "UPDATE libro SET activo=? WHERE codigo=?";
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setString(2, codigo);
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("No existe el libro con código: " + codigo);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando 'activo' del libro: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean tienePrestamosAbiertos(String codigo) {
        final String sql = "SELECT COUNT(*) FROM prestamo WHERE libro_codigo=? AND estado='ABIERTO'";
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando préstamos abiertos: " + e.getMessage(), e);
        }
    }

    // ================== Mapeo ==================
    private Libro map(ResultSet rs) throws SQLException {
        Libro l = new Libro();
        l.setCodigo(rs.getString("codigo"));
        l.setTitulo(rs.getString("titulo"));
        l.setAutor(rs.getString("autor"));
        l.setCategoria(rs.getString("categoria"));
        l.setEditorial(rs.getString("editorial"));
        l.setAnio(rs.getInt("anio"));
        l.setStock(rs.getInt("stock"));
        l.setActivo(rs.getBoolean("activo")); // en SQLite 0/1 -> boolean
        return l;
    }
}
