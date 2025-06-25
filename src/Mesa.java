import java.util.ArrayList;
import java.util.Collections; // ¡Esta importación es crucial!
import java.util.List; // Importación necesaria para List

public class Mesa {
    int numero;
    Integer horaApertura; // null si la mesa está libre, la hora de apertura si está ocupada
    ArrayList<Consumo> consumos; // Lista de consumos registrados en esta mesa

    /**
     * Constructor de Mesa.
     * @param numero El número identificador de la mesa.
     */
    public Mesa(int numero) {
        this.numero = numero;
        this.horaApertura = null; // Inicialmente la mesa está libre
        this.consumos = new ArrayList<>();
    }

    // Métodos getter
    public int getNumero() {
        return numero;
    }

    public Integer getHoraApertura() {
        return horaApertura;
    }

    /**
     * Verifica si la mesa está actualmente ocupada.
     * @return true si la mesa está ocupada (horaApertura no es null), false en caso contrario.
     */
    public boolean estaOcupada() {
        return horaApertura != null;
    }

    /**
     * Abre la mesa, asignándole una hora de apertura.
     * @param hora La hora en que la mesa es abierta.
     */
    public void abrir(int hora) {
        if (!estaOcupada()) {
            this.horaApertura = hora;
            this.consumos.clear(); // Limpiar consumos anteriores al abrir
        }
    }

    /**
     * Agrega un consumo a la lista de consumos de esta mesa.
     * @param consumo El objeto Consumo a añadir.
     */
    public void agregarConsumo(Consumo consumo) {
        if (estaOcupada()) {
            this.consumos.add(consumo);
        } else {
            System.err.println("Error: No se puede agregar consumo a una mesa cerrada.");
        }
    }

    /**
     * Obtiene una lista inmutable de los consumos de esta mesa.
     * @return Una lista de objetos Consumo.
     */
    public List<Consumo> getConsumos() {
        return Collections.unmodifiableList(consumos);
    }

    /**
     * Cierra la mesa, genera un ticket con los consumos registrados
     * y la deja disponible nuevamente.
     * @return Un String que representa el ticket de la mesa.
     */
    public String cerrar() {
        StringBuilder ticket = new StringBuilder();
        double total = 0;
        ticket.append("Ticket - Mesa ").append(numero).append("\n");
        ticket.append("--------------------------------\n");
        ticket.append(String.format("%-15s %5s %8s\n", "Producto", "Cant.", "Subtotal"));
        ticket.append("--------------------------------\n");

        for (Consumo c : consumos) {
            // ¡ESTA ES LA LÍNEA CRÍTICA! DEBE LLAMARSE SIN PARÁMETROS:
            double subtotal = c.getSubtotal();
            ticket.append(String.format("%-15s %5d %8.2f\n",
                    c.articulo.getDescripcion(), c.getCantidad(), subtotal));
            total += subtotal;
        }

        ticket.append("--------------------------------\n");
        ticket.append(String.format("TOTAL: %19.2f\n", total)); // Formato para alinear a la derecha
        ticket.append("--------------------------------\n");

        consumos.clear();     // Limpiar consumos
        horaApertura = null;  // Marcar mesa como libre
        return ticket.toString();
    }
}