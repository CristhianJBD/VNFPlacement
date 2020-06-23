package py.una.pol.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.apache.log4j.Logger;
import py.una.pol.dto.NFVdto.Node;
import py.una.pol.dto.NFVdto.Server;
import py.una.pol.dto.NFVdto.Vnf;

import java.util.ArrayList;
import java.util.List;


@Configuration
@PropertySource("file:${app.home}/vnf_placement.properties")
@Data
public class Configurations {
    Logger logger = Logger.getLogger(Configurations.class);

    public List<Vnf> vnfs = new ArrayList<>();
    public List<Server> servers = new ArrayList<>();
    public List<Node> nodes = new ArrayList<>();

    //Parametros de Sistema
    @Value("${separator}")
    private String separator;
    @Value("${matrix.name}")
    private String matrixName;
    @Value("${number.solutions}")
    private Integer numberSolutions;
    @Value("${k.shortest}")
    private Integer k;
    @Value("${retries.solution}")
    private Integer retriesSolution;

    //VNF
    @Value("${vnf.size}")
    private Integer vnfSize;
    @Value("${vnf.id}")
    private String vnfId;
    @Value("${vnf.delay}")
    private String vnfDelay;
    @Value("${vnf.deploy}")
    private String vnfDeploy;
    @Value("${vnf.resource.cpu}")
    private String vnfResourceCPU;
    @Value("${vnf.resource.ram}")
    private String vnfResourceRAM;
    @Value("${vnf.resource.storage}")
    private String vnfResourceStorage;
    @Value("${vnf.licence.cost}")
    private String vnfLicenceCost;
    @Value("${vnf.bandwidth.factor}")
    private String vnfBandwidthFactor;

    //Servidor
    @Value("${server.size}")
    private Integer serverSize;
    @Value("${server.id}")
    private String serverId;
    @Value("${server.licence.cost}")
    private String serverLicenceCost;
    @Value("${server.deploy}")
    private String serverDeploy;
    @Value("${server.energy.cost}")
    private String serverEnergyCost;
    @Value("${server.resource.cpu}")
    private String serverResourceCPU;
    @Value("${server.resource.ram}")
    private String serverResourceRAM;
    @Value("${server.resource.storage}")
    private String serverResourceStorage;
    @Value("${server.resource.cpu.cost}")
    private String serverResourceCPUCost;
    @Value("${server.resource.ram.cost}")
    private String serverResourceRAMCost;
    @Value("${server.resource.storage.cost}")
    private String serverResourceStorageCost;
    @Value("${server.energy.per.core.watts}")
    private String serverEnergyPerCoreWatts;
    @Value("${server.energy.peak.watts}")
    private String serverEnergyPeakWatts;
    @Value("${server.penalty.cpu.cost}")
    private double serverPenaltyCPUCost;
    @Value("${server.penalty.ram.cost}")
    private double serverPenaltyRAMCost;
    @Value("${server.penalty.storage.cost}")
    private double serverPenaltyStorageCost;
    @Value("${link.penalty.bandwidth.cost}")
    private double linkPenaltyBandwidthCost;

    //Nodo
    @Value("${node.size}")
    private Integer nodeSize;
    @Value("${node.id}")
    private String nodeId;
    @Value("${node.energy.cost}")
    private String nodeEnergyCost;
    @Value("${node.server}")
    private String nodeServer;

    //Trafico
    @Value("${traffic.bandwidth.min}")
    private int trafficBandwidthMin;
    @Value("${traffic.bandwidth.max}")
    private int trafficBandwidthMax;
    @Value("${traffic.delay.sla.min}")
    private int trafficDelaySlaMin;
    @Value("${traffic.delay.sla.max}")
    private int trafficDelaySlaMax;
    @Value("${traffic.penalty.slo.min}")
    private int trafficPenaltySloMin;
    @Value("${traffic.penalty.slo.max}")
    private int trafficPenaltySloMax;
    @Value("${traffic.sfc.min}")
    private int trafficSfcMin;
    @Value("${traffic.sfc.max}")
    private int trafficSfcMax;

    public String toStringVNFs() {
        final StringBuilder sb = new StringBuilder("VNFs{");
        sb.append("vnfSize=").append(vnfSize);
        sb.append(", vnfID='").append(vnfId).append('\'');
        sb.append(", vnfDelay='").append(vnfDelay).append('\'');
        sb.append(", vnfDeploy='").append(vnfDeploy).append('\'');
        sb.append(", vnfResourceCPU='").append(vnfResourceCPU).append('\'');
        sb.append(", vnfResourceRAM='").append(vnfResourceRAM).append('\'');
        sb.append(", vnfResourceStorage='").append(vnfResourceStorage).append('\'');
        sb.append(", vnfLicenceCost='").append(vnfLicenceCost).append('\'');
        sb.append(", vnfBandwidthFactor='").append(vnfBandwidthFactor).append('\'');
        sb.append('}');
        return sb.toString();
    }


    public String toStringServer() {
        final StringBuilder sb = new StringBuilder("Servers{");
        sb.append(", serverSize=").append(serverSize);
        sb.append(", serverId='").append(serverId).append('\'');
        sb.append(", serverLicenceCost='").append(serverLicenceCost).append('\'');
        sb.append(", serverEnergyCost='").append(serverEnergyCost).append('\'');
        sb.append(", serverResourceCPU='").append(serverResourceCPU).append('\'');
        sb.append(", serverResourceRAM='").append(serverResourceRAM).append('\'');
        sb.append(", serverResourceStorage='").append(serverResourceStorage).append('\'');
        sb.append(", serverResourceCPUCost='").append(serverResourceCPUCost).append('\'');
        sb.append(", serverResourceRAMCost='").append(serverResourceRAMCost).append('\'');
        sb.append(", serverResourceStorageCost='").append(serverResourceStorageCost).append('\'');
        sb.append(", serverEnergyPerCoreWatts='").append(serverEnergyPerCoreWatts).append('\'');
        sb.append(", serverEnergyPeakWatts='").append(serverEnergyPeakWatts).append('\'');
        sb.append(", serverPenaltyCPUCost=").append(serverPenaltyCPUCost);
        sb.append(", serverPenaltyRAMCost=").append(serverPenaltyRAMCost);
        sb.append(", serverPenaltyStorageCost=").append(serverPenaltyStorageCost);
        sb.append('}');
        return sb.toString();
    }

    public String toStringNodes() {
        final StringBuilder sb = new StringBuilder("Nodes{");
        sb.append("nodeSize=").append(nodeSize);
        sb.append(", nodeID='").append(nodeId).append('\'');
        sb.append(", nodeEnergyCost='").append(nodeEnergyCost).append('\'');
        sb.append(", nodeServer='").append(nodeServer).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
