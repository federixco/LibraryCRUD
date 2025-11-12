package ui;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import service.AuthService;
import service.AuthServiceImpl;
import dao.JdbcUsuarioDAO;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    private final JTextField txtNombre = new JTextField(18);
    private final JTextField txtUser   = new JTextField(14);
    private final JPasswordField txtPass = new JPasswordField(14);

    private final AuthService auth = new AuthServiceImpl(new JdbcUsuarioDAO());

    public RegisterDialog(Window owner) {
        super(owner, "Registrarse (Operador)", ModalityType.APPLICATION_MODAL);
        setSize(360, 220);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10,10));

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        form.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        form.add(new JLabel("Nombre:"));     form.add(txtNombre);
        form.add(new JLabel("Usuario:"));    form.add(txtUser);
        form.add(new JLabel("Contraseña:")); form.add(txtPass);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Crear cuenta");
        JButton cancel = new JButton("Cerrar");
        btns.add(ok); btns.add(cancel);

        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(ok);

        ok.addActionListener(e -> {
            try {
                auth.registrarOperador(
                        txtNombre.getText().trim(),
                        txtUser.getText().trim(),
                        new String(txtPass.getPassword())
                );
                JOptionPane.showMessageDialog(this, "Cuenta creada. Ya podés iniciar sesión.");
                setVisible(false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> setVisible(false));
    }
}
