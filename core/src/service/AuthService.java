package service;

import model.Usuario;

/**
 * Servicio de autenticación/registro/recuperación.
 */

public interface AuthService {
    Usuario login(String username, String passwordPlano);
    Usuario registrarOperador(String nombre, String username, String passwordPlano);
    String recuperarPasswordTemporal(String username);
    
    void cambiarPassword(String username, String actualPlano, String nuevaPlano);
}