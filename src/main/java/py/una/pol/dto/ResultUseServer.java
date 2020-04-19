package py.una.pol.dto;

import lombok.Data;
import py.una.pol.dto.NFVdto.Node;

@Data
public class ResultUseServer {

    Node node;

    double bandwidtCurrent;

    int indexSfc;

    boolean validPath;
}
