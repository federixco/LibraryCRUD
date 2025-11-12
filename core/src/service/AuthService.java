package service;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import model.Usuario;

/**
  * Servicio de autenticación y gestión básica de credenciales.
 *
 * ¿Qué hace?
 *  - Autentica usuarios contra las credenciales persistidas.
 *  - Registra operadores (alta con hash + salt).
 *  - Recupera contraseña generando una temporal segura.
 *  - Permite cambio de contraseña verificando la actual.
 *
 * Seguridad (contrato general):
 *  - Nunca manejar contraseñas en claro fuera del método que las recibe.
 *  - Las implementaciones deben hashear con salt (p. ej. SHA-256) y comparar por hash.
 *  - Evitar mensajes de error que filtren si el usuario existe o no.
 */





public interface AuthService {

    /**
     * Autentica un usuario por username y contraseña en texto plano.
     *
     * @param username       nombre de usuario.
     * @param passwordPlano  contraseña ingresada (en claro, solo en este punto).
     * @return {@link Usuario} autenticado si las credenciales son correctas.
     * @throws IllegalArgumentException si usuario o contraseña no son válidos.
     */
    Usuario login(String username, String passwordPlano);

    /**
     * Registra un nuevo operador (ROL=OPERADOR) con hash y salt.
     *
     * @param nombre         nombre visible.
     * @param username       username único.
     * @param passwordPlano  contraseña en claro (se hashea internamente).
     * @return usuario creado.
     * @throws IllegalArgumentException si los datos son inválidos o el username ya existe.
     */
    Usuario registrarOperador(String nombre, String username, String passwordPlano);

    /**
     * Genera una contraseña temporal para el usuario y la persiste (salt + hash).
     * Usos típicos: “Olvidé mi contraseña”.
     *
     * @param username username del usuario.
     * @return la contraseña temporal generada (debe mostrarse una sola vez al usuario).
     * @throws IllegalArgumentException si el usuario no existe.
     */
    String recuperarPasswordTemporal(String username);

    /**
     * Cambia la contraseña de un usuario verificando primero la actual.
     *
     * @param username     username del usuario.
     * @param actualPlano  contraseña actual en claro (verifica contra hash+salt).
     * @param nuevaPlano   nueva contraseña en claro (se persiste como hash+salt).
     * @throws IllegalArgumentException si la contraseña actual no coincide o datos inválidos.
     */
    void cambiarPassword(String username, String actualPlano, String nuevaPlano);
}
