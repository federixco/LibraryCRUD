package model;

/**
 * Admin
 * -----
 * Subtipo de {@link Usuario} que representa a un usuario con permisos completos del sistema.
 *
 * ¿Para qué existe?
 *  - Diferenciar explícitamente el rol ADMIN en la jerarquía de usuarios.
 *  - Facilitar chequeos de permisos (p.ej. `session.isAdmin()` en la UI/servicios).
 *
 * Decisión:
 *  - El rol se fija a {@link Rol#ADMIN} desde el constructor y no puede alterarse.
 */
public class Admin extends Usuario {

    /**
     * Construye un usuario administrador.
     *
     * @param id           identificador interno (UUID u otro).
     * @param nombre       nombre visible.
     * @param username     nombre de usuario único para login.
     * @param passwordHash hash de contraseña (hex).
     * @param salt         salt usado para el hash (hex).
     */
    public Admin(String id, String nombre, String username, String passwordHash, String salt) {
        super(id, nombre, username, passwordHash, salt, Rol.ADMIN);
    }
}
