package py.una.pol.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import py.una.pol.dto.NFVdto.Node;
import py.una.pol.dto.NFVdto.SFC;
import py.una.pol.dto.NFVdto.Traffic;
import py.una.pol.dto.NFVdto.Vnf;

import java.util.List;
import java.util.Random;

@Service
public class TrafficService {
    Logger logger = Logger.getLogger(TrafficService.class);

    public Traffic generateRandomtraffic(List<Node> nodes, List<Vnf> vnfs) throws Exception {
        Random rn = new Random();
        try {
            Traffic traffic = new Traffic();
            traffic.setBandwidth(rn.nextInt(50) + 10);
            traffic.setDelayMaxSLA(rn.nextInt(35) + 1);
            traffic.setNodeDestiny(nodes.get(5));
            traffic.setNodeOrigin(nodes.get(0));
            traffic.setPenaltyCostSLO(rn.nextInt(5) + 1);

            SFC sfc = new SFC();
            sfc.getVnfs().add(vnfs.get(rn.nextInt(5)));
            sfc.getVnfs().add(vnfs.get(rn.nextInt(5)));
            sfc.getVnfs().add(vnfs.get(rn.nextInt(5)));
            traffic.setSfc(sfc);

            return traffic;
        } catch (Exception e) {
            logger.error("Error al crear el traffico Random:" + e.getMessage());
            throw new Exception();
        }
    }
}
