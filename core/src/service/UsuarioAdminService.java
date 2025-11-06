package service;

import dao.UsuarioDao;
import model.*;
import util.HashUtil;

import java.util.List;
import java.util.UUID;

/**
 * UsuarioAdminService
 * - Acciones que solo puede hacer un ADMIN.
 */

public class UsuarioAdminService {

    private final UsuarioDao dao;

    public UsuarioAdminService(UsuarioDao dao) {
        this.dao = dao;
    }

    public List<Usuario> listar() {
        return dao.listar();
    }

    public Usuario registrarOperador(String nombre, String username, String passwordPlano) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username requerido");
        if (passwordPlano == null || passwordPlano.isBlank()) throw new IllegalArgumentException("Contraseña requerida");
        if (dao.buscarPorUsername(username) != null) throw new IllegalArgumentException("El username ya existe");

        String id = UUID.randomUUID().toString();
        String salt = HashUtil.newSaltHex(16);
        String hash = HashUtil.sha256Hex(salt, passwordPlano);
        Usuario op = new Operador(id, nombre, username, hash, salt);
        dao.crear(op);
        return op;
    }

    public void eliminar(String username, String ejecutor) {
        if ("admin".equalsIgnoreCase(username)) throw new IllegalArgumentException("No se puede eliminar el admin de seed");
        if (ejecutor != null && ejecutor.equalsIgnoreCase(username))
            throw new IllegalArgumentException("No podés eliminar tu propio usuario");
        dao.eliminar(username);
    }

    public void actualizarNombreYRol(String username, String nuevoNombre, Rol rol) {
        if (rol == null) throw new IllegalArgumentException("Rol requerido");
        dao.actualizarNombreYRol(username, nuevoNombre, rol.name());
    }

    public String resetearPasswordTemporal(String username) {
        Usuario u = dao.buscarPorUsername(username);
        if (u == null) throw new IllegalArgumentException("Usuario no encontrado");
        String temporal = generarPasswordTemporal(10);
        String salt = HashUtil.newSaltHex(16);
        String hash = HashUtil.sha256Hex(salt, temporal);
        dao.actualizarPassword(username, salt, hash);
        return temporal;
    }

    private String generarPasswordTemporal(int len) {
        final String ab = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(len);
        java.util.concurrent.ThreadLocalRandom r = java.util.concurrent.ThreadLocalRandom.current();
        for (int i = 0; i < len; i++) sb.append(ab.charAt(r.nextInt(ab.length())));
        return sb.toString();
    }
}
