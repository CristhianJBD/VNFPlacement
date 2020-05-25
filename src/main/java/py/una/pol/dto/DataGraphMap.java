package py.una.pol.dto;
import lombok.Data;

import java.util.Map;


@Data
public class DataGraphMap {

    //Map para obtener el nodo actual del grafo y el actualizado, el valor es el id del nodo en el grafo
    private Map<String, NodeGraph> nodesGraph;

    //Map para obtener el enlace actual del grafo y el actualizado, el valor es el id del nodo en el grafo
    private Map<String, LinkGraph> linksGraph;


}
