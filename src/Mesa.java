import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
     * NUEVO: Remueve un consumo de la lista usando su índice.
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

    // MODIFICADO: El método cerrar() ahora imprime el detalle del consumo.
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

    // El resto de los métodos permanecen igual...
    public int getNumero() { return numero; }
    public Integer getHoraApertura() { return horaApertura; }
    public boolean estaOcupada() { return horaApertura != null; }
    public void abrir(int hora) { if (!estaOcupada()) { this.horaApertura = hora; this.consumos.clear(); } }
    public void agregarConsumo(Consumo consumo) { if (estaOcupada()) { this.consumos.add(consumo); } }
    public List<Consumo> getConsumos() { return Collections.unmodifiableList(consumos); }
}