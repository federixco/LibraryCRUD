package model;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

/**
 * Subtipo de {@link Usuario} con permisos limitados (p.ej. no puede eliminar registros).
 *
 * ¿Para qué existe?
 *  - Representar al usuario habitual del sistema (carga/edita, presta/devuelve, consulta).
 *  - Facilitar chequeos de permisos (p.ej. en UI/servicios: if (!session.isAdmin()) ...).
 *
 * Decisión:
 *  - El rol se fija a {@link Rol#OPERADOR} desde el constructor y no puede alterarse.
 */

public class Operador extends Usuario {

    /**
     * Construye un usuario operador.
     *
     * @param id           identificador interno (UUID u otro).
     * @param nombre       nombre visible.
     * @param username     nombre de usuario único para login.
     * @param passwordHash hash de contraseña (hex).
     * @param salt         salt usado para el hash (hex).
     */
    public Operador(String id, String nombre, String username, String passwordHash, String salt) {
        super(id, nombre, username, passwordHash, salt, Rol.OPERADOR);
    }
}
