package py.una.pol.dto.NFVdto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Server {

    //Identificador del Servidor
    private String id;

    //Vnf que es instalado en el Servidor
    private List<Vnf> vnf = new ArrayList<>();

    //Costo de licencia del Servidor
    private double licenceCost;

    //Costo de Energia en Dolares por Watt
    private double energyCost;

    //Capacidad de CPU del Servidor (Cantidad de Cores)
    private int resourceCPU;

    //Capacidad de RAM del Servidor (En GB)
    private int resourceRAM;

    //Capacidad de almacenamiento del Servidor (En GB)
    private int resourceStorage;

    //Costo por unidad de CPU del Servidor (En dolares)
    private double resourceCPUCost;

    //Costo por unidad de RAM en GB del Servidor (En dolares)
    private double resourceRAMCost;

    //Costo por unidad de almacenamiento del Servidor (En dolares)
    private double resourceStorageCost;

    //Consumo de Energia por core en whatts
    private int energyPerCoreWatts;

    //Capacidad maxima de Energia en Watts del Servidor
    private int energyPeakWatts;

    //RAM utilizada
    private int resourceRAMUsed;

    //Storage Utilizado
    private int resourceStorageUsed;

    //CPU utilizado
    private int resourceCPUUsed;

    //Energia utilizada
    private int energyUsed;



    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Server: ");
        sb.append("id='").append(id).append('\'');
        sb.append(", licenceCost=").append(licenceCost);
        sb.append(", energyCost=").append(energyCost);
        sb.append(", resourceCPU=").append(resourceCPU);
        sb.append(", resourceRAM=").append(resourceRAM);
        sb.append(", resourceStorage=").append(resourceStorage);
        sb.append(", resourceCPUCost=").append(resourceCPUCost);
        sb.append(", resourceRAMCost=").append(resourceRAMCost);
        sb.append(", resourceStorageCost=").append(resourceStorageCost);
        sb.append(", energyIdleWatts=").append(energyPerCoreWatts);
        sb.append(", resourceRAMUsed=").append(resourceRAMUsed);
        sb.append(", resourceStorageUsed=").append(resourceStorageUsed);
        sb.append(", resourceCPUUsed=").append(resourceCPUUsed);
        sb.append(", energyUsed=").append(energyUsed);
        return sb.toString();
    }
}

