package ui;

/**
 * @author Federico Gabriel Arena
 * @author Fabrizio Manuel Mansilla
 */

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * Utilidades de UI:
 *  - Aplica Nimbus si está disponible.
 *  - Aplica "zebra striping" a una JTable y ajustes visuales.
 *  - Setea anchos preferidos de columnas.
 */



public final class UIUtil {
    private UIUtil() {}

    /** Intenta aplicar Nimbus; si falla, deja el L&F por defecto. */
    public static void applyNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignore) {}
    }

    /** Zebra striping + tweaks de la JTable. */
    public static void styleTable(JTable table) {
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true); // ordenar por columnas
        table.setShowGrid(false);

        // Renderer base con zebra
        DefaultTableCellRenderer zebra = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, isSel, hasFocus, row, col);
                if (!isSel) {
                    Color odd  = new Color(0,0,0,0); // transparente para el L&F
                    Color even = new Color(0,0,0,10); // leve sombreado
                    c.setBackground((row % 2 == 0) ? even : odd);
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, zebra);

        // Centrar enteros
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Integer.class, center);

        // Boolean ya se renderiza con checkbox por defecto si la columna es Boolean.class
    }

    /** Setea anchos preferidos. */
    public static void setColumnWidths(JTable table, int... widths) {
        TableColumnModel cm = table.getColumnModel();
        for (int i = 0; i < widths.length && i < cm.getColumnCount(); i++) {
            if (widths[i] <= 0) continue;
            cm.getColumn(i).setPreferredWidth(widths[i]);
        }
    }
}