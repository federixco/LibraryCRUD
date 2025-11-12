package dao;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import model.Libro;
import java.util.List;

/**
 * Contrato de acceso a datos para la entidad {@link Libro}.
 *
 * ¿Qué hace?
 *  - Define operaciones CRUD y consultas con filtro de texto.
 *  - Expone banderas de disponibilidad lógica (activo) y una consulta
 *    de integridad relacionada con préstamos abiertos.
 *
 * ¿Por qué interfaz?
 *  - Desacopla la capa de servicio de la tecnología de persistencia
 *    (JDBC, memoria, mocks para tests, etc.). Patrón DAO.
 *
 * Colabora con:
 *  - {@link model.Libro} como DTO/entidad de dominio.
 *  - Implementaciones concretas como {@code JdbcLibroDAO}.
 */



public interface LibroDao {

    /**
     * Inserta un nuevo libro en la fuente de datos.
     *
     * @param l entidad a persistir (se asume validada por la capa de servicio).
     * @throws RuntimeException si ocurre un error de acceso a datos.
     */
    void crear(Libro l);

    /**
     * Obtiene un libro por su código (clave primaria).
     *
     * @param codigo identificador del libro.
     * @return el libro encontrado o {@code null} si no existe.
     * @throws RuntimeException si ocurre un error de acceso a datos.
     */
    Libro leerPorCodigo(String codigo);

    /**
     * Actualiza los campos del libro identificado por su código.
     *
     * @param l entidad con datos actualizados (incluye el mismo código).
     * @throws RuntimeException si el libro no existe o hay error de acceso a datos.
     */
    void actualizar(Libro l);

    /**
     * Elimina físicamente un libro por código.
     * <p>
     * Nota: si se requiere baja lógica, preferir {@link #setActivo(String, boolean)}.
     *
     * @param codigo identificador del libro a eliminar.
     * @throws RuntimeException si no existe o hay error de acceso a datos.
     */
    void eliminar(String codigo);

    /**
     * Lista libros aplicando un filtro opcional por texto
     * (busca por título/autor/categoría, según implementación).
     *
     * @param filtroTexto texto a buscar; puede ser {@code null} o vacío para listar todo.
     * @return lista de libros que cumplen el criterio.
     * @throws RuntimeException si ocurre un error de acceso a datos.
     */
    List<Libro> listar(String filtroTexto);

    /**
     * Cambia el estado lógico de disponibilidad del libro (baja lógica).
     *
     * @param codigo código del libro.
     * @param activo {@code true} para activar, {@code false} para desactivar.
     * @throws RuntimeException si el libro no existe o hay error de acceso a datos.
     */
    void setActivo(String codigo, boolean activo);

    /**
     * Indica si el libro tiene préstamos abiertos (regla de integridad para
     * evitar inconsistencias al desactivar/eliminar).
     *
     * @param codigo código del libro.
     * @return {@code true} si existe al menos un préstamo con estado ABIERTO.
     * @throws RuntimeException si ocurre un error de acceso a datos.
     */
    boolean tienePrestamosAbiertos(String codigo);
}
