package py.una.pol.dto;

import lombok.Data;

import java.util.List;

@Data
public class Server {

    //Identificador del Servidor
    private String id;

    //Vnf que es instalado en el Servidor
    private List<Vnf> vnf;

    //Costo de licencia del Servidor
    private Integer licenceCost;

    //Costo de Energia en Dolares por Watt
    private Integer energyCost;

    //Capacidad de CPU del Servidor (Cantidad de Cores)
    private Integer resourceCPU;

    //Capacidad de RAM del Servidor (En GB)
    private Integer resourceRAM;

    //Capacidad de almacenamiento del Servidor (En GB)
    private Integer resourceStorage;

    //Consumo de Energia en Watts al activar el Servidor
    private Integer energyPerCoreWatts;

    //Capacidad maxima de Energia en Watts del Servidor
    private Integer energyPeakWatts;

    //RAM utilizada
    private Integer resourseUsedRAM;

    //Storage Utilizado
    private Integer resourseUsedStorage;

    //CPU utilizado
    private Integer resourceUsedCPU;

    //Energia utilizada
    private Integer energyUsed;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Server: ");
        sb.append("id='").append(id).append('\'');
        sb.append(", licenceCost=").append(licenceCost);
        sb.append(", energyCost=").append(energyCost);
        sb.append(", resourceCPU=").append(resourceCPU);
        sb.append(", resourceRAM=").append(resourceRAM);
        sb.append(", resourceStorage=").append(resourceStorage);
        sb.append(", energyIdleWatts=").append(energyPerCoreWatts);
        sb.append(", resourseUsedRAM=").append(resourseUsedRAM);
        sb.append(", resourseUsedStorage=").append(resourseUsedStorage);
        sb.append(", resourceUsedCPU=").append(resourceUsedCPU);
        sb.append(", energyUsed=").append(energyUsed);
        return sb.toString();
    }
}

