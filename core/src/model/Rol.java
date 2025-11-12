package model;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

/**
 * Perfiles de acceso del sistema.
 *
 * ¿Para qué existe?
 *  - Indicar el nivel de permisos de un {@link Usuario}.
 *  - Simplificar chequeos en UI/servicios (ej.: {@code if (session.isAdmin()) ...}).
 *
 * Convenciones:
 *  - Usar MAYÚSCULAS para los literales.
 *  - Persistencia: se guarda como texto (p.ej. "ADMIN", "OPERADOR").
 */
public enum Rol {
    ADMIN,
    OPERADOR
}
