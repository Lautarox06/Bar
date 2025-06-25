import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage; // Importación necesaria
import java.io.File;
import java.net.URL;

/**
 * Componente visual que representa un único producto del bar, mostrando
 * su imagen, descripción y precio. Es clicable para registrar consumos.
 */
public class ProductoPanel extends JPanel {
    private Articulo articulo;
    private static final int IMAGE_SIZE = 100; // Tamaño fijo para las imágenes

    public ProductoPanel(Articulo articulo) {
        this.articulo = articulo;
        setLayout(new BorderLayout(5, 5)); // Espacio entre componentes
        setPreferredSize(new Dimension(150, 180)); // Tamaño preferido del panel
        setBorder(createProductBorder()); // Borde alrededor del producto
        setBackground(Color.WHITE); // Fondo blanco

        // Panel para la imagen
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.setBackground(Color.WHITE); // Fondo blanco para el panel de imagen
        JLabel imageLabel = loadImage(articulo.getImagenPath());
        imagePanel.add(imageLabel);
        add(imagePanel, BorderLayout.CENTER);

        // Panel para texto (descripción y precio)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5)); // Margen interior

        JLabel descLabel = new JLabel(articulo.getDescripcion(), SwingConstants.CENTER);
        descLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrar texto
        textPanel.add(descLabel);

        JLabel priceLabel = new JLabel(String.format("$%.2f", articulo.getPrecioDiurno()), SwingConstants.CENTER); // Mostrar precio diurno por defecto
        priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        priceLabel.setForeground(new Color(50, 50, 50));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrar texto
        textPanel.add(priceLabel);

        add(textPanel, BorderLayout.SOUTH);
    }

    /**
     * Carga la imagen del artículo desde la ruta especificada.
     * Si la imagen no se encuentra, muestra una imagen de "no disponible".
     * @param imagePath La ruta de la imagen.
     * @return Un JLabel con la imagen cargada o un placeholder.
     */
    private JLabel loadImage(String imagePath) {
        ImageIcon icon = null;
        String fullPath = "images/" + imagePath; // Ruta relativa dentro del directorio de ejecución o JAR

        // Intentar cargar la imagen desde el sistema de archivos o recursos
        try {
            File imgFile = new File(fullPath);
            if (imgFile.exists() && !imgFile.isDirectory()) {
                icon = new ImageIcon(imgFile.getAbsolutePath());
            } else {
                // Intentar cargar como recurso del JAR (si se empaqueta)
                URL imageUrl = getClass().getClassLoader().getResource(fullPath);
                if (imageUrl != null) {
                    icon = new ImageIcon(imageUrl);
                } else {
                    System.err.println("Imagen no encontrada: " + fullPath);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imagen " + fullPath + ": " + e.getMessage());
            e.printStackTrace();
        }

        if (icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
            // Escalar la imagen si es demasiado grande
            Image img = icon.getImage();
            Image scaledImg = img.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImg);
        } else {
            // Imagen de placeholder si no se pudo cargar
            icon = new ImageIcon(createNoImagePlaceholder(IMAGE_SIZE, IMAGE_SIZE));
        }

        return new JLabel(icon);
    }

    /**
     * Crea un placeholder visual cuando no se encuentra una imagen.
     * @param width Ancho del placeholder.
     * @param height Alto del placeholder.
     * @return Una imagen de "no disponible".
     */
    private Image createNoImagePlaceholder(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics(); // Obtener el contexto gráfico de la imagen
        try {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, width, height); // Dibujar el fondo
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            String text = "No Image";
            FontMetrics fm = g2d.getFontMetrics(); // Usar fm de g2d
            int x = (width - fm.stringWidth(text)) / 2;
            int y = ((height - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(text, x, y); // Dibujar el texto
        } finally {
            g2d.dispose(); // Liberar los recursos gráficos
        }
        return img;
    }


    /**
     * Crea un borde estilizado para el panel del producto.
     * @return Un objeto Border.
     */
    private Border createProductBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // Borde exterior
                BorderFactory.createEmptyBorder(5, 5, 5, 5) // Margen interior
        );
    }

    // Getter para el artículo asociado a este panel
    public Articulo getArticulo() {
        return articulo;
    }
}