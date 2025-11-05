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
 * Uso:
 *  - Instanciar pasando una implementación de LibroDAO (p. ej. JdbcLibroDAO).
 *  - Llamar a crear/actualizar/eliminar/listar desde la interfaz gráfica.
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
        // Validaciones de negocio mínimas (podés ampliarlas si el profe pide más reglas).
        if (l == null) throw new IllegalArgumentException("Libro requerido");
        if (esVacio(l.getCodigo())) throw new IllegalArgumentException("Código requerido");
        if (esVacio(l.getTitulo())) throw new IllegalArgumentException("Título requerido");
        if (esVacio(l.getAutor()))  throw new IllegalArgumentException("Autor requerido");
        if (l.getAnio() < 0) throw new IllegalArgumentException("Año no puede ser negativo");
        if (l.getStock() < 0) throw new IllegalArgumentException("Stock no puede ser negativo");

        // Si todo ok, delegamos la persistencia en el DAO.
        dao.crear(l);
    }

    /**
     * Devuelve un libro existente por su código, o null si no existe.
     */
    public Libro obtener(String codigo) {
        if (esVacio(codigo)) throw new IllegalArgumentException("Código requerido");
        return dao.leerPorCodigo(codigo);
    }

    /**
     * Valida y actualiza un libro existente.
     */
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
     * Si preferís baja lógica, reemplazá por un update de 'activo=false'.
     */
    public void eliminar(String codigo) {
        if (esVacio(codigo)) throw new IllegalArgumentException("Código requerido");
        dao.eliminar(codigo);
    }

    /**
     * Lista libros con filtro de texto opcional.
     */
    public List<Libro> listar(String filtro) {
        // No validamos filtro porque puede ser null/blank (significa "sin filtro").
        return dao.listar(filtro);
    }

    // --- Helpers privados ---

    // Retorna true si s es null o está vacía/espacios.
    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }
}