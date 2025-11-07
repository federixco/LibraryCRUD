package model;

import java.time.LocalDateTime;

/** Registro de auditoría: quién, qué, cuándo (y datos relacionados). */
public class Auditoria {
    private Long id;
    private LocalDateTime ts;
    private String operadorUsername;
    private String tipo;          // PRESTAR, DEVOLVER, DESACTIVAR_LIBRO, ACTIVAR_LIBRO, RENOVAR
    private String libroCodigo;   // nullable
    private Long prestamoId;      // nullable
    private Integer cantidad;     // nullable
    private String destinatario;  // nullable
    private String detalle;       // nullable

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getTs() { return ts; }
    public void setTs(LocalDateTime ts) { this.ts = ts; }
    public String getOperadorUsername() { return operadorUsername; }
    public void setOperadorUsername(String operadorUsername) { this.operadorUsername = operadorUsername; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getLibroCodigo() { return libroCodigo; }
    public void setLibroCodigo(String libroCodigo) { this.libroCodigo = libroCodigo; }
    public Long getPrestamoId() { return prestamoId; }
    public void setPrestamoId(Long prestamoId) { this.prestamoId = prestamoId; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
