package model;

/**
 * Clase base **abstracta** para los usuarios del sistema.
 *
 * ¿Qué modela?
 *  - Identidad y credenciales persistidas: `id`, `username`, `passwordHash`, `salt`, `rol`.
 *  - Datos visibles: `nombre`.
 *
 * Seguridad:
 *  - **Nunca** se guarda el password en texto plano. Se almacena `passwordHash` (hex) y `salt` (hex).
 *  - La verificación se hace en la capa de servicio con el `salt` + algoritmo (p.ej. SHA-256).
 *
 * Jerarquía:
 *  - Subclases concretas: {@link Admin} y {@link Operador}.
 *
 * Invariantes recomendadas (validadas en servicio):
 *  - `username` único y no vacío.
 *  - `rol` no nulo.
 *  - `passwordHash` y `salt` no nulos.
 */


public abstract class Usuario {

    /** Identificador interno (UUID u otro). */
    private String id;

    /** Nombre visible del usuario. */
    private String nombre;

    /** Nombre de usuario único para login. */
    private String username;

    /** Hash de la contraseña (hex). Nunca almacenar la contraseña en claro. */
    private String passwordHash;

    /** Salt usado al generar el hash (hex). */
    private String salt;

    /** Rol de acceso (ADMIN / OPERADOR). */
    private Rol rol;

    /** Constructor protegido sin args para frameworks/ORM/serialización. */
    protected Usuario() {}

    /**
     * Construye un usuario con todos los campos.
     *
     * @param id           identificador interno.
     * @param nombre       nombre visible.
     * @param username     nombre de usuario único.
     * @param passwordHash hash de la contraseña en hex (derivado de salt+password).
     * @param salt         salt en hex usado para el hash.
     * @param rol          rol del usuario (no debe ser null).
     */
    public Usuario(String id, String nombre, String username, String passwordHash, String salt, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.rol = rol;
    }

    // ===================== Getters / Setters =====================

    /** @return identificador interno del usuario. */
    public String getId() { return id; }

    /** @param id identificador interno (UUID recomendado). */
    public void setId(String id) { this.id = id; }

    /** @return nombre visible del usuario. */
    public String getNombre() { return nombre; }

    /** @param nombre nombre visible. */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** @return username único para login. */
    public String getUsername() { return username; }

    /** @param username nombre de usuario (no debería ser vacío). */
    public void setUsername(String username) { this.username = username; }

    /** @return hash de contraseña (hex). */
    public String getPasswordHash() { return passwordHash; }

    /** @param passwordHash hash de contraseña (hex). */
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    /** @return salt usado para el hash (hex). */
    public String getSalt() { return salt; }

    /** @param salt salt (hex) asociado a este usuario. */
    public void setSalt(String salt) { this.salt = salt; }

    /** @return rol del usuario. */
    public Rol getRol() { return rol; }

    /** @param rol rol del usuario (ADMIN / OPERADOR). */
    public void setRol(Rol rol) { this.rol = rol; }
}
