package dao;

import model.Usuario;
import java.util.List;

public interface UsuarioDao {
    void crear(Usuario u);
    Usuario buscarPorUsername(String user);
    void actualizarPassword(String username, String newSaltHex, String newHashHex);

    // ya tenías:
    List<Usuario> listar();
    void eliminar(String username);
    void actualizarNombreYRol(String username, String rol, String nuevoNombre); // si tu firma era al revés, mantenela

    // NUEVO: para “recuperar usuario” por nombre (like)
    List<Usuario> buscarPorNombreLike(String patron);
}
