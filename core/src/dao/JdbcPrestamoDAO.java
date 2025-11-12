package dao;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import db.ConnectionFactory;
import model.Prestamo;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JdbcPrestamoDAO
 * ---------------
 * Implementación JDBC del contrato {@link PrestamoDao} para gestionar préstamos.
 *
 * ¿Qué hace?
 *   - Opera sobre la tabla prestamo (alta, devolución, renovación, consultas).
 *   - Ajusta el stock del libro relacionado al prestar/devolver.
 *   - Registra siempre un evento en auditoria.
 *
 * Decisiones:
 *   - Cada operación crítica (prestar/devolver/renovar) se ejecuta en **una única transacción**:
 *       * coherencia entre préstamo/stock/auditoría
 *       * si existe un fallo→ rollback()
 *   - Manejo de recursos con try-with-resources.
 *   - Errores SQL envueltos en RuntimeException (simplifica firmas).
 */



public class JdbcPrestamoDAO implements PrestamoDao {

    /**
     * Crea un préstamo ABIERTO, descuenta stock y registra auditoría —todo en una transacción.
     *
     * Flujo:
     *  1) Validaciones de dominio contra DB (libro activo, stock suficiente).
     *  2) INSERT del préstamo (estado = ABIERTO).
     *  3) UPDATE del stock del libro (stock = stock - cantidad).
     *  4) INSERT de auditoría (tipo=PRESTAR).
     *  5) commit.
     *
     * @param p entidad de préstamo con datos completos (libro, operador, destinatario, cantidad, fechas).
     * @return id autogenerado del préstamo.
     */
    @Override
    public long prestar(Prestamo p) {
        // SQL de alta de préstamo (fecha_devolucion queda NULL; estado ABIERTO)
        String insertPrestamo = """
          INSERT INTO prestamo (libro_codigo, operador_username, destinatario, cantidad,
                                fecha_prestamo, fecha_vencimiento, estado)
          VALUES (?,?,?,?,?,?, 'ABIERTO')
        """;

        // 1) Abrir conexión y desactivar autocommit para iniciar transacción explícita
        try (Connection cn = ConnectionFactory.getConnection()) {
            cn.setAutoCommit(false);
            try {
                // 2) Validaciones previas (en la misma conexión)
                if (!libroActivo(cn, p.getLibroCodigo()))
                    throw new RuntimeException("El libro está desactivado");
                if (!hayStockSuficiente(cn, p.getLibroCodigo(), p.getCantidad()))
                    throw new RuntimeException("Stock insuficiente");

                // 3) Insertar préstamo y obtener ID autogenerado
                long id;
                try (PreparedStatement ps = cn.prepareStatement(insertPrestamo, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, p.getLibroCodigo());
                    ps.setString(2, p.getOperadorUsername());
                    ps.setString(3, p.getDestinatario());
                    ps.setInt(4, p.getCantidad());
                    ps.setString(5, p.getFechaPrestamo().toString());
                    ps.setString(6, p.getFechaVencimiento().toString());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        id = rs.getLong(1);
                    }
                }

                // 4) Descontar stock de manera atómica y segura (chequea stock >= cantidad)
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE libro SET stock = stock - ? WHERE codigo=? AND stock >= ?")) {
                    ps.setInt(1, p.getCantidad());
                    ps.setString(2, p.getLibroCodigo());
                    ps.setInt(3, p.getCantidad());
                    if (ps.executeUpdate() == 0)
                        throw new RuntimeException("No se pudo descontar stock");
                }

                // 5) Registrar auditoría (PRESTAR)
                insertAudit(cn,
                        p.getOperadorUsername(),
                        "PRESTAR",
                        p.getLibroCodigo(),
                        id,
                        p.getCantidad(),
                        p.getDestinatario(),
                        "vencimiento=" + p.getFechaVencimiento());

                // 6) Confirmar transacción
                cn.commit();
                return id;

            } catch (Exception ex) {
                // 7) Si algo falla, revertimos todo
                cn.rollback();
                throw ex;
            } finally {
                // 8) Restaurar autocommit
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error prestando: " + e.getMessage(), e);
        }
    }

    /**
     * Marca como DEVUELTO un préstamo, repone stock y registra auditoría —todo transaccional.
     *
     * Flujo:
     *  1) Carga préstamo ABIERTO (cantidad, libro, operador, destinatario).
     *  2) UPDATE préstamo → estado=DEVUELTO, fecha_devolucion=now.
     *  3) UPDATE libro → stock = stock + cantidad.
     *  4) INSERT auditoría (tipo=DEVOLVER).
     *  5) commit.
     *
     * @param idPrestamo id de préstamo abierto a devolver.
     */
    @Override
    public void devolver(long idPrestamo) {
        try (Connection cn = ConnectionFactory.getConnection()) {
            cn.setAutoCommit(false);
            try {
                // 1) Traer datos necesarios del préstamo (debe estar ABIERTO)
                int cant;
                String codigo;
                String operador;
                String destinatario;
                try (PreparedStatement ps = cn.prepareStatement(
                        "SELECT cantidad, libro_codigo, operador_username, destinatario " +
                        "FROM prestamo WHERE id=? AND estado='ABIERTO'")) {
                    ps.setLong(1, idPrestamo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next())
                            throw new RuntimeException("Préstamo no abierto o inexistente");
                        cant = rs.getInt(1);
                        codigo = rs.getString(2);
                        operador = rs.getString(3);
                        destinatario = rs.getString(4);
                    }
                }

                // 2) Marcar préstamo como DEVUELTO y setear fecha_devolucion
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE prestamo SET estado='DEVUELTO', fecha_devolucion=? WHERE id=?")) {
                    ps.setString(1, LocalDateTime.now().toString());
                    ps.setLong(2, idPrestamo);
                    ps.executeUpdate();
                }

                // 3) Devolver stock al libro
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE libro SET stock = stock + ? WHERE codigo=?")) {
                    ps.setInt(1, cant);
                    ps.setString(2, codigo);
                    ps.executeUpdate();
                }

                // 4) Registrar auditoría (DEVOLVER)
                insertAudit(cn, operador, "DEVOLVER", codigo, idPrestamo, cant, destinatario, null);

                // 5) Confirmar
                cn.commit();

            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error devolviendo: " + e.getMessage(), e);
        }
    }

    /**
     * Extiende la fecha de vencimiento de un préstamo ABIERTO y registra auditoría.
     *
     * @param idPrestamo id del préstamo a renovar (debe estar ABIERTO).
     * @param dias cantidad de días a sumar (must > 0).
     */
    @Override
    public void renovar(long idPrestamo, int dias) {
        if (dias <= 0) throw new IllegalArgumentException("Días inválidos");

        try (Connection cn = ConnectionFactory.getConnection()) {
            cn.setAutoCommit(false);
            try {
                // 1) Cargar datos clave (para auditoría y validación de estado)
                String operador, codigo, destinatario;
                try (PreparedStatement ps = cn.prepareStatement(
                        "SELECT operador_username, libro_codigo, destinatario " +
                        "FROM prestamo WHERE id=? AND estado='ABIERTO'")) {
                    ps.setLong(1, idPrestamo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next())
                            throw new RuntimeException("Préstamo no abierto o inexistente");
                        operador = rs.getString(1);
                        codigo = rs.getString(2);
                        destinatario = rs.getString(3);
                    }
                }

                // 2) Sumar días al vencimiento (usando funciones de fecha de SQLite)
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE prestamo SET fecha_vencimiento = date(fecha_vencimiento, '+'||?||' day') " +
                        "WHERE id=? AND estado='ABIERTO'")) {
                    ps.setInt(1, dias);
                    ps.setLong(2, idPrestamo);
                    if (ps.executeUpdate() == 0)
                        throw new RuntimeException("No se pudo renovar");
                }

                // 3) Auditoría (RENOVAR) con detalle "+Nd"
                insertAudit(cn, operador, "RENOVAR", codigo, idPrestamo, null, destinatario, "+" + dias + "d");

                // 4) Confirmar
                cn.commit();

            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error renovando: " + e.getMessage(), e);
        }
    }

    /**
     * Devuelve la lista de préstamos ABIERTO con filtro opcional (por título, autor o destinatario).
     * Orden: vencimientos más próximos primero.
     *
     * @param filtro texto para buscar (puede ser null/"").
     */
    @Override
    public List<Prestamo> abiertos(String filtro) {
        String sql = """
        SELECT p.* FROM prestamo p
        JOIN libro l ON l.codigo = p.libro_codigo
        WHERE p.estado='ABIERTO' AND (
              ? IS NULL OR ?='' OR
              l.titulo LIKE '%'||?||'%' OR l.autor LIKE '%'||?||'%' OR p.destinatario LIKE '%'||?||'%'
        )
        ORDER BY p.fecha_vencimiento ASC
        """;

        // 1) Conectar y preparar
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) 5 placeholders → 5 parámetros con el mismo filtro
            for (int i = 1; i <= 5; i++) ps.setString(i, filtro);

            // 3) Ejecutar y mapear
            try (ResultSet rs = ps.executeQuery()) {
                List<Prestamo> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando abiertos: " + e.getMessage(), e);
        }
    }

    /**
     * Consulta histórica de préstamos con rango de fechas y filtro opcional.
     * Orden: más recientes primero.
     *
     * @param desde fecha mínima (inclusive) o null.
     * @param hasta fecha máxima (inclusive) o null.
     * @param filtro texto para buscar (puede ser null/"").
     */
    @Override
    public List<Prestamo> historico(LocalDate desde, LocalDate hasta, String filtro) {
        String sql = """
        SELECT p.* FROM prestamo p
        JOIN libro l ON l.codigo = p.libro_codigo
        WHERE (? IS NULL OR date(p.fecha_prestamo) >= ?)
          AND (? IS NULL OR date(p.fecha_prestamo) <= ?)
          AND (? IS NULL OR ?='' OR
               l.titulo LIKE '%'||?||'%' OR l.autor LIKE '%'||?||'%' OR p.destinatario LIKE '%'||?||'%')
        ORDER BY p.fecha_prestamo DESC
        """;

        // 1) Conectar y preparar
        try (Connection cn = ConnectionFactory.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            // 2) Rango de fechas (placeholders 1..4)
            ps.setString(1, desde == null ? null : desde.toString());
            ps.setString(2, desde == null ? null : desde.toString());
            ps.setString(3, hasta == null ? null : hasta.toString());
            ps.setString(4, hasta == null ? null : hasta.toString());

            // 3) Filtro de texto (placeholders 5..9)
            for (int i = 5; i <= 9; i++) ps.setString(i, filtro);

            // 4) Ejecutar y mapear
            try (ResultSet rs = ps.executeQuery()) {
                List<Prestamo> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando histórico: " + e.getMessage(), e);
        }
    }

    // ===== Helpers de validación en DB (misma conexión/tx) =====

    /** Devuelve true si el libro está marcado como activo. */
    private boolean libroActivo(Connection cn, String codigo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT activo FROM libro WHERE codigo=?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getBoolean(1); }
        }
    }

    /** Devuelve true si el stock actual del libro es >= cant. */
    private boolean hayStockSuficiente(Connection cn, String codigo, int cant) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT stock FROM libro WHERE codigo=?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) >= cant; }
        }
    }

    // ===== Registro de auditoría (misma transacción) =====

    /**
     * Inserta un evento de auditoría.
     * Campos opcionales se registran como NULL cuando corresponde.
     */
    private void insertAudit(Connection cn, String op, String tipo, String libro, Long prestamoId,
                             Integer cantidad, String dest, String detalle) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO auditoria (ts, operador_username, tipo, libro_codigo, prestamo_id, cantidad, destinatario, detalle) " +
                "VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setString(2, op);
            ps.setString(3, tipo);
            ps.setString(4, libro);
            if (prestamoId == null) ps.setNull(5, Types.INTEGER); else ps.setLong(5, prestamoId);
            if (cantidad   == null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, cantidad);
            ps.setString(7, dest);
            ps.setString(8, detalle);
            ps.executeUpdate();
        }
    }

    // ===== Mapeo ResultSet → Prestamo =====

    /**
     * Convierte la fila actual del ResultSet a un {@link Prestamo}.
     * Maneja correctamente columnas que pueden venir NULL.
     */
    private Prestamo map(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setId(rs.getLong("id"));
        p.setLibroCodigo(rs.getString("libro_codigo"));
        p.setOperadorUsername(rs.getString("operador_username"));
        p.setDestinatario(rs.getString("destinatario"));
        p.setCantidad(rs.getInt("cantidad"));
        p.setFechaPrestamo(LocalDateTime.parse(rs.getString("fecha_prestamo")));
        p.setFechaVencimiento(LocalDate.parse(rs.getString("fecha_vencimiento")));
        String fdev = rs.getString("fecha_devolucion");
        p.setFechaDevolucion(fdev == null ? null : LocalDateTime.parse(fdev));
        p.setEstado(Prestamo.Estado.valueOf(rs.getString("estado")));
        return p;
    }
}
