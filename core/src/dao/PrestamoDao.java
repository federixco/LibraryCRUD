package dao;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import model.Prestamo;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de persistencia para la gestión de préstamos.
 *
 * ¿Qué hace?
 *  - Define operaciones transaccionales de negocio:
 *      * prestar: crea préstamo, descuenta stock y registra auditoría.
 *      * devolver: marca DEVUELTO, repone stock y audita.
 *      * renovar: extiende vencimiento y audita.
 *  - Expone consultas operativas (abiertos) e históricas (con rango de fechas).
 *
 * ¿Por qué interfaz?
 *  - Desacopla la capa de servicio de la tecnología de persistencia (JDBC, memoria, mocks).
 *  - Facilita pruebas y cambios de implementación sin afectar al resto del sistema.
 */


public interface PrestamoDao {

    /**
     * Crea un préstamo en estado ABIERTO, descuenta stock y registra auditoría.
     * Debe ejecutarse de forma transaccional.
     *
     * @param p entidad de préstamo con datos completos (libro, operador, destinatario, cantidad, fechas).
     * @return id autogenerado del préstamo.
     * @throws RuntimeException si falla cualquier paso (insert, stock, auditoría).
     */
    long prestar(Prestamo p);

    /**
     * Marca un préstamo ABIERTO como DEVUELTO, repone stock y registra auditoría.
     * Debe ejecutarse de forma transaccional.
     *
     * @param idPrestamo identificador del préstamo a devolver.
     * @throws RuntimeException si el préstamo no existe, no está ABIERTO o falla la operación.
     */
    void devolver(long idPrestamo);

    /**
     * Extiende la fecha de vencimiento de un préstamo ABIERTO y registra auditoría.
     * Debe ejecutarse de forma transaccional.
     *
     * @param idPrestamo identificador del préstamo a renovar.
     * @param dias cantidad de días a sumar (debe ser &gt; 0).
     * @throws IllegalArgumentException si dias &lt;= 0.
     * @throws RuntimeException si el préstamo no existe/no está ABIERTO o falla la operación.
     */
    void renovar(long idPrestamo, int dias);

    /**
     * Lista préstamos en estado ABIERTO con filtro opcional por texto.
     * El filtro suele aplicarse sobre título/autor/destinatario, según implementación.
     *
     * @param filtroTexto texto a buscar; puede ser {@code null} o vacío para no filtrar.
     * @return lista de préstamos abiertos (típicamente ordenados por vencimiento ascendente).
     */
    List<Prestamo> abiertos(String filtroTexto);

    /**
     * Consulta histórica de préstamos con rango de fechas y filtro opcional por texto.
     *
     * @param desde fecha mínima (inclusive) o {@code null} para sin límite inferior.
     * @param hasta fecha máxima (inclusive) o {@code null} para sin límite superior.
     * @param filtroTexto texto a buscar; puede ser {@code null} o vacío para no filtrar.
     * @return lista de préstamos en el período (típicamente ordenados por fecha de préstamo desc).
     */
    List<Prestamo> historico(LocalDate desde, LocalDate hasta, String filtroTexto);
}
