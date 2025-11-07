package service;

import dao.PrestamoDao;
import model.Prestamo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Valida y delega en PrestamoDao (que maneja transacciones y auditoría). */
public class PrestamoService {
    private final PrestamoDao dao;

    public PrestamoService(PrestamoDao dao) { this.dao = dao; }

    public long prestar(String libroCodigo, String operadorUsername,
                        String destinatario, int cantidad, int dias) {
        if (libroCodigo == null || libroCodigo.isBlank()) throw new IllegalArgumentException("Código requerido");
        if (operadorUsername == null || operadorUsername.isBlank()) throw new IllegalArgumentException("Operador requerido");
        if (destinatario == null || destinatario.isBlank()) throw new IllegalArgumentException("Destinatario requerido");
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");
        if (dias <= 0) throw new IllegalArgumentException("Días inválidos");

        Prestamo p = new Prestamo();
        p.setLibroCodigo(libroCodigo.trim());
        p.setOperadorUsername(operadorUsername.trim());
        p.setDestinatario(destinatario.trim());
        p.setCantidad(cantidad);
        p.setFechaPrestamo(LocalDateTime.now());
        p.setFechaVencimiento(LocalDate.now().plusDays(dias));
        p.setEstado(Prestamo.Estado.ABIERTO);
        return dao.prestar(p);
    }

    public void devolver(long idPrestamo) { dao.devolver(idPrestamo); }
    public void renovar(long idPrestamo, int dias) { dao.renovar(idPrestamo, dias); }

    public List<Prestamo> abiertos(String filtro) { return dao.abiertos(filtro); }
    public List<Prestamo> historico(LocalDate desde, LocalDate hasta, String filtro) {
        return dao.historico(desde, hasta, filtro);
    }
}
