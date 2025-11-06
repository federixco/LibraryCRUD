package model;

/**
 * Usuario (abstracta)
 * - Define los campos comunes de un usuario de la app.
 * - passwordHash y salt almacenan el hash y la sal del password.
 */


public abstract class Usuario {
    private String id;           // UUID
    private String nombre;
    private String username;     // único
    private String passwordHash; // hex
    private String salt;         // hex
    private Rol rol;

    protected Usuario() {}

    public Usuario(String id, String nombre, String username, String passwordHash, String salt, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.rol = rol;
    }

    // Getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}