package service;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import dao.LibroDao;
import model.Libro;

import java.util.List;

/**
 * Fachada de negocio para la entidad {@link Libro}.
 *
 * Responsabilidades:
 *  - Validar datos de entrada antes de persistir.
 *  - Orquestar llamadas al {@link LibroDao}.
 *  - Exponer operaciones de alta/lectura/actualización/borrado y
 *    control de baja lógica (activar/desactivar) con reglas de integridad.
 *
 * Decisiones:
 *  - Las validaciones de formato/rango se hacen aquí (no en el DAO).
 *  - La baja recomendada es LÓGICA (flag activo), no física, para
 *    evitar “huérfanos” cuando existan préstamos relacionados.
 *
 * Colabora con:
 *  - {@link LibroDao} para el acceso a datos.
 */


public class LibroService {

    /** DAO inyectado (permite tests y cambiar de implementación sin tocar la UI). */
    private final LibroDao dao;

    /** Inyección por constructor (DI manual). */
    public LibroService(LibroDao dao) {
        this.dao = dao;
    }

    /**
     * Crea un libro luego de validar sus campos básicos.
     *
     * @param l libro a persistir.
     * @throws IllegalArgumentException si falta algún dato obligatorio o hay rangos inválidos.
     */
    public void crear(Libro l) {
        // 1) Validaciones de negocio mínimas
        if (l == null) throw new IllegalArgumentException("Libro requerido");
        if (esVacio(l.getCodigo())) throw new IllegalArgumentException("Código requerido");
        if (esVacio(l.getTitulo())) throw new IllegalArgumentException("Título requerido");
        if (esVacio(l.getAutor()))  throw new IllegalArgumentException("Autor requerido");
        if (l.getAnio() < 0)        throw new IllegalArgumentException("Año no puede ser negativo");
        if (l.getStock() < 0)       throw new IllegalArgumentException("Stock no puede ser negativo");

        // 2) Delegar persistencia
        dao.crear(l);
    }

    /**
     * Obtiene un libro por código.
     * @param codigo clave primaria.
     * @return libro o null si no existe.
     */
    public Libro obtener(String codigo) {
        if (esVacio(codigo)) throw new IllegalArgumentException("Código requerido");
        return dao.leerPorCodigo(codigo);
    }

    /**
     * Actualiza un libro existente tras validar sus campos.
     * @param l entidad con datos actualizados.
     */
    public void actualizar(Libro l) {
        // 1) Validaciones
        if (l == null) throw new IllegalArgumentException("Libro requerido");
        if (esVacio(l.getCodigo())) throw new IllegalArgumentException("Código requerido");
        if (esVacio(l.getTitulo())) throw new IllegalArgumentException("Título requerido");
        if (esVacio(l.getAutor()))  throw new IllegalArgumentException("Autor requerido");
        if (l.getAnio() < 0)        throw new IllegalArgumentException("Año no puede ser negativo");
        if (l.getStock() < 0)       throw new IllegalArgumentException("Stock no puede ser negativo");

        // 2) Persistir cambios
        dao.actualizar(l);
    }

    /**
     * Elimina físicamente un libro por su código.
     * Recomendación: preferir baja lógica con {@link #desactivar(String)}.
     */
    public void eliminar(String codigo) {
        if (esVacio(codigo)) throw new IllegalArgumentException("Código requerido");
        dao.eliminar(codigo);
    }

    /**
     * Lista libros con filtro opcional por texto (título/autor/categoría).
     * @param filtro texto a buscar; null/"" lista todo.
     */
    public List<Libro> listar(String filtro) {
        return dao.listar(filtro);
    }

    // ================== BAJA LÓGICA ==================

    /**
     * Indica si un libro puede desactivarse (no tiene préstamos ABIERTO).
     * @param codigo código del libro.
     */
    public boolean puedeDesactivar(String codigo) {
        if (esVacio(codigo)) throw new IllegalArgumentException("Código requerido");
        return !dao.tienePrestamosAbiertos(codigo);
    }

    /**
     * Desactiva (baja lógica) un libro. Regla: no debe tener préstamos ABIERTO.
     * @param codigo código del libro.
     * @throws IllegalArgumentException si hay préstamos abiertos.
     */
    public void desactivar(String codigo) {
        if (esVacio(codigo)) throw new IllegalArgumentException("Código requerido");
        if (!puedeDesactivar(codigo)) {
            throw new IllegalArgumentException(
                "No se puede desactivar: hay préstamos abiertos para este libro."
            );
        }
        dao.setActivo(codigo, false);
    }

    /**
     * Activa (alta lógica) un libro previamente desactivado.
     * @param codigo código del libro.
     */
    public void activar(String codigo) {
        if (esVacio(codigo)) throw new IllegalArgumentException("Código requerido");
        dao.setActivo(codigo, true);
    }

    // ================== Helpers ==================
    /** true si la cadena es null o solo espacios. */
    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }
}
