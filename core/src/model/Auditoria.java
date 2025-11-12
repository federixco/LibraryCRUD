package model;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import java.time.LocalDateTime;

public class Auditoria {

    /** Identificador autoincremental del evento (PK). */
    private Long id;

    /** Timestamp del evento (ISO-8601), en hora local del servidor. */
    private LocalDateTime ts;

    /** Username del operador que originó la acción. */
    private String operadorUsername;

    /**
     * Tipo de evento, por convención en MAYÚSCULAS:
     *  - PRESTAR, DEVOLVER, RENOVAR
     *  - DESACTIVAR_LIBRO, ACTIVAR_LIBRO
     *  (Podés extender con otros tipos si el sistema crece.)
     */
    private String tipo;

    /** Código del libro afectado (nullable si el evento no aplica a un libro). */
    private String libroCodigo;

    /** Id del préstamo relacionado (nullable si no corresponde). */
    private Long prestamoId;

    /** Cantidad de unidades afectadas (nullable; p.ej. en PRESTAR/DEVOLVER). */
    private Integer cantidad;

    /** Persona/área destinataria del préstamo (nullable). */
    private String destinatario;

    /** Campo libre para detalles adicionales (nullable), p.ej. "vencimiento=2025-11-30". */
    private String detalle;

    // ===================== Getters / Setters =====================

    /** @return id autogenerado del evento. */
    public Long getId() { return id; }

    /** @param id identificador autogenerado del evento. */
    public void setId(Long id) { this.id = id; }

    /** @return instante en que ocurrió el evento. */
    public LocalDateTime getTs() { return ts; }

    /** @param ts instante del evento (no debería ser null al persistir). */
    public void setTs(LocalDateTime ts) { this.ts = ts; }

    /** @return username del operador origen. */
    public String getOperadorUsername() { return operadorUsername; }

    /** @param operadorUsername username del operador que ejecutó la acción. */
    public void setOperadorUsername(String operadorUsername) { this.operadorUsername = operadorUsername; }

    /** @return tipo de evento (p.ej. "PRESTAR"). */
    public String getTipo() { return tipo; }

    /** @param tipo literal del tipo de evento en MAYÚSCULAS. */
    public void setTipo(String tipo) { this.tipo = tipo; }

    /** @return código del libro afectado o null si no aplica. */
    public String getLibroCodigo() { return libroCodigo; }

    /** @param libroCodigo código del libro afectado (nullable). */
    public void setLibroCodigo(String libroCodigo) { this.libroCodigo = libroCodigo; }

    /** @return id del préstamo vinculado o null si no aplica. */
    public Long getPrestamoId() { return prestamoId; }

    /** @param prestamoId id del préstamo vinculado (nullable). */
    public void setPrestamoId(Long prestamoId) { this.prestamoId = prestamoId; }

    /** @return cantidad involucrada o null si no aplica. */
    public Integer getCantidad() { return cantidad; }

    /** @param cantidad unidades afectadas (nullable). */
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    /** @return destinatario del préstamo o null si no aplica. */
    public String getDestinatario() { return destinatario; }

    /** @param destinatario persona/área destinataria (nullable). */
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    /** @return detalle libre del evento o null si no aplica. */
    public String getDetalle() { return detalle; }

    /** @param detalle texto adicional para el evento (nullable). */
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
