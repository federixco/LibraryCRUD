package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Prestamo
 * --------
 * Préstamo de un ÚNICO libro a un destinatario.
 * Estado: ABIERTO o DEVUELTO.
 */
public class Prestamo {
    private Long id;
    private String libroCodigo;
    private String operadorUsername;
    private String destinatario;
    private int cantidad;
    private LocalDateTime fechaPrestamo;
    private LocalDate fechaVencimiento;
    private LocalDateTime fechaDevolucion; // null si no volvió
    private Estado estado;

    public enum Estado { ABIERTO, DEVUELTO }

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
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
}
