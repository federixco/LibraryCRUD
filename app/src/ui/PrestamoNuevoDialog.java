package ui;

import dao.JdbcPrestamoDAO;
import service.PrestamoService;

import javax.swing.*;
import java.awt.*;

public class PrestamoNuevoDialog extends JDialog {

    private final JTextField txtLibro   = new JTextField(10);   // código del libro
    private final JTextField txtDest    = new JTextField(18);   // destinatario
    private final JSpinner   spCant     = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JSpinner   spDias     = new JSpinner(new SpinnerNumberModel(7, 1, 90, 1));

    private final PrestamoService svc   = new PrestamoService(new JdbcPrestamoDAO());
    private final String operadorUsername; // del Session

    public PrestamoNuevoDialog(Window owner, String operadorUsername) {
        super(owner, "Nuevo préstamo", ModalityType.APPLICATION_MODAL);
        this.operadorUsername = operadorUsername;

        setSize(420, 230);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        form.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        form.add(new JLabel("Código libro:"));   form.add(txtLibro);
        form.add(new JLabel("Destinatario:"));   form.add(txtDest);
        form.add(new JLabel("Cantidad:"));       form.add(spCant);
        form.add(new JLabel("Días de préstamo:")); form.add(spDias);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Prestar");
        JButton cancel = new JButton("Cancelar");
        btns.add(ok); btns.add(cancel);

        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(ok);

        ok.addActionListener(e -> onPrestar());
        cancel.addActionListener(e -> setVisible(false));
    }

    private void onPrestar() {
        try {
            String codigo = txtLibro.getText().trim();
            String dest   = txtDest.getText().trim();
            int cant      = (Integer) spCant.getValue();
            int dias      = (Integer) spDias.getValue();

            long id = svc.prestar(codigo, operadorUsername, dest, cant, dias);
            JOptionPane.showMessageDialog(this, "Préstamo generado (id=" + id + ").");
            setVisible(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
