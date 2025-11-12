package dao;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import db.ConnectionFactory;
import model.Libro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC del contrato {@link LibroDao} para SQLite.
 *
 * ¿Qué hace?
 *   - Ejecuta operaciones CRUD sobre la tabla libro.
 *   - Aplica baja lógica (columna activo) y validaciones de negocio simples
 *     en conjunto con la capa de servicio.
 *
 * Estructura esperada de la tabla 'libro':
 *   codigo TEXT PK,
 *   titulo TEXT,
 *   autor TEXT,
 *   categoria TEXT,
 *   editorial TEXT,
 *   anio INTEGER,
 *   stock INTEGER,
 *   activo INTEGER (0/1)
 *
 * Decisiones:
 *   - Manejo de recursos con try-with-resources.
 *   - Errores de acceso a datos envueltos en {@link RuntimeException}.
 *   - Mapeo directo ResultSet → {@link Libro}.
 */


public class JdbcLibroDAO implements LibroDao {

    /**
     * Inserta un libro nuevo.
     *
     * @param l entidad a persistir (no debe ser null; validado en Service).
     * @throws RuntimeException si ocurre un error SQL.
     */
    @Override
    public void crear(Libro l) {
        final String sql = """
                INSERT INTO libro
                  (codigo, titulo, autor, categoria, editorial, anio, stock, activo)
                VALUES (?,?,?,?,?,?,?,?)
                """;
        // 1) Abrir conexión y preparar sentencia
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Bind de parámetros
            ps.setString(1, l.getCodigo());
            ps.setString(2, l.getTitulo());
            ps.setString(3, l.getAutor());
            ps.setString(4, l.getCategoria());
            ps.setString(5, l.getEditorial());
            ps.setInt(6, l.getAnio());
            ps.setInt(7, l.getStock());
            ps.setBoolean(8, l.isActivo());

            // 3) Ejecutar
            ps.executeUpdate();

        } catch (SQLException e) {
            // 4) Reportar error de acceso a datos
            throw new RuntimeException("Error creando libro: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza todos los campos editables de un libro según su código.
     *
     * @param l entidad con datos actualizados.
     * @throws RuntimeException si no existe el código o ocurre un error SQL.
     */
    @Override
    public void actualizar(Libro l) {
        final String sql = """
                UPDATE libro SET
                   titulo=?, autor=?, categoria=?, editorial=?, anio=?, stock=?, activo=?
                WHERE codigo=?
                """;
        // 1) Abrir conexión y preparar sentencia
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Bind de parámetros (orden debe coincidir con el SQL)
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getCategoria());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getAnio());
            ps.setInt(6, l.getStock());
            ps.setBoolean(7, l.isActivo());
            ps.setString(8, l.getCodigo());

            // 3) Ejecutar y validar que afectó 1 fila
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("No existe el libro con código: " + l.getCodigo());
            }

        } catch (SQLException e) {
            // 4) Reportar error de acceso a datos
            throw new RuntimeException("Error actualizando libro: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina físicamente un libro por su código.
     * (Si preferís baja lógica, usar {@link #setActivo(String, boolean)}).
     *
     * @param codigo identificador del libro.
     * @throws RuntimeException si no existe o hay un error SQL.
     */
    @Override
    public void eliminar(String codigo) {
        final String sql = "DELETE FROM libro WHERE codigo=?";
        // 1) Abrir conexión y preparar sentencia
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Bind y ejecutar
            ps.setString(1, codigo);
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("No existe el libro con código: " + codigo);
            }

        } catch (SQLException e) {
            // 3) Reportar error
            throw new RuntimeException("Error eliminando libro: " + e.getMessage(), e);
        }
    }

    /**
     * Lee un libro por su código.
     *
     * @param codigo identificador a buscar.
     * @return el libro encontrado o null si no existe.
     * @throws RuntimeException si ocurre un error SQL.
     */
    @Override
    public Libro leerPorCodigo(String codigo) {
        final String sql = "SELECT * FROM libro WHERE codigo=?";
        // 1) Conectar y preparar
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Bind
            ps.setString(1, codigo);

            // 3) Ejecutar y mapear
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }

        } catch (SQLException e) {
            // 4) Reportar error
            throw new RuntimeException("Error leyendo libro: " + e.getMessage(), e);
        }
    }

    /**
     * Lista libros aplicando un filtro opcional por texto (título, autor o categoría).
     *
     * @param filtroTexto texto a buscar; puede ser null/"" para listar todo.
     * @return lista de libros ordenados por título (collate NOCASE).
     * @throws RuntimeException si ocurre un error SQL.
     */
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
        // 1) Conectar y preparar
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) 5 placeholders → 5 parámetros (mismo valor de filtro en todas las condiciones)
            ps.setString(1, filtroTexto); // ? IS NULL
            ps.setString(2, filtroTexto); // ? = ''
            ps.setString(3, filtroTexto); // titulo LIKE
            ps.setString(4, filtroTexto); // autor LIKE
            ps.setString(5, filtroTexto); // categoria LIKE

            // 3) Ejecutar y mapear
            try (ResultSet rs = ps.executeQuery()) {
                List<Libro> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }

        } catch (SQLException e) {
            // 4) Reportar error
            throw new RuntimeException("Error listando libros: " + e.getMessage(), e);
        }
    }

    // ================== Baja lógica / Reglas con préstamos ==================

    /**
     * Actualiza el flag de disponibilidad lógica del libro.
     *
     * @param codigo código del libro.
     * @param activo nuevo estado (true = activo, false = desactivado).
     * @throws RuntimeException si el libro no existe o hay error SQL.
     */
    @Override
    public void setActivo(String codigo, boolean activo) {
        final String sql = "UPDATE libro SET activo=? WHERE codigo=?";
        // 1) Conectar y preparar
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Bind y ejecutar
            ps.setBoolean(1, activo);
            ps.setString(2, codigo);
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("No existe el libro con código: " + codigo);
            }

        } catch (SQLException e) {
            // 3) Reportar error
            throw new RuntimeException("Error actualizando 'activo' del libro: " + e.getMessage(), e);
        }
    }

    /**
     * Indica si existen préstamos abiertos asociados a un libro.
     *
     * @param codigo código del libro.
     * @return true si hay al menos un préstamo con estado 'ABIERTO'.
     * @throws RuntimeException si hay error SQL.
     */
    @Override
    public boolean tienePrestamosAbiertos(String codigo) {
        final String sql = "SELECT COUNT(*) FROM prestamo WHERE libro_codigo=? AND estado='ABIERTO'";
        // 1) Conectar y preparar
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Bind y ejecutar
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            // 3) Reportar error
            throw new RuntimeException("Error verificando préstamos abiertos: " + e.getMessage(), e);
        }
    }

    // ================== Mapeo ==================

    /**
     * Convierte la fila actual del {@link ResultSet} a un {@link Libro}.
     *
     * @param rs result set posicionado.
     * @return instancia de Libro con todos los campos básicos.
     * @throws SQLException si falla la lectura de columnas.
     */
    private Libro map(ResultSet rs) throws SQLException {
        // 1) Construir y poblar DTO
        Libro l = new Libro();
        l.setCodigo(rs.getString("codigo"));
        l.setTitulo(rs.getString("titulo"));
        l.setAutor(rs.getString("autor"));
        l.setCategoria(rs.getString("categoria"));
        l.setEditorial(rs.getString("editorial"));
        l.setAnio(rs.getInt("anio"));
        l.setStock(rs.getInt("stock"));
        // SQLite guarda boolean como 0/1 → getBoolean lo interpreta correctamente
        l.setActivo(rs.getBoolean("activo"));
        return l;
    }
}
