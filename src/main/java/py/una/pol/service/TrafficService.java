package py.una.pol.service;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import py.una.pol.dto.NFVdto.*;
import py.una.pol.util.Configurations;
import py.una.pol.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class TrafficService {
    Logger logger = Logger.getLogger(TrafficService.class);

    private final Configurations conf;

    public TrafficService(Configurations conf) {
        this.conf = conf;
    }

    public List<Traffic> generateRandomtraffic(Map<String, Node> nodesMap, List<Vnf> vnfs) throws Exception {
        List<Traffic> traffics = new ArrayList<>();
        Random rn = new Random();
        int sfcSize;
        boolean aux;
        String[] nodesIdArray = new String[nodesMap.size()];
        int nodesSize;
        try {
            int i = 0;
            for(Node node : nodesMap.values())
                nodesIdArray[i++] = node.getId();
            nodesSize = nodesIdArray.length;

            for (int j = 0; j < conf.getNumberTraffic(); j++) {
                Traffic traffic = new Traffic();
                traffic.setBandwidth(rn.nextInt
                        (conf.getTrafficBandwidthMax() - conf.getTrafficBandwidthMin() + 1) + conf.getTrafficBandwidthMin());
                traffic.setDelayMaxSLA(rn.nextInt
                        (conf.getTrafficDelaySlaMax() - conf.getTrafficDelaySlaMin() + 1) + conf.getTrafficDelaySlaMin());
                traffic.setPenaltyCostSLO(rn.nextInt
                        (conf.getTrafficPenaltySloMax() - conf.getTrafficPenaltySloMin() + 1) + conf.getTrafficPenaltySloMin());
                traffic.setProcessed(false);

                aux = false;
                while (!aux) {
                    traffic.setNodeDestinyId(nodesIdArray[rn.nextInt(nodesSize)]);
                    traffic.setNodeOriginId(nodesIdArray[rn.nextInt(nodesSize)]);
                    if (!traffic.getNodeOriginId().equals(traffic.getNodeDestinyId()))
                        aux = true;
                }

                sfcSize = rn.nextInt(conf.getTrafficSfcMax() - conf.getTrafficSfcMin())
                        + conf.getTrafficSfcMin();

                Vnf vnf;
                SFC sfc = new SFC();
                for (int k = 0; k < sfcSize; k++) {
                    vnf = new Vnf(vnfs.get(rn.nextInt(vnfs.size())));
                    sfc.getVnfs().add(vnf);
                }
                traffic.setSfc(sfc);
                traffics.add(traffic);
            }
            return traffics;
        } catch (Exception e) {
            logger.error("Error al crear el traffico Random:" + e.getMessage());
            throw new Exception();
        }
    }
}
