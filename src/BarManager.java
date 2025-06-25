import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase que gestiona la lógica de negocio del bar.
 */
public class BarManager {
    private Map<String, Articulo> articulos;
    private Map<Integer, Mesa> mesas;
    private boolean isHappyHourActive = false;
    private LocalTime sistemaTime;

    /**
     * Constructor de BarManager.
     */
    public BarManager() {
        this.articulos = new HashMap<>();
        this.mesas = new HashMap<>();
        this.sistemaTime = LocalTime.now().withNano(0);
        cargarArticulos("productos.csv");
        inicializarMesas(5);
    }

    /**
     * CORREGIDO: Avanza el reloj del sistema en un segundo de forma segura.
     * La palabra clave 'synchronized' previene conflictos si múltiples hilos
     * intentaran modificar la hora al mismo tiempo.
     */
    public synchronized void tick() {
        this.sistemaTime = this.sistemaTime.plusSeconds(1);
    }

    /**
     * CORREGIDO: Establece la hora del sistema de forma segura.
     * @param hora La nueva hora a establecer (0-23).
     * @return true si la hora fue válida, false en caso contrario.
     */
    public synchronized boolean setSistemaHora(int hora) {
        if (hora >= 0 && hora <= 23) {
            this.sistemaTime = LocalTime.of(hora, 0, 0);
            return true;
        }
        return false;
    }

    /**
     * Obtiene el objeto LocalTime completo del sistema.
     * @return El tiempo actual del sistema.
     */
    public synchronized LocalTime getSistemaTime() {
        return this.sistemaTime;
    }

    /**
     * Obtiene solo la componente de la hora del sistema.
     * @return La hora actual del sistema (0-23).
     */
    public int getSistemaHora() {
        return getSistemaTime().getHour(); // Llama al método sincronizado para seguridad
    }

    // --- El resto de los métodos permanecen igual ---

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
                    if (imagenPath.isEmpty() || !new java.io.File(imagenPath).exists()) {
                        imagenPath = "images/default.png";
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

    private void inicializarMesas(int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            mesas.put(i, new Mesa(i));
        }
    }

    public boolean abrirMesa(int numeroMesa, int horaApertura) {
        Mesa mesa = mesas.get(numeroMesa);
        if (mesa != null && !mesa.estaOcupada()) {
            mesa.abrir(horaApertura);
            return true;
        }
        return false;
    }

    public String cerrarMesa(int numeroMesa) {
        Mesa mesa = mesas.get(numeroMesa);
        if (mesa != null && mesa.estaOcupada()) {
            return mesa.cerrar();
        }
        return null;
    }

    public boolean agregarConsumoAMesa(int numeroMesa, String codigoArticulo, int cantidad, int horaConsumo) {
        Mesa mesa = mesas.get(numeroMesa);
        Articulo articulo = articulos.get(codigoArticulo);
        if (mesa != null && articulo != null && mesa.estaOcupada()) {
            double precioUnitarioCalculado;
            if (isHappyHourActive) {
                precioUnitarioCalculado = articulo.getPrecioHappyHour();
            } else {
                precioUnitarioCalculado = articulo.getPrecioPorHora(horaConsumo);
            }
            mesa.agregarConsumo(new Consumo(articulo, cantidad, horaConsumo, precioUnitarioCalculado));
            return true;
        }
        return false;
    }

    public boolean addArticulo(Articulo articulo) {
        if (articulos.containsKey(articulo.getCodigo())) {
            return false;
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

    public synchronized void toggleHappyHour() {
        this.isHappyHourActive = !this.isHappyHourActive;
    }

    public synchronized boolean isHappyHourActive() {
        return isHappyHourActive;
    }
}