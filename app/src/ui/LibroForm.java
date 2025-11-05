package ui;


import model.Libro;

import javax.swing.*;
import java.awt.*;

/**
 * Clase: LibroForm
 * -----------------------
 * Diálogo modal de Swing para crear o editar un Libro.
 *  - Si el constructor recibe un Libro != null, precarga los campos (modo edición).
 *  - Si recibe null, queda listo para alta (modo nuevo).
 *
 * Interacción:
 *  - El botón "Aceptar" marca ok=true y cierra el diálogo.
 *  - Luego MainFrame consulta isOk() y, si es true, llama a getLibro() para obtener los datos.
 * 
 * 
 */

public class LibroForm extends JDialog {

    // --- Campos de entrada (componentes visuales) ---
    private final JTextField txtCodigo = new JTextField(10);
    private final JTextField txtTitulo = new JTextField(20);
    private final JTextField txtAutor = new JTextField(20);
    private final JTextField txtCategoria = new JTextField(15);
    private final JTextField txtEditorial = new JTextField(15);
    private final JSpinner spAnio = new JSpinner(new SpinnerNumberModel(2000, 0, 3000, 1));
    private final JSpinner spStock = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
    private final JCheckBox chkActivo = new JCheckBox("Activo", true);

    // Bandera para saber si el usuario aceptó
    private boolean ok = false;

    /**
     * Constructor.
     * @param owner ventana padre (para centrar y modalidad)
     * @param title título del diálogo
     * @param libro libro existente a editar; si es null, es alta
     */
    public LibroForm(Frame owner, String title, Libro libro) {
        super(owner, title, true); // true = modal
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10,10));

        // --- Panel de formulario con grid 2 columnas (etiqueta / campo) ---
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        form.add(new JLabel("Código:"));
        form.add(txtCodigo);

        form.add(new JLabel("Título:"));
        form.add(txtTitulo);

        form.add(new JLabel("Autor:"));
        form.add(txtAutor);

        form.add(new JLabel("Categoría:"));
        form.add(txtCategoria);

        form.add(new JLabel("Editorial:"));
        form.add(txtEditorial);

        form.add(new JLabel("Año:"));
        form.add(spAnio);

        form.add(new JLabel("Stock:"));
        form.add(spStock);

        form.add(new JLabel("")); // celda vacía para alinear
        form.add(chkActivo);

        // --- Panel de botones ---
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Aceptar");
        JButton btnCancel = new JButton("Cancelar");
        btns.add(btnOk);
        btns.add(btnCancel);

        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        // --- Si vino un libro, precargar datos (modo edición) ---
        if (libro != null) {
            txtCodigo.setText(libro.getCodigo());
            txtCodigo.setEnabled(false); // PK no editable al editar
            txtTitulo.setText(libro.getTitulo());
            txtAutor.setText(libro.getAutor());
            txtCategoria.setText(libro.getCategoria());
            txtEditorial.setText(libro.getEditorial());
            spAnio.setValue(libro.getAnio());
            spStock.setValue(libro.getStock());
            chkActivo.setSelected(libro.isActivo());
        }

        // --- Acciones de botones ---
        btnOk.addActionListener(e -> {
            // Validaciones mínimas de UI (las fuertes están en el Service)
            if (txtCodigo.isEnabled() && txtCodigo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El código es obligatorio.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtTitulo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El título es obligatorio.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtAutor.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El autor es obligatorio.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ok = true;   // marcamos que el usuario confirmó
            setVisible(false); // cerramos el diálogo
        });

        btnCancel.addActionListener(e -> {
            ok = false;           // el usuario canceló
            setVisible(false);    // cerramos sin cambios
        });
    }

    /** Indica si el usuario aceptó (true) o canceló (false). */
    public boolean isOk() {
        return ok;
    }

    /**
     * Construye y devuelve un objeto Libro con lo ingresado en el formulario.
     * Se llama desde MainFrame solo si isOk()==true.
     */
    public Libro getLibro() {
        Libro l = new Libro();
        l.setCodigo(txtCodigo.getText().trim());
        l.setTitulo(txtTitulo.getText().trim());
        l.setAutor(txtAutor.getText().trim());
        l.setCategoria(txtCategoria.getText().trim());
        l.setEditorial(txtEditorial.getText().trim());
        l.setAnio((Integer) spAnio.getValue());
        l.setStock((Integer) spStock.getValue());
        l.setActivo(chkActivo.isSelected());
        return l;
    }
}