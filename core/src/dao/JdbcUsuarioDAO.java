package dao;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import db.ConnectionFactory;
import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC del contrato {@link UsuarioDao} para la tabla <code>usuario</code>.
 *
 * ¿Qué hace?
 *   - CRUD y consultas comunes de usuarios (admin y operador).
 *   - Mapea filas → jerarquía {@link Usuario} concreta ({@link Admin} / {@link Operador})
 *     en función del campo <code>rol</code>.
 *
 * Decisiones:
 *   - Manejo de recursos con try-with-resources.
 *   - Errores SQL envueltos en {@link RuntimeException} para simplificar firmas.
 *   - Los métodos asumen validaciones previas en capa de servicio (formatos, unicidad, etc.).
 */


public class JdbcUsuarioDAO implements UsuarioDao {

    /**
     * Inserta un usuario nuevo.
     *
     * @param u entidad ya construida con id, nombre, username, hash, salt y rol.
     * @throws RuntimeException si falla el INSERT (p. ej. violación de UNIQUE).
     */
    @Override
    public void crear(Usuario u) {
        final String sql = "INSERT INTO usuario (id, nombre, username, password_hash, salt, rol) VALUES (?,?,?,?,?,?)";
        // 1) Abrir conexión y preparar sentencia
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Bind de parámetros (orden debe coincidir con el SQL)
            ps.setString(1, u.getId());
            ps.setString(2, u.getNombre());
            ps.setString(3, u.getUsername());
            ps.setString(4, u.getPasswordHash());
            ps.setString(5, u.getSalt());
            ps.setString(6, u.getRol().name());

            // 3) Ejecutar
            ps.executeUpdate();

        } catch (SQLException e) {
            // 4) Reportar error de acceso a datos
            throw new RuntimeException("Error creando usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Busca un usuario por su username.
     *
     * @param username identificador lógico único.
     * @return instancia de {@link Usuario} (Admin/Operador) o null si no existe.
     */
    @Override
    public Usuario buscarPorUsername(String username) {
        final String sql = "SELECT * FROM usuario WHERE username = ?";
        // 1) Conectar y preparar
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Bind y ejecutar
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                // 3) Mapear si hay fila
                if (!rs.next()) return null;
                return map(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza el par (salt, password_hash) de un usuario.
     *
     * @param username username del usuario a afectar.
     * @param newSaltHex nuevo salt (hex).
     * @param newHashHex nuevo hash (hex) de la contraseña.
     * @throws RuntimeException si no existe el usuario o falla el UPDATE.
     */
    @Override
    public void actualizarPassword(String username, String newSaltHex, String newHashHex) {
        final String sql = "UPDATE usuario SET salt=?, password_hash=? WHERE username=?";
        // 1) Conectar y preparar
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Bind y ejecutar
            ps.setString(1, newSaltHex);
            ps.setString(2, newHashHex);
            ps.setString(3, username);
            if (ps.executeUpdate() == 0)
                throw new RuntimeException("No existe el usuario: " + username);

        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando password: " + e.getMessage(), e);
        }
    }

    // --- NUEVO ---

    /**
     * Lista todos los usuarios ordenados por username.
     *
     * @return lista de {@link Usuario}.
     */
    @Override
    public List<Usuario> listar() {
        final String sql = "SELECT * FROM usuario ORDER BY username";
        // 1) Conectar y preparar
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // 2) Recorrer y mapear filas
            List<Usuario> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("Error listando usuarios: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina físicamente un usuario por username.
     *
     * @param username clave lógica única.
     * @throws RuntimeException si falla el DELETE (p. ej. FK) o hay error SQL.
     */
    @Override
    public void eliminar(String username) {
        final String sql = "DELETE FROM usuario WHERE username = ?";
        // 1) Conectar y preparar
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Bind y ejecutar (si afecta 0 filas, no existe)
            ps.setString(1, username);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza nombre y rol de un usuario.
     *
     * @param username username del usuario a modificar.
     * @param nuevoNombre nuevo nombre visible.
     * @param rol nombre del rol (string) —se recomienda pasar {@link Rol#name()} desde servicio.
     * @throws RuntimeException si no existe el usuario o falla el UPDATE.
     */
    @Override
    public void actualizarNombreYRol(String username, String nuevoNombre, String rol) {
        final String sql = "UPDATE usuario SET nombre=?, rol=? WHERE username=?";
        // 1) Conectar y preparar
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Bind y ejecutar
            ps.setString(1, nuevoNombre);
            ps.setString(2, rol);
            ps.setString(3, username);
            if (ps.executeUpdate() == 0)
                throw new RuntimeException("No existe el usuario: " + username);

        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Busca usuarios por coincidencia en el nombre (LIKE %patrón%).
     *
     * @param patron texto a buscar; si es null se toma como "".
     * @return lista de coincidencias ordenadas por username.
     */
    @Override
    public List<Usuario> buscarPorNombreLike(String patron) {
        final String sql = "SELECT * FROM usuario WHERE nombre LIKE ? ORDER BY username";
        // 1) Conectar y preparar
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Bind patrón con comodines
            ps.setString(1, "%" + (patron == null ? "" : patron.trim()) + "%");

            // 3) Ejecutar y mapear
            try (ResultSet rs = ps.executeQuery()) {
                List<Usuario> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando usuarios por nombre: " + e.getMessage(), e);
        }
    }

    // ================== Mapeo fila → Usuario concreto ==================

    /**
     * Convierte la fila actual del {@link ResultSet} en un {@link Usuario} concreto.
     * Si <code>rol</code> es ADMIN devuelve {@link Admin}; caso contrario {@link Operador}.
     *
     * @param rs result set posicionado en una fila válida.
     * @return instancia concreta de Usuario.
     * @throws SQLException si falla la lectura de columnas.
     */
    private Usuario map(ResultSet rs) throws SQLException {
        // 1) Leer columnas base
        String id    = rs.getString("id");
        String nombre= rs.getString("nombre");
        String user  = rs.getString("username");
        String hash  = rs.getString("password_hash");
        String salt  = rs.getString("salt");
        Rol rol      = Rol.valueOf(rs.getString("rol"));

        // 2) Crear subtipo según rol
        return (rol == Rol.ADMIN)
                ? new Admin(id, nombre, user, hash, salt)
                : new Operador(id, nombre, user, hash, salt);
    }
}
