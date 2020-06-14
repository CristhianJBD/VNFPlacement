package py.una.pol.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResultPath {

    //Paths de la solucion
    private List<Path> paths;

    //Servidores donde se instalaron los VNFs
    List<String> serverVnf;
}
