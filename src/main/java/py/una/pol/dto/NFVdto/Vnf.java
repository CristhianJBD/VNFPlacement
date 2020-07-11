package py.una.pol.dto.NFVdto;


import lombok.Data;
import py.una.pol.util.Constants;

@Data
public class Vnf {
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

    public Vnf(Vnf vnf) {
        String[] split = vnf.getId().split(Constants.separatorVnf);
        this.id = split[0];
        this.type = split[1];
        this.resourceCPU = vnf.getResourceCPU();
        this.resourceRAM = vnf.getResourceRAM();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Vnf{");
        sb.append("id='").append(id).append('\'');
        sb.append(", resourceCPU=").append(resourceCPU);
        sb.append(", resourceRAM=").append(resourceRAM);
        sb.append('}');
        return sb.toString();
    }
}
