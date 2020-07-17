package py.una.pol.dto.NFVdto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VnfShared {
    //Identificador del VNF
    private String id;

    //Vnfs del mismo tipo que comparten
    private List<Vnf> vnfs = new ArrayList<>();

    //Delay de procesamiento del VNF
    private int delay;

    //Costo de Deployar o Instalar el VNF
    private int deploy;

    //Costo de licencia del VNF (En dolares)
    private double licenceCost;

    //Factor de modiicacion del VNF (En %)
    private double bandwidthFactor;

    //Requerimiento de CPU del VNF (Cantidad de Cores)
    private int resourceCPU;

    //CPU utilizado
    private int resourceCPUUsed;

    //Requerimiento de RAM del VNF (En GB)
    private int resourceRAM;

    //RAM utilizada
    private int resourceRAMUsed;

    //Requerimiento de almacenamiento del VNF (En GB)
    private int resourceStorage;

    public VnfShared(VnfShared vnfShared) {
        this.id = vnfShared.getId();
        this.delay = vnfShared.getDelay();
        this.deploy = vnfShared.getDeploy();
        this.resourceCPU = vnfShared.getResourceCPU();
        this.resourceRAM = vnfShared.getResourceRAM();
        this.resourceStorage = vnfShared.getResourceStorage();
        this.licenceCost = vnfShared.getLicenceCost();
        this.bandwidthFactor = vnfShared.getBandwidthFactor();
        this.resourceRAMUsed = vnfShared.getResourceRAMUsed();
        this.resourceCPUUsed = vnfShared.getResourceCPUUsed();

        List<Vnf> vnfs = new ArrayList<>();
        vnfShared.getVnfs().forEach(vnf -> {
            vnfs.add(new Vnf(vnf.getId(),vnf.getType(),
                    vnf.getResourceCPU(), vnf.getResourceRAM()));
        });

        this.vnfs = vnfs;
    }

    public VnfShared() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VnfShared{");
        sb.append("id='").append(id).append('\'');
        sb.append(", delay=").append(delay);
        sb.append(", deploy=").append(deploy);
        sb.append(", licenceCost=").append(licenceCost);
        sb.append(", bandwidthFactor=").append(bandwidthFactor);
        sb.append(", resourceStorage=").append(resourceStorage);
        sb.append(", resourceCPU=").append(resourceCPU);
        sb.append(", resourceCPUUsed=").append(resourceCPUUsed);
        sb.append(", resourceRAM=").append(resourceRAM);
        sb.append(", resourceRAMUsed=").append(resourceRAMUsed);
        sb.append('}');
        return sb.toString();
    }
}
