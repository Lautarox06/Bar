package bar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 	Logica del sistema de gestion del bar.
 */
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
        inicializarMesas(5); // Cantidad de mesas que deseamos crear
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

    /**
     * Guarda todos los productos en el archivo CSV.
     * @param filename Nombre del archivo.
     */
    private void guardarProductos(String filename) {
        // Configurar formato numérico con punto decimal
        DecimalFormat df = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
        df.setGroupingUsed(false);

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("codigo,nombre,precio_diurno,precio_nocturno,precio_happy_hour,imagen,especificaciones");
            for (Articulo art : articulos.values()) {
                String especificacionesStr = art.getEspecificaciones().stream()
                        .map(spec -> spec.getNombre() + ":" + spec.getTipo().name() + ":" + df.format(spec.getPrecioPorUnidad()))
                        .collect(Collectors.joining(";"));

                pw.println(String.format("%s,%s,%s,%s,%s,%s,%s",
                        art.getCodigo(),
                        art.getDescripcion(),
                        df.format(art.getPrecioDiurno()),
                        df.format(art.getPrecioNocturno()),
                        df.format(art.getPrecioHappyHour()),
                        art.getImagenPath(),
                        especificacionesStr));
            }
        } catch (IOException e) {
            System.err.println("Error al guardar productos: " + e.getMessage());
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

    /**
     * Activa/Desactiva la hora feliz.
     */
    public void toggleHappyHour() {
        isHappyHourActive = !isHappyHourActive;
    }

    /**
     * Metodo para agregar un consumo a una mesa.
     */
    public boolean agregarConsumoAMesa(int numeroMesa, Articulo articulo, int cantidad, double precioUnitarioBase, Map<Especificacion, Integer> opcionesSeleccionadas) {
        Mesa mesa = mesas.get(numeroMesa);
        if (mesa != null && mesa.estaOcupada()) {
            // Se pasa la hora actual del sistema al crear el consumo
            Consumo consumo = new Consumo(articulo, cantidad, precioUnitarioBase, opcionesSeleccionadas, sistemaTime);
            mesa.agregarConsumo(consumo);
            return true;
        }
        return false;
    }

    /**
     * Remueve un consumo de una mesa especifica por su indice.
     * @param numeroMesa El número de la mesa.
     * @param indiceConsumo El índice del consumo a remover (basado en la tabla/lista interna de consumos de la mesa).
     * @return true si se removio exitosamente, false en caso contrario.
     */
    public boolean removerConsumoDeMesa(int numeroMesa, int indiceConsumo) {
        Mesa mesa = mesas.get(numeroMesa);
        if (mesa != null && mesa.estaOcupada()) {
            return mesa.removerConsumo(indiceConsumo);
        }
        return false;
    }


    /**
     * Elimina un articulo del catalogo.
     * @param codigo El código del artículo a eliminar.
     * @return 0 si se elimino con éxito, 1 si el articulo no existe, 2 si el articulo esta en uso en una mesa abierta.
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
        guardarProductos("productos.csv"); // Guardar cambios en el CSV
        return 0; // Éxito
    }

    /**
     * Modifica un artículo existente en el catalogo.
     * El Articulo pasado debe tener el codigo de un articulo existente.
     * @param articuloModificado El objeto Articulo con los datos actualizados.
     * @return true si el articulo fue modificado, false si no se encontro el articulo.
     */
    public boolean modificarArticulo(Articulo articuloModificado) {
        if (articulos.containsKey(articuloModificado.getCodigo())) {
            articulos.put(articuloModificado.getCodigo(), articuloModificado);
            guardarProductos("productos.csv"); // Guardar cambios en el CSV
            return true;
        }
        return false;
    }

    /**
     * Funcion para abrir una mesa.
     * @param numeroMesa El numero de la mesa que se desea abrir.
     * @param horaApertura Hora (con minutos) que se registrara que se abrio la mesa.
     * @return
     */
    public boolean abrirMesa(int numeroMesa, LocalTime horaApertura) {
        Mesa mesa = mesas.get(numeroMesa);
        if (mesa != null && !mesa.estaOcupada()) {
            mesa.abrir(horaApertura);
            return true;
        }
        return false;
    }

    /**
     * Funcion para cerrar una mesa.
     * @param numeroMesa El numero de la mesa que se desea cerrar.
     * @return
     */
    public String cerrarMesa(int numeroMesa) { Mesa mesa = mesas.get(numeroMesa); if (mesa != null && mesa.estaOcupada()) { return mesa.cerrar(); } return null; }

    /**
     * Funcion para agregar un articulo nuevo.
     * @param articulo
     * @return
     */
    public boolean addArticulo(Articulo articulo) {
        if (articulos.containsKey(articulo.getCodigo())) {
            return false;
        }
        articulos.put(articulo.getCodigo(), articulo);
        guardarProductos("productos.csv"); // Guardar cambios en el CSV
        return true;
    }
    public Mesa getMesa(int numeroMesa) { return mesas.get(numeroMesa); }

    /**
     * Devuelve una lista de todas las mesas que existen.
     * @return
     */
    public List<Mesa> getTodasLasMesas() { List<Mesa> sortedMesas = new ArrayList<>(mesas.values()); Collections.sort(sortedMesas, Comparator.comparingInt(Mesa::getNumero)); return Collections.unmodifiableList(sortedMesas); }

    /**
     * Devuelve una lista de los numeros de mesas libres.
     * @return
     */
    public List<Integer> getNumerosMesasLibres() { return mesas.values().stream().filter(m -> !m.estaOcupada()).map(Mesa::getNumero).sorted().collect(Collectors.toList()); }

    /**
     * Devuelve una lista de los numeros de mesas ocupadas.
     * @return
     */
    public List<Integer> getNumerosMesasOcupadas() { return mesas.values().stream().filter(Mesa::estaOcupada).map(Mesa::getNumero).sorted().collect(Collectors.toList()); }

    /**
     * Devuelve una lista de todos los articulos creados.
     * @return
     */
    public Map<String, Articulo> getTodosLosArticulos() { return Collections.unmodifiableMap(articulos); }

    /**
     * Devuelve un Articulo.
     * @param codigo Segun el codigo diferencia los articulos.
     * @return
     */
    public Articulo getArticulo(String codigo) { return articulos.get(codigo); }
}