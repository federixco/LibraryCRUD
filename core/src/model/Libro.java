package model;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

/**
 * Clase: Libro
 * -----------------------
 * Propósito:
 *  - Representa la entidad principal del sistema de Biblioteca.
 *  - Encapsula los datos de un libro (estado) y provee acceso controlado (getters/setters).
 *  - No contiene lógica de base de datos ni GUI: solo modelo de dominio.
 *
 * Uso:
 *  - Es el objeto que persiste el DAO y manipula el Service.
 */


public class Libro {

    // --- Atributos (estado del objeto) ---
    // Clave primaria del libro (ej.: "L001"). Debe ser único.
    private String codigo;

    // Título del libro (obligatorio).
    private String titulo;

    // Nombre del autor (para mantenerlo simple como texto).
    private String autor;

    // Categoría o género del libro (ej.: "Programación", "Novela").
    private String categoria;

    // Editorial del libro (puede ser null).
    private String editorial;

    // Año de publicación (>= 0 por simplicidad).
    private int anio;

    // Unidades disponibles en stock (>= 0).
    private int stock;

    // Si el libro está activo/visible en el catálogo.
    private boolean activo;

    // --- Constructores ---
    public Libro() {
        // Constructor vacío requerido por frameworks/serializadores y para crear primero y setear después.
    }

    public Libro(String codigo, String titulo, String autor, String categoria,
                 String editorial, int anio, int stock, boolean activo) {
        // Asigna cada campo recibido a los atributos encapsulados.
        this.codigo = codigo;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.editorial = editorial;
        this.anio = anio;
        this.stock = stock;
        this.activo = activo;
    }

    // --- Getters y Setters (encapsulamiento) ---
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getEditorial() { return editorial; }
    public void setEditorial(String editorial) { this.editorial = editorial; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    // --- Opcionalmente equals/hashCode/toString si los necesitás para tablas/colecciones ---
    @Override
    public String toString() {
        return codigo + " - " + titulo + " (" + autor + ")";
    }
}