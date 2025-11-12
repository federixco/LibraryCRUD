package dao;

import model.Usuario;
import java.util.List;

/**
 * Contrato de acceso a datos para la entidad {@link Usuario}.
 *
 * ¿Qué hace?
 *  - Define operaciones de creación, consulta y mantenimiento de credenciales/rol.
 *  - Expone listados y búsquedas auxiliares para administración.
 *
 * ¿Por qué interfaz?
 *  - Desacopla la capa de servicio de la tecnología de persistencia (JDBC, mocks para tests, etc.).
 *  - Permite intercambiar implementaciones sin romper el resto del sistema (patrón DAO).
 */
public interface UsuarioDao {

    /**
     * Inserta un usuario nuevo.
     *
     * @param u entidad ya construida con id, nombre, username, hash, salt y rol.
     * @throws RuntimeException si ocurre un error de acceso a datos (UNIQUE, FK, etc.).
     */
    void crear(Usuario u);

    /**
     * Busca un usuario por su username.
     *
     * @param user username lógico único.
     * @return instancia de {@link Usuario} o {@code null} si no existe.
     * @throws RuntimeException si ocurre un error de acceso a datos.
     */
    Usuario buscarPorUsername(String user);

    /**
     * Actualiza el par (salt, password_hash) de un usuario.
     *
     * @param username username del usuario.
     * @param newSaltHex nuevo salt en hex.
     * @param newHashHex nuevo hash en hex.
     * @throws RuntimeException si no existe el usuario o falla el UPDATE.
     */
    void actualizarPassword(String username, String newSaltHex, String newHashHex);

    /**
     * Lista todos los usuarios ordenados por username.
     *
     * @return lista de usuarios.
     * @throws RuntimeException si ocurre un error de acceso a datos.
     */
    List<Usuario> listar();

    /**
     * Elimina físicamente un usuario por username.
     *
     * @param username clave lógica única.
     * @throws RuntimeException si falla el DELETE.
     */
    void eliminar(String username);

    /**
     * Actualiza el nombre visible y el rol de un usuario.
     * <p><b>Importante:</b> firma alineada con JdbcUsuarioDAO → (username, nuevoNombre, rol).</p>
     *
     * @param username username del usuario a modificar.
     * @param nuevoNombre nuevo nombre a mostrar.
     * @param rol rol destino (usar {@code Rol.name()} desde capa de servicio).
     * @throws RuntimeException si no existe el usuario o falla el UPDATE.
     */
    void actualizarNombreYRol(String username, String nuevoNombre, String rol);

    /**
     * Busca usuarios por coincidencia en el nombre (LIKE %patrón%).
     *
     * @param patron texto a buscar; {@code null} se trata como cadena vacía.
     * @return lista de coincidencias.
     * @throws RuntimeException si ocurre un error de acceso a datos.
     */
    List<Usuario> buscarPorNombreLike(String patron);
}
