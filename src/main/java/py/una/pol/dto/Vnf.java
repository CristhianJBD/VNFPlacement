package py.una.pol.dto;

import lombok.Data;

@Data
public class Vnf {
    //Identificador del VNF
    private String id;

    //Delay de procesamiento del VNF
    private Integer delay;

    //Costo de Deployar o Instalar el VNF
    private Integer deploy;

    //Requerimiento de CPU del VNF (Cantidad de Cores)
    private Integer resourceCPU;

    //Requerimiento de RAM del VNF (En GB)
    private Integer resourceRAM;

    //Requerimiento de almacenamiento del VNF (En GB)
    private Integer resourceStorage;

    //Costo de licencia del VNF (En dolares)
    private Integer licenceCost;

    //Factor de modiicacion del VNF (En %)
    private Double bandwidthFactor;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Vnf: ");
        sb.append("id='").append(id).append('\'');
        sb.append(", delay=").append(delay);
        sb.append(", deploy=").append(deploy);
        sb.append(", resourceCPU=").append(resourceCPU);
        sb.append(", resourceRAM=").append(resourceRAM);
        sb.append(", resourceStorage=").append(resourceStorage);
        sb.append(", licenceCost=").append(licenceCost);
        sb.append(", bandwidthFactor=").append(bandwidthFactor);
        return sb.toString();
    }
}
