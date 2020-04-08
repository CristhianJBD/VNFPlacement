package py.una.pol.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SFC {
    //Secuencia de VNFs - Cadena de Servicio
    List<Vnf> vnfs = new ArrayList<>();

}
