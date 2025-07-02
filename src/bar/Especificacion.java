package bar;

/**
 * Apartado hecho especificamente para los extras en los consumos. Por ejemplo: Queso en una hamburguesa.
 */
public class Especificacion {
    private String nombre;
    private TipoEspecificacion tipo;
    private double precioPorUnidad;

    /**
     * Devuelve todos los valores de una especificacion.
     * @param nombre
     * @param tipo
     * @param precioPorUnidad
     */
    public Especificacion(String nombre, TipoEspecificacion tipo, double precioPorUnidad) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.precioPorUnidad = precioPorUnidad;
    }

    /**
     * Devuelve el nombre del agregado, por ejemplo: Queso.
     * @return
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve el tipo de agregado, puede ser: SI_NO o CANTIDAD
     * @return
     */
    public TipoEspecificacion getTipo() {
        return tipo;
    }

    /**
     * Devuelve el precio de usar este agregado.
     * @return
     */
    public double getPrecioPorUnidad() {
        return precioPorUnidad;
    }

    @Override
    public String toString() {
        return nombre; // Para mostrar en JComboBox
    }
}