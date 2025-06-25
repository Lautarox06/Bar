import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase que gestiona la lógica de negocio del bar, incluyendo
 * la carga de artículos, la gestión de mesas y el registro de consumos.
 * Actúa como una capa entre la interfaz de usuario y los modelos de datos.
 */
public class BarManager {
    private Map<String, Articulo> articulos;
    private Map<Integer, Mesa> mesas;
    private boolean isHappyHourActive = false; // Estado de Happy Hour

    /**
     * Constructor de BarManager. Inicializa los mapas de artículos y mesas
     * y carga los datos desde los archivos correspondientes.
     */
    public BarManager() {
        this.articulos = new HashMap<>();
        this.mesas = new HashMap<>();
        cargarArticulos("productos.csv"); // Cargar artículos al inicializar el manager
        inicializarMesas(5); // Inicializar un número predefinido de mesas
    }

    /**
     * Carga los artículos disponibles desde un archivo CSV.
     * @param filename El nombre del archivo CSV (ej. "productos.csv").
     */
    private void cargarArticulos(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine(); // Saltar la línea del encabezado
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 7) {
                    String codigo = data[0].trim();
                    String nombre = data[1].trim();
                    double precioDiurno = Double.parseDouble(data[3].trim());
                    double precioNocturno = Double.parseDouble(data[4].trim());
                    double precioHappyHour = Double.parseDouble(data[5].trim());
                    String imagenPath = data[6].trim();
                    // Asegurarse de que la imagen exista o usar una por defecto
                    if (imagenPath.isEmpty() || !new java.io.File(imagenPath).exists()) {
                        imagenPath = "images/default.png"; // Ruta a una imagen por defecto
                    }
                    articulos.put(codigo, new Articulo(codigo, nombre, precioDiurno, precioNocturno, precioHappyHour, imagenPath));
                } else {
                    System.err.println("Advertencia: Línea con formato incorrecto en CSV de artículos: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar artículos desde " + filename + ": " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error de formato numérico en el CSV de artículos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inicializa un número determinado de mesas para el bar.
     * @param cantidad El número de mesas a crear.
     */
    private void inicializarMesas(int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            mesas.put(i, new Mesa(i));
        }
    }

    /**
     * Abre una mesa específica.
     * @param numeroMesa El número de la mesa a abrir.
     * @param horaApertura La hora en que se abre la mesa.
     * @return true si la mesa se abrió con éxito, false si ya estaba ocupada o no existe.
     */
    public boolean abrirMesa(int numeroMesa, int horaApertura) {
        Mesa mesa = mesas.get(numeroMesa);
        if (mesa != null && !mesa.estaOcupada()) {
            mesa.abrir(horaApertura);
            return true;
        }
        return false;
    }

    /**
     * Cierra una mesa específica y devuelve el ticket.
     * @param numeroMesa El número de la mesa a cerrar.
     * @return El ticket de la mesa en formato String, o null si la mesa no existe o no está ocupada.
     */
    public String cerrarMesa(int numeroMesa) {
        Mesa mesa = mesas.get(numeroMesa);
        if (mesa != null && mesa.estaOcupada()) {
            return mesa.cerrar();
        }
        return null;
    }

    /**
     * Agrega un consumo a una mesa específica.
     * Calcula el precio final por unidad en el momento del consumo.
     * @param numeroMesa El número de la mesa.
     * @param codigoArticulo El código del artículo consumido.
     * @param cantidad La cantidad del artículo.
     * @param horaConsumo La hora del consumo.
     * @return true si el consumo se agregó con éxito, false si la mesa o el artículo no existen, o la mesa no está ocupada.
     */
    public boolean agregarConsumoAMesa(int numeroMesa, String codigoArticulo, int cantidad, int horaConsumo) {
        Mesa mesa = mesas.get(numeroMesa);
        Articulo articulo = articulos.get(codigoArticulo);
        if (mesa != null && articulo != null && mesa.estaOcupada()) {
            double precioUnitarioCalculado;
            if (isHappyHourActive) { // Si Happy Hour está activo, usa ese precio
                precioUnitarioCalculado = articulo.getPrecioHappyHour();
            } else { // Si no, usa el precio por hora normal (diurno/nocturno)
                precioUnitarioCalculado = articulo.getPrecioPorHora(horaConsumo);
            }
            // Crear el Consumo con el precio unitario ya calculado
            mesa.agregarConsumo(new Consumo(articulo, cantidad, horaConsumo, precioUnitarioCalculado));
            return true;
        }
        return false;
    }

    /**
     * Agrega un nuevo artículo al catálogo del bar.
     * @param articulo El objeto Articulo a añadir.
     * @return true si el artículo se añadió con éxito (el código no existía), false en caso contrario.
     */
    public boolean addArticulo(Articulo articulo) {
        if (articulos.containsKey(articulo.getCodigo())) {
            return false; // El artículo con ese código ya existe
        }
        articulos.put(articulo.getCodigo(), articulo);
        return true;
    }


    public Mesa getMesa(int numeroMesa) {
        return mesas.get(numeroMesa);
    }

    public List<Mesa> getTodasLasMesas() {
        List<Mesa> sortedMesas = new ArrayList<>(mesas.values());
        Collections.sort(sortedMesas, Comparator.comparingInt(Mesa::getNumero));
        return Collections.unmodifiableList(sortedMesas);
    }

    public List<Integer> getNumerosMesasLibres() {
        return mesas.values().stream()
                .filter(mesa -> !mesa.estaOcupada())
                .map(Mesa::getNumero)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Integer> getNumerosMesasOcupadas() {
        return mesas.values().stream()
                .filter(Mesa::estaOcupada)
                .map(Mesa::getNumero)
                .sorted()
                .collect(Collectors.toList());
    }

    public Map<String, Articulo> getTodosLosArticulos() {
        return Collections.unmodifiableMap(articulos);
    }

    public Articulo getArticulo(String codigo) {
        return articulos.get(codigo);
    }

    /**
     * Alterna el estado de Happy Hour (activar/desactivar).
     */
    public void toggleHappyHour() {
        this.isHappyHourActive = !this.isHappyHourActive;
    }

    /**
     * Devuelve el estado actual de Happy Hour.
     * @return true si Happy Hour está activo, false en caso contrario.
     */
    public boolean isHappyHourActive() {
        return isHappyHourActive;
    }
}