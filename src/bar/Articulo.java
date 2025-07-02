package bar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un articulo disponible en el bar, con precios variables
 * segun el horario y una lista de especificaciones adicionales.
 */
public class Articulo {

    /** Codigo unico del articulo */
    String codigo;

    /** Descripcion del articulo */
    String descripcion;

    /** Precio durante el horario diurno */
    double precioDiurno;

    /** Precio durante el horario nocturno */
    double precioNocturno;

    /** Precio durante la Happy Hour */
    double precioHappyHour;

    /** Ruta de la imagen asociada al articulo */
    String imagenPath;

    /** Lista de especificaciones adicionales del articulo */
    List<Especificacion> especificaciones;

    /**
     * Constructor que recibe especificaciones como String y las parsea.
     *
     * @param codigo Codigo del articulo.
     * @param descripcion Descripcion del articulo.
     * @param precioDiurno Precio durante el dia.
     * @param precioNocturno Precio durante la noche.
     * @param precioHappyHour Precio durante la Happy Hour.
     * @param imagenPath Ruta de la imagen.
     * @param especificacionesStr Cadena con especificaciones separadas por ";" y ":".
     */
    public Articulo(String codigo, String descripcion, double precioDiurno, double precioNocturno, double precioHappyHour, String imagenPath, String especificacionesStr) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.precioDiurno = precioDiurno;
        this.precioNocturno = precioNocturno;
        this.precioHappyHour = precioHappyHour;
        this.imagenPath = imagenPath;
        parseEspecificaciones(especificacionesStr);
    }

    /**
     * Constructor que recibe especificaciones como lista.
     *
     * @param codigo Codigo del articulo.
     * @param descripcion Descripcion del articulo.
     * @param precioDiurno Precio durante el dia.
     * @param precioNocturno Precio durante la noche.
     * @param precioHappyHour Precio durante la Happy Hour.
     * @param imagenPath Ruta de la imagen.
     * @param especificaciones Lista de especificaciones.
     */
    public Articulo(String codigo, String descripcion, double precioDiurno, double precioNocturno, double precioHappyHour, String imagenPath, List<Especificacion> especificaciones) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.precioDiurno = precioDiurno;
        this.precioNocturno = precioNocturno;
        this.precioHappyHour = precioHappyHour;
        this.imagenPath = imagenPath;
        this.especificaciones = especificaciones != null ? especificaciones : new ArrayList<>();
    }

    /**
     * Parsea una cadena de texto con especificaciones y las convierte a objetos.
     *
     * @param especificacionesStr Cadena de especificaciones en formato: nombre:tipo:precio
     */
    private void parseEspecificaciones(String especificacionesStr) {
        this.especificaciones = new ArrayList<>();
        if (especificacionesStr == null || especificacionesStr.trim().isEmpty()) {
            return;
        }

        String[] pares = especificacionesStr.split(";");
        for (String par : pares) {
            String[] partes = par.split(":");
            if (partes.length == 3) {
                try {
                    String nombre = partes[0].trim();
                    TipoEspecificacion tipo = TipoEspecificacion.valueOf(partes[1].trim().toUpperCase());
                    double precio = Double.parseDouble(partes[2].trim());
                    this.especificaciones.add(new Especificacion(nombre, tipo, precio));
                } catch (IllegalArgumentException e) {
                    System.err.println("Error al parsear especificación (tipo o número inválido): " + par);
                }
            }
        }
    }

    /**
     * Devuelve una lista inmodificable de especificaciones del articulo.
     *
     * @return Lista de especificaciones.
     */
    public List<Especificacion> getEspecificaciones() {
        return Collections.unmodifiableList(especificaciones);
    }
    /**
     * Devuelve el codigo unico del articulo.
     *
     * @return Codigo del articulo.
     */
    public String getCodigo() { return codigo; }

    /**
     * Devuelve la descripcion/nombre del articulo.
     *
     * @return Descripcion del articulo.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Devuelve el precio del articulo durante el horario diurno.
     *
     * @return Precio diurno.
     */
    public double getPrecioDiurno() { return precioDiurno; }

    /**
     * Devuelve el precio del articulo durante el horario nocturno.
     *
     * @return Precio nocturno.
     */
    public double getPrecioNocturno() { return precioNocturno; }

    /**
     * Devuelve el precio del articulo durante la Happy Hour.
     *
     * @return Precio de Happy Hour.
     */
    public double getPrecioHappyHour() { return precioHappyHour; }

    /**
     * Devuelve la ruta de la imagen asociada al articulo.
     *
     * @return Ruta de la imagen.
     */
    public String getImagenPath() { return imagenPath; }

    /**
     * Retorna el precio del articulo segun la hora del día.
     *
     * @param hora Hora actual (formato 0-23).
     * @return Precio correspondiente al horario.
     */
    public double getPrecioPorHora(int hora) {
        if (hora >= 20 || hora < 6) {
            return precioNocturno;
        } else {
            return precioDiurno;
        }
    }
}