package py.una.pol.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ResultPath implements Serializable {
    private static final long serialVersionUID = -7784888753852315823L;

    //Paths de la solucion
    private List<Path> paths;

    //Servidores donde se instalaron los VNFs
    List<String> serverVnf;
}
