package service;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import dao.UsuarioDao;
import model.Operador;
import model.Usuario;
import util.HashUtil;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementación del contrato {@link AuthService} para autenticación,
 * registro y manejo de contraseñas.
 *
 * Responsabilidades:
 *  - Autenticar usuarios verificando hash+salt.
 *  - Registrar operadores (crea salt y hash seguros).
 *  - Generar y persistir contraseñas temporales (flujo “Olvidé mi contraseña”).
 *  - Cambiar contraseña validando primero la actual.
 *
 * Decisiones:
 *  - Se delega el acceso a datos en {@link UsuarioDao} (patrón DAO).
 *  - Se usa {@link HashUtil} para hashing con salt (SHA-256).
 *  - Los errores de validación se comunican con {@link IllegalArgumentException}
 *    para mantener firmas simples en la capa de servicio.
 */


public class AuthServiceImpl implements AuthService {

    /** DAO de usuarios (inyectado por constructor). */
    private final UsuarioDao usuarioDao;

    /**
     * Crea el servicio de autenticación con su DAO de usuarios.
     * @param usuarioDAO implementación concreta de {@link UsuarioDao}
     */
    public AuthServiceImpl(UsuarioDao usuarioDAO) {
        this.usuarioDao = usuarioDAO;
    }

    /**
     * Autentica al usuario verificando el hash almacenado con el salt de la base.
     *
     * Flujo:
     *  1) Validar parámetros mínimos.
     *  2) Buscar usuario por username.
     *  3) Verificar hash: sha256(salt + passwordPlano) == password_hash_guardado.
     *  4) Devolver la entidad {@link Usuario} si es correcto.
     *
     * @param username nombre de usuario.
     * @param passwordPlano contraseña en claro (solo en este punto).
     * @return el usuario autenticado.
     * @throws IllegalArgumentException si usuario o contraseña son inválidos.
     */
    @Override
    public Usuario login(String username, String passwordPlano) {
        // 1) Validaciones mínimas
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Usuario requerido");
        if (passwordPlano == null) passwordPlano = "";

        // 2) Buscar usuario
        Usuario u = usuarioDao.buscarPorUsername(username);
        if (u == null) throw new IllegalArgumentException("Usuario o contraseña inválidos");

        // 3) Verificar hash con salt
        boolean ok = HashUtil.verify(u.getSalt(), passwordPlano, u.getPasswordHash());
        if (!ok) throw new IllegalArgumentException("Usuario o contraseña inválidos");

        // 4) OK → devolver entidad de dominio
        return u;
    }

    /**
     * Registra un nuevo usuario con rol OPERADOR.
     *
     * Flujo:
     *  1) Validar datos de entrada.
     *  2) Verificar unicidad de username.
     *  3) Generar id (UUID), salt y hash de la contraseña.
     *  4) Persistir mediante DAO.
     *
     * @param nombre nombre visible.
     * @param username username único.
     * @param passwordPlano contraseña en claro.
     * @return el nuevo operador creado.
     * @throws IllegalArgumentException si faltan datos o el username ya existe.
     */
    @Override
    public Usuario registrarOperador(String nombre, String username, String passwordPlano) {
        // 1) Validaciones
        if (nombre == null || nombre.isBlank())    throw new IllegalArgumentException("Nombre requerido");
        if (username == null || username.isBlank())throw new IllegalArgumentException("Username requerido");
        if (passwordPlano == null || passwordPlano.isBlank())
            throw new IllegalArgumentException("Contraseña requerida");

        // 2) Unicidad
        if (usuarioDao.buscarPorUsername(username) != null)
            throw new IllegalArgumentException("El username ya existe");

        // 3) Generar credenciales seguras
        String id   = UUID.randomUUID().toString();
        String salt = HashUtil.newSaltHex(16);
        String hash = HashUtil.sha256Hex(salt, passwordPlano);

        // 4) Persistir
        Usuario op = new Operador(id, nombre, username, hash, salt);
        usuarioDao.crear(op);
        return op;
    }

    /**
     * Genera una contraseña temporal, la persiste (salt+hash nuevos) y la retorna.
     * Se usa en el flujo “Olvidé mi contraseña”.
     *
     * Flujo:
     *  1) Validar username y buscar usuario.
     *  2) Generar contraseña temporal aleatoria.
     *  3) Generar nuevo salt y hash para esa temporal.
     *  4) Persistir (update) y devolver la temporal para que la vea el usuario.
     *
     * @param username username del usuario.
     * @return contraseña temporal generada (mostrarla una sola vez).
     * @throws IllegalArgumentException si el usuario no existe.
     */
    @Override
    public String recuperarPasswordTemporal(String username) {
        // 1) Validación + búsqueda
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Usuario requerido");
        Usuario u = usuarioDao.buscarPorUsername(username);
        if (u == null) throw new IllegalArgumentException("Usuario no encontrado");

        // 2) Generar temporal aleatoria
        String temporal = generarPasswordTemporal(10);

        // 3) Re-hashear con nuevo salt
        String newSalt = HashUtil.newSaltHex(16);
        String newHash = HashUtil.sha256Hex(newSalt, temporal);

        // 4) Persistir
        usuarioDao.actualizarPassword(username, newSalt, newHash);

        // 5) Devolver la temporal (la UI debería forzar cambio al ingresar)
        return temporal;
    }

    /**
     * Cambia la contraseña de un usuario validando primero la actual (que puede ser temporal).
     *
     * Flujo:
     *  1) Validar parámetros.
     *  2) Buscar usuario.
     *  3) Verificar la contraseña actual contra el hash+salt guardados.
     *  4) Generar nuevo salt + hash para la nueva contraseña y persistir.
     *
     * @param username    username del usuario.
     * @param actualPlano contraseña actual (puede ser la temporal).
     * @param nuevaPlano  nueva contraseña a establecer.
     * @throws IllegalArgumentException si el usuario no existe, la actual no coincide o la nueva es inválida.
     */
    @Override
    public void cambiarPassword(String username, String actualPlano, String nuevaPlano) {
        // 1) Validaciones
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Usuario requerido");
        if (nuevaPlano == null || nuevaPlano.isBlank())
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");

        // 2) Buscar usuario
        Usuario u = usuarioDao.buscarPorUsername(username);
        if (u == null) throw new IllegalArgumentException("Usuario no encontrado");

        // 3) Verificar contraseña actual (normal o temporal)
        if (actualPlano == null) actualPlano = "";
        boolean ok = HashUtil.verify(u.getSalt(), actualPlano, u.getPasswordHash());
        if (!ok) throw new IllegalArgumentException("La contraseña actual es incorrecta");

        // 4) Generar nuevo salt + hash y persistir
        String newSalt = HashUtil.newSaltHex(16);
        String newHash = HashUtil.sha256Hex(newSalt, nuevaPlano);
        usuarioDao.actualizarPassword(username, newSalt, newHash);
    }

    // ===================== Helpers =====================

    /**
     * Genera una contraseña temporal aleatoria evitando caracteres ambiguos.
     * Alfabeto sin 0/O/I/l para reducir confusiones al leer.
     *
     * @param len longitud deseada.
     * @return cadena aleatoria segura para uso temporal.
     */
    private String generarPasswordTemporal(int len) {
        final String ab = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(len);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < len; i++) sb.append(ab.charAt(r.nextInt(ab.length())));
        return sb.toString();
    }
}
