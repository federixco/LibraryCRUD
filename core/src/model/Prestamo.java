package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa el préstamo de un ÚNICO libro a un destinatario.
 *
 * Responsabilidad:
 *  - Modelar los datos necesarios para gestionar el ciclo de vida del préstamo
 *    (alta → ABIERTO, devolución → DEVUELTO, renovaciones → nuevo vencimiento).
 *
 * Invariantes/Reglas:
 *  - Cuando {estado == ABIERTO}, {@code fechaDevolucion} debe ser {null}.
 *  - {cantidad} debe ser > 0.
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
    private EstadoPrestamo estado;

    // ===================== Getters / Setters =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLibroCodigo() { return libroCodigo; }
    public void setLibroCodigo(String libroCodigo) { this.libroCodigo = libroCodigo; }

    public String getOperadorUsername() { return operadorUsername; }
    public void setOperadorUsername(String operadorUsername) { this.operadorUsername = operadorUsername; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public LocalDateTime getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDateTime fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public LocalDateTime getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDateTime fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public EstadoPrestamo getEstado() { return estado; }
    /** @param estado ABIERTO o DEVUELTO (si DEVUELTO, debería existir fechaDevolucion). */
    public void setEstado(EstadoPrestamo estado) { this.estado = estado; }
}
