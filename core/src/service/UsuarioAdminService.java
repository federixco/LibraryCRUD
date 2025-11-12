package service;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import dao.UsuarioDao;
import model.*;
import util.HashUtil;

import java.util.List;
import java.util.UUID;

/**
 * Servicio con **acciones exclusivas de ADMIN** sobre usuarios.
 *
 * Responsabilidades:
 *  - Listar, crear operadores, eliminar usuarios, cambiar nombre/rol.
 *  - Resetear contraseña generando una temporal segura (salt + hash).
 *
 * Decisiones:
 *  - Validaciones de negocio aquí (unicidad, no auto-eliminarse, no borrar admin seed).
 *  - Hashing con {@link HashUtil} (salt aleatorio + SHA-256) —nunca almacenar contraseñas en claro.
 *  - Delegar persistencia en {@link UsuarioDao}.
 */
public class UsuarioAdminService {

    /** DAO inyectado: permite tests y cambiar implementación sin tocar la UI. */
    private final UsuarioDao dao;

    /** Inyección por constructor. */
    public UsuarioAdminService(UsuarioDao dao) {
        this.dao = dao;
    }

    /**
     * Lista todos los usuarios ordenados por username.
     * @return lista completa de {@link Usuario}.
     */
    public List<Usuario> listar() {
        return dao.listar();
    }

    /**
     * Crea un nuevo usuario con rol OPERADOR.
     *
     * Flujo:
     *  1) Validar datos y unicidad de username.
     *  2) Generar id (UUID), salt y hash.
     *  3) Persistir vía DAO.
     *
     * @param nombre nombre visible.
     * @param username username único.
     * @param passwordPlano contraseña en claro (se hashea acá).
     * @return operador creado.
     */
    public Usuario registrarOperador(String nombre, String username, String passwordPlano) {
        // Validaciones básicas
        if (nombre == null || nombre.isBlank())    throw new IllegalArgumentException("Nombre requerido");
        if (username == null || username.isBlank())throw new IllegalArgumentException("Username requerido");
        if (passwordPlano == null || passwordPlano.isBlank())
            throw new IllegalArgumentException("Contraseña requerida");
        if (dao.buscarPorUsername(username) != null)
            throw new IllegalArgumentException("El username ya existe");

        // Credenciales seguras
        String id   = UUID.randomUUID().toString();
        String salt = HashUtil.newSaltHex(16);
        String hash = HashUtil.sha256Hex(salt, passwordPlano);

        // Persistencia
        Usuario op = new Operador(id, nombre, username, hash, salt);
        dao.crear(op);
        return op;
    }

    /**
     * Elimina un usuario por username.
     * Reglas:
     *  - No se puede eliminar el admin “seed”.
     *  - Un admin no puede eliminarse a sí mismo.
     *
     * @param username usuario objetivo.
     * @param ejecutor username de quien ejecuta la acción (para impedir auto-eliminarse).
     */
    public void eliminar(String username, String ejecutor) {
        if ("admin".equalsIgnoreCase(username))
            throw new IllegalArgumentException("No se puede eliminar el admin de seed");
        if (ejecutor != null && ejecutor.equalsIgnoreCase(username))
            throw new IllegalArgumentException("No podés eliminar tu propio usuario");
        dao.eliminar(username);
    }

    /**
     * Actualiza el nombre visible y el rol.
     *
     * @param username     usuario objetivo.
     * @param nuevoNombre  nuevo nombre visible.
     * @param rol          rol destino (ADMIN/OPERADOR).
     */
    public void actualizarNombreYRol(String username, String nuevoNombre, Rol rol) {
        if (rol == null) throw new IllegalArgumentException("Rol requerido");
        dao.actualizarNombreYRol(username, nuevoNombre, rol.name());
    }

    /**
     * Resetea la contraseña generando una temporal (se devuelve para mostrarla una sola vez).
     *
     * Flujo:
     *  1) Verificar que el usuario existe.
     *  2) Generar temporal aleatoria sin caracteres ambiguos.
     *  3) Generar nuevo salt + hash y persistir.
     *  4) Devolver la temporal.
     *
     * @param username usuario objetivo.
     * @return contraseña temporal recién asignada.
     */
    public String resetearPasswordTemporal(String username) {
        Usuario u = dao.buscarPorUsername(username);
        if (u == null) throw new IllegalArgumentException("Usuario no encontrado");

        String temporal = generarPasswordTemporal(10);
        String salt = HashUtil.newSaltHex(16);
        String hash = HashUtil.sha256Hex(salt, temporal);
        dao.actualizarPassword(username, salt, hash);
        return temporal;
    }

    // ===================== Helpers =====================

    /**
     * Genera una contraseña temporal aleatoria evitando 0/O/I/l.
     * @param len longitud deseada.
     */
    private String generarPasswordTemporal(int len) {
        final String ab = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(len);
        java.util.concurrent.ThreadLocalRandom r = java.util.concurrent.ThreadLocalRandom.current();
        for (int i = 0; i < len; i++) sb.append(ab.charAt(r.nextInt(ab.length())));
        return sb.toString();
    }
}
