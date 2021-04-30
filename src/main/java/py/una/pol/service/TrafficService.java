package py.una.pol.service;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import py.una.pol.dto.NFVdto.*;
import py.una.pol.util.Configurations;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TrafficService {
    static Logger logger = Logger.getLogger(TrafficService.class);
    public static List<Traffic> traffics = new ArrayList<>();

    public List<Traffic> generateRandomtraffic(Map<String, Node> nodesMap, List<Vnf> vnfs) throws Exception {
        Random rn = new Random();
        int sfcSize;int nodesSize;boolean aux;
        String[] nodesIdArray = new String[nodesMap.size()];
        try {
            if (Configurations.trafficsReadFile) {
                traffics = readTraffics();
            } else {
                int i = 0;
                for (Node node : nodesMap.values())
                    nodesIdArray[i++] = node.getId();
                nodesSize = nodesIdArray.length;

                for (int j = 0; j < Configurations.numberTraffic; j++) {
                    Traffic traffic = new Traffic();
                    traffic.setBandwidth(rn.nextInt
                            (Configurations.trafficBandwidthMax - Configurations.trafficBandwidthMin + 1) + Configurations.trafficBandwidthMin);
                    traffic.setPenaltyCostSLO(rn.nextInt
                            (Configurations.trafficPenaltySloMax - Configurations.trafficPenaltySloMin + 1) + Configurations.trafficPenaltySloMin);
                    traffic.setProcessed(false);

                    aux = false;
                    while (!aux) {
                        traffic.setNodeDestinyId(nodesIdArray[rn.nextInt(nodesSize)]);
                        traffic.setNodeOriginId(nodesIdArray[rn.nextInt(nodesSize)]);
                        if (!traffic.getNodeOriginId().equals(traffic.getNodeDestinyId()))
                            aux = true;
                    }

                    sfcSize = rn.nextInt(Configurations.trafficSfcMax - Configurations.trafficSfcMin + 1)
                            + Configurations.trafficSfcMin;

                    Vnf vnf;
                    SFC sfc = new SFC();
                    for (int k = 0; k < sfcSize; k++) {
                        vnf = new Vnf(vnfs.get(rn.nextInt(vnfs.size())));
                        sfc.getVnfs().add(vnf);
                    }
                    traffic.setSfc(sfc);
                    traffic.setDelayMaxSLA(getDelayMax(sfc, traffic.getNodeOriginId(), traffic.getNodeDestinyId()));

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

    public List<Traffic> generateAllToAlltraffic(Map<String, Node> nodesMap, List<Vnf> vnfs) throws Exception {
        Random rn = new Random();
        int sfcSize;
        try {
            if (Configurations.trafficsReadFile) {
                traffics = readTraffics();
            } else {

                for (Node nodeOrigin : nodesMap.values()) {
                    for(Node nodeDestiny : nodesMap.values()){
                        if(!nodeDestiny.getId().equalsIgnoreCase(nodeOrigin.getId())) {
                            Traffic traffic = new Traffic();
                            traffic.setNodeOriginId(nodeOrigin.getId());
                            traffic.setNodeDestinyId(nodeDestiny.getId());
                            traffic.setBandwidth(rn.nextInt
                                    (Configurations.trafficBandwidthMax - Configurations.trafficBandwidthMin + 1) + Configurations.trafficBandwidthMin);
                            traffic.setPenaltyCostSLO(rn.nextInt
                                    (Configurations.trafficPenaltySloMax - Configurations.trafficPenaltySloMin + 1) + Configurations.trafficPenaltySloMin);
                            traffic.setProcessed(false);

                            sfcSize = rn.nextInt(Configurations.trafficSfcMax - Configurations.trafficSfcMin + 1) + Configurations.trafficSfcMin;

                            Vnf vnf;
                            SFC sfc = new SFC();
                            for (int k = 0; k < sfcSize; k++) {
                                vnf = new Vnf(vnfs.get(rn.nextInt(vnfs.size())));
                                sfc.getVnfs().add(vnf);
                            }
                            traffic.setSfc(sfc);
                            traffic.setDelayMaxSLA(getDelayMax(sfc, traffic.getNodeOriginId(), traffic.getNodeDestinyId()));

                            traffics.add(traffic);
                        }
                    }
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
            fileOutputStream = new FileOutputStream(new File(System.getProperty("app.home") + Configurations.trafficsFileName));
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

    public static List<Traffic> readTraffics() throws Exception {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        Gson gson = new Gson();
        String trafficStringRead;
        try {
            fileInputStream = new FileInputStream(new File(System.getProperty("app.home") + Configurations.trafficsFileName));
            objectInputStream = new ObjectInputStream(fileInputStream);

            for (int i = 0; i < Configurations.numberTraffic; i++) {
                trafficStringRead = (String) objectInputStream.readObject();
                traffics.add(gson.fromJson(trafficStringRead, Traffic.class));
            }
            return traffics;
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

    public double getDelayMax(SFC sfc, String originId, String destinyId) throws Exception {
        try {
            List<GraphPath<Node, Link>> paths;
            double delayMin = 0;

            KShortestPaths<Node, Link> pathInspector =
                    new KShortestPaths<>(DataService.graph, 3, Integer.MAX_VALUE);

            paths = pathInspector.getPaths(DataService.nodesMap.get(originId), DataService.nodesMap.get(destinyId));

            for (Vnf vnf : sfc.getVnfs())
                delayMin = delayMin + DataService.vnfsShared.get(vnf.getId()).getDelay();

            if (paths != null && paths.size() > 0)
                for (Link link : paths.get(paths.size()-1).getEdgeList())
                    delayMin = delayMin + link.getDelay();

            return delayMin + (delayMin * (Configurations.trafficDelayMax / 100));
        }catch (Exception e){
            logger.error("Error al calcular el delay maximo: " + e.getMessage());
            throw new Exception();
        }
    }
}
