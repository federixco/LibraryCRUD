package ui;

import dao.JdbcLibroDAO;
import model.Libro;
import service.LibroService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Clase: MainFrame
 * -----------------------
 * Ventana principal de la aplicación (GUI en Swing).
 * Muestra una tabla con los libros y provee botones para CRUD (Nuevo/Editar/Eliminar)
 * y un campo de búsqueda por texto (título/autor/categoría).
 *
 * Flujo:
 *   - Crea un LibroService con un JdbcLibroDAO (la lógica de negocio y acceso a datos).
 *   - Carga los libros en un JTable.
 *   - Abre un diálogo (LibroForm) para alta/edición.
 */


/**
 * Ventana principal con barra de acciones, búsqueda y tabla estilizada.
 */
public class Mainframe extends JFrame {

    private final LibroService service = new LibroService(new JdbcLibroDAO());

    // Modelo tipado para que Boolean se vea como checkbox y enteros centrados
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Código","Título","Autor","Categoría","Editorial","Año","Stock","Activo"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 5, 6 -> Integer.class;  // Año, Stock
                case 7 -> Boolean.class;     // Activo
                default -> String.class;
            };
        }
    };

    private final JTable table = new JTable(model);
    private final JTextField txtFiltro = new JTextField(28);

    public Mainframe() {
        setTitle("Biblioteca - CRUD");
        setMinimumSize(new Dimension(960, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Top: título y búsqueda
        JPanel north = new JPanel(new BorderLayout());
        JLabel title = new JLabel("  Biblioteca - CRUD", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        north.add(title, BorderLayout.WEST);

        JPanel search = new JPanel();
        txtFiltro.setToolTipText("Buscar por título, autor o categoría (Enter)");
        JButton btnBuscar = new JButton("Buscar");
        search.add(new JLabel("Buscar:"));
        search.add(txtFiltro);
        search.add(btnBuscar);
        north.add(search, BorderLayout.EAST);

        // Centro: tabla
        UIUtil.styleTable(table);
        JScrollPane sp = new JScrollPane(table);

        // Sur: barra de acciones
        JToolBar actions = new JToolBar();
        actions.setFloatable(false);
        JButton btnNuevo = new JButton("Nuevo");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar");

        // Mnemonics (Alt+N, Alt+E, ...)
        btnNuevo.setMnemonic(KeyEvent.VK_N);
        btnEditar.setMnemonic(KeyEvent.VK_E);
        btnEliminar.setMnemonic(KeyEvent.VK_L);
        btnRefrescar.setMnemonic(KeyEvent.VK_R);

        actions.add(btnNuevo);
        actions.add(btnEditar);
        actions.add(btnEliminar);
        actions.addSeparator();
        actions.add(btnRefrescar);

        // Layout principal
        add(north, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        // Acciones
        Runnable cargar = this::cargarTabla;
        btnBuscar.addActionListener(e -> cargar.run());
        btnRefrescar.addActionListener(e -> { txtFiltro.setText(""); cargar.run(); });

        // Buscar con Enter
        txtFiltro.addActionListener(e -> cargar.run());

        // Nuevo
        btnNuevo.addActionListener(e -> {
            LibroForm dlg = new LibroForm(this, "Nuevo Libro", null);
            dlg.setVisible(true);
            if (dlg.isOk()) {
                try {
                    service.crear(dlg.getLibro());
                    cargar.run();
                } catch (Exception ex) { mostrarError(ex); }
            }
        });

        // Editar
        btnEditar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { aviso("Seleccioná un libro de la tabla."); return; }
            String codigo = model.getValueAt(table.convertRowIndexToModel(row), 0).toString();
            Libro l = service.obtener(codigo);
            if (l == null) { mostrarError(new RuntimeException("No se encontró el libro " + codigo)); return; }

            LibroForm dlg = new LibroForm(this, "Editar Libro", l);
            dlg.setVisible(true);
            if (dlg.isOk()) {
                try {
                    service.actualizar(dlg.getLibro());
                    cargar.run();
                } catch (Exception ex) { mostrarError(ex); }
            }
        });

        // Eliminar
        btnEliminar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { aviso("Seleccioná un libro de la tabla."); return; }
            String codigo = model.getValueAt(table.convertRowIndexToModel(row), 0).toString();
            int opt = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar el libro con código " + codigo + "?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    service.eliminar(codigo);
                    cargar.run();
                } catch (Exception ex) { mostrarError(ex); }
            }
        });

        // Ajustes de columnas y carga inicial
        UIUtil.setColumnWidths(table, 90, 260, 180, 150, 150, 70, 70, 70);
        cargar.run();
    }

    private void cargarTabla() {
        model.setRowCount(0);
        List<Libro> data = service.listar(txtFiltro.getText());
        for (Libro l : data) {
            model.addRow(new Object[]{
                    l.getCodigo(), l.getTitulo(), l.getAutor(), l.getCategoria(),
                    l.getEditorial(), l.getAnio(), l.getStock(), l.isActivo()
            });
        }
        if (table.getRowCount() > 0) table.setRowSelectionInterval(0,0);
    }

    private void mostrarError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }
}