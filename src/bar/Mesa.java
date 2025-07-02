package bar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Todas las funciones relacionadas con el cierre/apertura de mesas y sumar consumos a las mismas.
 */
public class Mesa {
    int numero;
    Integer horaApertura;
    ArrayList<Consumo> consumos;

    public Mesa(int numero) {
        this.numero = numero;
        this.horaApertura = null;
        this.consumos = new ArrayList<>();
    }

    /**
     * Remueve un consumo de la lista usando su índice.
     * @param indice El índice del consumo a remover.
     * @return true si se removió exitosamente, false en caso contrario.
     */
    public boolean removerConsumo(int indice) {
        if (indice >= 0 && indice < this.consumos.size()) {
            this.consumos.remove(indice);
            return true;
        }
        return false;
    }

    /**
     * Funcion para cerrar una mesa que este ocupada, devolviendo el ticket de los consumos realizados.
     * @return
     */
    public String cerrar() {
        StringBuilder ticket = new StringBuilder();
        double total = 0;
        ticket.append("Ticket - Mesa ").append(numero).append("\n");
        ticket.append("----------------------------------------\n");
        ticket.append(String.format("%-20s %5s %10s\n", "Producto", "Cant.", "Subtotal"));
        ticket.append("----------------------------------------\n");

        for (Consumo c : consumos) {
            double subtotal = c.getSubtotal();
            String descripcionCompleta = c.articulo.getDescripcion();
            // Añadir detalle si existe
            if (c.getDetalle() != null && !c.getDetalle().isEmpty()) {
                descripcionCompleta += " " + c.getDetalle();
            }

            ticket.append(String.format("%-20.20s %5d %10.2f\n",
                    descripcionCompleta, c.getCantidad(), subtotal));
            total += subtotal;
        }

        ticket.append("----------------------------------------\n");
        ticket.append(String.format("TOTAL: %26.2f\n", total));
        ticket.append("----------------------------------------\n");

        consumos.clear();
        horaApertura = null;
        return ticket.toString();
    }

    /**
     * Devuelve el numero de mesa actualmente seleccionada.
     * @return
     */
    public int getNumero() { return numero; }

    /**
     * Devuelve a la hora que se abrio una mesa.
     * @return
     */
    public Integer getHoraApertura() { return horaApertura; }

    /**
     * Informa si la mesa esta ocupada/desocupada
     * @return
     */
    public boolean estaOcupada() { return horaApertura != null; }

    /**
     * Si la mesa seleccionada esta desocupada, la abre.
     * @param hora Hora de apertura
     */
    public void abrir(int hora) { if (!estaOcupada()) { this.horaApertura = hora; this.consumos.clear(); } }

    /**
     * Se le suman los consumos que se realizan en la mesa.
     * @param consumo
     */
    public void agregarConsumo(Consumo consumo) { if (estaOcupada()) { this.consumos.add(consumo); } }

    /**
     * Devuelve todos los consumos realizados en la mesa.
     * @return
     */
    public List<Consumo> getConsumos() { return Collections.unmodifiableList(consumos); }
}