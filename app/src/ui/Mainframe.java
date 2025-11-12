package ui;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import dao.JdbcLibroDAO;
import model.Libro;
import service.LibroService;
import session.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Ventana principal del sistema de Biblioteca.
 *
 * ¿Qué hace?
 *  - CRUD de Libros (con búsqueda por texto).
 *  - Permisos por rol (Operador no puede eliminar; menús de admin).
 *  - Menú "Usuarios" (solo ADMIN): registrar y gestionar usuarios.
 *  - Menú "Préstamos": crear préstamo y ver préstamos abiertos.
 *  - Menú "Libros" (solo ADMIN): activar/desactivar (baja lógica) el seleccionado.
 *  - Menú "Informes": ver auditoría reciente.
 *  - Menú "Cuenta": cambiar contraseña / cerrar sesión.
 *
 * Notas de diseño:
 *  - Este frame recibe un {@link session.Session} para saber quién está logueado
 *    y aplicar permisos de UI.
 *  - Usa {@link LibroService} como fachada de negocio (la UI nunca habla con JDBC directo).
 *  - El cierre de sesión se señala con una bandera (logoutRequested) que lee el Main.
 */
public class Mainframe extends JFrame {

    // ===================== Estado y servicios =====================

    /** Sesión activa (usuario logueado). */
    private final Session session;

    /** Servicio de negocio para Libros (inyecta DAO JDBC). */
    private final LibroService service = new LibroService(new JdbcLibroDAO());

    /**
     * Bandera de logout: cuando el usuario elige "Cerrar sesión", seteamos true y
     * hacemos dispose(); el Main detecta esto y vuelve a abrir el Login.
     */
    private boolean logoutRequested = false;
    /** Lo consulta el Main para decidir si reabrir el Login. */
    public boolean isLogoutRequested() { return logoutRequested; }

    // ===================== Tabla (modelo y componentes) =====================

    /**
     * Modelo de tabla tipado:
     *  - Evita edición directa.
     *  - Mapea clases de columnas (Integer, Boolean) para render adecuado (checkbox/centrado).
     */
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Código","Título","Autor","Categoría","Editorial","Año","Stock","Activo"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 5, 6 -> Integer.class;   // Año, Stock
                case 7 -> Boolean.class;      // Activo (checkbox)
                default -> String.class;      // resto
            };
        }
    };

    /** Tabla principal que muestra los libros. */
    private final JTable table = new JTable(model);

    // ===================== Controles de búsqueda/acciones =====================

    private final JTextField txtFiltro = new JTextField(28);
    private final JButton btnBuscar    = new JButton("Buscar");
    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnNuevo     = new JButton("Nuevo");
    private final JButton btnEditar    = new JButton("Editar");
    private final JButton btnEliminar  = new JButton("Eliminar");

    // ===================== Constructor =====================

    /**
     * Crea el frame principal recibiendo la sesión actual (para permisos y título).
     */
    public Mainframe(Session session) {
        this.session = session;

        // ---- Ventana base ----
        setTitle("Biblioteca - CRUD  |  Usuario: " +
                session.getUsuario().getUsername() + " (" + session.getUsuario().getRol() + ")");
        setMinimumSize(new Dimension(960, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);  // importante para que el Main reciba windowClosed

        // ---- Menú superior ----
        JMenuBar mb = new JMenuBar();
        setJMenuBar(mb);

        // Menú Usuarios (solo ADMIN)
        if (session.isAdmin()) {
            JMenu mUsuarios = new JMenu("Usuarios");
            JMenuItem miRegistrar = new JMenuItem("Registrar operador…");
            JMenuItem miGestion   = new JMenuItem("Gestión de usuarios…");

            // Abre diálogo de registro de operador
            miRegistrar.addActionListener(e -> new RegistrarOperadorDialog(this).setVisible(true));
            // Abre diálogo de gestión (listar/editar/eliminar)
            miGestion.addActionListener(e -> new GestionUsuariosDialog(this, session).setVisible(true));

            mUsuarios.add(miRegistrar);
            mUsuarios.add(miGestion);
            mb.add(mUsuarios);
        }

        // Menú Préstamos (todos)
        JMenu mPrestamos = new JMenu("Préstamos");
        JMenuItem miNuevoPrestamo = new JMenuItem("Nuevo préstamo…");
        JMenuItem miAbiertos      = new JMenuItem("Abiertos…");

        // Crea préstamo (si descuenta stock, recarga la tabla)
        miNuevoPrestamo.addActionListener(e -> {
            new PrestamoNuevoDialog(this, session.getUsuario().getUsername()).setVisible(true);
            cargarTabla(); // refrescar por si cambió el stock
        });
        // Lista préstamos abiertos
        miAbiertos.addActionListener(e -> new PrestamosAbiertosDialog(this).setVisible(true));

        mPrestamos.add(miNuevoPrestamo);
        mPrestamos.add(miAbiertos);
        mb.add(mPrestamos);

        // Menú Libros (solo ADMIN): activar/desactivar (baja lógica)
        if (session.isAdmin()) {
            JMenu mLibros = new JMenu("Libros");
            JMenuItem miToggleActivo = new JMenuItem("Activar/Desactivar seleccionado");
            miToggleActivo.addActionListener(e -> toggleActivoSeleccionado());
            mLibros.add(miToggleActivo);
            mb.add(mLibros);
        }

        // Menú Informes (todos): auditoría reciente
        JMenu mInformes = new JMenu("Informes");
        JMenuItem miAuditoria = new JMenuItem("Auditoría (recientes)...");
        miAuditoria.addActionListener(e -> new AuditoriaRecientesDialog(this).setVisible(true));
        mInformes.add(miAuditoria);
        mb.add(mInformes);

        // Menú Cuenta (todos): cambiar contraseña / cerrar sesión
        JMenu mCuenta = new JMenu("Cuenta");

        JMenuItem miCambiarPass = new JMenuItem("Cambiar contraseña…");
        miCambiarPass.addActionListener(e ->
                new ChangePasswordDialog(this, session.getUsuario().getUsername()).setVisible(true));
        mCuenta.add(miCambiarPass);

        JMenuItem miLogout = new JMenuItem("Cerrar sesión");
        miLogout.addActionListener(e -> {
            logoutRequested = true;  // que el Main lo detecte y reabra el login
            dispose();               // cerrar este frame
        });
        mCuenta.add(miLogout);

        mb.add(mCuenta);

        // ---- Norte: título + búsqueda ----
        JPanel north = new JPanel(new BorderLayout());

        JLabel title = new JLabel("  Biblioteca - CRUD", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        north.add(title, BorderLayout.WEST);

        JPanel search = new JPanel();
        txtFiltro.setToolTipText("Buscar por título, autor o categoría (Enter)");
        search.add(new JLabel("Buscar:"));
        search.add(txtFiltro);
        search.add(btnBuscar);
        north.add(search, BorderLayout.EAST);

        // ---- Centro: tabla con estilos ----
        UIUtil.styleTable(table);
        JScrollPane sp = new JScrollPane(table);

        // ---- Sur: barra de acciones CRUD ----
        JToolBar actions = new JToolBar();
        actions.setFloatable(false);

        // Atajos de teclado: Alt+N/E/L/R
        btnNuevo.setMnemonic(KeyEvent.VK_N);
        btnEditar.setMnemonic(KeyEvent.VK_E);
        btnEliminar.setMnemonic(KeyEvent.VK_L);
        btnRefrescar.setMnemonic(KeyEvent.VK_R);

        actions.add(btnNuevo);
        actions.add(btnEditar);
        actions.add(btnEliminar);
        actions.addSeparator();
        actions.add(btnRefrescar);

        // ---- Layout principal ----
        add(north, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        // ---- Permisos por rol (UI) ----
        if (!session.isAdmin()) {
            btnEliminar.setEnabled(false); // operador no puede eliminar
            btnEliminar.setToolTipText("Solo los administradores pueden eliminar registros");
        }

        // ===================== Acciones (listeners) =====================

        // Helper para recargar tabla (DRY)
        Runnable cargar = this::cargarTabla;

        // Buscar (click) y Enter en el campo
        btnBuscar.addActionListener(e -> cargar.run());
        txtFiltro.addActionListener(e -> cargar.run());

        // Refrescar
        btnRefrescar.addActionListener(e -> {
            txtFiltro.setText("");
            cargar.run();
        });

        // Alta de libro (abre diálogo; si OK → crear y recargar)
        btnNuevo.addActionListener(e -> {
            LibroForm dlg = new LibroForm(this, "Nuevo Libro", null);
            dlg.setVisible(true);
            if (dlg.isOk()) {
                try {
                    service.crear(dlg.getLibro());
                    cargar.run();
                } catch (Exception ex) {
                    mostrarError(ex);
                }
            }
        });

        // Edición del seleccionado
        btnEditar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { aviso("Seleccioná un libro de la tabla."); return; }

            // Convertir índice de la vista al del modelo por si hay sorters/filters
            int modelRow = table.convertRowIndexToModel(row);
            String codigo = model.getValueAt(modelRow, 0).toString();

            // Leer desde servicio (por si hubo cambios)
            Libro l = service.obtener(codigo);
            if (l == null) { mostrarError(new RuntimeException("No se encontró el libro " + codigo)); return; }

            LibroForm dlg = new LibroForm(this, "Editar Libro", l);
            dlg.setVisible(true);
            if (dlg.isOk()) {
                try {
                    service.actualizar(dlg.getLibro());
                    cargar.run();
                } catch (Exception ex) {
                    mostrarError(ex);
                }
            }
        });

        // Eliminación física (solo admins lo tienen habilitado)
        btnEliminar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { aviso("Seleccioná un libro de la tabla."); return; }
            int modelRow = table.convertRowIndexToModel(row);
            String codigo = model.getValueAt(modelRow, 0).toString();

            int opt = JOptionPane.showConfirmDialog(
                    this,
                    "¿Eliminar el libro con código " + codigo + "?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION
            );
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    service.eliminar(codigo);
                    cargar.run();
                } catch (Exception ex) {
                    mostrarError(ex);
                }
            }
        });

        // Ajuste de anchos y carga inicial de la tabla
        UIUtil.setColumnWidths(table, 90, 260, 180, 150, 150, 70, 70, 70);
        cargar.run();
    }

    // ===================== Lógica de tabla =====================

    /**
     * Recarga la tabla desde el servicio con el filtro actual.
     * Limpia el modelo y agrega filas mapeando cada {@link Libro}.
     */
    private void cargarTabla() {
        model.setRowCount(0); // limpiar
        List<Libro> data = service.listar(txtFiltro.getText());
        for (Libro l : data) {
            model.addRow(new Object[]{
                l.getCodigo(),
                l.getTitulo(),
                l.getAutor(),
                l.getCategoria(),
                l.getEditorial(),
                l.getAnio(),
                l.getStock(),
                l.isActivo()
            });
        }
        // Seleccionar primera fila si hay datos (buena UX)
        if (table.getRowCount() > 0) table.setRowSelectionInterval(0, 0);
    }

    // ===================== Helpers UI =====================

    /** Muestra una excepción como diálogo de error y loguea el stacktrace. */
    private void mostrarError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Muestra un aviso (warning) simple. */
    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Activa/Desactiva (baja/alta lógica) el libro seleccionado.
     * Reglas (validadas en el servicio):
     *  - No se puede desactivar si tiene préstamos ABIERTO.
     */
    private void toggleActivoSeleccionado() {
        int row = table.getSelectedRow();
        if (row < 0) { aviso("Seleccioná un libro de la tabla."); return; }

        int modelRow = table.convertRowIndexToModel(row);
        String codigo = model.getValueAt(modelRow, 0).toString();
        boolean activo = (Boolean) model.getValueAt(modelRow, 7);

        try {
            if (activo) {
                // Intentar desactivar: el servicio bloquea si hay préstamos abiertos
                if (!service.puedeDesactivar(codigo)) {
                    aviso("No se puede desactivar: hay préstamos abiertos para este libro.");
                    return;
                }
                service.desactivar(codigo);
            } else {
                // Activar nuevamente
                service.activar(codigo);
            }
            cargarTabla();
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }
}
