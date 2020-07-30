package py.una.pol.service;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import py.una.pol.dto.NFVdto.*;
import py.una.pol.util.Configurations;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class TrafficService {
    Logger logger = Logger.getLogger(TrafficService.class);

    @Autowired
    private DataService data;

    private final Configurations conf;

    public TrafficService(Configurations conf) {
        this.conf = conf;
    }

    public List<Traffic> generateRandomtraffic(Graph<Node, Link> graph, Map<String, Node> nodesMap, List<Vnf> vnfs) throws Exception {
        List<Traffic> traffics = new ArrayList<>();
        Random rn = new Random();
        int sfcSize;int nodesSize;boolean aux;
        String[] nodesIdArray = new String[nodesMap.size()];
        try {
            if (conf.isReadTrafficFile()) {
                traffics = readTraffics();
            } else {
                int i = 0;
                for (Node node : nodesMap.values())
                    nodesIdArray[i++] = node.getId();
                nodesSize = nodesIdArray.length;

                for (int j = 0; j < conf.getNumberTraffic(); j++) {
                    Traffic traffic = new Traffic();
                    traffic.setBandwidth(rn.nextInt
                            (conf.getTrafficBandwidthMax() - conf.getTrafficBandwidthMin() + 1) + conf.getTrafficBandwidthMin());
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
                    traffic.setDelayMaxSLA(data.getDelayMax(sfc, traffic.getNodeOriginId(), traffic.getNodeDestinyId()));

                    traffics.add(traffic);
                }
                writeTraffics(traffics);
            }

            return traffics;
        } catch (Exception e) {
            logger.error("Error al generar el trafico: " + e.getMessage());
            throw new Exception();
        }
    }

    public void writeTraffics(List<Traffic> traffics) throws Exception {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        String trafficStringToWrite;
        Gson gson = new Gson();
        try {
            fileOutputStream = new FileOutputStream(new File(System.getProperty("app.home") + conf.getTrafficsFileName()));
            objectOutputStream = new ObjectOutputStream(fileOutputStream);

            for (Traffic traffic : traffics) {
                trafficStringToWrite = gson.toJson(traffic);
                objectOutputStream.writeObject(trafficStringToWrite);
            }
        } catch (Exception e) {
            logger.error("Error al escribir en el archivo de traficos");
            throw new Exception();
        } finally {
            if (objectOutputStream != null)
                objectOutputStream.close();
            if (fileOutputStream != null)
                fileOutputStream.close();
        }
    }

    public List<Traffic> readTraffics() throws Exception {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        List<Traffic> trafficList = new ArrayList<>();
        Gson gson = new Gson();
        String trafficStringRead;
        try {
            fileInputStream = new FileInputStream(new File(System.getProperty("app.home") + conf.getTrafficsFileName()));
            objectInputStream = new ObjectInputStream(fileInputStream);

            for (int i = 0; i < conf.getNumberTraffic(); i++) {
                trafficStringRead = (String) objectInputStream.readObject();
                trafficList.add(gson.fromJson(trafficStringRead, Traffic.class));
            }
            return trafficList;
        } catch (Exception e) {
            logger.error("Error al leer del archivo de traficos");
            throw new Exception();
        } finally {
            if (fileInputStream != null)
                fileInputStream.close();
            if (objectInputStream != null)
                objectInputStream.close();
        }
    }
}
