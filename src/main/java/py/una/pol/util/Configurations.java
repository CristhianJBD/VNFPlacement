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
    @Value("${links.file.name}")
    private String linksFileName;
    @Value("${nodes.file.name}")
    private String nodesFileName;
    @Value("${servers.file.name}")
    private String serversFileName;
    @Value("${vnfs.sfc.file.name}")
    private String vnfsSfcFileName;
    @Value("${vnfs.share.file.name}")
    private String vnfsShareFileName;
    @Value("${traffics.file.name}")
    private String trafficsFileName;
    @Value("${solutions.file.name}")
    private String solutionsFileName;
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

    @Value("${server.penalty.cpu.cost}")
    private double serverPenaltyCPUCost;
    @Value("${server.penalty.ram.cost}")
    private double serverPenaltyRAMCost;
    @Value("${server.penalty.storage.cost}")
    private double serverPenaltyStorageCost;
    @Value("${link.penalty.bandwidth.cost}")
    private double linkPenaltyBandwidthCost;

    //Trafico
    @Value("${traffic.bandwidth.min}")
    private int trafficBandwidthMin;
    @Value("${traffic.bandwidth.max}")
    private int trafficBandwidthMax;
    @Value("${traffic.percentage.delay.max}")
    private double trafficDelayMax;
    @Value("${traffic.penalty.slo.min}")
    private int trafficPenaltySloMin;
    @Value("${traffic.penalty.slo.max}")
    private int trafficPenaltySloMax;
    @Value("${traffic.sfc.min}")
    private int trafficSfcMin;
    @Value("${traffic.sfc.max}")
    private int trafficSfcMax;


}
