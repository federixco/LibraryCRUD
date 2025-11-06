package ui;


import dao.JdbcUsuarioDAO;
import model.Usuario;
import service.AuthService;
import service.AuthServiceImpl;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo de Login:
 *  - Pide username y password.
 *  - Llama a AuthService.login() y, si ok, expone el Usuario.
 */


public class LoginDialog extends JDialog {
    private final JTextField txtUser = new JTextField(16);
    private final JPasswordField txtPass = new JPasswordField(16);
    private final AuthService auth = new AuthServiceImpl(new JdbcUsuarioDAO());
    private Usuario autenticado;

    public LoginDialog(Frame owner) {
        super(owner, "Iniciar sesión", true);
        setSize(360, 180);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10,10));

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        form.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        form.add(new JLabel("Usuario:"));
        form.add(txtUser);
        form.add(new JLabel("Contraseña:"));
        form.add(txtPass);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Entrar");
        JButton btnCancel = new JButton("Cancelar");
        buttons.add(btnOk); buttons.add(btnCancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnOk);

        btnOk.addActionListener(e -> doLogin());
        btnCancel.addActionListener(e -> { autenticado = null; setVisible(false); });
    }

    private void doLogin() {
        try {
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());
            autenticado = auth.login(user, pass);
            setVisible(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login fallido", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Usuario getAutenticado() { return autenticado; }
}