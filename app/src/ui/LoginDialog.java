package ui;

import service.AuthService;
import service.AuthServiceImpl;
import dao.JdbcUsuarioDAO;
import model.Usuario;
import ui.RegisterDialog;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private final JTextField txtUser = new JTextField(16);
    private final JPasswordField txtPass = new JPasswordField(16);

    private final JButton btnEntrar = new JButton("Entrar");
    private final JButton btnCancelar = new JButton("Cancelar");
    private final JButton btnRegistrar = new JButton("Registrarse");
    private final JButton btnOlvidePass = new JButton("Olvidé mi contraseña");
    private final JButton btnOlvideUser = new JButton("Olvidé mi usuario");

    private final AuthService auth = new AuthServiceImpl(new JdbcUsuarioDAO());
    private Usuario autenticado;

    public LoginDialog(Frame owner) {
        super(owner, "Iniciar sesión", true);
        setSize(420, 240);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10,10));

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        form.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        form.add(new JLabel("Usuario:"));     form.add(txtUser);
        form.add(new JLabel("Contraseña:"));  form.add(txtPass);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnEntrar);
        actions.add(btnCancelar);

        JPanel links = new JPanel(new FlowLayout(FlowLayout.LEFT));
        links.add(btnRegistrar);
        links.add(btnOlvidePass);
        links.add(btnOlvideUser);

        add(form, BorderLayout.CENTER);
        add(links, BorderLayout.NORTH);
        add(actions, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnEntrar);

        // Acciones
        btnEntrar.addActionListener(e -> doLogin());
        btnCancelar.addActionListener(e -> { autenticado = null; setVisible(false); });

        btnRegistrar.addActionListener(e -> {
            RegisterDialog rd = new RegisterDialog(this);
            rd.setVisible(true);
        });

        btnOlvidePass.addActionListener(e -> {
            ForgotPasswordDialog fd = new ForgotPasswordDialog(this);
            fd.setVisible(true);
        });

        btnOlvideUser.addActionListener(e -> {
            ForgotUsernameDialog fu = new ForgotUsernameDialog(this);
            fu.setVisible(true);
        });
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
