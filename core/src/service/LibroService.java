package service;

import dao.LibroDao;
import model.Libro;

import java.util.List;

/**
 * Clase: LibroService
 * -----------------------
 * Propósito:
 *  - Encapsular la lógica de negocio y validaciones relacionadas con Libros.
 *  - Orquestar llamadas al DAO, validando datos antes de persistir.
 *  - Ser el punto de entrada para la GUI (la app Swing debería hablar con esta clase).
 *
 * Notas nuevas:
 *  - Soporta BAJA LÓGICA mediante activar/desactivar.
 *  - Antes de desactivar valida que no haya préstamos ABIERTO para ese libro.
 *
 * Requisitos en LibroDao:
 *  - leerPorCodigo(String)
 *  - listar(String)
 *  - crear(Libro), actualizar(Libro), eliminar(String)   (si seguís usando baja física)
 *  - setActivo(String, boolean)                          (NUEVO)
 *  - tienePrestamosAbiertos(String)                      (NUEVO)
 */
public class LibroService {

    // Dependencia al DAO: permite invertir control y facilitar pruebas/cambios.
    private final LibroDao dao;

    // Inyectamos el DAO por constructor (Dependency Injection manual).
    public LibroService(LibroDao dao) {
        this.dao = dao;
    }

    /**
     * Valida y crea un libro.
     * @param l objeto Libro a crear.
     * @throws IllegalArgumentException si alguna validación falla.
     */
    public void crear(Libro l) {
        if (l == null) throw new IllegalArgumentException("Libro requerido");
        if (esVacio(l.getCodigo())) throw new IllegalArgumentException("Código requerido");
        if (esVacio(l.getTitulo())) throw new IllegalArgumentException("Título requerido");
        if (esVacio(l.getAutor()))  throw new IllegalArgumentException("Autor requerido");
        if (l.getAnio() < 0) throw new IllegalArgumentException("Año no puede ser negativo");
        if (l.getStock() < 0) throw new IllegalArgumentException("Stock no puede ser negativo");
        dao.crear(l);
    }

    /** Devuelve un libro existente por su código, o null si no existe. */
    public Libro obtener(String codigo) {
        if (esVacio(codigo)) throw new IllegalArgumentException("Código requerido");
        return dao.leerPorCodigo(codigo);
    }

    /** Valida y actualiza un libro existente. */
    public void actualizar(Libro l) {
        if (l == null) throw new IllegalArgumentException("Libro requerido");
        if (esVacio(l.getCodigo())) throw new IllegalArgumentException("Código requerido");
        if (esVacio(l.getTitulo())) throw new IllegalArgumentException("Título requerido");
        if (esVacio(l.getAutor()))  throw new IllegalArgumentException("Autor requerido");
        if (l.getAnio() < 0) throw new IllegalArgumentException("Año no puede ser negativo");
        if (l.getStock() < 0) throw new IllegalArgumentException("Stock no puede ser negativo");
        dao.actualizar(l);
    }

    /**
     * Elimina un libro por su código (baja física).
     * Recomendación: usar baja lógica (desactivar) para evitar “huérfanos”.
     */
    public void eliminar(String codigo) {
        if (esVacio(codigo)) throw new IllegalArgumentException("Código requerido");
        dao.eliminar(codigo);
    }

    /** Lista libros con filtro de texto opcional. */
    public List<Libro> listar(String filtro) {
        return dao.listar(filtro);
    }

    // ================== BAJA LÓGICA (nuevo) ==================

    /** Devuelve true si el libro puede desactivarse (no tiene préstamos ABIERTO). */
    public boolean puedeDesactivar(String codigo) {
        if (esVacio(codigo)) throw new IllegalArgumentException("Código requerido");
        return !dao.tienePrestamosAbiertos(codigo);
    }

    /**
     * Desactiva un libro (baja lógica). Valida que no existan préstamos ABIERTO.
     * Lanza IllegalArgumentException si no puede desactivarse.
     */
    public void desactivar(String codigo) {
        if (esVacio(codigo)) throw new IllegalArgumentException("Código requerido");
        if (!puedeDesactivar(codigo)) {
            throw new IllegalArgumentException("No se puede desactivar: hay préstamos abiertos para este libro.");
        }
        dao.setActivo(codigo, false);
    }

    /** Activa (alta lógica) un libro previamente desactivado. */
    public void activar(String codigo) {
        if (esVacio(codigo)) throw new IllegalArgumentException("Código requerido");
        dao.setActivo(codigo, true);
    }

    // --- Helpers privados ---
    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }
}
