package biblioteca.app;

import db.DbInit;
import session.Session;
import ui.LoginDialog;
import ui.Mainframe;
import ui.UIUtil;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main
 * ----
 * Punto de entrada de la aplicación de escritorio (Swing).
 *
 * Flujo general:
 *   1) Aplica Look & Feel (Nimbus).
 *   2) Inicializa la base de datos (crea tablas y seed si faltan).
 *   3) Muestra el Login (modal). Si autentica → crea {@link Session} y abre {@link Mainframe}.
 *   4) Si en el Mainframe el usuario elige "Cerrar sesión", se vuelve a mostrar el Login.
 *
 * Notas:
 *   - La lógica de “volver al login al hacer logout” se implementa escuchando el evento
 *     de cierre (windowClosed) del Mainframe y consultando {@code isLogoutRequested()}.
 */
public class Main {

    public static void main(String[] args) {
        // 1) Look & Feel (visual más moderno y consistente en Swing)
        UIUtil.applyNimbus();

        // 2) Inicializar base de datos (DDL + seed). Si falla, se informa y se aborta.
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
            return; // Abortamos la app si la base no se pudo preparar
        }

        // 3) Arranque del ciclo Login → Mainframe (con posibilidad de volver a Login por logout)
        SwingUtilities.invokeLater(Main::showLoginThenMain);
    }

    /**
     * Muestra el diálogo de Login; si autentica correctamente, abre el Mainframe.
     * Si el Mainframe se cierra con la bandera de logout activada, vuelve a invocar
     * este mismo método para reabrir el Login (ciclo de sesión).
     */
    private static void showLoginThenMain() {
        // --- LOGIN (modal: bloquea hasta que el usuario cierre el diálogo) ---
        LoginDialog dlg = new LoginDialog(null);
        dlg.setVisible(true);

        // Si canceló o el login falló, no hay usuario autenticado → terminar flujo.
        if (dlg.getAutenticado() == null) {
            return;
        }

        // --- SESIÓN + MAINFRAME ---
        Session session = new Session(dlg.getAutenticado());
        Mainframe mf = new Mainframe(session);

        // Hook: si el frame se cierra por "Cerrar sesión", reabrimos el login.
        mf.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (mf.isLogoutRequested()) {
                    // Vuelve al punto de entrada de sesión
                    SwingUtilities.invokeLater(Main::showLoginThenMain);
                }
                // Si NO es logout, simplemente no hacemos nada: la app finaliza.
            }
        });

        // Mostrar la ventana principal (no es modal)
        mf.setVisible(true);
    }
}
