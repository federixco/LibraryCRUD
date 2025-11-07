package biblioteca.app;

import db.DbInit;
import session.Session;
import ui.LoginDialog;
import ui.Mainframe;
import ui.UIUtil;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {

    public static void main(String[] args) {
        // 1) Look & Feel
        UIUtil.applyNimbus();

        // 2) Inicializar base
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

        // 3) Cadena Login -> Mainframe (y si hace logout, vuelve a login)
        SwingUtilities.invokeLater(Main::showLoginThenMain);
    }

    /** Muestra login; si ok, abre Mainframe.
     *  Si Mainframe se cierra por "Cerrar sesión", vuelve a invocar este método. */
    private static void showLoginThenMain() {
        // --- LOGIN (modal, bloquea hasta cerrar) ---
        LoginDialog dlg = new LoginDialog(null);
        dlg.setVisible(true);
        if (dlg.getAutenticado() == null) {
            // canceló o falló -> terminar app
            return;
        }

        // --- MAINFRAME ---
        Session session = new Session(dlg.getAutenticado());
        Mainframe mf = new Mainframe(session);

        // Cuando el frame se cierra (dispose), decidimos si reabrimos login
        mf.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                // Si pidió logout, volvemos a mostrar el login
                if (mf.isLogoutRequested()) {
                    SwingUtilities.invokeLater(Main::showLoginThenMain);
                }
                // Si NO pidió logout, no hacemos nada -> la app termina sola
            }
        });

        mf.setVisible(true); // NO bloquea; el flujo continúa por el listener de arriba cuando se cierre
    }
}
