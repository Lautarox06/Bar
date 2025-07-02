public class Especificacion {
    private String nombre;
    private TipoEspecificacion tipo;
    private double precioPorUnidad;

    public Especificacion(String nombre, TipoEspecificacion tipo, double precioPorUnidad) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.precioPorUnidad = precioPorUnidad;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoEspecificacion getTipo() {
        return tipo;
    }

    public double getPrecioPorUnidad() {
        return precioPorUnidad;
    }

    @Override
    public String toString() {
        return nombre; // Para mostrar en JComboBox
    }
}