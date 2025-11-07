package ui;

import dao.JdbcUsuarioDAO;
import model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ForgotUsernameDialog extends JDialog {
    private final JTextField txtNombre = new JTextField(18);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Username","Nombre","Rol"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JdbcUsuarioDAO dao = new JdbcUsuarioDAO();

    public ForgotUsernameDialog(Window owner) {
        super(owner, "Recuperar usuario", ModalityType.APPLICATION_MODAL);
        setSize(520, 340);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Nombre (o parte):"));
        top.add(txtNombre);
        JButton btnBuscar = new JButton("Buscar");
        top.add(btnBuscar);

        UIUtil.styleTable(table);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCerrar = new JButton("Cerrar");
        south.add(btnCerrar);
        add(south, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscar());
        txtNombre.addActionListener(e -> buscar());
        btnCerrar.addActionListener(e -> setVisible(false));
    }

    private void buscar() {
        model.setRowCount(0);
        List<Usuario> users = dao.buscarPorNombreLike(txtNombre.getText());
        for (Usuario u : users) {
            model.addRow(new Object[]{ u.getUsername(), u.getNombre(), u.getRol().name() });
        }
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Sin coincidencias.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
