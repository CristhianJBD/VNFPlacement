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

    //Costo de Deployar o Instalar el VNF
    private int deploy;

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

    public Server() {
    }

    public Server(Server server) {
        this.id = server.getId();
        this.licenceCost = server.getLicenceCost();
        this.deploy = server.getDeploy();
        this.energyCost = server.getEnergyCost();
        this.resourceCPU = server.getResourceCPU();
        this.resourceRAM = server.getResourceRAM();
        this.resourceStorage = server.getResourceStorage();
        this.resourceCPUCost = server.getResourceCPUCost();
        this.resourceRAMCost = server.getResourceRAMCost();
        this.resourceStorageCost = server.getResourceStorageCost();
        this.energyPerCoreWatts = server.getEnergyPerCoreWatts();
        this.energyPeakWatts = server.getEnergyPeakWatts();
        this.resourceRAMUsed = server.getResourceRAMUsed();
        this.resourceStorageUsed = server.getResourceStorageUsed();
        this.resourceCPUUsed = server.getResourceCPUUsed();
        this.energyUsed = server.getEnergyUsed();

        List<Vnf> vnfs = new ArrayList<>();
        for(Vnf vnf : server.getVnf()){
            Vnf vnfToCopy = new Vnf();
            vnfToCopy.setId(vnf.getId());
            vnfToCopy.setDelay(vnf.getDelay());
            vnfToCopy.setDeploy(vnf.getDeploy());
            vnfToCopy.setBandwidthFactor(vnf.getBandwidthFactor());
            vnfToCopy.setLicenceCost(vnf.getLicenceCost());
            vnfToCopy.setResourceCPU(vnf.getResourceCPU());
            vnfToCopy.setResourceRAM(vnf.getResourceRAM());
            vnfToCopy.setResourceStorage(vnf.getResourceStorage());
            vnfs.add(vnfToCopy);
        }
        this.vnf = vnfs;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Server{");
        sb.append("id='").append(id).append('\'');
        sb.append(", vnf=").append(vnf);
        sb.append(", licenceCost=").append(licenceCost);
        sb.append(", deploy=").append(deploy);
        sb.append(", energyCost=").append(energyCost);
        sb.append(", resourceCPU=").append(resourceCPU);
        sb.append(", resourceRAM=").append(resourceRAM);
        sb.append(", resourceStorage=").append(resourceStorage);
        sb.append(", resourceCPUCost=").append(resourceCPUCost);
        sb.append(", resourceRAMCost=").append(resourceRAMCost);
        sb.append(", resourceStorageCost=").append(resourceStorageCost);
        sb.append(", energyPerCoreWatts=").append(energyPerCoreWatts);
        sb.append(", energyPeakWatts=").append(energyPeakWatts);
        sb.append(", resourceRAMUsed=").append(resourceRAMUsed);
        sb.append(", resourceStorageUsed=").append(resourceStorageUsed);
        sb.append(", resourceCPUUsed=").append(resourceCPUUsed);
        sb.append(", energyUsed=").append(energyUsed);
        sb.append('}');
        return sb.toString();
    }
}

