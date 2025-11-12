package ui;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import dao.JdbcUsuarioDAO;
import model.Rol;
import model.Usuario;
import service.UsuarioAdminService;
import session.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GestionUsuariosDialog extends JDialog {

    private final UsuarioAdminService svc = new UsuarioAdminService(new JdbcUsuarioDAO());
    private final Session session;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Username","Nombre","Rol"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public GestionUsuariosDialog(Frame owner, Session session) {
        super(owner, "Gestión de usuarios", true);
        this.session = session;

        setSize(560, 380);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        UIUtil.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnResetPass = new JButton("Resetear contraseña");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnCerrar = new JButton("Cerrar");
        actions.add(btnResetPass);
        actions.add(btnEditar);
        actions.add(btnEliminar);
        actions.add(btnCerrar);
        add(actions, BorderLayout.SOUTH);

        btnCerrar.addActionListener(e -> setVisible(false));

        btnResetPass.addActionListener(e -> onResetPass());
        btnEliminar.addActionListener(e -> onEliminar());
        btnEditar.addActionListener(e -> onEditar());

        cargar();
    }

    private void cargar() {
        model.setRowCount(0);
        List<Usuario> data = svc.listar();
        for (Usuario u : data) {
            model.addRow(new Object[]{ u.getUsername(), u.getNombre(), u.getRol().name() });
        }
        if (table.getRowCount() > 0) table.setRowSelectionInterval(0,0);
    }

    private String getSelectedUsername() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return model.getValueAt(table.convertRowIndexToModel(row), 0).toString();
    }

    private void onResetPass() {
        String user = getSelectedUsername();
        if (user == null) { warn("Seleccioná un usuario."); return; }
        try {
            String tmp = svc.resetearPasswordTemporal(user);
            JOptionPane.showMessageDialog(this,
                    "Contraseña temporal para '" + user + "':\n\n" + tmp +
                    "\n\nSolicitá que la cambie al ingresar.",
                    "Password temporal", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { error(ex); }
    }

    private void onEliminar() {
        String user = getSelectedUsername();
        if (user == null) { warn("Seleccioná un usuario."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el usuario '" + user + "'?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            svc.eliminar(user, session.getUsuario().getUsername());
            cargar();
        } catch (Exception ex) { error(ex); }
    }

    private void onEditar() {
        String user = getSelectedUsername();
        if (user == null) { warn("Seleccioná un usuario."); return; }

        String nombre = JOptionPane.showInputDialog(this, "Nuevo nombre para " + user + ":",
                model.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 1));
        if (nombre == null) return;

        Rol rol = (Rol) JOptionPane.showInputDialog(this, "Rol:",
                "Cambiar rol", JOptionPane.QUESTION_MESSAGE, null, Rol.values(), Rol.OPERADOR);
        if (rol == null) return;

        try {
            svc.actualizarNombreYRol(user, nombre.trim(), rol);
            cargar();
        } catch (Exception ex) { error(ex); }
    }

    private void warn(String m) { JOptionPane.showMessageDialog(this, m, "Atención", JOptionPane.WARNING_MESSAGE); }
    private void error(Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
}
