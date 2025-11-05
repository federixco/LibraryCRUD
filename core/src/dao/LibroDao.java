package dao;

import model.Libro;
import java.util.List;

/**
 * Interfaz: LibroDAO
 * -----------------------
 * Propósito:
 *  - Definir las operaciones de acceso a datos (CRUD + listar con filtro) para la entidad Libro.
 *  - Desacoplar la capa de servicio de la tecnología de persistencia (JDBC, archivos, etc.).
 *
 * Uso:
 *  - La implementación concreta (JdbcLibroDAO) realiza estas operaciones contra la base.
 */


public interface LibroDao {

    // Crea un libro nuevo en la base (INSERT).
    void crear(Libro l);

    // Lee un libro por su clave primaria (SELECT ... WHERE codigo=?).
    Libro leerPorCodigo(String codigo);

    // Actualiza todos los campos del libro identificado por su código (UPDATE).
    void actualizar(Libro l);

    // Elimina físicamente el libro (DELETE). Si preferís baja lógica, podrías
    // reemplazar por un UPDATE que ponga activo=0.
    void eliminar(String codigo);

    // Lista libros con un filtro de texto opcional (por título/autor/categoría).
    List<Libro> listar(String filtroTexto);
}