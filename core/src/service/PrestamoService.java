package service;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import dao.PrestamoDao;
import model.Prestamo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Fachada de negocio para gestionar el ciclo de vida de los préstamos.
 *
 * Responsabilidades:
 *  - Validar inputs de alto nivel (required, rangos, formatos básicos).
 *  - Construir el DTO {@link Prestamo} con fechas/estado adecuados.
 *  - Delegar la persistencia/transacciones/auditoría en {@link PrestamoDao}.
 *
 * Decisiones:
 *  - La lógica transaccional (descontar/rehabilitar stock + auditoría) vive en el DAO JDBC.
 *  - Aquí solo se hace orquestación y validación previa.
 */
public class PrestamoService {

    /** DAO inyectado (permite cambiar implementación y facilitar tests). */
    private final PrestamoDao dao;

    /** Inyección por constructor. */
    public PrestamoService(PrestamoDao dao) { this.dao = dao; }

    /**
     * Registra un nuevo préstamo ABIERTO.
     *
     * Flujo:
     *  1) Valida parámetros (cadenas no vacías, cantidad/días > 0).
     *  2) Construye {@link Prestamo} con:
     *     - fechaPrestamo = ahora
     *     - fechaVencimiento = hoy + días
     *     - estado = ABIERTO
     *  3) Delegar en {@link PrestamoDao#prestar(Prestamo)} (transacción + auditoría).
     *
     * @param libroCodigo código del libro (FK).
     * @param operadorUsername username del operador que registra el préstamo.
     * @param destinatario persona/área que recibe el libro.
     * @param cantidad cantidad de ejemplares (> 0).
     * @param dias días hasta el vencimiento (> 0).
     * @return id autogenerado del préstamo.
     * @throws IllegalArgumentException si algún parámetro es inválido.
     * @throws RuntimeException si la capa DAO falla (stock/estado/auditoría).
     */
    public long prestar(String libroCodigo, String operadorUsername,
                        String destinatario, int cantidad, int dias) {

        // 1) Validaciones mínimas
        if (libroCodigo == null || libroCodigo.isBlank())
            throw new IllegalArgumentException("Código requerido");
        if (operadorUsername == null || operadorUsername.isBlank())
            throw new IllegalArgumentException("Operador requerido");
        if (destinatario == null || destinatario.isBlank())
            throw new IllegalArgumentException("Destinatario requerido");
        if (cantidad <= 0)
            throw new IllegalArgumentException("Cantidad inválida");
        if (dias <= 0)
            throw new IllegalArgumentException("Días inválidos");

        // 2) Construir DTO de dominio
        Prestamo p = new Prestamo();
        p.setLibroCodigo(libroCodigo.trim());
        p.setOperadorUsername(operadorUsername.trim());
        p.setDestinatario(destinatario.trim());
        p.setCantidad(cantidad);
        p.setFechaPrestamo(LocalDateTime.now());
        p.setFechaVencimiento(LocalDate.now().plusDays(dias));
        p.setEstado(Prestamo.Estado.ABIERTO);

        // 3) Delegar operación transaccional
        return dao.prestar(p);
    }

    /**
     * Marca un préstamo como DEVUELTO (repone stock y audita en DAO).
     *
     * @param idPrestamo id de préstamo ABIERTO.
     * @throws RuntimeException si no existe/no está abierto o falla persistencia.
     */
    public void devolver(long idPrestamo) {
        dao.devolver(idPrestamo);
    }

    /**
     * Extiende la fecha de vencimiento de un préstamo ABIERTO.
     *
     * @param idPrestamo id del préstamo.
     * @param dias días a sumar (> 0).
     * @throws IllegalArgumentException si días <= 0.
     * @throws RuntimeException si no existe/no está abierto o falla persistencia.
     */
    public void renovar(long idPrestamo, int dias) {
        dao.renovar(idPrestamo, dias);
    }

    /**
     * Lista préstamos en estado ABIERTO con filtro opcional por texto.
     *
     * @param filtro texto a buscar (título/autor/destinatario, según implementación); puede ser null/"".
     * @return lista de préstamos abiertos (típicamente ordenados por vencimiento asc).
     */
    public List<Prestamo> abiertos(String filtro) {
        return dao.abiertos(filtro);
    }

    /**
     * Consulta histórica de préstamos con rango de fechas y filtro opcional.
     *
     * @param desde fecha mínima (inclusive) o null.
     * @param hasta fecha máxima (inclusive) o null.
     * @param filtro texto a buscar o null/"" para no filtrar.
     * @return lista de préstamos en el período (típicamente más recientes primero).
     */
    public List<Prestamo> historico(LocalDate desde, LocalDate hasta, String filtro) {
        return dao.historico(desde, hasta, filtro);
    }
}
