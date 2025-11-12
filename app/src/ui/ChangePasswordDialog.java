package ui;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import dao.JdbcUsuarioDAO;
import service.AuthService;
import service.AuthServiceImpl;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private final JPasswordField txtActual = new JPasswordField(16);
    private final JPasswordField txtNueva  = new JPasswordField(16);
    private final JPasswordField txtRepite = new JPasswordField(16);

    private final AuthService auth = new AuthServiceImpl(new JdbcUsuarioDAO());
    private final String username; // usuario logueado

    public ChangePasswordDialog(Window owner, String username) {
        super(owner, "Cambiar contraseña", ModalityType.APPLICATION_MODAL);
        this.username = username;

        setSize(400, 220);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10,10));

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        form.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        form.add(new JLabel("Actual:")); form.add(txtActual);
        form.add(new JLabel("Nueva:"));  form.add(txtNueva);
        form.add(new JLabel("Repetir:"));form.add(txtRepite);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Guardar");
        JButton cancel = new JButton("Cancelar");
        btns.add(ok); btns.add(cancel);

        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(ok);

        ok.addActionListener(e -> onGuardar());
        cancel.addActionListener(e -> setVisible(false));
    }

    private void onGuardar() {
        try {
            String actual = new String(txtActual.getPassword());
            String nueva  = new String(txtNueva.getPassword());
            String repite = new String(txtRepite.getPassword());

            if (!nueva.equals(repite)) {
                JOptionPane.showMessageDialog(this, "La nueva contraseña no coincide en ambos campos.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (nueva.length() < 6) {
                JOptionPane.showMessageDialog(this, "La nueva contraseña debe tener al menos 6 caracteres.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            auth.cambiarPassword(username, actual, nueva);
            JOptionPane.showMessageDialog(this, "Contraseña actualizada.");
            setVisible(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
