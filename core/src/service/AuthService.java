package service;

import model.Usuario;

/**
 * Servicio de autenticación/registro/recuperación.
 */

public interface AuthService {
    Usuario login(String username, String passwordPlano);
    Usuario registrarOperador(String nombre, String username, String passwordPlano);
    //futuro String recuperarPasswordTemporal(String username);
}