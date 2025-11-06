package service;


import dao.UsuarioDao;
import model.*;
import util.HashUtil;
import java.util.UUID;



/**
 * Implementación de AuthService
 * - Hashea password con salt (SHA-256).
 * - Crea Operadores por defecto; Admines se seed-ean en DbInit.
 */
public class AuthServiceImpl implements AuthService {

    private final UsuarioDao usuarioDao;

    public AuthServiceImpl(UsuarioDao usuarioDAO) {
        this.usuarioDao = usuarioDAO;
    }

    @Override
    public Usuario login(String username, String passwordPlano) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Usuario requerido");
        if (passwordPlano == null) passwordPlano = "";
        Usuario u = usuarioDao.buscarPorUsername(username);
        if (u == null) throw new IllegalArgumentException("Usuario o contraseña inválidos");
        boolean ok = HashUtil.verify(u.getSalt(), passwordPlano, u.getPasswordHash());
        if (!ok) throw new IllegalArgumentException("Usuario o contraseña inválidos");
        return u;
    }

    @Override
    public Usuario registrarOperador(String nombre, String username, String passwordPlano) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username requerido");
        if (passwordPlano == null || passwordPlano.isBlank()) throw new IllegalArgumentException("Contraseña requerida");

        if (usuarioDao.buscarPorUsername(username) != null)
            throw new IllegalArgumentException("El username ya existe");

        String id = UUID.randomUUID().toString();
        String salt = HashUtil.newSaltHex(16);
        String hash = HashUtil.sha256Hex(salt, passwordPlano);
        Usuario op = new Operador(id, nombre, username, hash, salt);
        usuarioDao.crear(op);
        return op;
    }
}
