package py.una.pol.dto.NFVdto;
import lombok.Data;

import java.io.Serializable;

@Data
public class Vnf implements Serializable {
        private static final long serialVersionUID = -4346254958072354423L;
    //Identificador del VNF
    private String id;

    //Tipo del VNF
    private String type;

    //Requerimiento de CPU del VNF (Cantidad de Cores)
    private int resourceCPU;

    //Requerimiento de RAM del VNF (En GB)
    private int resourceRAM;

    public Vnf() {
    }

    public Vnf(String id, String type, int resourceCPU, int resourceRAM) {
        this.id = id;
        this.type = type;
        this.resourceCPU = resourceCPU;
        this.resourceRAM = resourceRAM;
    }

    public Vnf(Vnf vnf) {
        this.id = vnf.getId();
        this.type = vnf.getType();
        this.resourceCPU = vnf.getResourceCPU();
        this.resourceRAM = vnf.getResourceRAM();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Vnf{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type=").append(type);
        sb.append(", resourceCPU=").append(resourceCPU);
        sb.append(", resourceRAM=").append(resourceRAM);
        sb.append('}');
        return sb.toString();
    }
}
