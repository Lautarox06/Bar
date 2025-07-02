package bar;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interfaz grafica principal del sistema de gestion de bar.
 * Permite visualizar y operar sobre mesas, productos y consumos.
 * Soporta funciones como abrir/cerrar mesas, añadir o modificar articulos,
 * gestionar consumos, y activar/desactivar Happy Hour.
 *
 * Usa Swing para construir la interfaz visual y se apoya en BarManager
 * para la logica de negocio.
 */
public class  BarGUI extends JFrame {
    private BarManager barManager;

    // Componentes visuales
    private JPanel mesaSelectionPanel;
    private JLayeredPane productDisplayLayeredPane; // Para el efecto de transparencia
    private JPanel productDisplayPanel;
    private JPanel overlayPanel; // Panel para el efecto de transparencia
    private JTextArea statusArea;
    private JButton mainMesaActionButton, happyHourToggleBtn, addProductoBtn, setHoraBtn, eliminarProductoBtn, removerConsumoBtn, modificarProductoBtn; // AGREGADO: modificarProductoBtn
    private JLabel sistemaHoraLabel;
    private JLabel activeMesaLabel;
    private Timer sistemaTimer;

    // Modelos de datos de la GUI
    private Mesa mesaActiva = null;
    private DefaultTableModel consumosTableModel;
    private JTable consumosTable;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Constructor principal de la interfaz grafica.
     */
    public BarGUI() {
        barManager = new BarManager();

        setTitle("La Taberna - Sistema de Gestión de Bar");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // --- Panel Superior (Header) ---
        setupHeaderPanel();

        // --- Panel Principal (Split Pane) ---
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.2);
        mainSplitPane.setBorder(null);

        // Panel Izquierdo: Selección de Mesas
        mesaSelectionPanel = new JPanel();
        mesaSelectionPanel.setLayout(new BoxLayout(mesaSelectionPanel, BoxLayout.Y_AXIS));
        mesaSelectionPanel.setBorder(BorderFactory.createTitledBorder("Mesas"));
        JScrollPane mesaScrollPane = new JScrollPane(mesaSelectionPanel);
        mainSplitPane.setLeftComponent(mesaScrollPane);

        // Panel Central-Derecho: Contenido
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        activeMesaLabel = new JLabel("Seleccione una mesa", SwingConstants.CENTER);
        activeMesaLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        contentPanel.add(activeMesaLabel, BorderLayout.NORTH);

        // Pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        setupTabbedPane(tabbedPane);
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        // Panel de Acciones (Botones)
        setupActionPanel(contentPanel);

        mainSplitPane.setRightComponent(contentPanel);
        add(mainSplitPane, BorderLayout.CENTER);

        // --- Panel Inferior (Status Area) ---
        statusArea = new JTextArea(5, 50);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setBorder(BorderFactory.createTitledBorder("Registro de Eventos"));
        add(statusScrollPane, BorderLayout.SOUTH);

        // Inicializaciones finales
        populateProductDisplayPanel();
        updateMesaButtons();
        updateSistemaHoraDisplay();
        updateStatus("Sistema iniciado.");
        selectMesa(null); // Asegura que el panel de productos esté inicialmente deshabilitado
        setVisible(true);
    }

    /**
     * Configura el panel superior con el titulo y los relojes.
     */
    private void setupHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 40, 10));
        JLabel titleLabel = new JLabel("La Taberna", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 38));
        titleLabel.setForeground(new Color(255, 200, 100));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timePanel.setBackground(headerPanel.getBackground());
        sistemaHoraLabel = new JLabel("", SwingConstants.RIGHT);
        sistemaHoraLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        sistemaHoraLabel.setForeground(new Color(150, 255, 150));
        sistemaHoraLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        timePanel.add(sistemaHoraLabel);

        JLabel clockLabel = new JLabel(LocalTime.now().format(timeFormatter), SwingConstants.RIGHT);
        clockLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        clockLabel.setForeground(Color.WHITE);
        timePanel.add(clockLabel);
        headerPanel.add(timePanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        new Timer(1000, e -> clockLabel.setText(LocalTime.now().format(timeFormatter))).start();
        sistemaTimer = new Timer(1000, e -> {
            barManager.tick();
            updateSistemaHoraDisplay();
            updateAllProductPrices();
        });
        sistemaTimer.start();
    }

    /**
     * Configura las pestañas de la aplicación.
     * @param tabbedPane El JTabbedPane a configurar.
     */
    private void setupTabbedPane(JTabbedPane tabbedPane) {
        // Pestaña 1: Registrar Consumo
        productDisplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        productDisplayPanel.setBackground(Color.WHITE); // Fondo blanco para los productos

        // Overlay panel para el efecto de transparencia
        overlayPanel = new JPanel();
        overlayPanel.setBackground(new Color(100, 100, 100, 150)); // Gris transparente
        overlayPanel.setOpaque(true);
        overlayPanel.setVisible(true); // Inicialmente visible (deshabilitado)

        productDisplayLayeredPane = new JLayeredPane();
        productDisplayLayeredPane.setPreferredSize(new Dimension(800, 400)); // Ajustar si es necesario
        productDisplayLayeredPane.setLayout(new OverlayLayout(productDisplayLayeredPane)); // Usa OverlayLayout

        JScrollPane productScrollPane = new JScrollPane(productDisplayPanel);
        productScrollPane.setBorder(BorderFactory.createTitledBorder("Seleccionar Producto"));
        productScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        productScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        productDisplayLayeredPane.add(productScrollPane, JLayeredPane.DEFAULT_LAYER);
        productDisplayLayeredPane.add(overlayPanel, JLayeredPane.PALETTE_LAYER); // Overlay encima

        tabbedPane.addTab("Registrar Consumo", productDisplayLayeredPane);

        // Pestaña 2: Ver Consumos
        JPanel consumosPanel = new JPanel(new BorderLayout(5, 5));
        consumosPanel.setBorder(BorderFactory.createTitledBorder("Consumos Actuales"));

        String[] columnNames = {"Producto", "Detalle", "Cantidad", "Hora", "Subtotal"};
        consumosTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        consumosTable = new JTable(consumosTableModel);
        consumosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        consumosPanel.add(new JScrollPane(consumosTable), BorderLayout.CENTER);

        removerConsumoBtn = new JButton("Remover Consumo Seleccionado");
        removerConsumoBtn.addActionListener(e -> removerConsumo());
        JPanel removerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        removerPanel.add(removerConsumoBtn);
        consumosPanel.add(removerPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Ver Consumos", consumosPanel);
    }

    /**
     * Configura el panel inferior de botones de acción.
     * @param parentPanel El panel al que se agregarán los botones.
     */
    private void setupActionPanel(JPanel parentPanel) {
        JPanel mesaActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        mainMesaActionButton = createStyledButton("Abrir/Cerrar Mesa", new Color(100, 100, 100)); // Botón inicial neutral

        happyHourToggleBtn = createStyledButton("Happy Hour OFF", new Color(200, 150, 0));
        addProductoBtn = createStyledButton("Añadir Producto", new Color(0, 100, 150));
        modificarProductoBtn = createStyledButton("Modificar Producto", new Color(0, 150, 150)); // NUEVO BOTÓN
        eliminarProductoBtn = createStyledButton("Eliminar Producto", new Color(150, 0, 0));
        setHoraBtn = createStyledButton("Fijar Hora", new Color(100, 100, 255));

        mainMesaActionButton.addActionListener(e -> handleMesaAction());
        happyHourToggleBtn.addActionListener(e -> toggleHappyHour());
        addProductoBtn.addActionListener(e -> addNuevoProducto());
        modificarProductoBtn.addActionListener(e -> modificarProducto()); // Acción para el nuevo botón
        eliminarProductoBtn.addActionListener(e -> eliminarProducto());
        setHoraBtn.addActionListener(e -> setSistemaHora());

        mesaActionPanel.add(mainMesaActionButton);
        mesaActionPanel.add(happyHourToggleBtn);
        mesaActionPanel.add(addProductoBtn);
        mesaActionPanel.add(modificarProductoBtn); // Añadir el nuevo botón al panel
        mesaActionPanel.add(eliminarProductoBtn);
        mesaActionPanel.add(setHoraBtn);
        parentPanel.add(mesaActionPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private void updateSistemaHoraDisplay() {
        sistemaHoraLabel.setText("Sistema: " + barManager.getSistemaTime().format(timeFormatter));
    }

    private void updateAllProductPrices() {
        int currentHour = barManager.getSistemaHora();
        boolean isHappyHour = barManager.isHappyHourActive();
        for (Component comp : productDisplayPanel.getComponents()) {
            if (comp instanceof ProductoPanel) {
                ((ProductoPanel) comp).updatePrice(currentHour, isHappyHour);
            }
        }
    }

    private void populateProductDisplayPanel() {
        productDisplayPanel.removeAll();
        for (Articulo art : barManager.getTodosLosArticulos().values()) {
            ProductoPanel pPanel = new ProductoPanel(art);
            pPanel.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { registrarConsumoVisual(pPanel.getArticulo()); }
            });
            productDisplayPanel.add(pPanel);
        }
        updateAllProductPrices();
        productDisplayPanel.revalidate();
        productDisplayPanel.repaint();
    }

    private void setSistemaHora() {
        String horaStr = JOptionPane.showInputDialog(this, "Ingrese la nueva hora del sistema (0-23):", String.valueOf(barManager.getSistemaHora()));
        if (horaStr != null && !horaStr.trim().isEmpty()) {
            try {
                int nuevaHora = Integer.parseInt(horaStr.trim());
                if (barManager.setSistemaHora(nuevaHora)) {
                    updateStatus("La hora del sistema se ha establecido en " + nuevaHora + ":00.");
                    updateSistemaHoraDisplay();
                } else {
                    JOptionPane.showMessageDialog(this, "Hora inválida. Debe ser entre 0 y 23.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void selectMesa(Mesa mesa) {
        mesaActiva = mesa;

        // Actualizar el estado del label de la mesa activa
        if (mesa != null) {
            if (mesa.estaOcupada()) {
                // Formatear la hora de apertura con minutos
                String horaAperturaFormateada = mesa.getHoraApertura().format(DateTimeFormatter.ofPattern("HH:mm"));
                activeMesaLabel.setText("Mesa " + mesa.getNumero() + " - Abierta desde " + horaAperturaFormateada);
                activeMesaLabel.setForeground(new Color(200, 0, 0));
                // Habilitar panel de productos y ocultar overlay
                overlayPanel.setVisible(false);
                removerConsumoBtn.setEnabled(true);
            } else {
                activeMesaLabel.setText("Mesa " + mesa.getNumero() + " - Libre");
                activeMesaLabel.setForeground(new Color(0, 120, 0));
                // Deshabilitar panel de productos y mostrar overlay
                overlayPanel.setVisible(true);
                removerConsumoBtn.setEnabled(false);
            }
        } else {
            activeMesaLabel.setText("Seleccione una mesa");
            activeMesaLabel.setForeground(new Color(0, 120, 0));
            // Deshabilitar panel de productos y mostrar overlay
            overlayPanel.setVisible(true);
            removerConsumoBtn.setEnabled(false);
        }

        // Actualizar el botón principal de acción de mesa
        updateMainMesaActionButton();

        // Limpiar y poblar la tabla de consumos
        consumosTableModel.setRowCount(0);
        if (mesa != null && mesa.estaOcupada()) {
            for (Consumo c : mesa.getConsumos()) {
                consumosTableModel.addRow(new Object[]{
                        c.getArticulo().getDescripcion(),
                        c.getDetalle(),
                        c.getCantidad(),
                        c.getHoraConsumoFormateada(), // Usa el método formateado
                        String.format("$%.2f", c.getSubtotal())
                });
            }
        }
        if (mesa != null) {
            updateStatus("Mesa " + mesa.getNumero() + " seleccionada.");
        }
    }

    private void updateMainMesaActionButton() {
        if (mesaActiva == null) {
            mainMesaActionButton.setText("Seleccione Mesa");
            mainMesaActionButton.setBackground(new Color(100, 100, 100)); // Gris neutro
            mainMesaActionButton.setEnabled(false);
        } else if (mesaActiva.estaOcupada()) {
            mainMesaActionButton.setText("Cerrar Mesa " + mesaActiva.getNumero());
            mainMesaActionButton.setBackground(new Color(180, 50, 50)); // Rojo para cerrar
            mainMesaActionButton.setEnabled(true);
        } else {
            mainMesaActionButton.setText("Abrir Mesa " + mesaActiva.getNumero());
            mainMesaActionButton.setBackground(new Color(50, 150, 50)); // Verde para abrir
            mainMesaActionButton.setEnabled(true);
        }
    }

    private void handleMesaAction() {
        if (mesaActiva == null) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una mesa primero.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (mesaActiva.estaOcupada()) {
            cerrarMesa();
        } else {
            abrirMesa();
        }
    }

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

    private void registrarConsumoVisual(Articulo art) {
        if (mesaActiva == null || !mesaActiva.estaOcupada()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione y abra una mesa primero.", "Mesa no abierta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Registrar Consumo: " + art.getDescripcion(), true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qtyPanel.add(new JLabel("Cantidad:"));
        JSpinner cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        qtyPanel.add(cantidadSpinner);
        mainPanel.add(qtyPanel);
        mainPanel.add(new JSeparator());

        List<JComponent> specControls = new ArrayList<>();
        if (art.getEspecificaciones().isEmpty()) {
            mainPanel.add(new JLabel("No hay especificaciones disponibles."));
            mainPanel.add(Box.createVerticalStrut(10));
        } else {
            for (Especificacion spec : art.getEspecificaciones()) {
                JPanel specPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                specPanel.add(new JLabel(spec.getNombre() + " (+" + String.format("$%.2f", spec.getPrecioPorUnidad()) + "):"));

                if (spec.getTipo() == TipoEspecificacion.CANTIDAD) {
                    JSpinner specSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
                    specPanel.add(specSpinner);
                    specControls.add(specSpinner);
                } else { // SI_NO
                    JCheckBox checkBox = new JCheckBox();
                    specPanel.add(checkBox);
                    specControls.add(checkBox);
                }
                mainPanel.add(specPanel);
            }
        }

        mainPanel.add(Box.createVerticalGlue());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JLabel totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("Aceptar");
        JButton cancelButton = new JButton("Cancelar");
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        bottomPanel.add(totalLabel, BorderLayout.WEST);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        Runnable updatePriceAction = () -> {
            double precioBase = barManager.isHappyHourActive() ? art.getPrecioHappyHour() : art.getPrecioPorHora(barManager.getSistemaHora());
            double costoOpciones = 0;

            for (int i = 0; i < art.getEspecificaciones().size(); i++) {
                Especificacion spec = art.getEspecificaciones().get(i);
                JComponent control = specControls.get(i);
                int value = 0;
                if (control instanceof JSpinner) {
                    value = (Integer) ((JSpinner) control).getValue();
                } else if (control instanceof JCheckBox) {
                    value = ((JCheckBox) control).isSelected() ? 1 : 0;
                }
                costoOpciones += spec.getPrecioPorUnidad() * value;
            }

            double precioUnitarioFinal = precioBase + costoOpciones;
            int cantidadTotal = (Integer) cantidadSpinner.getValue();
            double precioTotal = precioUnitarioFinal * cantidadTotal;

            totalLabel.setText(String.format("Total: $%.2f", precioTotal));
        };

        cantidadSpinner.addChangeListener(e -> updatePriceAction.run());
        specControls.forEach(c -> {
            if (c instanceof JSpinner) ((JSpinner) c).addChangeListener(e -> updatePriceAction.run());
            if (c instanceof JCheckBox) ((JCheckBox) c).addActionListener(e -> updatePriceAction.run());
        });

        updatePriceAction.run();

        okButton.addActionListener(e -> {
            Map<Especificacion, Integer> opcionesSeleccionadas = new HashMap<>();
            for (int i = 0; i < art.getEspecificaciones().size(); i++) {
                Especificacion spec = art.getEspecificaciones().get(i);
                JComponent control = specControls.get(i);
                int value = 0;
                if (control instanceof JSpinner) value = (Integer) ((JSpinner) control).getValue();
                if (control instanceof JCheckBox) value = ((JCheckBox) control).isSelected() ? 1 : 0;

                // Solo añadir opciones que tengan un valor (ej. spinner > 0 o checkbox seleccionado)
                if (value > 0) {
                    opcionesSeleccionadas.put(spec, value);
                }
            }

            int cantidad = (Integer) cantidadSpinner.getValue();
            double precioBase = barManager.isHappyHourActive() ? art.getPrecioHappyHour() : art.getPrecioPorHora(barManager.getSistemaHora());

            barManager.agregarConsumoAMesa(mesaActiva.getNumero(), art, cantidad, precioBase, opcionesSeleccionadas); // Se pasa el objeto Articulo completo
            updateStatus("Consumo registrado: " + cantidad + "x " + art.getDescripcion() + (opcionesSeleccionadas.isEmpty() ? "" : " con especificaciones."));
            selectMesa(mesaActiva);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.pack(); // Ajusta el tamaño de la ventana al contenido
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Boton para eliminar un consumo registrado en la mesa activa.
     */
    private void removerConsumo() {
        if (mesaActiva == null || !mesaActiva.estaOcupada()) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una mesa activa.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = consumosTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un consumo de la tabla para remover.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea remover el consumo seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (barManager.removerConsumoDeMesa(mesaActiva.getNumero(), selectedRow)) {
                updateStatus("Consumo removido de la mesa " + mesaActiva.getNumero());
                selectMesa(mesaActiva);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo remover el consumo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Elimina un producto seleccionado del catálogo.
     */
    private void eliminarProducto() {
        Map<String, Articulo> articulosMap = barManager.getTodosLosArticulos();
        if (articulosMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos para eliminar.", "Catálogo Vacío", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<String> descripciones = articulosMap.values().stream()
                .map(art -> art.getDescripcion() + " (" + art.getCodigo() + ")")
                .sorted()
                .collect(Collectors.toList());

        String seleccion = (String) JOptionPane.showInputDialog(
                this, "Seleccione el producto a eliminar:", "Eliminar Producto",
                JOptionPane.QUESTION_MESSAGE, null, descripciones.toArray(), descripciones.get(0)
        );

        if (seleccion != null) {
            String codigo = seleccion.substring(seleccion.lastIndexOf("(") + 1, seleccion.lastIndexOf(")"));

            int confirm = JOptionPane.showConfirmDialog(this, "Esta acción es permanente.\n¿Eliminar '" + seleccion + "'?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                int resultado = barManager.eliminarArticulo(codigo);
                switch (resultado) {
                    case 0: // Éxito
                        updateStatus("Producto " + codigo + " eliminado.");
                        populateProductDisplayPanel();
                        break;
                    case 1: // No encontrado
                        JOptionPane.showMessageDialog(this, "Error: Producto no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    case 2: // En uso
                        JOptionPane.showMessageDialog(this, "Producto en uso en una mesa abierta. No se puede eliminar.", "Acción Bloqueada", JOptionPane.ERROR_MESSAGE);
                        break;
                }
            }
        }
    }

    /**
     * 	Permite modificar un producto existente.
     */
    private void modificarProducto() {
        Map<String, Articulo> articulosMap = barManager.getTodosLosArticulos();
        if (articulosMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos para modificar.", "Catálogo Vacío", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<String> descripciones = articulosMap.values().stream()
                .map(art -> art.getDescripcion() + " (" + art.getCodigo() + ")")
                .sorted()
                .collect(Collectors.toList());

        String seleccion = (String) JOptionPane.showInputDialog(
                this, "Seleccione el producto a modificar:", "Modificar Producto",
                JOptionPane.QUESTION_MESSAGE, null, descripciones.toArray(), descripciones.get(0)
        );

        if (seleccion != null) {
            String codigoSeleccionado = seleccion.substring(seleccion.lastIndexOf("(") + 1, seleccion.lastIndexOf(")"));
            Articulo articuloExistente = barManager.getArticulo(codigoSeleccionado);

            if (articuloExistente == null) {
                JOptionPane.showMessageDialog(this, "Error: Producto no encontrado para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Crear campos de entrada pre-rellenados
            JTextField codigoField = new JTextField(articuloExistente.getCodigo(), 10);
            codigoField.setEditable(false); // El código no se puede modificar
            JTextField descripcionField = new JTextField(articuloExistente.getDescripcion(), 20);
            JTextField precioDiurnoField = new JTextField(String.valueOf(articuloExistente.getPrecioDiurno()), 5);
            JTextField precioNocturnoField = new JTextField(String.valueOf(articuloExistente.getPrecioNocturno()), 5);
            JTextField precioHappyHourField = new JTextField(String.valueOf(articuloExistente.getPrecioHappyHour()), 5);
            JTextField imagenPathField = new JTextField(articuloExistente.getImagenPath(), 20);

            // Convertir la lista de especificaciones a String para mostrar en el JTextArea
            String especificacionesStr = articuloExistente.getEspecificaciones().stream()
                    .map(s -> s.getNombre() + ":" + s.getTipo().name() + ":" + String.format("%.0f", s.getPrecioPorUnidad()))
                    .collect(Collectors.joining(";"));
            JTextArea especificacionesArea = new JTextArea(especificacionesStr, 3, 20);
            especificacionesArea.setLineWrap(true);
            especificacionesArea.setWrapStyleWord(true);


            JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
            panel.add(new JLabel("Código:"));
            panel.add(codigoField);
            panel.add(new JLabel("Descripción:"));
            panel.add(descripcionField);
            panel.add(new JLabel("Precio Diurno:"));
            panel.add(precioDiurnoField);
            panel.add(new JLabel("Precio Nocturno:"));
            panel.add(precioNocturnoField);
            panel.add(new JLabel("Precio Happy Hour:"));
            panel.add(precioHappyHourField);
            panel.add(new JLabel("Ruta Imagen:"));
            panel.add(imagenPathField);
            panel.add(new JLabel("Especificaciones (Nombre:TIPO:Precio;...):"));
            panel.add(new JScrollPane(especificacionesArea));

            int result = JOptionPane.showConfirmDialog(this, panel, "Modificar Producto: " + articuloExistente.getDescripcion(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    String codigo = codigoField.getText().trim(); // No se modifica, pero se obtiene
                    String nuevaDescripcion = descripcionField.getText().trim();
                    double nuevoPrecioDiurno = Double.parseDouble(precioDiurnoField.getText().trim());
                    double nuevoPrecioNocturno = Double.parseDouble(precioNocturnoField.getText().trim());
                    double nuevoPrecioHappyHour = Double.parseDouble(precioHappyHourField.getText().trim());
                    String nuevaImagenPath = imagenPathField.getText().trim();
                    String nuevasEspecificacionesStr = especificacionesArea.getText().trim();

                    // Crear un nuevo objeto Articulo con los datos actualizados
                    // El constructor de Articulo se encargará de parsear las especificaciones
                    Articulo articuloModificado = new Articulo(codigo, nuevaDescripcion, nuevoPrecioDiurno,
                            nuevoPrecioNocturno, nuevoPrecioHappyHour, nuevaImagenPath, nuevasEspecificacionesStr);

                    if (barManager.modificarArticulo(articuloModificado)) {
                        updateStatus("Producto '" + nuevaDescripcion + "' modificado exitosamente.");
                        populateProductDisplayPanel(); // Refrescar el panel de productos
                    } else {
                        JOptionPane.showMessageDialog(this, "Error: No se pudo modificar el producto (código no encontrado).", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Por favor, ingrese valores numéricos válidos para los precios.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al modificar producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     *  Boton para activar/desactivar la Hora Feliz
     */
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
        updateAllProductPrices();
    }

    /**
     * Boton para abrir una mesa seleccionada
     */
    private void abrirMesa() {
        if (mesaActiva == null) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una mesa de la lista.", "Ninguna mesa seleccionada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (mesaActiva.estaOcupada()) {
            JOptionPane.showMessageDialog(this, "La mesa " + mesaActiva.getNumero() + " ya está ocupada.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Abre la mesa con la hora actual del sistema (con minutos)
        LocalTime horaActual = barManager.getSistemaTime();
        if (barManager.abrirMesa(mesaActiva.getNumero(), horaActual)) {
            updateStatus("Mesa " + mesaActiva.getNumero() + " abierta a las " + horaActual.format(DateTimeFormatter.ofPattern("HH:mm")));
            selectMesa(mesaActiva); // Refresca el estado visual
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo abrir la mesa.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     *  Funcion para cerrar una mesa, lo que genera su ticket con sus respectivos consumos.
     */
    private void cerrarMesa() {
        if (mesaActiva == null || !mesaActiva.estaOcupada()) return;

        int confirm = JOptionPane.showConfirmDialog(this, "¿Cerrar la Mesa " + mesaActiva.getNumero() + "?", "Confirmar Cierre", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String ticket = barManager.cerrarMesa(mesaActiva.getNumero());
            JTextArea ticketArea = new JTextArea(ticket, 20, 35);
            ticketArea.setEditable(false);
            ticketArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(ticketArea), "Ticket - Mesa " + mesaActiva.getNumero(), JOptionPane.INFORMATION_MESSAGE);

            updateStatus("Mesa " + mesaActiva.getNumero() + " cerrada.");
            selectMesa(null); // Deseleccionar la mesa
        }
    }

    /**
     * Abre un formulario para agregar un nuevo producto al catálogo.
     */
    private void addNuevoProducto() {
        JTextField codigoField = new JTextField(10);
        JTextField descripcionField = new JTextField(20);
        JTextField precioDiurnoField = new JTextField(5);
        JTextField precioNocturnoField = new JTextField(5);
        JTextField precioHappyHourField = new JTextField(5);
        JTextField imagenPathField = new JTextField(20);
        JTextArea especificacionesArea = new JTextArea(3, 20); // Para especificaciones
        especificacionesArea.setLineWrap(true); // Habilitar ajuste de línea
        especificacionesArea.setWrapStyleWord(true); // Ajustar por palabra

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Código:"));
        panel.add(codigoField);
        panel.add(new JLabel("Descripción:"));
        panel.add(descripcionField);
        panel.add(new JLabel("Precio Diurno:"));
        panel.add(precioDiurnoField);
        panel.add(new JLabel("Precio Nocturno:"));
        panel.add(precioNocturnoField);
        panel.add(new JLabel("Precio Happy Hour:"));
        panel.add(precioHappyHourField);
        panel.add(new JLabel("Ruta Imagen:"));
        panel.add(imagenPathField);
        panel.add(new JLabel("Especificaciones (ej: Extra Queso:SI_NO:100;Cantidad Papas:CANTIDAD:50):"));
        panel.add(new JScrollPane(especificacionesArea));

        int result = JOptionPane.showConfirmDialog(this, panel, "Añadir Nuevo Producto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String codigo = codigoField.getText().trim();
                String descripcion = descripcionField.getText().trim();
                double precioDiurno = Double.parseDouble(precioDiurnoField.getText().trim());
                double precioNocturno = Double.parseDouble(precioNocturnoField.getText().trim());
                double precioHappyHour = Double.parseDouble(precioHappyHourField.getText().trim());
                String imagenPath = imagenPathField.getText().trim();
                String especificacionesStr = especificacionesArea.getText().trim();

                Articulo nuevoArticulo = new Articulo(codigo, descripcion, precioDiurno, precioNocturno, precioHappyHour, imagenPath, especificacionesStr);

                if (barManager.addArticulo(nuevoArticulo)) {
                    updateStatus("Nuevo producto añadido: " + descripcion);
                    populateProductDisplayPanel(); // Refrescar el panel de productos
                } else {
                    JOptionPane.showMessageDialog(this, "El código de producto ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese valores numéricos válidos para los precios.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al añadir producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Agrega un mensaje al área de estado con marca de hora.
     * @param message Mensaje
     */
    private void updateStatus(String message) {
        statusArea.append("\n[" + LocalTime.now().format(timeFormatter) + "] " + message);
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
        updateMesaButtons(); // Siempre actualizar los botones de mesa al cambiar el estado
    }

    /**
     * Punto de entrada principal de la aplicacion. Inicia la GUI.
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BarGUI());
    }
}