package bar;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Todas las funciones relacionadas a obtener informacion de un consumo.
 */
public class Consumo {
    Articulo articulo; // Ahora almacena el objeto Articulo directamente
    int cantidad;
    double precioUnitarioBase; // El precio del artículo sin extras
    Map<Especificacion, Integer> opcionesSeleccionadas;
    private LocalTime horaConsumo; // Nuevo campo para almacenar la hora exacta del consumo

    /**
     * Devuelve toda la informacion de un consumo en base a un articulo.
     * @param articulo
     * @param cantidad
     * @param precioUnitarioBase
     * @param opcionesSeleccionadas
     * @param horaConsumo Hora exacta en que se registra el consumo
     */
    public Consumo(Articulo articulo, int cantidad, double precioUnitarioBase, Map<Especificacion, Integer> opcionesSeleccionadas, LocalTime horaConsumo) {
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.precioUnitarioBase = precioUnitarioBase;
        this.opcionesSeleccionadas = opcionesSeleccionadas;
        this.horaConsumo = horaConsumo;
    }

    /**
     * En base a las especificaciones agregadas al consumo, devuelve su costo final.
     * @return
     */
    public double getCostoDeOpciones() {
        double costoOpciones = 0;
        if (opcionesSeleccionadas != null) {
            for (Map.Entry<Especificacion, Integer> entry : opcionesSeleccionadas.entrySet()) {
                costoOpciones += entry.getKey().getPrecioPorUnidad() * entry.getValue();
            }
        }
        return costoOpciones;
    }

    /**
     * Devuelve el precio del articulo individual junto a los agregados.
     * @return
     */
    public double getPrecioUnitarioFinal() {
        return precioUnitarioBase + getCostoDeOpciones();
    }

    /**
     * Devuelve el precio multiplicado la cantidad solicitada.
     * @return
     */
    public double getSubtotal() {
        return getPrecioUnitarioFinal() * cantidad;
    }

    /**
     * Devuelve todos los detalles del consumo.
     * @return
     */
    public String getDetalle() {
        if (opcionesSeleccionadas == null || opcionesSeleccionadas.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("(");
        for (Map.Entry<Especificacion, Integer> entry : opcionesSeleccionadas.entrySet()) {
            Especificacion spec = entry.getKey();
            int value = entry.getValue();
            if (value > 0) {
                sb.append(spec.getNombre());
                if (spec.getTipo() == TipoEspecificacion.CANTIDAD) {
                    sb.append(": ").append(value);
                }
                sb.append(", ");
            }
        }
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2); // Remover la última coma y espacio
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Devuelve el articulo solicitado por el consumo
     * @return
     */
    public Articulo getArticulo() { return articulo; }

    /**
     * Deuelve la cantidad solicitada
     * @return
     */
    public int getCantidad() { return cantidad; }

    /**
     * Deuelve las opciones seleccionadas.
     * @return
     */
    public Map<Especificacion, Integer> getOpcionesSeleccionadas() { return opcionesSeleccionadas; }

    /**
     * Devuelve la hora a la que se realizo el Consumo.
     * @return
     */
    public LocalTime getHoraConsumo() {
        return horaConsumo;
    }

    /**
     * Devuelve la hora formateada como cadena (HH:mm)
     * @return
     */
    public String getHoraConsumoFormateada() {
        return horaConsumo.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}