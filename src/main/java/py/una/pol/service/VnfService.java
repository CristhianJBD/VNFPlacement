package py.una.pol.service;

import org.apache.log4j.Logger;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import py.una.pol.dto.SFC;
import py.una.pol.dto.Trafic;
import py.una.pol.dto.Vnf;

import java.util.List;


@Component
public class VnfService {
    Logger logger = Logger.getLogger(VnfService.class);

    @Autowired
    private DataService data;

    public boolean placement (){
        try {
            data.loadData();

            Trafic trafic = new Trafic();
            trafic.setBandwidth(300);
            trafic.setDelayMaxSLA(3);
            trafic.setNodeDestiny(data.nodes.get(5));
            trafic.setNodeOrigin(data.nodes.get(0));
            trafic.setPenaltyCostSLO(4);

            SFC sfc = new SFC();
            trafic.setSfc(sfc);

            DijkstraShortestPath dijkstraShortestPath = new DijkstraShortestPath(data.graph);

            List<Vnf> shortestPath = dijkstraShortestPath
                    .getPath(trafic.getNodeOrigin(),trafic.getNodeDestiny()).getVertexList();

            logger.info(shortestPath);
        }catch (Exception e){
            logger.error("Error Placement VNF");
        }
        return true;
    }

}
