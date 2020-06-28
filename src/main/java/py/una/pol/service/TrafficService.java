package py.una.pol.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import py.una.pol.dto.NFVdto.Node;
import py.una.pol.dto.NFVdto.SFC;
import py.una.pol.dto.NFVdto.Traffic;
import py.una.pol.dto.NFVdto.Vnf;
import py.una.pol.util.Configurations;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class TrafficService {
    Logger logger = Logger.getLogger(TrafficService.class);

    @Autowired
    private Configurations conf;

    public Traffic generateRandomtraffic(Map<String, Node> nodesMap, List<Vnf> vnfs) throws Exception {
        Random rn = new Random();
        Object[] arrayNode;
        int sfcSize;
        boolean aux = false;
        try {

            Traffic traffic = new Traffic();
            traffic.setBandwidth(rn.nextInt
                    (conf.getTrafficBandwidthMax()-conf.getTrafficBandwidthMin() + 1) + conf.getTrafficBandwidthMin());
            traffic.setDelayMaxSLA(rn.nextInt
                    (conf.getTrafficDelaySlaMax()- conf.getTrafficDelaySlaMin() + 1) + conf.getTrafficDelaySlaMin());
            traffic.setPenaltyCostSLO(rn.nextInt
                    (conf.getTrafficPenaltySloMax()-conf.getTrafficPenaltySloMin() + 1) + conf.getTrafficPenaltySloMin());
            traffic.setProcessed(false);

            arrayNode = nodesMap.values().toArray();
            while (!aux) {
                traffic.setNodeDestiny((Node) arrayNode[rn.nextInt(arrayNode.length)]);
                traffic.setNodeOrigin((Node) arrayNode[rn.nextInt(arrayNode.length)]);
                if (!traffic.getNodeOrigin().equals(traffic.getNodeDestiny()))
                    aux = true;
            }

            sfcSize = rn.nextInt(conf.getTrafficSfcMax() - conf.getTrafficSfcMin())
                    + conf.getTrafficSfcMin();

            SFC sfc = new SFC();
            for(int i = 0; i < sfcSize ; i++) {
                sfc.getVnfs().add(vnfs.get(rn.nextInt(vnfs.size())));
            }
            traffic.setSfc(sfc);
            return traffic;
        } catch (Exception e) {
            logger.error("Error al crear el traffico Random:" + e.getMessage());
            throw new Exception();
        }
    }
}
