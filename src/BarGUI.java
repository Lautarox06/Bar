import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

public class BarGUI extends JFrame {
    private BarManager barManager;

    private JPanel mesaSelectionPanel;
    private JPanel productDisplayPanel;
    private JTextArea statusArea;
    private JButton cerrarMesaBtn, abrirMesaBtn, happyHourToggleBtn, addProductoBtn, setHoraBtn;
    private JLabel sistemaHoraLabel;
    private Timer sistemaTimer; // NUEVO: Timer para el reloj del sistema

    private Mesa mesaActiva = null;
    private JLabel activeMesaLabel;
    private DefaultTableModel consumosTableModel;
    private JTable consumosTable;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public BarGUI() {
        barManager = new BarManager();

        setTitle("La Taberna - Sistema de Gestión de Bar");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel Superior (Header)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 40, 10));
        JLabel titleLabel = new JLabel("La Taberna", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 38));
        titleLabel.setForeground(new Color(255, 200, 100));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timePanel.setBackground(headerPanel.getBackground());

        sistemaHoraLabel = new JLabel("", SwingConstants.RIGHT);
        sistemaHoraLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        sistemaHoraLabel.setForeground(new Color(150, 255, 150));
        sistemaHoraLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        JLabel clockLabel = new JLabel(LocalTime.now().format(timeFormatter), SwingConstants.RIGHT);
        clockLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        timePanel.add(sistemaHoraLabel);
        timePanel.add(clockLabel);
        headerPanel.add(timePanel, BorderLayout.EAST);

        // Timer para el reloj real
        new Timer(1000, e -> clockLabel.setText(LocalTime.now().format(timeFormatter))).start();

        // NUEVO: Timer para el reloj del sistema
        sistemaTimer = new Timer(1000, e -> {
            barManager.tick(); // Avanza el tiempo en el manager
            updateSistemaHoraDisplay(); // Actualiza la etiqueta
            updateAllProductPrices(); // Actualiza los precios en los paneles de productos
        });
        sistemaTimer.start();

        add(headerPanel, BorderLayout.NORTH);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.2);
        mainSplitPane.setBorder(null);

        mesaSelectionPanel = new JPanel();
        mesaSelectionPanel.setLayout(new BoxLayout(mesaSelectionPanel, BoxLayout.Y_AXIS));
        JScrollPane mesaScrollPane = new JScrollPane(mesaSelectionPanel);
        mainSplitPane.setLeftComponent(mesaScrollPane);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        activeMesaLabel = new JLabel("Seleccione una mesa", SwingConstants.CENTER);
        activeMesaLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        contentPanel.add(activeMesaLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        productDisplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        JScrollPane productScrollPane = new JScrollPane(productDisplayPanel);
        tabbedPane.addTab("Registrar Consumo", productScrollPane);

        consumosTableModel = new DefaultTableModel(new String[]{"Producto", "Cantidad", "Hora", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        consumosTable = new JTable(consumosTableModel);
        tabbedPane.addTab("Ver Consumos", new JScrollPane(consumosTable));

        JPanel mesaActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        abrirMesaBtn = createStyledButton("Abrir Mesa", new Color(50, 150, 50));
        cerrarMesaBtn = createStyledButton("Cerrar Mesa", new Color(180, 50, 50));
        happyHourToggleBtn = createStyledButton("Happy Hour OFF", new Color(200, 150, 0));
        addProductoBtn = createStyledButton("Añadir Producto", new Color(0, 100, 150));
        setHoraBtn = createStyledButton("Fijar Hora", new Color(100, 100, 255));
        cerrarMesaBtn.setEnabled(false);

        abrirMesaBtn.addActionListener(e -> abrirMesa());
        cerrarMesaBtn.addActionListener(e -> cerrarMesa());
        happyHourToggleBtn.addActionListener(e -> toggleHappyHour());
        addProductoBtn.addActionListener(e -> addNuevoProducto());
        setHoraBtn.addActionListener(e -> setSistemaHora());

        mesaActionPanel.add(abrirMesaBtn);
        mesaActionPanel.add(cerrarMesaBtn);
        mesaActionPanel.add(happyHourToggleBtn);
        mesaActionPanel.add(addProductoBtn);
        mesaActionPanel.add(setHoraBtn);
        contentPanel.add(mesaActionPanel, BorderLayout.SOUTH);

        mainSplitPane.setRightComponent(contentPanel);
        add(mainSplitPane, BorderLayout.CENTER);

        statusArea = new JTextArea(5, 50);
        add(new JScrollPane(statusArea), BorderLayout.SOUTH);

        // Inicializaciones
        populateProductDisplayPanel();
        updateMesaButtons();
        updateStatus("Sistema iniciado.");
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    /**
     * Actualiza la etiqueta que muestra la hora del sistema.
     */
    private void updateSistemaHoraDisplay() {
        sistemaHoraLabel.setText("Sistema: " + barManager.getSistemaTime().format(timeFormatter));
    }

    /**
     * NUEVO: Actualiza el precio en todos los paneles de productos.
     */
    private void updateAllProductPrices() {
        int currentHour = barManager.getSistemaHora();
        boolean isHappyHour = barManager.isHappyHourActive();
        for (Component comp : productDisplayPanel.getComponents()) {
            if (comp instanceof ProductoPanel) {
                ((ProductoPanel) comp).updatePrice(currentHour, isHappyHour);
            }
        }
    }

    private void setSistemaHora() {
        String horaStr = JOptionPane.showInputDialog(this, "Ingrese la nueva hora del sistema (0-23):", String.valueOf(barManager.getSistemaHora()));
        if (horaStr != null && !horaStr.trim().isEmpty()) {
            try {
                int nuevaHora = Integer.parseInt(horaStr.trim());
                if (barManager.setSistemaHora(nuevaHora)) {
                    updateStatus("La hora del sistema se ha establecido en " + nuevaHora + ":00.");
                    updateSistemaHoraDisplay(); // Actualiza inmediatamente la pantalla
                } else {
                    JOptionPane.showMessageDialog(this, "Hora inválida. Debe ser entre 0 y 23.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void populateProductDisplayPanel() {
        productDisplayPanel.removeAll();
        for (Articulo art : barManager.getTodosLosArticulos().values()) {
            ProductoPanel pPanel = new ProductoPanel(art);
            pPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    registrarConsumoVisual(pPanel.getArticulo());
                }
            });
            productDisplayPanel.add(pPanel);
        }
        updateAllProductPrices(); // Poner los precios iniciales correctos
        productDisplayPanel.revalidate();
        productDisplayPanel.repaint();
    }

    private void toggleHappyHour() {
        barManager.toggleHappyHour();
        if (barManager.isHappyHourActive()) {
            happyHourToggleBtn.setText("Happy Hour ON");
            happyHourToggleBtn.setBackground(new Color(0, 150, 0));
            updateStatus("¡Happy Hour activado!");
        } else {
            happyHourToggleBtn.setText("Happy Hour OFF");
            happyHourToggleBtn.setBackground(new Color(200, 150, 0));
            updateStatus("Happy Hour desactivado.");
        }
        updateAllProductPrices(); // MODIFICADO: Actualizar precios al cambiar estado
    }

    private void registrarConsumoVisual(Articulo art) {
        if (mesaActiva == null || !mesaActiva.estaOcupada()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione y abra una mesa primero.", "Mesa no abierta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Producto: " + art.getDescripcion()));

        final int horaActualSistema = barManager.getSistemaHora();
        double currentCalculatedPrice = barManager.isHappyHourActive() ? art.getPrecioHappyHour() : art.getPrecioPorHora(horaActualSistema);

        panel.add(new JLabel(String.format("Precio actual (Hora: %d:00): $%.2f", horaActualSistema, currentCalculatedPrice)));

        JTextField cantidadField = new JTextField("1");
        JTextField horaField = new JTextField(String.valueOf(horaActualSistema));

        panel.add(new JLabel("Cantidad:"));
        panel.add(cantidadField);
        panel.add(new JLabel("Hora del consumo (0-23):"));
        panel.add(horaField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Registrar Consumo en Mesa " + mesaActiva.getNumero(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int cantidad = Integer.parseInt(cantidadField.getText());
                int hora = Integer.parseInt(horaField.getText());
                if (cantidad <= 0 || hora < 0 || hora > 23) throw new NumberFormatException("Datos fuera de rango.");

                if (barManager.agregarConsumoAMesa(mesaActiva.getNumero(), art.getCodigo(), cantidad, hora)) {
                    updateStatus("Consumo registrado: " + cantidad + "x " + art.getDescripcion() + " en mesa " + mesaActiva.getNumero());
                    selectMesa(mesaActiva);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Datos inválidos. Verifique cantidad y hora.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Otros métodos de la GUI (abrirMesa, cerrarMesa, etc.) permanecen sin cambios significativos...
    private void updateMesaButtons() {
        mesaSelectionPanel.removeAll();
        for (Mesa currentMesa : barManager.getTodasLasMesas()) {
            JButton mesaBtn = new JButton("Mesa " + currentMesa.getNumero());
            mesaBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            mesaBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            mesaBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
            mesaBtn.setForeground(Color.WHITE);
            mesaBtn.setFocusPainted(false);

            if (currentMesa.estaOcupada()) {
                mesaBtn.setBackground(new Color(255, 140, 0));
                mesaBtn.setText("Mesa " + currentMesa.getNumero() + " (Ocupada)");
            } else {
                mesaBtn.setBackground(new Color(100, 180, 100));
                mesaBtn.setText("Mesa " + currentMesa.getNumero() + " (Libre)");
            }

            mesaBtn.addActionListener(e -> selectMesa(currentMesa));
            mesaSelectionPanel.add(mesaBtn);
            mesaSelectionPanel.add(Box.createVerticalStrut(8));
        }
        mesaSelectionPanel.revalidate();
        mesaSelectionPanel.repaint();
    }

    private void selectMesa(Mesa mesa) {
        mesaActiva = mesa;
        if (mesa.estaOcupada()) {
            activeMesaLabel.setText("Mesa " + mesa.getNumero() + " - Abierta desde " + mesa.horaApertura + ":00");
            activeMesaLabel.setForeground(new Color(200, 0, 0));
        } else {
            activeMesaLabel.setText("Mesa " + mesa.getNumero() + " - Libre");
            activeMesaLabel.setForeground(new Color(0, 120, 0));
        }
        cerrarMesaBtn.setEnabled(mesa.estaOcupada());
        consumosTableModel.setRowCount(0);
        if (mesa.estaOcupada()) {
            for (Consumo c : mesa.getConsumos()) {
                consumosTableModel.addRow(new Object[]{
                        c.getArticulo().getDescripcion(),
                        c.getCantidad(),
                        String.format("%02d:00", c.getHoraConsumo()),
                        String.format("$%.2f", c.getSubtotal())
                });
            }
        }
        updateStatus("Mesa " + mesa.getNumero() + " seleccionada.");
    }

    private void abrirMesa() {
        List<Integer> mesaNumbers = barManager.getNumerosMesasLibres();
        if (mesaNumbers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todas las mesas están ocupadas.", "No hay mesas disponibles", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Integer[] availableMesaNumbers = mesaNumbers.toArray(new Integer[0]);
        Integer mesaNum = (Integer) JOptionPane.showInputDialog(this, "Seleccione mesa a abrir:", "Abrir Mesa", JOptionPane.QUESTION_MESSAGE, null, availableMesaNumbers, availableMesaNumbers[0]);
        if (mesaNum == null) return;

        String horaStr = JOptionPane.showInputDialog(this, "Hora de apertura (0-23):", String.valueOf(barManager.getSistemaHora()));
        if (horaStr == null || horaStr.isEmpty()) return;

        try {
            int hora = Integer.parseInt(horaStr);
            if (hora < 0 || hora > 23) throw new NumberFormatException("La hora debe estar entre 0 y 23.");
            if (barManager.abrirMesa(mesaNum, hora)) {
                updateStatus("Mesa " + mesaNum + " abierta a las " + hora + ":00");
                selectMesa(barManager.getMesa(mesaNum));
            } else {
                JOptionPane.showMessageDialog(this, "La mesa " + mesaNum + " ya está ocupada o no existe.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Hora inválida: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addNuevoProducto() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField codigoField = new JTextField(10), descField = new JTextField(20), pdField = new JTextField("0.0"), pnField = new JTextField("0.0"), phhField = new JTextField("0.0"), imgField = new JTextField("images/default.png");
        panel.add(new JLabel("Código:")); panel.add(codigoField);
        panel.add(new JLabel("Descripción:")); panel.add(descField);
        panel.add(new JLabel("Precio Diurno:")); panel.add(pdField);
        panel.add(new JLabel("Precio Nocturno:")); panel.add(pnField);
        panel.add(new JLabel("Precio Happy Hour:")); panel.add(phhField);
        panel.add(new JLabel("Ruta de Imagen:")); panel.add(imgField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Añadir Nuevo Producto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String codigo = codigoField.getText().trim(), descripcion = descField.getText().trim(), imagenPath = imgField.getText().trim();
                if (codigo.isEmpty() || descripcion.isEmpty()) { JOptionPane.showMessageDialog(this, "Código y Descripción no pueden estar vacíos.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                if (barManager.getArticulo(codigo) != null) { JOptionPane.showMessageDialog(this, "Ya existe un producto con el código '" + codigo + "'.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                double pDiurno = Double.parseDouble(pdField.getText().trim()), pNocturno = Double.parseDouble(pnField.getText().trim()), pHH = Double.parseDouble(phhField.getText().trim());
                if (pDiurno < 0 || pNocturno < 0 || pHH < 0) { JOptionPane.showMessageDialog(this, "Los precios no pueden ser negativos.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                if (imagenPath.isEmpty() || !new java.io.File(imagenPath).exists()) { imagenPath = "images/default.png"; }
                Articulo nuevoArticulo = new Articulo(codigo, descripcion, pDiurno, pNocturno, pHH, imagenPath);
                if (barManager.addArticulo(nuevoArticulo)) { updateStatus("Producto '" + descripcion + "' añadido."); populateProductDisplayPanel(); }
                else { JOptionPane.showMessageDialog(this, "Error al añadir el producto.", "Error", JOptionPane.ERROR_MESSAGE); }
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Valores de precio inválidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void cerrarMesa() {
        if (mesaActiva == null || !mesaActiva.estaOcupada()) {
            JOptionPane.showMessageDialog(this, "Seleccione una mesa abierta para cerrar.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Cerrar la Mesa " + mesaActiva.getNumero() + "?", "Confirmar Cierre", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String ticket = barManager.cerrarMesa(mesaActiva.getNumero());
            if (ticket != null) {
                JTextArea ticketArea = new JTextArea(ticket);
                ticketArea.setEditable(false);
                ticketArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                JScrollPane scrollPane = new JScrollPane(ticketArea);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                JOptionPane.showMessageDialog(this, scrollPane, "Ticket - Mesa " + mesaActiva.getNumero(), JOptionPane.INFORMATION_MESSAGE);
                updateStatus("Mesa " + mesaActiva.getNumero() + " cerrada.");
                selectMesa(null);
                activeMesaLabel.setText("Seleccione una mesa");
                activeMesaLabel.setForeground(new Color(80, 80, 80));
                consumosTableModel.setRowCount(0);
                cerrarMesaBtn.setEnabled(false);
            }
        }
    }

    private void updateStatus(String message) {
        statusArea.append("\n[" + LocalTime.now().format(timeFormatter) + "] " + message);
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
        updateMesaButtons();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BarGUI());
    }
}