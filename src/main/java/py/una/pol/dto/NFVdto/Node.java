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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Node:");
        sb.append("id='").append(id).append('\'');
        sb.append(", server=").append(server);
        sb.append(", energyCost=").append(energyCost);
        return sb.toString();
    }
}
