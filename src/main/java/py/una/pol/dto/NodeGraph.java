package py.una.pol.dto;

import lombok.Data;
import py.una.pol.dto.NFVdto.Node;

@Data
public class NodeGraph {

    //Nodo actual del grafo
    private Node node;

    //Nodo actualizado que debe cargarse al grafo
    private Node nodeUpdated;

    //Indica si se modifico o no el nodo del grafo
    private boolean updated;

    public NodeGraph(Node node) {
        this.node = node;
        this.nodeUpdated = new Node();
        this.nodeUpdated.setId(node.getId());
        this.nodeUpdated.setEnergyCost(node.getEnergyCost());
        this.nodeUpdated.setServer(node.getServer());
        this.updated = false;
    }
}
