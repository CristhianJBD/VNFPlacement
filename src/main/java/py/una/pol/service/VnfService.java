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

    private Map<String, List<ShortestPath>> shortestPathMap;
    private DirectedGraph<String, Path> graphMultiStage;
    private Map<String, Node> nodesMap;
    private Map<String, Link> linksMap;

    public boolean placement() {
        ResultPath resultPath;
        Traffic traffic;
        try {
            data.loadData();
            shortestPathMap = data.shortestPathMap;
            nodesMap = data.nodesMap;
            linksMap = data.linksMap;

            for (int i = 0; i < conf.getNumberSolutions(); i++) {
                traffic = trafficService.generateRandomtraffic(data.nodes, data.vnfs);
                graphMultiStage = createGraphtMultiStage(traffic);
                if (graphMultiStage == null) {
                    logger.error("No se pudo crear el Grafo Multi Estados");
                } else {
                    resultPath = provisionTraffic(traffic);
                    if (resultPath == null) {
                        logger.error("No se pudo encontrar una solucion");
                    } else {
                        logger.info("Solucion");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error VNF placement: " + e.getMessage());
        }
        return true;
    }

    private DirectedGraph<String, Path> createGraphtMultiStage(Traffic traffic) throws Exception {
        List<Node> states = new ArrayList<>();
        DirectedGraph<String, Path> gMStage = new DefaultDirectedGraph<>(Path.class);
        Random rn = new Random();
        List<ShortestPath> kShortestPath;
        Path path;
        int numberStages = traffic.getSfc().getVnfs().size();
        Node nMSDestiny, nMSOrigin;
        ShortestPath shortestPath = new ShortestPath();
        try {
            //Se verifica si existe alguna ruta entre el origin y el destino del trafico
            if (shortestPathMap.get(traffic.getNodeOrigin().getId() + "-" + traffic.getNodeDestiny().getId()) == null)
                return null;

            //Se guarda en el grafo multi estados el origen y el destino del trafico
            gMStage.addVertex(traffic.getNodeOrigin().getId());
            gMStage.addVertex(traffic.getNodeDestiny().getId());

            //Se crea enlaces desde el origen a la primera etapa
            for (Node node : nodesMap.values()) {
                //Ser verfifica si el nodo tiene servidor y si el origen tiene un camino al mismo
                if (node.getServer() != null && isResourceAvailableServer(node.getServer()) &&
                        (kShortestPath = shortestPathMap.
                                get(traffic.getNodeOrigin().getId() + "-" + node.getId())) != null) {
                    //Se guardan los nodos con servidor
                    states.add(node);

                    if (kShortestPath.size() > 0) {
                        //Se obtiene de forma randomica uno de los k caminos mas cortos
                        shortestPath = kShortestPath.get(rn.nextInt(kShortestPath.size()));

                        //Se cambia la referencia del nodo guardando en otro objeto
                        nMSDestiny = changeId(node, 1);
                        path = new Path(shortestPath, traffic.getNodeOrigin().getId() + "-" + nMSDestiny.getId());

                        //se guarda el nodo en el grafo multiestados con ID = numero de etapa y el id del nodo
                        gMStage.addVertex(nMSDestiny.getId());

                        //Se crea el enlace del grafo multi estados
                        // que seria el camino (conjunto de IDs de los nodos y enlaces del grafo principal)
                        // entre el par de nodos del grafo multi estados
                        gMStage.addEdge(traffic.getNodeOrigin().getId(), nMSDestiny.getId(), path);

                        // Si el nodo origen es igual al nodo destino
                    } else if (traffic.getNodeOrigin().getServer() != null && traffic.getNodeOrigin().equals(node)) {
                        nMSDestiny = changeId(node, 1);
                        shortestPath.getNodes().add(node.getId());
                        path = new Path(shortestPath, traffic.getNodeOrigin().getId() + "-" + nMSDestiny.getId());
                        gMStage.addVertex(nMSDestiny.getId());
                        gMStage.addEdge(traffic.getNodeOrigin().getId(), nMSDestiny.getId(), path);
                    }
                }
            }
            //Crear enlaces entre las etapas
            for (int i = 1; i < numberStages; i++) {
                for (Node nodeOrigin : states) {
                    for (Node nodeDestiny : states) {
                        kShortestPath = shortestPathMap.get(nodeOrigin.getId() + "-" + nodeDestiny.getId());
                        shortestPath = new ShortestPath();
                        if (kShortestPath != null && kShortestPath.size() > 0) {
                            shortestPath = kShortestPath.get(rn.nextInt(kShortestPath.size()));
                            nMSOrigin = changeId(nodeOrigin, i);
                            nMSDestiny = changeId(nodeDestiny, i + 1);
                            path = new Path(shortestPath, nMSOrigin.getId() + "-" + nMSDestiny.getId());
                            gMStage.addVertex(nMSDestiny.getId());
                            gMStage.addEdge(nMSOrigin.getId(), nMSDestiny.getId(), path);
                        } else if (nodeOrigin.equals(nodeDestiny)) {
                            nMSOrigin = changeId(nodeOrigin, i);
                            nMSDestiny = changeId(nodeDestiny, i + 1);
                            shortestPath.getNodes().add(nodeDestiny.getId());
                            path = new Path(shortestPath, nMSOrigin.getId() + "-" + nMSDestiny.getId());
                            gMStage.addVertex(nMSDestiny.getId());
                            gMStage.addEdge(nMSOrigin.getId(), nMSDestiny.getId(), path);
                        }
                    }
                }
            }
            //Crear enlaces entre la ultima etapa y el destino
            for (Node node : states) {
                kShortestPath = shortestPathMap.get(node.getId() + "-" + traffic.getNodeDestiny().getId());

                if (kShortestPath != null && kShortestPath.size() > 0) {
                    shortestPath = kShortestPath.get(rn.nextInt(kShortestPath.size()));
                    nMSOrigin = changeId(node, numberStages);
                    path = new Path(shortestPath, nMSOrigin.getId() + "-" + traffic.getNodeDestiny().getId());
                    gMStage.addEdge(nMSOrigin.getId(), traffic.getNodeDestiny().getId(), path);
                } else if (traffic.getNodeDestiny().getServer() != null && node.equals(traffic.getNodeDestiny())) {
                    nMSOrigin = changeId(node, numberStages);
                    shortestPath.getNodes().add(node.getId());
                    path = new Path(nMSOrigin.getId() + "-" + traffic.getNodeDestiny().getId());
                    gMStage.addEdge(changeId(node, numberStages).getId(), traffic.getNodeDestiny().getId(), path);
                }
            }

            DijkstraShortestPath<String, Path> dijkstraShortestPath = new DijkstraShortestPath<>(gMStage);
            GraphPath<String, Path> dijkstra = dijkstraShortestPath
                    .getPath(traffic.getNodeOrigin().getId(), traffic.getNodeDestiny().getId());

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
        Map<String, Node> nodesMapAux;
        Map<String, Link> linksMapAux;
        List<Path> pathNodeIds = new ArrayList<>();
        List<String> serverVnf = new ArrayList<>();
        double bandwidtCurrent;
        boolean validPlacement = false;
        int retries = 0, indexVnf = 0;
        Vnf vnf;
        try {
            nodesMapAux = loadNodesMapAux();
            linksMapAux = loadLinkMapAux();
            originNodeId = traffic.getNodeOrigin().getId();
            bandwidtCurrent = traffic.getBandwidth();

            while (!validPlacement && retries <= conf.getRetriesSolution()) {
                retries = retries + 1;

                while (!validPlacement) {  //Hasta completar una ruta random
                    //De forma randomica se obtiene un nodo del grafo multi estados
                    Set<Path> links = graphMultiStage.outgoingEdgesOf(originNodeId);
                    Path path = (Path) links.toArray()
                            [rn.nextInt(graphMultiStage.outDegreeOf(originNodeId))];
                    randomNodeId = graphMultiStage.getEdgeTarget(path);
                    //la ruta es valida si se llega hasta el nodo destino
                    if (traffic.getNodeDestiny().getId().equals(randomNodeId)) {
                        if (!isResourceAvailableLink(originNodeId, randomNodeId,
                                bandwidtCurrent, linksMapAux))
                            break;
                        else {
                            validPlacement = true;
                            pathNodeIds.add(path);
                        }
                    } else {
                        vnf = traffic.getSfc().getVnfs().get(indexVnf);
                        if (!isResourceAvailableGraph(originNodeId, randomNodeId, bandwidtCurrent,
                                vnf, nodesMapAux, linksMapAux, serverVnf))
                            break;
                        else {
                            bandwidtCurrent = vnf.getBandwidthFactor() * bandwidtCurrent;
                            pathNodeIds.add(path);
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
            }else
                return null;
        } catch (Exception e) {
            logger.error("Error en el provisionTraffic:" + e.getMessage());
            throw new Exception();
        }
    }

    private boolean isResourceAvailableGraph(String nodeOriginId, String nodeDestinyId,
                                             double bandwidtCurrent, Vnf vnf, Map<String, Node> nodesMapAux,
                                                     Map<String, Link> linksMapAux, List<String> serverVnf) throws Exception {
        int cpuToUse, ramToUse, storageToUse, energyToUse;
        Path path;
        Link link;
        try {
            path = graphMultiStage.getEdge(nodeOriginId, nodeDestinyId);
            String nodeId = path.getShortestPath().getNodes().
                    get(path.getShortestPath().getNodes().size() - 1);

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
            } else
                return false;

            if (!nodeDestinyId.equals(nodeOriginId)) {
                for (String linkId : path.getShortestPath().getLinks()) {
                    link = linksMapAux.get(linkId);
                    if (link.getBandwidth() < bandwidtCurrent) {
                        return false;
                    }
                }
                for (String linkId : path.getShortestPath().getLinks()) {
                    link = linksMapAux.get(linkId);
                    link.setBandwidthUsed(link.getBandwidthUsed() + bandwidtCurrent);
                }
            }

            //setear los recursos utilizados
            server.setResourceCPUUsed(cpuToUse);
            server.setResourceRAMUsed(ramToUse);
            server.setResourceStorageUsed(storageToUse);
            server.setEnergyUsed(energyToUse);
            server.getVnf().add(vnf);
            serverVnf.add(node.getId());

            return true;
        } catch (Exception e) {
            logger.error("Error en IsResourceAvailable: " + e.getMessage());
            throw new Exception();
        }
    }

    private boolean isResourceAvailableLink(String nodeOriginId, String nodeDestinyId,
                                            double bandwidtCurrent, Map<String, Link> linksMapAux) throws Exception {
        Path path;
        Link link;
        try {
            path = graphMultiStage.getEdge(nodeOriginId, nodeDestinyId);

            if (!nodeDestinyId.equals(nodeOriginId)) {
                for (String linkId : path.getShortestPath().getLinks()) {
                    link = linksMapAux.get(linkId);
                    if (link.getBandwidth() < bandwidtCurrent) {
                        return false;
                    }
                }
                for (String linkId : path.getShortestPath().getLinks()) {
                    link = linksMapAux.get(linkId);
                    link.setBandwidthUsed(link.getBandwidthUsed() + bandwidtCurrent);
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("Error en IsResourceAvailable: " + e.getMessage());
            throw new Exception();
        }
    }


    private boolean isResourceAvailableServer(Server server) throws Exception {
        try {
            double energy = ((double) server.getEnergyUsed() / server.getEnergyPeakWatts()) * 100;
            double cpu = ((double) server.getResourceCPUUsed() / server.getResourceCPU()) * 100;
            double ram = ((double) server.getResourceRAMUsed() / server.getResourceRAM()) * 100;
            double storage = ((double) server.getResourceStorageUsed() / server.getResourceStorage()) * 100;

            if (energy > conf.getPercentageUtilEnergy() || ram > conf.getPercentageUtilResource() ||
                    cpu > conf.getPercentageUtilResource() || storage > conf.getPercentageUtilResource())
                return false;
            else
                return true;
        } catch (Exception e) {
            logger.error("Error en IsResourceAvailable: " + e.getMessage());
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
}

