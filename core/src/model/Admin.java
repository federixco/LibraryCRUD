package model;


/** Admin: usuario con permisos completos. */
public class Admin extends Usuario {
    public Admin(String id, String nombre, String username, String passwordHash, String salt) {
        super(id, nombre, username, passwordHash, salt, Rol.ADMIN);
    }
}