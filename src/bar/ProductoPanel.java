package bar;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

/**
 * Componente visual que representa un unico producto del bar.
 */
public class ProductoPanel extends JPanel {
    private Articulo articulo;
    private JLabel priceLabel;
    private static final int IMAGE_SIZE = 100;

    public ProductoPanel(Articulo articulo) {
        this.articulo = articulo;
        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension(150, 180));
        setBorder(createProductBorder());
        setBackground(Color.WHITE);

        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.setBackground(Color.WHITE);
        JLabel imageLabel = loadImage(articulo.getImagenPath());
        imagePanel.add(imageLabel);
        add(imagePanel, BorderLayout.CENTER);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        JLabel descLabel = new JLabel(articulo.getDescripcion(), SwingConstants.CENTER);
        descLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(descLabel);

        // MODIFICADO: Se inicializa la etiqueta de precio para poder actualizarla después
        priceLabel = new JLabel("$0.00", SwingConstants.CENTER); // Texto inicial
        priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        priceLabel.setForeground(new Color(50, 50, 50));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(priceLabel);

        add(textPanel, BorderLayout.SOUTH);
    }

    /**
     * Actualiza el precio mostrado en el panel.
     * @param currentHour La hora actual para determinar el precio (diurno/nocturno).
     * @param isHappyHour true si la Hora feliz está activo.
     */
    public void updatePrice(int currentHour, boolean isHappyHour) {
        double price;
        if (isHappyHour) {
            price = articulo.getPrecioHappyHour();
        } else {
            price = articulo.getPrecioPorHora(currentHour);
        }
        priceLabel.setText(String.format("$%.2f", price));
    }

    private JLabel loadImage(String imagePath) {
        ImageIcon icon = null;
        try {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                icon = new ImageIcon(imgFile.getAbsolutePath());
            } else {
                icon = new ImageIcon(getClass().getClassLoader().getResource(imagePath));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen: " + imagePath);
        }

        if (icon != null && icon.getIconWidth() > 0) {
            Image scaledImg = icon.getImage().getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(scaledImg));
        } else {
            return new JLabel(new ImageIcon(createNoImagePlaceholder(IMAGE_SIZE, IMAGE_SIZE)));
        }
    }

    private Image createNoImagePlaceholder(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.DARK_GRAY);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "No Image";
        int x = (width - fm.stringWidth(text)) / 2;
        int y = ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, x, y);
        g2d.dispose();
        return img;
    }

    private Border createProductBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );
    }

    /**
     * Devuelve todos los valores de un Articulo
     * @return
     */
    public Articulo getArticulo() {
        return articulo;
    }
}