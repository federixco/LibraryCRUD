package biblioteca.app;

import ui.Mainframe;
import ui.UIUtil;
import db.DbInit;

import javax.swing.*;

/**
 * Punto de entrada.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Working dir = " + System.getProperty("user.dir"));

        // Look & Feel más prolijo
        UIUtil.applyNimbus();

        // Inicializar BD
        try {
            DbInit.ensureInit();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            String rutaReal = System.getProperty("user.home") + "/.biblioteca/biblioteca.db";
            JOptionPane.showMessageDialog(
                null,
                "No pude abrir/crear la base de datos.\n" +
                "Ruta real del archivo:\n" + rutaReal + "\n\n" +
                "Detalle: " + ex.getMessage(),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        SwingUtilities.invokeLater(() -> new Mainframe().setVisible(true));
    }
}