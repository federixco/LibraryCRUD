package dao;
import model.Usuario;
import java.util.List;


/**
 * DAO de Usuario: acceso a datos de usuarios.
 */

public interface UsuarioDao {
    void crear(Usuario u);
    Usuario buscarPorUsername(String user);
    void actualizarPassword(String username, String newSaltHex, String newHashHex);

    
    List<Usuario> listar();
    void eliminar(String username);
    void actualizarNombreYRol(String username, String nuevoNombre, String rol); // "ADMIN" | "OPERADOR"
}
