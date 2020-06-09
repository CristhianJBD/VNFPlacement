package py.una.pol.dto;

import lombok.Data;
import py.una.pol.dto.NFVdto.Link;
import py.una.pol.dto.NFVdto.Node;

import java.util.List;

@Data
public class ResultPath {

    //Paths de la solucion
    List<Path> paths;
}
