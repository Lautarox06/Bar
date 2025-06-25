public class Articulo {
    String codigo;
    String descripcion;
    double precioDiurno;
    double precioNocturno;
    double precioHappyHour;
    String imagenPath;

    public Articulo(String codigo, String descripcion, double precioDiurno, double precioNocturno, double precioHappyHour, String imagenPath) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.precioDiurno = precioDiurno;
        this.precioNocturno = precioNocturno;
        this.precioHappyHour = precioHappyHour;
        this.imagenPath = imagenPath;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecioDiurno() {
        return precioDiurno;
    }

    public double getPrecioNocturno() {
        return precioNocturno;
    }

    public double getPrecioHappyHour() {
        return precioHappyHour;
    }

    public String getImagenPath() {
        return imagenPath;
    }

    /**
     * Calcula el precio del artículo solo según la hora del día (diurno/nocturno).
     * Esta función ya NO considera Happy Hour; esa lógica se manejará antes de crear el Consumo.
     * @param hora La hora actual (0-23).
     * @return El precio correspondiente (diurno o nocturno).
     */
    public double getPrecioPorHora(int hora) {
        if (hora >= 9 && hora < 21) { // Horario Diurno (ejemplo)
            return precioDiurno;
        } else { // Horario Nocturno (ejemplo)
            return precioNocturno;
        }
    }
}