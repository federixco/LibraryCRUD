package ui;

import service.AuthService;
import service.AuthServiceImpl;
import dao.JdbcUsuarioDAO;

import javax.swing.*;
import java.awt.*;

public class ForgotPasswordDialog extends JDialog {
    private final JTextField txtUser = new JTextField(16);
    private final AuthService auth = new AuthServiceImpl(new JdbcUsuarioDAO());

    public ForgotPasswordDialog(Window owner) {
        super(owner, "Recuperar contraseña", ModalityType.APPLICATION_MODAL);
        setSize(360, 160);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10,10));

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        form.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        form.add(new JLabel("Usuario:")); form.add(txtUser);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Generar temporal");
        JButton cancel = new JButton("Cerrar");
        btns.add(ok); btns.add(cancel);

        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(ok);

        ok.addActionListener(e -> {
            try {
                String tmp = auth.recuperarPasswordTemporal(txtUser.getText().trim());
                JOptionPane.showMessageDialog(this,
                        "Tu contraseña temporal es:\n\n" + tmp +
                        "\n\nIniciá sesión y cambiala desde 'Cuenta > Cambiar contraseña' (si lo habilitás).",
                        "Temporal generada", JOptionPane.INFORMATION_MESSAGE);
                setVisible(false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> setVisible(false));
    }
}
