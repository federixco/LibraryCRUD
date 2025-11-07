package ui;

import dao.JdbcAuditoriaDao;
import model.Auditoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AuditoriaRecientesDialog extends JDialog {

    private final JdbcAuditoriaDao dao = new JdbcAuditoriaDao();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Fecha/Hora","Operador","Tipo","Libro","Cant.","Destinatario","Detalle"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public AuditoriaRecientesDialog(Window owner) {
        super(owner, "Auditoría (recientes)", ModalityType.APPLICATION_MODAL);
        setSize(900, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        UIUtil.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCerrar = new JButton("Cerrar");
        south.add(btnCerrar);
        add(south, BorderLayout.SOUTH);

        btnCerrar.addActionListener(e -> setVisible(false));

        cargar();
    }

    private void cargar() {
        model.setRowCount(0);
        List<Auditoria> rows = dao.listarRecientes(100);
        for (Auditoria a : rows) {
            model.addRow(new Object[]{
                    a.getTs().toString(),
                    a.getOperadorUsername(),
                    a.getTipo(),
                    a.getLibroCodigo(),
                    a.getCantidad()==null? "" : a.getCantidad(),
                    a.getDestinatario()==null? "" : a.getDestinatario(),
                    a.getDetalle()==null? "" : a.getDetalle()
            });
        }
        if (table.getRowCount() > 0) table.setRowSelectionInterval(0,0);
    }
}
