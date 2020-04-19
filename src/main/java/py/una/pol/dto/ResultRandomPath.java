package py.una.pol.dto;

import lombok.Data;
import py.una.pol.dto.NFVdto.Link;
import py.una.pol.dto.NFVdto.Node;

import java.util.List;

@Data
public class ResultRandomPath {

    //Lista de nodos aleatorios
    List<Node> randomNodes;

    //Lista de enlaces aleatorios
    List<Link> randomLinks;


}
