package dao;

import model.Prestamo;

import java.time.LocalDate;
import java.util.List;

/** Contrato de persistencia para préstamos. */
public interface PrestamoDao {
    long prestar(Prestamo p);                 // crea + descuenta stock + audita
    void devolver(long idPrestamo);           // marca DEVUELTO + repone stock + audita
    void renovar(long idPrestamo, int dias);  // extiende vencimiento + audita

    List<Prestamo> abiertos(String filtroTexto);
    List<Prestamo> historico(LocalDate desde, LocalDate hasta, String filtroTexto);
}
