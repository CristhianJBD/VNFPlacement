package py.una.pol.dto.NFVdto;

import lombok.Data;

@Data
public class Node {

    //Identificador del Nodo
    private String id;

    //Servidor conectado al Nodo
    private Server server;

    //Costo en dolares por utilizar la Energia del Nodo
    private double energyCost;

    //Cantidad de flujos que pasan por el nodo
    private int trafficAmount;

    public Node() {
    }

    public Node(Node node) {
        this.id = node.getId();
        this.server = node.getServer()!=null ? new Server(node.getServer()) : null;
        this.energyCost = node.getEnergyCost();
        this.trafficAmount = node.getTrafficAmount();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Node: ");
        sb.append("id='").append(id).append('\'');
        sb.append(", server=").append(server);
        sb.append(", energyCost=").append(energyCost);
        sb.append(", trafficAmount=").append(trafficAmount);
        return sb.toString();
    }
}
