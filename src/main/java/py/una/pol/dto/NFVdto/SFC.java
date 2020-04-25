package py.una.pol.dto.NFVdto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SFC {
    //Secuencia de VNFs - Cadena de Servicio
    List<Vnf> vnfs = new ArrayList<>();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SFC{");
        sb.append("vnfs=").append(vnfs);
        sb.append('}');
        return sb.toString();
    }
}
