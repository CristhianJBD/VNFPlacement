package py.una.pol.dto.NFVdto;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class Server {

    //Identificador del Servidor
    private String id;

    //Vnf que es instalado en el Servidor
    private Map<String, List<VnfShared>> vnfs = new HashMap<>();

    //Costo de Deployar o Instalar el VNF
    private int deploy;

    //Costo de licencia del Servidor
    private double licenceCost;

    //Consumo de Energia por core en whatts
    private int energyIdleWatts;

    //Capacidad maxima de Energia en Watts del Servidor
    private int energyPeakWatts;

    //Costo por unidad de CPU del Servidor (En dolares)
    private double resourceCPUCost;

    //Costo por unidad de RAM en GB del Servidor (En dolares)
    private double resourceRAMCost;

    //Costo por unidad de almacenamiento del Servidor (En dolares)
    private double resourceStorageCost;

    //Capacidad de CPU del Servidor (Cantidad de Cores)
    private int resourceCPU;

    //CPU utilizado
    private int resourceCPUUsed;

    //Capacidad de RAM del Servidor (En GB)
    private int resourceRAM;

    //RAM utilizada
    private int resourceRAMUsed;

    //Capacidad de almacenamiento del Servidor (En GB)
    private int resourceStorage;

    //Storage Utilizado
    private int resourceStorageUsed;


    public Server() {
    }

    public Server(Server server) {
        this.id = server.getId();
        this.licenceCost = server.getLicenceCost();
        this.deploy = server.getDeploy();
        this.resourceCPU = server.getResourceCPU();
        this.resourceRAM = server.getResourceRAM();
        this.resourceStorage = server.getResourceStorage();
        this.resourceCPUCost = server.getResourceCPUCost();
        this.resourceRAMCost = server.getResourceRAMCost();
        this.resourceStorageCost = server.getResourceStorageCost();
        this.energyIdleWatts = server.getEnergyIdleWatts();
        this.energyPeakWatts = server.getEnergyPeakWatts();
        this.resourceRAMUsed = server.getResourceRAMUsed();
        this.resourceStorageUsed = server.getResourceStorageUsed();
        this.resourceCPUUsed = server.getResourceCPUUsed();

        this.vnfs = server.getVnfs().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new ArrayList<>(copyVnfsShared(e.getValue()))));
    }

    public List<VnfShared> copyVnfsShared(List<VnfShared> vnfsShared){
        List<VnfShared> vnfsharedToCopy = new ArrayList<>();

        vnfsShared.forEach((vnfShared) -> {
            vnfsharedToCopy.add( new VnfShared(vnfShared));
        });
        return vnfsharedToCopy;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Server{");
        sb.append("id='").append(id).append('\'');
        sb.append(", vnf=").append(vnfs);
        sb.append(", licenceCost=").append(licenceCost);
        sb.append(", deploy=").append(deploy);
        sb.append(", energyPeakWatts=").append(energyPeakWatts);
        sb.append(", energyIdleWatts=").append(energyIdleWatts);
        sb.append(", resourceCPUCost=").append(resourceCPUCost);
        sb.append(", resourceRAMCost=").append(resourceRAMCost);
        sb.append(", resourceStorageCost=").append(resourceStorageCost);
        sb.append(", resourceCPU=").append(resourceCPU);
        sb.append(", resourceCPUUsed=").append(resourceCPUUsed);
        sb.append(", resourceRAM=").append(resourceRAM);
        sb.append(", resourceRAMUsed=").append(resourceRAMUsed);
        sb.append(", resourceStorage=").append(resourceStorage);
        sb.append(", resourceStorageUsed=").append(resourceStorageUsed);

        sb.append('}');
        return sb.toString();
    }
}

