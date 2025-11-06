package session;

import model.Usuario;


/**
 * Modela la sesión de usuario logueado.
 */


public class Session {
    private final Usuario usuario;
    public Session(Usuario usuario) { this.usuario = usuario; }
    public Usuario getUsuario() { return usuario; }
    public boolean isAdmin() { return usuario.getRol().name().equals("ADMIN"); }
}