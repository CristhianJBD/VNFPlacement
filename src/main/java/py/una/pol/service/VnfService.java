package py.una.pol.service;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import py.una.pol.dto.*;
import py.una.pol.dto.NFVdto.*;
import py.una.pol.util.Configurations;

import java.util.*;


@Service
public class VnfService {
    Logger logger = Logger.getLogger(VnfService.class);

    @Autowired
    private DataService data;
    @Autowired
    private Configurations conf;
    @Autowired
    private TrafficService trafficService;
    @Autowired
    private ObjectiveFunctionService ofs;

    private Map<String, List<ShortestPath>> shortestPathMap;
    private DirectedGraph<String, KPath> graphMultiStage;
    private Map<String, Node> nodesMap;
    private Map<String, Link> linksMap;

    public boolean placement() {
        ResultPath resultPath;
        List<Traffic> traffics;
        try {
            data.loadData();
            shortestPathMap = data.shortestPathMap;
            nodesMap = data.nodesMap;
            linksMap = data.linksMap;

            traffics = trafficService.generateRandomtraffic(nodesMap, data.vnfs);
            int count = 1;
                for (Traffic traffic : traffics) {
                    graphMultiStage = createGraphtMultiStage(traffic);
                    if (graphMultiStage == null) {
                        logger.warn(count + "- No se pudo crear el Grafo Multi-Estados: " +
                                "NodoOrigen: " + traffic.getNodeOriginId() + ", NodoDestino: " + traffic.getNodeDestinyId());
                    } else {
                        resultPath = provisionTraffic(traffic);
                        if (resultPath == null) {
                            logger.warn(count + "- No se pudo encontrar una solucion: " +
                                    "NodoOrigen: " + traffic.getNodeOriginId() + ", NodoDestino: " + traffic.getNodeDestinyId());
                        } else {
                            traffic.setProcessed(true);
                            logger.info(count + "- Solucion: " +
                                    "NodoOrigen: " + traffic.getNodeOriginId() + ", NodoDestino: " + traffic.getNodeDestinyId());
                        }
                    }
                    count++;
                }
                ofs.solutionFOs(nodesMap,linksMap, traffics);
                logger.info(ofs.solutions);
        } catch (Exception e) {
            logger.error("Error VNF placement: " + e.getMessage());
        }
        return true;
    }

    private DirectedGraph<String, KPath> createGraphtMultiStage(Traffic traffic) throws Exception {
        List<Node> states = new ArrayList<>();
        DirectedGraph<String, KPath> gMStage = new DefaultDirectedGraph<>(KPath.class);
        List<ShortestPath> kShortestPath;
        KPath path;
        int numberStages = traffic.getSfc().getVnfs().size();
        Node nMSDestiny, nMSOrigin;
        Vnf vnf;
        try {
            //Se verifica si existe alguna ruta entre el origin y el destino del trafico
            if (shortestPathMap.get(traffic.getNodeOriginId() + "-" + traffic.getNodeDestinyId()) == null)
                return null;

            //Se guarda en el grafo multi estados el origen y el destino del trafico
            gMStage.addVertex(traffic.getNodeOriginId());
            gMStage.addVertex(traffic.getNodeDestinyId());

            //Se crea enlaces desde el origen a la primera etapa
            vnf = traffic.getSfc().getVnfs().get(0);
            for (Node node : nodesMap.values()) {
                if (node.getServer() != null) {
                    kShortestPath = shortestPathMap.get(traffic.getNodeOriginId()+ "-" + node.getId());

                    //Se guardan los nodos con servidor
                    states.add(node);
                    if (isResourceAvailableServer(node.getServer(), vnf)) {
                        //Se cambia la referencia del nodo guardando en otro objeto
                        nMSDestiny = changeId(node, 1);
                        if (kShortestPath != null && kShortestPath.size() > 0) {
                            path = new KPath(kShortestPath, traffic.getNodeOriginId() + "-" + nMSDestiny.getId());

                            //se guarda el nodo en el grafo multiestados con ID = numero de etapa y el id del nodo
                            gMStage.addVertex(nMSDestiny.getId());

                            //Se crea el enlace del grafo multi estados
                            // que seria el camino (conjunto de IDs de los nodos y enlaces del grafo principal)
                            // entre el par de nodos del grafo multi estados
                            gMStage.addEdge(traffic.getNodeOriginId(), nMSDestiny.getId(), path);

                            // Si el nodo origen es igual al nodo destino
                        } else if (traffic.getNodeOriginId().equals(node.getId())) {
                            kShortestPath = new ArrayList<>();
                            ShortestPath shortestPath = new ShortestPath();
                            shortestPath.getNodes().add(node.getId());
                            kShortestPath.add(shortestPath);
                            path = new KPath(kShortestPath, traffic.getNodeOriginId() + "-" + nMSDestiny.getId());
                            gMStage.addVertex(nMSDestiny.getId());
                            gMStage.addEdge(traffic.getNodeOriginId(), nMSDestiny.getId(), path);
                        }
                    }
                }
            }
            //Crear enlaces entre las etapas
            for (int i = 1; i < numberStages; i++) {
                vnf = traffic.getSfc().getVnfs().get(i);
                for (Node nodeOrigin : states) {
                    for (Node nodeDestiny : states) {
                        nMSOrigin = changeId(nodeOrigin, i);
                        nMSDestiny = changeId(nodeDestiny, i + 1);
                        if (isResourceAvailableServer(nodeDestiny.getServer(), vnf) &&
                                gMStage.containsVertex(nMSOrigin.getId())) {
                            kShortestPath = shortestPathMap.get(nodeOrigin.getId() + "-" + nodeDestiny.getId());

                            if (kShortestPath != null && kShortestPath.size() > 0) {
                                path = new KPath(kShortestPath, nMSOrigin.getId() + "-" + nMSDestiny.getId());
                                gMStage.addVertex(nMSDestiny.getId());
                                gMStage.addEdge(nMSOrigin.getId(), nMSDestiny.getId(), path);
                            }
                            else if (nodeOrigin.equals(nodeDestiny)) {
                                kShortestPath = new ArrayList<>();
                                ShortestPath shortestPath = new ShortestPath();
                                shortestPath.getNodes().add(nodeDestiny.getId());
                                kShortestPath.add(shortestPath);
                                path = new KPath(kShortestPath, nMSOrigin.getId() + "-" + nMSDestiny.getId());
                                gMStage.addVertex(nMSDestiny.getId());
                                gMStage.addEdge(nMSOrigin.getId(), nMSDestiny.getId(), path);
                            }
                        }
                    }
                }
            }
            //Crear enlaces entre la ultima etapa y el destino
            for (Node node : states) {
                nMSOrigin = changeId(node, numberStages);
                if(gMStage.containsVertex(nMSOrigin.getId())) {
                    kShortestPath = shortestPathMap.get(node.getId() + "-" + traffic.getNodeDestinyId());
                    if (kShortestPath != null && kShortestPath.size() > 0) {
                        path = new KPath(kShortestPath, nMSOrigin.getId() + "-" + traffic.getNodeDestinyId());
                        gMStage.addEdge(nMSOrigin.getId(), traffic.getNodeDestinyId(), path);
                    } else if (node.getId().equals(traffic.getNodeDestinyId())) {
                        kShortestPath = new ArrayList<>();
                        ShortestPath shortestPath = new ShortestPath();
                        shortestPath.getNodes().add(node.getId());
                        kShortestPath.add(shortestPath);
                        path = new KPath(kShortestPath, nMSOrigin.getId() + "-" + traffic.getNodeDestinyId());
                        gMStage.addEdge(changeId(node, numberStages).getId(), traffic.getNodeDestinyId(), path);
                    }
                }
            }

            DijkstraShortestPath<String, KPath> dijkstraShortestPath = new DijkstraShortestPath<>(gMStage);
            GraphPath<String, KPath> dijkstra = dijkstraShortestPath
                    .getPath(traffic.getNodeOriginId(), traffic.getNodeDestinyId());

            if (dijkstra == null)
                gMStage = null;

            return gMStage;
        } catch (Exception e) {
            logger.error("No se pudo crear el grafo multi-estados: " + e.getMessage());
            throw new Exception();
        }
    }

    private ResultPath provisionTraffic(Traffic traffic) throws Exception {
        ResultPath resultPath = new ResultPath();
        String randomNodeId, originNodeId;
        Random rn = new Random();
        Map<String, Node> nodesMapAux = null;
        Map<String, Link> linksMapAux = null;
        List<Path> pathNodeIds = new ArrayList<>();
        double bandwidtCurrent;
        boolean validPlacement = false;
        int retries = 0, indexVnf;
        List<ShortestPath> kShortestPath;
        ShortestPath shortestPath;
        List<String> serverVnf = new ArrayList<>();
        Vnf vnf;
        try {
            while (!validPlacement && retries <= conf.getRetriesSolution()) {
                originNodeId = traffic.getNodeOriginId();
                bandwidtCurrent = traffic.getBandwidth();
                nodesMapAux = loadNodesMapAux();
                linksMapAux = loadLinkMapAux();
                retries = retries + 1;
                indexVnf = 0;

                while (!validPlacement) {  //Hasta completar una ruta random
                    //De forma randomica se obtiene un nodo del grafo multi estados
                    Set<KPath> links = graphMultiStage.outgoingEdgesOf(originNodeId);
                    KPath kPath = (KPath) links.toArray()
                            [rn.nextInt(graphMultiStage.outDegreeOf(originNodeId))];
                    kShortestPath = kPath.getKShortestPath();
                    shortestPath = kShortestPath.get(rn.nextInt(kShortestPath.size()));
                    randomNodeId = graphMultiStage.getEdgeTarget(kPath);

                    //la ruta es valida si se llega hasta el nodo destino
                    if (traffic.getNodeDestinyId().equals(randomNodeId)) {
                        if (!isResourceAvailableLink(originNodeId, randomNodeId,
                                bandwidtCurrent, linksMapAux, nodesMapAux, shortestPath))
                            break;
                        else {
                            validPlacement = true;
                            pathNodeIds.add(resultPath(kPath.getId(), shortestPath));
                        }
                    } else {
                        vnf = traffic.getSfc().getVnfs().get(indexVnf);
                        if (!isResourceAvailableGraph(originNodeId, randomNodeId, bandwidtCurrent,
                                vnf, nodesMapAux, linksMapAux, shortestPath, serverVnf))
                            break;
                        else {
                            bandwidtCurrent = vnf.getBandwidthFactor() * bandwidtCurrent;
                            pathNodeIds.add(resultPath(kPath.getId(), shortestPath));
                            originNodeId = randomNodeId;
                            indexVnf = indexVnf + 1;
                        }
                    }
                }
            }
            if (validPlacement) {
                updateGraphMap(nodesMapAux, linksMapAux);
                resultPath.setPaths(pathNodeIds);
                resultPath.setServerVnf(serverVnf);
                return resultPath;
            } else
                return null;
        } catch (Exception e) {
            logger.error("Error en el provisionTraffic:" + e.getMessage());
            throw new Exception();
        }
    }

    private boolean isResourceAvailableGraph(String nodeOriginId, String nodeDestinyId,
                                             double bandwidtCurrent, Vnf vnf, Map<String, Node> nodesMapAux,
                                             Map<String, Link> linksMapAux, ShortestPath shortestPath,
                                             List<String> serverVnf) throws Exception {
        int cpuToUse, ramToUse, storageToUse, energyToUse;
        double bandwidtUsed;
        Link link;
        try {
            String nodeId = shortestPath.getNodes().get(shortestPath.getNodes().size() - 1);

            Node node = nodesMapAux.get(nodeId);
            Server server = node.getServer();
            if (server != null) {
                //Se calculan los recursos que seran utilizados
                cpuToUse = server.getResourceCPUUsed() + vnf.getResourceCPU();
                ramToUse = server.getResourceRAMUsed() + vnf.getResourceRAM();
                storageToUse = server.getResourceStorageUsed() + vnf.getResourceStorage();
                energyToUse = server.getEnergyUsed() + vnf.getResourceCPU() * server.getEnergyPerCoreWatts();

                if (cpuToUse > server.getResourceCPU() || ramToUse > server.getResourceRAM() ||
                        storageToUse > server.getResourceStorage() || energyToUse > server.getEnergyPeakWatts())
                    return false;
                else{
                    //setear los recursos utilizados
                    server.setResourceCPUUsed(cpuToUse);
                    server.setResourceRAMUsed(ramToUse);
                    server.setResourceStorageUsed(storageToUse);
                    server.setEnergyUsed(energyToUse);
                    server.getVnf().add(vnf);
                    serverVnf.add(node.getId());
                }
            } else
                return false;

            if (!nodeDestinyId.equals(nodeOriginId)) {
                for (String linkId : shortestPath.getLinks()) {
                    link = linksMapAux.get(linkId);
                    bandwidtUsed = link.getBandwidthUsed() + bandwidtCurrent;
                    if (link.getBandwidth() < bandwidtUsed)
                        return false;
                    else {
                        link.setBandwidthUsed(bandwidtUsed);
                        link.setTrafficAmount(link.getTrafficAmount() + 1);
                    }
                }
                if(shortestPath.getLinks().size() != 0) {
                    for (int i = 0; i < shortestPath.getNodes().size() - 1; i++) {
                        node = nodesMapAux.get(shortestPath.getNodes().get(i));
                        node.setTrafficAmount(node.getTrafficAmount() + 1);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Error en IsResourceAvailableGraph: " + e.getMessage());
            throw new Exception();
        }
    }

    private boolean isResourceAvailableLink(String nodeOriginId, String nodeDestinyId, double bandwidtCurrent,
                                            Map<String, Link> linksMapAux, Map<String, Node> nodesMapAux, ShortestPath shortestPath) throws Exception {
        Link link; Node node;
        double bandwidtUsed;
        try {
            if (!nodeDestinyId.equals(nodeOriginId)) {
                for (String linkId : shortestPath.getLinks()) {
                    link = linksMapAux.get(linkId);
                    bandwidtUsed = link.getBandwidthUsed() + bandwidtCurrent;
                    if (link.getBandwidth() < bandwidtUsed) {
                        return false;
                    } else {
                        link.setBandwidthUsed(bandwidtUsed);
                        link.setTrafficAmount(link.getTrafficAmount() + 1);
                    }
                }
                if(shortestPath.getLinks().size() != 0) {
                    for (String id : shortestPath.getNodes()) {
                        node = nodesMapAux.get(id);
                        node.setTrafficAmount(node.getTrafficAmount() + 1);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Error en IsResourceAvailableLink: " + e.getMessage());
            throw new Exception();
        }
    }


    private boolean isResourceAvailableServer(Server server, Vnf vnf) throws Exception {
        int cpuToUse, ramToUse, storageToUse, energyToUse;
        try {
            cpuToUse = server.getResourceCPUUsed() + vnf.getResourceCPU();
            ramToUse = server.getResourceRAMUsed() + vnf.getResourceRAM();
            storageToUse = server.getResourceStorageUsed() + vnf.getResourceStorage();
            energyToUse = server.getEnergyUsed() + vnf.getResourceCPU() * server.getEnergyPerCoreWatts();

            if (cpuToUse > server.getResourceCPU() || ramToUse > server.getResourceRAM() ||
                    storageToUse > server.getResourceStorage() || energyToUse > server.getEnergyPeakWatts())
                return false;
            else
                return true;
        } catch (Exception e) {
            logger.error("Error en IsResourceAvailableServer: " + e.getMessage());
            throw new Exception();
        }
    }


    private Node changeId(Node originalNode, int stage) {
        Node node = new Node();
        node.setId("s" + stage + originalNode.getId());
        node.setEnergyCost(originalNode.getEnergyCost());
        node.setServer(originalNode.getServer());
        return node;
    }

    private Map<String, Node> loadNodesMapAux() throws Exception {
        Map<String, Node> nodesMapAux = new HashMap<>();
        try {
            for (Node node : nodesMap.values()) {
                Node nodeAux = new Node();
                nodeAux.setId(node.getId());
                nodeAux.setEnergyCost(node.getEnergyCost());
                nodeAux.setTrafficAmount(node.getTrafficAmount());
                if (node.getServer() != null) {
                    Server server = new Server(node.getServer());
                    nodeAux.setServer(server);
                }
                nodesMapAux.put(nodeAux.getId(), nodeAux);
            }
            return nodesMapAux;
        } catch (Exception e) {
            logger.error("Error en loadNodesMapAux: " + e.getMessage());
            throw new Exception();
        }
    }


    private Map<String, Link> loadLinkMapAux() throws Exception {
        Map<String, Link> linksMapAux = new HashMap<>();
        try {
            for (Link link : linksMap.values()) {
                Link linkAux = new Link();
                linkAux.setId(link.getId());
                linkAux.setDelay(link.getDelay());
                linkAux.setDistance(link.getDistance());
                linkAux.setBandwidth(link.getBandwidth());
                linkAux.setBandwidthUsed(link.getBandwidthUsed());
                linkAux.setBandwidthCost(link.getBandwidthCost());
                linkAux.setTrafficAmount(link.getTrafficAmount());
                linksMapAux.put(linkAux.getId(), linkAux);
            }
            return linksMapAux;
        } catch (Exception e) {
            logger.error("Error en loadLinkMapAux: " + e.getMessage());
            throw new Exception();
        }
    }

    private void updateGraphMap(Map<String, Node> nodesMapAux, Map<String, Link> linksMapAux)
            throws Exception {
        try {
            nodesMap = new HashMap<>();
            linksMap = new HashMap<>();

            nodesMap.putAll(nodesMapAux);
            linksMap.putAll(linksMapAux);
        } catch (Exception e) {
            logger.error("Error en updateGraphMap: " + e.getMessage());
            throw new Exception();
        }
    }

    private Path resultPath(String pathId, ShortestPath shortestPath){
        Path path = new Path();
        path.setId(pathId);
        path.setShortestPath(shortestPath);
        return path;
    }
}

