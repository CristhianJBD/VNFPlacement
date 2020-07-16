package py.una.pol.dto.NFVdto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SFC implements Serializable {
    private static final long serialVersionUID = -6931116426223595911L;
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
