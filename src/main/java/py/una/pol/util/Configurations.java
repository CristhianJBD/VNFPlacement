package py.una.pol.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.apache.log4j.Logger;
import py.una.pol.dto.NFVdto.Node;
import py.una.pol.dto.NFVdto.Server;
import py.una.pol.dto.NFVdto.VnfShared;

import java.util.ArrayList;
import java.util.List;


@Configuration
@PropertySource("file:${app.home}/vnf_placement.properties")
@Data
public class Configurations {
    Logger logger = Logger.getLogger(Configurations.class);

    public List<VnfShared> vnfs = new ArrayList<>();
    public List<Server> servers = new ArrayList<>();
    public List<Node> nodes = new ArrayList<>();

    //Parametros de Sistema
    @Value("${matrix.file.name}")
    private String matrixFileName;
    @Value("${traffics.file.name}")
    private String trafficsFileName;
    @Value("${number.solution}")
    private Integer numberSolutions;
    @Value("${number.traffic}")
    private Integer numberTraffic;
    @Value("${k.shortest}")
    private Integer k;
    @Value("${retries.solution}")
    private Integer retriesSolution;
    @Value("${read.traffic.file}")
    private boolean readTrafficFile;

    //VNF Shared
    @Value("${vnf.shared.size}")
    private Integer vnfSharedSize;
    @Value("${vnf.shared.id}")
    private String vnfSharedId;
    @Value("${vnf.shared.delay}")
    private String vnfSharedDelay;
    @Value("${vnf.shared.deploy}")
    private String vnfSharedDeploy;
    @Value("${vnf.shared.resource.cpu}")
    private String vnfSharedResourceCPU;
    @Value("${vnf.shared.resource.ram}")
    private String vnfSharedResourceRAM;
    @Value("${vnf.shared.resource.storage}")
    private String vnfSharedResourceStorage;
    @Value("${vnf.shared.licence.cost}")
    private String vnfSharedLicenceCost;
    @Value("${vnf.shared.bandwidth.factor}")
    private String vnfSharedBandwidthFactor;

    //VNF Shared
    @Value("${vnf.size}")
    private Integer vnfSize;
    @Value("${vnf.id}")
    private String vnfId;
    @Value("${vnf.resource.cpu}")
    private String vnfResourceCPU;
    @Value("${vnf.resource.ram}")
    private String vnfResourceRAM;

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


}
