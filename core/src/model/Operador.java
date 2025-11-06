package model;



/** Operador: permisos limitados (p.ej., sin eliminar). */
public class Operador extends Usuario {
    public Operador(String id, String nombre, String username, String passwordHash, String salt) {
        super(id, nombre, username, passwordHash, salt, Rol.OPERADOR);
    }
}