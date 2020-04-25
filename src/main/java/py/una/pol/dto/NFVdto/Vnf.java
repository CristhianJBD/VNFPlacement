package py.una.pol.dto.NFVdto;

import lombok.Data;

@Data
public class Vnf {
    //Identificador del VNF
    private String id;

    //Delay de procesamiento del VNF
    private int delay;

    //Costo de Deployar o Instalar el VNF
    private int deploy;

    //Requerimiento de CPU del VNF (Cantidad de Cores)
    private int resourceCPU;

    //Requerimiento de RAM del VNF (En GB)
    private int resourceRAM;

    //Requerimiento de almacenamiento del VNF (En GB)
    private int resourceStorage;

    //Costo de licencia del VNF (En dolares)
    private double licenceCost;

    //Factor de modiicacion del VNF (En %)
    private double bandwidthFactor;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Vnf{");
        sb.append("id='").append(id).append('\'');
        sb.append(", delay=").append(delay);
        sb.append(", deploy=").append(deploy);
        sb.append(", resourceCPU=").append(resourceCPU);
        sb.append(", resourceRAM=").append(resourceRAM);
        sb.append(", resourceStorage=").append(resourceStorage);
        sb.append(", licenceCost=").append(licenceCost);
        sb.append(", bandwidthFactor=").append(bandwidthFactor);
        sb.append('}');
        return sb.toString();
    }
}
