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

/**
 * Clase principal de la interfaz gráfica de usuario para el sistema de gestión del bar.
 * Interactúa con BarManager para gestionar la lógica de negocio,
 * permitiendo al usuario abrir/cerrar mesas y registrar consumos de forma visual.
 */
public class BarGUI extends JFrame {
    private BarManager barManager; // Instancia de BarManager

    // Componentes visuales principales
    private JPanel mesaSelectionPanel; // Panel para botones de mesa
    private JPanel productDisplayPanel; // Panel para mostrar productos
    private JTextArea statusArea;
    private JButton cerrarMesaBtn;
    private JButton abrirMesaBtn;
    private JButton happyHourToggleBtn; // Botón para Happy Hour
    private JButton addProductoBtn; // NUEVO: Botón para añadir producto

    // Componentes para la mesa activa
    private Mesa mesaActiva = null; // La mesa actualmente seleccionada en la GUI
    private JLabel activeMesaLabel; // Muestra "Mesa X - Abierta desde Y"
    private DefaultTableModel consumosTableModel; // Modelo para la JTable de consumos
    private JTable consumosTable;

    /**
     * Constructor de la interfaz gráfica del bar.
     * Inicializa el BarManager y los componentes visuales de la aplicación.
     */
    public BarGUI() {
        barManager = new BarManager(); // Inicializar el BarManager

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

        JLabel clockLabel = new JLabel(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), SwingConstants.RIGHT);
        clockLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        clockLabel.setForeground(new Color(255, 255, 255));
        clockLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        headerPanel.add(clockLabel, BorderLayout.EAST);

        Timer timer = new Timer(1000, e -> clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        timer.start();

        add(headerPanel, BorderLayout.NORTH);

        // Panel Principal con Split Panes
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.2);
        mainSplitPane.setDividerSize(5);
        mainSplitPane.setBorder(null);

        // Panel Izquierdo: Selección de Mesas
        mesaSelectionPanel = new JPanel();
        mesaSelectionPanel.setLayout(new BoxLayout(mesaSelectionPanel, BoxLayout.Y_AXIS));
        mesaSelectionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Mesas", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 14), new Color(50, 50, 50)));
        JScrollPane mesaScrollPane = new JScrollPane(mesaSelectionPanel);
        mesaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mesaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainSplitPane.setLeftComponent(mesaScrollPane);

        // Panel Central-Derecho: Contenido de la mesa (Consumos y Productos)
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        activeMesaLabel = new JLabel("Seleccione una mesa", SwingConstants.CENTER);
        activeMesaLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        activeMesaLabel.setForeground(new Color(80, 80, 80));
        contentPanel.add(activeMesaLabel, BorderLayout.NORTH);

        // Pestañas para consumo y detalle de consumos
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 14));
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        // Pestaña "Registrar Consumo"
        productDisplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        productDisplayPanel.setBackground(new Color(245, 245, 245));
        JScrollPane productScrollPane = new JScrollPane(productDisplayPanel);
        productScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Seleccionar Producto", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 14), new Color(50, 50, 50)));
        tabbedPane.addTab("Registrar Consumo", productScrollPane);
        populateProductDisplayPanel(); // Llenar con ProductoPanels

        // Pestaña "Ver Consumos" (JTable)
        String[] columnNames = {"Producto", "Cantidad", "Hora", "Subtotal"};
        consumosTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        consumosTable = new JTable(consumosTableModel);
        consumosTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        consumosTable.setRowHeight(25);
        consumosTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        consumosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane consumosScrollPane = new JScrollPane(consumosTable);
        consumosScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Consumos Actuales", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 14), new Color(50, 50, 50)));
        tabbedPane.addTab("Ver Consumos", consumosScrollPane);

        // Panel para botones de acción (Abriir, Cerrar, Happy Hour, Añadir Producto)
        JPanel mesaActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        abrirMesaBtn = new JButton("Abrir Mesa");
        abrirMesaBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        abrirMesaBtn.setBackground(new Color(50, 150, 50));
        abrirMesaBtn.setForeground(Color.WHITE);
        abrirMesaBtn.setFocusPainted(false);

        cerrarMesaBtn = new JButton("Cerrar Mesa");
        cerrarMesaBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        cerrarMesaBtn.setBackground(new Color(180, 50, 50));
        cerrarMesaBtn.setForeground(Color.WHITE);
        cerrarMesaBtn.setFocusPainted(false);
        cerrarMesaBtn.setEnabled(false);

        happyHourToggleBtn = new JButton("Happy Hour OFF"); // Estado inicial
        happyHourToggleBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        happyHourToggleBtn.setBackground(new Color(200, 150, 0)); // Naranja por defecto
        happyHourToggleBtn.setForeground(Color.WHITE);
        happyHourToggleBtn.setFocusPainted(false);

        // NUEVO: Botón para añadir producto
        addProductoBtn = new JButton("Añadir Producto");
        addProductoBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        addProductoBtn.setBackground(new Color(0, 100, 150)); // Azul
        addProductoBtn.setForeground(Color.WHITE);
        addProductoBtn.setFocusPainted(false);

        abrirMesaBtn.addActionListener(e -> abrirMesa());
        cerrarMesaBtn.addActionListener(e -> cerrarMesa());
        happyHourToggleBtn.addActionListener(e -> toggleHappyHour());
        addProductoBtn.addActionListener(e -> addNuevoProducto()); // Listener para el nuevo botón

        mesaActionPanel.add(abrirMesaBtn);
        mesaActionPanel.add(cerrarMesaBtn);
        mesaActionPanel.add(happyHourToggleBtn);
        mesaActionPanel.add(addProductoBtn); // Añadir el nuevo botón
        contentPanel.add(mesaActionPanel, BorderLayout.SOUTH);

        mainSplitPane.setRightComponent(contentPanel);
        add(mainSplitPane, BorderLayout.CENTER);

        // Panel Inferior: Status Area
        statusArea = new JTextArea(5, 50);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusArea.setBackground(new Color(230, 230, 230));
        statusArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Registro de Eventos", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 14), new Color(50, 50, 50)));
        add(statusScrollPane, BorderLayout.SOUTH);

        // Inicializar estado de mesas en el panel de selección
        updateMesaButtons();
        updateStatus("Sistema iniciado. Seleccione una mesa para comenzar.");

        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Actualiza los botones que representan las mesas en el panel de selección.
     * Cada botón refleja el estado actual de la mesa (ocupada/libre).
     */
    private void updateMesaButtons() {
        mesaSelectionPanel.removeAll();
        for (Mesa currentMesa : barManager.getTodasLasMesas()) {
            JButton mesaBtn = new JButton("Mesa " + currentMesa.getNumero());
            mesaBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            mesaBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            mesaBtn.setMinimumSize(new Dimension(100, 60));
            mesaBtn.setPreferredSize(new Dimension(150, 60));
            mesaBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
            mesaBtn.setForeground(Color.WHITE);
            mesaBtn.setFocusPainted(false);
            mesaBtn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            if (currentMesa.estaOcupada()) {
                mesaBtn.setBackground(new Color(255, 140, 0)); // Naranja oscuro para ocupadas
                mesaBtn.setText("Mesa " + currentMesa.getNumero() + " (Ocupada)");
            } else {
                mesaBtn.setBackground(new Color(100, 180, 100)); // Verde claro para libres
                mesaBtn.setText("Mesa " + currentMesa.getNumero() + " (Libre)");
            }

            mesaBtn.addActionListener(e -> selectMesa(currentMesa));
            mesaSelectionPanel.add(mesaBtn);
            mesaSelectionPanel.add(Box.createVerticalStrut(8));
        }
        mesaSelectionPanel.revalidate();
        mesaSelectionPanel.repaint();
    }

    /**
     * Selecciona una mesa y actualiza la interfaz para mostrar su información y consumos.
     * @param mesa La mesa seleccionada.
     */
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

        // Actualizar la tabla de consumos
        consumosTableModel.setRowCount(0);
        if (mesa.estaOcupada()) {
            for (Consumo c : mesa.getConsumos()) {
                double subtotal = c.getSubtotal();
                consumosTableModel.addRow(new Object[]{
                        c.getArticulo().getDescripcion(),
                        c.getCantidad(),
                        String.format("%02d:00", c.getHoraConsumo()),
                        String.format("$%.2f", subtotal)
                });
            }
        }
        updateStatus("Mesa " + mesa.getNumero() + " seleccionada.");
    }

    /**
     * Llena el panel de visualización de productos con `ProductoPanel` interactivos.
     */
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
        productDisplayPanel.revalidate();
        productDisplayPanel.repaint();
    }

    /**
     * Permite al usuario abrir una mesa. Solicita el número de mesa y la hora de apertura.
     */
    private void abrirMesa() {
        List<Integer> mesaNumbers = barManager.getNumerosMesasLibres();

        if (mesaNumbers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todas las mesas están ocupadas.", "No hay mesas disponibles", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Integer[] availableMesaNumbers = mesaNumbers.toArray(new Integer[0]);

        Integer mesaNum = (Integer) JOptionPane.showInputDialog(
                this,
                "Seleccione mesa a abrir:",
                "Abrir Mesa",
                JOptionPane.QUESTION_MESSAGE,
                null,
                availableMesaNumbers,
                availableMesaNumbers[0]
        );

        if (mesaNum == null) return;

        String horaStr = JOptionPane.showInputDialog(this, "Hora de apertura (0-23):", String.valueOf(LocalTime.now().getHour()));
        if (horaStr == null || horaStr.isEmpty()) return;

        try {
            int hora = Integer.parseInt(horaStr);
            if (hora < 0 || hora > 23) throw new NumberFormatException("La hora debe estar entre 0 y 23.");

            if (barManager.abrirMesa(mesaNum, hora)) {
                updateStatus("Mesa " + mesaNum + " abierta a las " + hora + ":00");
                selectMesa(barManager.getMesa(mesaNum));
            } else {
                JOptionPane.showMessageDialog(this, "La mesa " + mesaNum + " ya está ocupada o no existe.", "Error al abrir mesa", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Hora inválida: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Permite al usuario registrar un consumo en la mesa actualmente seleccionada.
     * @param art El artículo seleccionado para el consumo.
     */
    private void registrarConsumoVisual(Articulo art) {
        if (mesaActiva == null || !mesaActiva.estaOcupada()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione y abra una mesa primero para registrar consumos.", "Mesa no seleccionada/abierta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Producto: " + art.getDescripcion()));

        // Mostrar precio actual basado en el estado de Happy Hour y la hora actual
        double currentCalculatedPrice;
        if (barManager.isHappyHourActive()) {
            currentCalculatedPrice = art.getPrecioHappyHour();
        } else {
            currentCalculatedPrice = art.getPrecioPorHora(LocalTime.now().getHour());
        }
        panel.add(new JLabel("Precio actual: $" + String.format("%.2f", currentCalculatedPrice)));

        JTextField cantidadField = new JTextField("1");
        JTextField horaField = new JTextField(String.valueOf(LocalTime.now().getHour()));

        panel.add(new JLabel("Cantidad:"));
        panel.add(cantidadField);
        panel.add(new JLabel("Hora del consumo (0-23):"));
        panel.add(horaField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Registrar Consumo de " + art.getDescripcion() + " en Mesa " + mesaActiva.getNumero(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int cantidad = Integer.parseInt(cantidadField.getText());
                int hora = Integer.parseInt(horaField.getText());

                if (cantidad <= 0) throw new NumberFormatException("Cantidad debe ser mayor a 0.");
                if (hora < 0 || hora > 23) throw new NumberFormatException("Hora debe ser entre 0 y 23.");

                // Llamar a agregarConsumoAMesa; el BarManager ahora calculará el precio final
                if (barManager.agregarConsumoAMesa(mesaActiva.getNumero(), art.getCodigo(), cantidad, hora)) {
                    updateStatus("Consumo registrado: " + cantidad + "x " + art.getDescripcion() + " en mesa " + mesaActiva.getNumero());
                    selectMesa(mesaActiva); // Refrescar la tabla de consumos de la mesa activa
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo registrar el consumo. Asegúrese de que la mesa esté activa.", "Error de Consumo", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Datos inválidos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * NUEVO MÉTODO: Permite al usuario añadir un nuevo producto al catálogo.
     */
    private void addNuevoProducto() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5)); // 0 filas, 2 columnas, espacio 5x5

        JTextField codigoField = new JTextField(10);
        JTextField descField = new JTextField(20);
        JTextField pdField = new JTextField("0.0"); // Precio Diurno
        JTextField pnField = new JTextField("0.0"); // Precio Nocturno
        JTextField phhField = new JTextField("0.0"); // Precio Happy Hour
        JTextField imgField = new JTextField("images/default.png"); // Ruta de Imagen (puede ser un icono por defecto)

        panel.add(new JLabel("Código:"));
        panel.add(codigoField);
        panel.add(new JLabel("Descripción:"));
        panel.add(descField);
        panel.add(new JLabel("Precio Diurno:"));
        panel.add(pdField);
        panel.add(new JLabel("Precio Nocturno:"));
        panel.add(pnField);
        panel.add(new JLabel("Precio Happy Hour:"));
        panel.add(phhField);
        panel.add(new JLabel("Ruta de Imagen:"));
        panel.add(imgField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Añadir Nuevo Producto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String codigo = codigoField.getText().trim();
                String descripcion = descField.getText().trim();
                double precioDiurno = Double.parseDouble(pdField.getText().trim());
                double precioNocturno = Double.parseDouble(pnField.getText().trim());
                double precioHappyHour = Double.parseDouble(phhField.getText().trim());
                String imagenPath = imgField.getText().trim();

                // Validaciones
                if (codigo.isEmpty() || descripcion.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Código y Descripción no pueden estar vacíos.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (barManager.getArticulo(codigo) != null) {
                    JOptionPane.showMessageDialog(this, "Ya existe un producto con el código '" + codigo + "'.", "Código Duplicado", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (precioDiurno < 0 || precioNocturno < 0 || precioHappyHour < 0) {
                    JOptionPane.showMessageDialog(this, "Los precios no pueden ser negativos.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Usar una imagen por defecto si la ruta está vacía o el archivo no existe
                if (imagenPath.isEmpty() || !new java.io.File(imagenPath).exists()) {
                    imagenPath = "images/default.png"; // Asegúrate de tener una imagen "default.png" en la carpeta "images"
                }


                Articulo nuevoArticulo = new Articulo(codigo, descripcion, precioDiurno, precioNocturno, precioHappyHour, imagenPath);

                if (barManager.addArticulo(nuevoArticulo)) {
                    updateStatus("Producto '" + descripcion + "' (" + codigo + ") añadido con éxito.");
                    populateProductDisplayPanel(); // Refrescar el panel de productos para mostrar el nuevo
                } else {
                    JOptionPane.showMessageDialog(this, "Error al añadir el producto.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, introduce valores numéricos válidos para los precios.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    /**
     * Alterna el estado de Happy Hour y actualiza el botón.
     */
    private void toggleHappyHour() {
        barManager.toggleHappyHour();
        if (barManager.isHappyHourActive()) {
            happyHourToggleBtn.setText("Happy Hour ON");
            happyHourToggleBtn.setBackground(new Color(0, 150, 0)); // Verde cuando activo
            updateStatus("¡Happy Hour activado!");
        } else {
            happyHourToggleBtn.setText("Happy Hour OFF");
            happyHourToggleBtn.setBackground(new Color(200, 150, 0)); // Naranja cuando inactivo
            updateStatus("Happy Hour desactivado.");
        }
    }


    /**
     * Cierra la mesa actualmente seleccionada y muestra el ticket.
     */
    private void cerrarMesa() {
        if (mesaActiva == null || !mesaActiva.estaOcupada()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una mesa abierta para cerrar.", "Error al cerrar", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea cerrar la Mesa " + mesaActiva.getNumero() + "?", "Confirmar Cierre", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String ticket = barManager.cerrarMesa(mesaActiva.getNumero());
            if (ticket != null) {
                JTextArea ticketArea = new JTextArea(ticket);
                ticketArea.setEditable(false);
                ticketArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                ticketArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JScrollPane scrollPane = new JScrollPane(ticketArea);
                scrollPane.setPreferredSize(new Dimension(400, 300));

                JOptionPane.showMessageDialog(this, scrollPane, "Ticket - Mesa " + mesaActiva.getNumero(), JOptionPane.INFORMATION_MESSAGE);
                updateStatus("Mesa " + mesaActiva.getNumero() + " cerrada.");

                mesaActiva = null; // Deseleccionar mesa activa
                activeMesaLabel.setText("Seleccione una mesa");
                activeMesaLabel.setForeground(new Color(80, 80, 80));
                consumosTableModel.setRowCount(0);
                cerrarMesaBtn.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo cerrar la mesa. Asegúrese de que la mesa esté activa y exista.", "Error al cerrar mesa", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Actualiza el área de estado con un nuevo mensaje y refresca la visualización de los botones de mesa.
     * @param message El mensaje a añadir al área de estado.
     */
    private void updateStatus(String message) {
        statusArea.append("\n[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message);
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
        updateMesaButtons();
    }

    /**
     * Método principal para iniciar la aplicación GUI del bar.
     * @param args Argumentos de la línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BarGUI());
    }
}