package biblioteca.app;

import db.DbInit;
import session.Session;
import ui.LoginDialog;
import ui.Mainframe;
import ui.UIUtil;

import javax.swing.*;

/**
 * Punto de entrada de la App:
 * 1) Aplica L&F (Nimbus si está disponible)
 * 2) Inicializa la base (crea tablas y seed si faltan)
 * 3) Pide login y abre la ventana principal con la Session
 */
public class Main {

    public static void main(String[] args) {
        // 1) Look & Feel
        UIUtil.applyNimbus();

        // 2) Inicializar base de datos
        try {
            DbInit.ensureInit();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "No pude abrir/crear la base de datos.\n\nDetalle: " + ex.getMessage(),
                    "Error BD",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // 3) Login → Session → Mainframe
        SwingUtilities.invokeLater(() -> {
            LoginDialog dlg = new LoginDialog(null);
            dlg.setVisible(true);
            if (dlg.getAutenticado() == null) {
                // canceló o falló
                return;
            }
            Session session = new Session(dlg.getAutenticado());
            new Mainframe(session).setVisible(true);
        });
    }
}
