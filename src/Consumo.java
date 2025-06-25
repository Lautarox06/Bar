public class Consumo {
    Articulo articulo;
    int cantidad;
    int horaConsumo;
    double precioUnitarioConsumo; // NUEVA PROPIEDAD: El precio por unidad ya calculado al momento del consumo

    /**
     * Constructor de Consumo.
     * @param articulo El artículo consumido.
     * @param cantidad La cantidad del artículo.
     * @param horaConsumo La hora en que se realizó el consumo.
     * @param precioUnitarioConsumo El precio por unidad del artículo en el momento del consumo (ya considerando Happy Hour/diurno/nocturno).
     */
    public Consumo(Articulo articulo, int cantidad, int horaConsumo, double precioUnitarioConsumo) { // CONSTRUCTOR ACTUALIZADO
        this.articulo = articulo;
        this.cantidad = cantidad;
        this.horaConsumo = horaConsumo;
        this.precioUnitarioConsumo = precioUnitarioConsumo; // Asignar el precio ya calculado
    }

    // Métodos getter existentes
    public Articulo getArticulo() {
        return articulo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getHoraConsumo() {
        return horaConsumo;
    }

    /**
     * Calcula el subtotal para este consumo.
     * Es ahora muy simple, solo multiplica la cantidad por el precio unitario guardado.
     * @return El subtotal del consumo.
     */
    public double getSubtotal() { // ¡SIN PARÁMETROS!
        return cantidad * precioUnitarioConsumo;
    }
}