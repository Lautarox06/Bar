import java.util.Map;
import java.text.DecimalFormat;

public class Consumo {
    Articulo articulo; // Ahora almacena el objeto Articulo directamente
    int cantidad;
    double precioUnitarioBase; // El precio del artículo sin extras
    Map<Especificacion, Integer> opcionesSeleccionadas;

    // MODIFICADO: El primer parámetro ahora es un objeto Articulo
    public Consumo(Articulo articulo, int cantidad, double precioUnitarioBase, Map<Especificacion, Integer> opcionesSeleccionadas) {
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.precioUnitarioBase = precioUnitarioBase;
        this.opcionesSeleccionadas = opcionesSeleccionadas;
    }

    public double getCostoDeOpciones() {
        double costoOpciones = 0;
        if (opcionesSeleccionadas != null) {
            for (Map.Entry<Especificacion, Integer> entry : opcionesSeleccionadas.entrySet()) {
                costoOpciones += entry.getKey().getPrecioPorUnidad() * entry.getValue();
            }
        }
        return costoOpciones;
    }

    public double getPrecioUnitarioFinal() {
        return precioUnitarioBase + getCostoDeOpciones();
    }

    public double getSubtotal() {
        return getPrecioUnitarioFinal() * cantidad;
    }

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

    // Getters
    public Articulo getArticulo() { return articulo; }
    public int getCantidad() { return cantidad; }
    public Map<Especificacion, Integer> getOpcionesSeleccionadas() { return opcionesSeleccionadas; }
    public int getHoraConsumo() {
        // En un sistema real, la hora se debería pasar al crear el consumo
        // o generarse en el momento de agregarlo a la mesa.
        // Por ahora, asumimos que se gestiona la hora en BarManager.
        return 0; // O el valor que corresponda
    }
}