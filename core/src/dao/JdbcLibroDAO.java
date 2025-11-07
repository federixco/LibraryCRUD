package dao;

import db.ConnectionFactory;
import model.Libro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Clase: JdbcLibroDAO
 * ----------------------------
 * Propósito:
 *  - Implementar LibroDAO usando JDBC puro (SQLite o MySQL).
 *  - Traducir filas (ResultSet) a objetos Libro y viceversa.
 *
 * Notas:
 *  - Manejo de errores simple: wrappeamos SQLException en RuntimeException para simplificar el integrador.
 *  - Cada método abre su conexión con try-with-resources (se cierra solo al terminar).
 */


public class JdbcLibroDAO implements LibroDao {

    @Override
    public void crear(Libro l) {
        // SQL parametrizado con placeholders (?) para evitar inyección y manejar tipos.
        final String sql = "INSERT INTO libro (codigo, titulo, autor, categoria, editorial, anio, stock, activo) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        // try-with-resources: abre conexión y PreparedStatement y los cierra automáticamente.
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // Mapear campos del objeto Libro → parámetros del INSERT
            ps.setString(1, l.getCodigo());
            ps.setString(2, l.getTitulo());
            ps.setString(3, l.getAutor());
            ps.setString(4, l.getCategoria());
            ps.setString(5, l.getEditorial());
            ps.setInt(6, l.getAnio());
            ps.setInt(7, l.getStock());
            ps.setInt(8, l.isActivo() ? 1 : 0); // SQLite no tiene boolean nativo

            // Ejecutar el INSERT
            ps.executeUpdate();

        } catch (SQLException e) {
            // Convertimos la checked exception en unchecked para no ensuciar firmas de métodos.
            throw new RuntimeException("Error creando libro: " + e.getMessage(), e);
        }
    }

    @Override
    public Libro leerPorCodigo(String codigo) {
        final String sql = "SELECT * FROM libro WHERE codigo = ?";

        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // Asignar el valor del parámetro de búsqueda
            ps.setString(1, codigo);

            // Ejecutar consulta y obtener ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                // Si hay fila, la mapeamos a un objeto Libro
                if (rs.next()) return mapRow(rs);
                // Si no hay resultados, devolvemos null
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo libro: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Libro l) {
        final String sql = "UPDATE libro SET titulo=?, autor=?, categoria=?, editorial=?, anio=?, stock=?, activo=? " +
                           "WHERE codigo=?";

        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // Seteamos los nuevos valores en el mismo orden que los ? del SQL
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getCategoria());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getAnio());
            ps.setInt(6, l.getStock());
            ps.setInt(7, l.isActivo() ? 1 : 0);
            ps.setString(8, l.getCodigo());

            // Ejecutar UPDATE
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando libro: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(String codigo) {
        final String sql = "DELETE FROM libro WHERE codigo = ?";

        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // El libro a eliminar se identifica por su código
            ps.setString(1, codigo);

            // Ejecutar DELETE
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando libro: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Libro> listar(String filtroTexto) {
        // Armamos el SQL en dos partes para agregar WHERE solo si hay filtro.
        final String base = "SELECT * FROM libro";
        final boolean hayFiltro = (filtroTexto != null && !filtroTexto.isBlank());
        final String where = hayFiltro ? " WHERE titulo LIKE ? OR autor LIKE ? OR categoria LIKE ?" : "";
        final String order = " ORDER BY titulo ASC";
        final String sql = base + where + order;

        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // Si hay filtro, seteamos el mismo valor (con %) en los tres campos consultados
            if (hayFiltro) {
                String like = "%" + filtroTexto.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }

            // Ejecutamos y transformamos todas las filas a objetos Libro
            try (ResultSet rs = ps.executeQuery()) {
                List<Libro> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando libros: " + e.getMessage(), e);
        }
    }

    // --- Método utilitario privado: convierte una fila del ResultSet en un Libro ---
    private Libro mapRow(ResultSet rs) throws SQLException {
        // Extraemos cada columna por nombre y construimos el objeto
        return new Libro(
            rs.getString("codigo"),
            rs.getString("titulo"),
            rs.getString("autor"),
            rs.getString("categoria"),
            rs.getString("editorial"),
            rs.getInt("anio"),
            rs.getInt("stock"),
            rs.getInt("activo") == 1 // convertimos 0/1 a boolean
        );
    }
}