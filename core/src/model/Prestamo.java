package model;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa el préstamo de un ÚNICO libro a un destinatario.
 *
 * Responsabilidad:
 *  - Modelar los datos necesarios para gestionar el ciclo de vida del préstamo
 *    (alta → abierto, devolución → devuelto, renovaciones → nuevo vencimiento).
 *
 * Invariantes/Reglas:
 *  - Cuando {estado == ABIERTO}, {@code fechaDevolucion} debe ser { null}.
 *  - { cantidad} debe ser > 0.
 *  - {libroCodigo} y {@code operadorUsername} no deberían ser null/blank.
 *  - {fechaPrestamo} y {@code fechaVencimiento} deben estar informadas al crear el préstamo.
 *
 * Colabora con:
 *  - Capa DAO (persistencia) para altas/consultas/actualizaciones.
 *  - Capa Service para validaciones de negocio y transacciones (ajuste de stock, auditoría).
 */


public class Prestamo {

    /** Identificador del préstamo (PK autogenerada en DB). */
    private Long id;

    /** Código del libro prestado (FK a tabla LIBRO). */
    private String libroCodigo;

    /** Usuario operador que registró el préstamo. */
    private String operadorUsername;

    /** Persona/área que recibe el libro. */
    private String destinatario;

    /** Cantidad de ejemplares prestados (debe ser > 0). */
    private int cantidad;

    /** Fecha/hora en que se registró el préstamo. */
    private LocalDateTime fechaPrestamo;

    /** Fecha límite para la devolución (día calendario). */
    private LocalDate fechaVencimiento;

    /** Fecha/hora efectiva de devolución; {@code null} mientras esté ABIERTO. */
    private LocalDateTime fechaDevolucion;

    /** Estado actual del préstamo. */
    private Estado estado;

    /** Estados posibles del préstamo. */
    public enum Estado { ABIERTO, DEVUELTO }

    // ===================== Getters / Setters =====================

    /** @return id del préstamo (puede ser null antes de persistir). */
    public Long getId() { return id; }
    /** @param id identificador autogenerado. */
    public void setId(Long id) { this.id = id; }

    /** @return código del libro asociado. */
    public String getLibroCodigo() { return libroCodigo; }
    /** @param libroCodigo código del libro; no debería ser null/blank. */
    public void setLibroCodigo(String libroCodigo) { this.libroCodigo = libroCodigo; }

    /** @return username del operador que registró el préstamo. */
    public String getOperadorUsername() { return operadorUsername; }
    /** @param operadorUsername username del operador. */
    public void setOperadorUsername(String operadorUsername) { this.operadorUsername = operadorUsername; }

    /** @return destinatario del préstamo. */
    public String getDestinatario() { return destinatario; }
    /** @param destinatario persona/área que recibe el libro. */
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    /** @return cantidad de ejemplares prestados (> 0). */
    public int getCantidad() { return cantidad; }
    /** @param cantidad cantidad de ejemplares; la capa Service valida > 0. */
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    /** @return fecha/hora en que se registró el préstamo. */
    public LocalDateTime getFechaPrestamo() { return fechaPrestamo; }
    /** @param fechaPrestamo instante de registro. */
    public void setFechaPrestamo(LocalDateTime fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    /** @return fecha de vencimiento (día calendario). */
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    /** @param fechaVencimiento fecha límite para devolver. */
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    /** @return fecha/hora efectiva de devolución o {@code null} si sigue abierto. */
    public LocalDateTime getFechaDevolucion() { return fechaDevolucion; }
    /** @param fechaDevolucion instante de devolución; null mientras esté abierto. */
    public void setFechaDevolucion(LocalDateTime fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    /** @return estado actual del préstamo. */
    public Estado getEstado() { return estado; }
    /** @param estado ABIERTO o DEVUELTO (si DEVUELTO, debería existir fechaDevolucion). */
    public void setEstado(Estado estado) { this.estado = estado; }
}
