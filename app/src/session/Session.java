package session;

import model.Rol;
import model.Usuario;

/**
 * Session
 * -------
 * Objeto simple que representa la **sesión activa** de la aplicación.
 *
 * ¿Qué guarda?
 *  - El {@link Usuario} autenticado (admin u operador).
 *
 * ¿Para qué sirve?
 *  - Consultar rápidamente el usuario logueado.
 *  - Preguntar por permisos (p. ej. `isAdmin()` en la UI para habilitar/ocultar acciones).
 *
 * Nota:
 *  - Mantiene solo datos en memoria; no persiste nada. La duración la gestiona la App.
 */
public class Session {

    /** Usuario autenticado asociado a esta sesión (inmutable). */
    private final Usuario usuario;

    /**
     * Crea una sesión para el usuario dado.
     * @param usuario entidad autenticada (no debe ser null).
     */
    public Session(Usuario usuario) {
        this.usuario = usuario;
    }

    /** @return el usuario logueado. */
    public Usuario getUsuario() {
        return usuario;
    }

    /** @return {@code true} si el usuario tiene rol ADMIN. */
    public boolean isAdmin() {
        // Comparar por enum es más seguro que por cadena.
        return usuario.getRol() == Rol.ADMIN;
    }
}
