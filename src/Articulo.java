import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Articulo {
    String codigo;
    String descripcion;
    double precioDiurno;
    double precioNocturno;
    double precioHappyHour;
    String imagenPath;
    // MODIFICADO: Ahora usa una lista de objetos Especificacion
    List<Especificacion> especificaciones;

    public Articulo(String codigo, String descripcion, double precioDiurno, double precioNocturno, double precioHappyHour, String imagenPath, String especificacionesStr) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.precioDiurno = precioDiurno;
        this.precioNocturno = precioNocturno;
        this.precioHappyHour = precioHappyHour;
        this.imagenPath = imagenPath;
        parseEspecificaciones(especificacionesStr);
    }

    public Articulo(String codigo, String descripcion, double precioDiurno, double precioNocturno, double precioHappyHour, String imagenPath, List<Especificacion> especificaciones) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.precioDiurno = precioDiurno;
        this.precioNocturno = precioNocturno;
        this.precioHappyHour = precioHappyHour;
        this.imagenPath = imagenPath;
        this.especificaciones = especificaciones != null ? especificaciones : new ArrayList<>();
    }

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

    public List<Especificacion> getEspecificaciones() {
        return Collections.unmodifiableList(especificaciones);
    }

    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
    public double getPrecioDiurno() { return precioDiurno; }
    public double getPrecioNocturno() { return precioNocturno; }
    public double getPrecioHappyHour() { return precioHappyHour; }
    public String getImagenPath() { return imagenPath; }

    public double getPrecioPorHora(int hora) {
        if (hora >= 20 || hora < 6) {
            return precioNocturno;
        } else {
            return precioDiurno;
        }
    }
}