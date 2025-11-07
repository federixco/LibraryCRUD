package service;

import dao.UsuarioDao;
import model.Operador;
import model.Usuario;
import util.HashUtil;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
        if (usuarioDao.buscarPorUsername(username) != null) throw new IllegalArgumentException("El username ya existe");

        String id = UUID.randomUUID().toString();
        String salt = HashUtil.newSaltHex(16);
        String hash = HashUtil.sha256Hex(salt, passwordPlano);
        Usuario op = new Operador(id, nombre, username, hash, salt);
        usuarioDao.crear(op);
        return op;
    }

    @Override
    public String recuperarPasswordTemporal(String username) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Usuario requerido");
        Usuario u = usuarioDao.buscarPorUsername(username);
        if (u == null) throw new IllegalArgumentException("Usuario no encontrado");

        String temporal = generarPasswordTemporal(10);
        String newSalt = HashUtil.newSaltHex(16);
        String newHash = HashUtil.sha256Hex(newSalt, temporal);
        usuarioDao.actualizarPassword(username, newSalt, newHash);
        return temporal;
    }

    @Override
    public void cambiarPassword(String username, String actualPlano, String nuevaPlano) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Usuario requerido");
        if (nuevaPlano == null || nuevaPlano.isBlank()) throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");

        Usuario u = usuarioDao.buscarPorUsername(username);
        if (u == null) throw new IllegalArgumentException("Usuario no encontrado");

        // Validar la contraseña actual (puede ser temporal o la “normal”)
        if (actualPlano == null) actualPlano = "";
        boolean ok = HashUtil.verify(u.getSalt(), actualPlano, u.getPasswordHash());
        if (!ok) throw new IllegalArgumentException("La contraseña actual es incorrecta");

        // Generar nueva salt + hash para la nueva contraseña
        String newSalt = HashUtil.newSaltHex(16);
        String newHash = HashUtil.sha256Hex(newSalt, nuevaPlano);
        usuarioDao.actualizarPassword(username, newSalt, newHash);
    }

    // === helpers ===
    private String generarPasswordTemporal(int len) {
        final String ab = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(len);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < len; i++) sb.append(ab.charAt(r.nextInt(ab.length())));
        return sb.toString();
    }
}
