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

public class BarManager {
    private Map<String, Articulo> articulos;
    private Map<Integer, Mesa> mesas;
    private boolean isHappyHourActive = false;
    private LocalTime sistemaTime;

    public BarManager() {
        this.articulos = new HashMap<>();
        this.mesas = new HashMap<>();
        this.sistemaTime = LocalTime.now().withNano(0);
        cargarArticulos("productos.csv"); // Asegúrate de que este archivo exista y tenga el formato correcto
        inicializarMesas(5);
    }

    // MODIFICADO: Cargar artículos ahora procesa la 8ª columna de especificaciones.
    private void cargarArticulos(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine(); // Saltar encabezado
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1); // -1 para incluir campos vacíos al final
                if (data.length >= 7) { // Asegurarse de tener al menos los campos básicos
                    String codigo = data[0].trim();
                    String nombre = data[1].trim();
                    double precioDiurno = Double.parseDouble(data[2].trim());
                    double precioNocturno = Double.parseDouble(data[3].trim());
                    double precioHappyHour = Double.parseDouble(data[4].trim());
                    String imagenPath = data[5].trim();
                    String especificacionesStr = data.length > 6 ? data[6].trim() : ""; // La 7ma columna (índice 6)

                    Articulo articulo = new Articulo(codigo, nombre, precioDiurno, precioNocturno, precioHappyHour, imagenPath, especificacionesStr);
                    articulos.put(codigo, articulo);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar artículos: " + e.getMessage());
            // Manejo de error: podrías inicializar con artículos predeterminados o mostrar un mensaje al usuario.
            // Por ahora, simplemente se informa el error.
        } catch (NumberFormatException e) {
            System.err.println("Error de formato numérico en el archivo de artículos: " + e.getMessage());
        }
    }

    private void inicializarMesas(int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            mesas.put(i, new Mesa(i));
        }
    }

    public void tick() {
        sistemaTime = sistemaTime.plusSeconds(1);
    }

    public LocalTime getSistemaTime() {
        return sistemaTime;
    }

    public int getSistemaHora() {
        return sistemaTime.getHour();
    }

    public boolean setSistemaHora(int hour) {
        if (hour >= 0 && hour <= 23) {
            sistemaTime = sistemaTime.withHour(hour).withMinute(0).withSecond(0);
            return true;
        }
        return false;
    }

    public boolean isHappyHourActive() {
        return isHappyHourActive;
    }

    public void toggleHappyHour() {
        isHappyHourActive = !isHappyHourActive;
    }

    /**
     * Nuevo método para agregar un consumo a una mesa.
     * Ahora recibe el objeto Articulo completo y el mapa de opciones seleccionadas.
     */
    public boolean agregarConsumoAMesa(int numeroMesa, Articulo articulo, int cantidad, double precioUnitarioBase, Map<Especificacion, Integer> opcionesSeleccionadas) {
        Mesa mesa = mesas.get(numeroMesa);
        if (mesa != null && mesa.estaOcupada()) {
            Consumo consumo = new Consumo(articulo, cantidad, precioUnitarioBase, opcionesSeleccionadas);
            mesa.agregarConsumo(consumo);
            return true;
        }
        return false;
    }

    /**
     * Remueve un consumo de una mesa específica por su índice.
     * @param numeroMesa El número de la mesa.
     * @param indiceConsumo El índice del consumo a remover (basado en la tabla/lista interna de consumos de la mesa).
     * @return true si se removió exitosamente, false en caso contrario.
     */
    public boolean removerConsumoDeMesa(int numeroMesa, int indiceConsumo) {
        Mesa mesa = mesas.get(numeroMesa);
        if (mesa != null && mesa.estaOcupada()) {
            return mesa.removerConsumo(indiceConsumo);
        }
        return false;
    }


    /**
     * Elimina un artículo del catálogo.
     * @param codigo El código del artículo a eliminar.
     * @return 0 si se eliminó con éxito, 1 si el artículo no existe, 2 si el artículo está en uso en una mesa abierta.
     */
    public int eliminarArticulo(String codigo) {
        if (!articulos.containsKey(codigo)) {
            return 1; // Artículo no encontrado
        }

        // Verificar si el artículo está en uso en alguna mesa abierta
        for (Mesa mesa : mesas.values()) {
            if (mesa.estaOcupada()) {
                for (Consumo consumo : mesa.getConsumos()) {
                    if (consumo.getArticulo().getCodigo().equals(codigo)) {
                        return 2; // Artículo en uso
                    }
                }
            }
        }

        articulos.remove(codigo);
        return 0; // Éxito
    }

    /**
     * NUEVO: Modifica un artículo existente en el catálogo.
     * El Articulo pasado debe tener el código de un artículo existente.
     * @param articuloModificado El objeto Articulo con los datos actualizados.
     * @return true si el artículo fue modificado, false si no se encontró el artículo.
     */
    public boolean modificarArticulo(Articulo articuloModificado) {
        if (articulos.containsKey(articuloModificado.getCodigo())) {
            articulos.put(articuloModificado.getCodigo(), articuloModificado);
            return true;
        }
        return false;
    }

    // Getters y otros métodos existentes
    public boolean abrirMesa(int numeroMesa, int horaApertura) { Mesa mesa = mesas.get(numeroMesa); if (mesa != null && !mesa.estaOcupada()) { mesa.abrir(horaApertura); return true; } return false; }
    public String cerrarMesa(int numeroMesa) { Mesa mesa = mesas.get(numeroMesa); if (mesa != null && mesa.estaOcupada()) { return mesa.cerrar(); } return null; }
    public boolean addArticulo(Articulo articulo) { if (articulos.containsKey(articulo.getCodigo())) { return false; } articulos.put(articulo.getCodigo(), articulo); return true; }
    public Mesa getMesa(int numeroMesa) { return mesas.get(numeroMesa); }
    public List<Mesa> getTodasLasMesas() { List<Mesa> sortedMesas = new ArrayList<>(mesas.values()); Collections.sort(sortedMesas, Comparator.comparingInt(Mesa::getNumero)); return Collections.unmodifiableList(sortedMesas); }
    public List<Integer> getNumerosMesasLibres() { return mesas.values().stream().filter(m -> !m.estaOcupada()).map(Mesa::getNumero).sorted().collect(Collectors.toList()); }
    public List<Integer> getNumerosMesasOcupadas() { return mesas.values().stream().filter(Mesa::estaOcupada).map(Mesa::getNumero).sorted().collect(Collectors.toList()); }
    public Map<String, Articulo> getTodosLosArticulos() { return Collections.unmodifiableMap(articulos); }
    public Articulo getArticulo(String codigo) { return articulos.get(codigo); }
}