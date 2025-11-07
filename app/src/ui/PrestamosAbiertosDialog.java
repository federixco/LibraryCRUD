package ui;

import dao.JdbcLibroDAO;
import dao.JdbcPrestamoDAO;
import model.Libro;
import model.Prestamo;
import service.LibroService;
import service.PrestamoService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class PrestamosAbiertosDialog extends JDialog {

    private final PrestamoService svc = new PrestamoService(new JdbcPrestamoDAO());
    private final LibroService libroSvc = new LibroService(new JdbcLibroDAO());

    private final JTextField txtFiltro = new JTextField(20);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID","Código","Título","Destinatario","Cantidad","Prestado","Vence"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            return switch (c) { case 0,4 -> Integer.class; default -> String.class; };
        }
    };
    private final JTable table = new JTable(model);

    public PrestamosAbiertosDialog(Window owner) {
        super(owner, "Préstamos abiertos", ModalityType.APPLICATION_MODAL);
        setSize(800, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Buscar (título/autor/destinatario):"));
        top.add(txtFiltro);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnRefrescar = new JButton("Refrescar");
        top.add(btnBuscar); top.add(btnRefrescar);

        UIUtil.styleTable(table);
        JScrollPane sp = new JScrollPane(table);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDevolver = new JButton("Devolver");
        JButton btnRenovar  = new JButton("Renovar +7");
        JButton btnCerrar   = new JButton("Cerrar");
        south.add(btnDevolver);
        south.add(btnRenovar);
        south.add(btnCerrar);

        add(top, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> cargar());
        btnRefrescar.addActionListener(e -> { txtFiltro.setText(""); cargar(); });
        txtFiltro.addActionListener(e -> cargar());
        btnCerrar.addActionListener(e -> setVisible(false));

        btnDevolver.addActionListener(e -> onDevolver());
        btnRenovar.addActionListener(e -> onRenovar());

        cargar();
    }

    private void cargar() {
        model.setRowCount(0);
        List<Prestamo> data = svc.abiertos(txtFiltro.getText().trim());
        for (Prestamo p : data) {
            String titulo = "";
            try {
                Libro l = libroSvc.obtener(p.getLibroCodigo());
                if (l != null) titulo = l.getTitulo();
            } catch (Exception ignore) {}
            model.addRow(new Object[]{
                    p.getId().intValue(),
                    p.getLibroCodigo(),
                    titulo,
                    p.getDestinatario(),
                    p.getCantidad(),
                    p.getFechaPrestamo().toString(),
                    p.getFechaVencimiento().toString()
            });
        }
        // Resaltar vencidos
        table.setDefaultRenderer(Object.class, new VencimientoCellRenderer(6));
        if (table.getRowCount() > 0) table.setRowSelectionInterval(0,0);
    }

    private Integer getSelectedId() {
        int r = table.getSelectedRow();
        if (r < 0) return null;
        return (Integer) model.getValueAt(table.convertRowIndexToModel(r), 0);
    }

    private void onDevolver() {
        Integer id = getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Seleccioná un préstamo.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "¿Marcar como devuelto (id=" + id + ")?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            svc.devolver(id);
            cargar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRenovar() {
        Integer id = getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Seleccioná un préstamo.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            svc.renovar(id, 7);
            cargar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Renderer simple para marcar en rojo los vencidos (si no está seleccionado). */
    private static class VencimientoCellRenderer extends DefaultTableCellRenderer {
        private final int vencCol;
        VencimientoCellRenderer(int vencCol) { this.vencCol = vencCol; }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            try {
                String s = table.getValueAt(row, vencCol).toString();
                boolean vencido = LocalDate.parse(s).isBefore(LocalDate.now());
                if (!isSelected) c.setForeground(vencido ? new Color(180, 0, 0) : Color.DARK_GRAY);
            } catch (Exception ignore) {}
            return c;
        }
    }
}
