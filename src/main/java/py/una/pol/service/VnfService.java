package py.una.pol.service;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.moeaframework.core.variable.Permutation;
import py.una.pol.dto.*;
import py.una.pol.dto.NFVdto.*;
import py.una.pol.util.Configurations;

import java.util.*;
import java.util.stream.Collectors;


public class VnfService {
    Logger logger = Logger.getLogger(VnfService.class);

    private Map<String, List<ShortestPath>> shortestPathMap;
    private DirectedGraph<String, KPath> graphMultiStage;
    private Map<String, Node> nodesMap;
    private Map<String, Link> linksMap;
    private Map<String, VnfShared> vnfSharedMap;

    public SolutionTraffic placement(List<Traffic> traffics, Permutation permutation) {
        ObjectiveFunctionService ofs = new ObjectiveFunctionService();
        SolutionTraffic solutionTraffic;
        try {
            nodesMap = loadNodesMapAux(DataService.nodesMap);
            linksMap = loadLinkMapAux(DataService.linksMap);

            traffics = loadTraffics(traffics);
            solution(traffics, permutation);
            solutionTraffic = ofs.solutionTrafficFOs(nodesMap, linksMap, traffics, DataService.vnfsShared);

            return solutionTraffic;
        } catch (Exception e) {
            logger.error("Error VNF placement: " + e.getMessage());
        }
        return null;
    }

    public ResultGraphMap placementGraph(List<Traffic> traffics, Permutation permutation) {
        ResultGraphMap resultGraphMap = new ResultGraphMap();
        try {
            nodesMap = loadNodesMapAux(DataService.nodesMap);
            linksMap = loadLinkMapAux(DataService.linksMap);

            traffics = loadTraffics(traffics);
            solution(traffics, permutation);

            resultGraphMap.setNodesMap(nodesMap);
            resultGraphMap.setLinksMap(linksMap);

            return resultGraphMap;
        } catch (Exception e) {
            logger.error("Error en placementGraph: " + e.getMessage());
        }
        return null;
    }

    public void solution(List<Traffic> traffics, Permutation permutation) {
        ResultPath resultPath;
        Traffic traffic;
        try {
            shortestPathMap = DataService.shortestPathMap;
            vnfSharedMap = DataService.vnfsShared;

            int count = 1;
            for (int i = 0; i < permutation.size(); i++) {
                traffic = traffics.get(permutation.get(i));
                traffic.setRejectLink(0);
                traffic.setRejectNode(0);
                graphMultiStage = createGraphtMultiStage(traffic);
                if (graphMultiStage == null) {
                    traffic.setRejectNode(1);
                    traffic.setProcessed(false);
                    traffic.setResultPath(null);
                    logger.warn(count + "- No Grafo Multi-Estados: " +
                            "origen: " + traffic.getNodeOriginId() + ", destino: " + traffic.getNodeDestinyId());
                } else {
                    resultPath = provisionTraffic(traffic);
                    traffic.setResultPath(resultPath);
                    if (resultPath == null) {
                        traffic.setProcessed(false);
                        logger.warn(count + "- No Solucion: " +
                                "origen: " + traffic.getNodeOriginId() + ", destino: " + traffic.getNodeDestinyId());
                    } else {
                        traffic.setProcessed(true);
                        logger.info(count + "- Solucion: " +
                                "origen: " + traffic.getNodeOriginId() + ", destino: " + traffic.getNodeDestinyId());
                    }
                }
                count++;
            }
        } catch (Exception e) {
            logger.error("Error VNF placement: " + e.getMessage());
        }
    }

    private DirectedGraph<String, KPath> createGraphtMultiStage(Traffic traffic) throws Exception {
        DirectedGraph<String, KPath> gMStage = new DefaultDirectedGraph<>(KPath.class);
        int numberStages = traffic.getSfc().getVnfs().size();
        List<Node> states = new ArrayList<>();
        List<ShortestPath> kShortestPath;
        String nMSDestinyId, nMSOriginId;
        KPath path;
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
                    kShortestPath = shortestPathMap.get(traffic.getNodeOriginId() + "-" + node.getId());

                    //Se guardan los nodos con servidor
                    states.add(node);
                    if (isResourceAvailableServer(node.getServer(), vnf)) {
                        //Se cambia la referencia del nodo guardando en otro objeto
                        nMSDestinyId = changeId(node.getId(), 1);
                        if (kShortestPath != null && kShortestPath.size() > 0) {
                            path = new KPath(kShortestPath, traffic.getNodeOriginId() + "-" + nMSDestinyId);

                            //se guarda el nodo en el grafo multiestados con ID = numero de etapa y el id del nodo
                            gMStage.addVertex(nMSDestinyId);

                            //Se crea el enlace del grafo multi estados
                            // que seria el camino (conjunto de IDs de los nodos y enlaces del grafo principal)
                            // entre el par de nodos del grafo multi estados
                            gMStage.addEdge(traffic.getNodeOriginId(), nMSDestinyId, path);

                            // Si el nodo origen es igual al nodo destino
                        } else if (traffic.getNodeOriginId().equals(node.getId())) {
                            kShortestPath = new ArrayList<>();
                            ShortestPath shortestPath = new ShortestPath();
                            shortestPath.getNodes().add(node.getId());
                            kShortestPath.add(shortestPath);
                            path = new KPath(kShortestPath, traffic.getNodeOriginId() + "-" + nMSDestinyId);
                            gMStage.addVertex(nMSDestinyId);
                            gMStage.addEdge(traffic.getNodeOriginId(), nMSDestinyId, path);
                        }
                    }
                }
            }
            //Crear enlaces entre las etapas
            for (int i = 1; i < numberStages; i++) {
                vnf = traffic.getSfc().getVnfs().get(i);
                for (Node nodeOrigin : states) {
                    for (Node nodeDestiny : states) {
                        nMSOriginId = changeId(nodeOrigin.getId(), i);
                        nMSDestinyId = changeId(nodeDestiny.getId(), i + 1);
                        if (isResourceAvailableServer(nodeDestiny.getServer(), vnf) &&
                                gMStage.containsVertex(nMSOriginId)) {
                            kShortestPath = shortestPathMap.get(nodeOrigin.getId() + "-" + nodeDestiny.getId());

                            if (kShortestPath != null && kShortestPath.size() > 0) {
                                path = new KPath(kShortestPath, nMSOriginId + "-" + nMSDestinyId);
                                gMStage.addVertex(nMSDestinyId);
                                gMStage.addEdge(nMSOriginId, nMSDestinyId, path);
                            } else if (nodeOrigin.equals(nodeDestiny)) {
                                kShortestPath = new ArrayList<>();
                                ShortestPath shortestPath = new ShortestPath();
                                shortestPath.getNodes().add(nodeDestiny.getId());
                                kShortestPath.add(shortestPath);
                                path = new KPath(kShortestPath, nMSOriginId + "-" + nMSDestinyId);
                                gMStage.addVertex(nMSDestinyId);
                                gMStage.addEdge(nMSOriginId, nMSDestinyId, path);
                            }
                        }
                    }
                }
            }
            //Crear enlaces entre la ultima etapa y el destino
            for (Node node : states) {
                nMSOriginId = changeId(node.getId(), numberStages);
                if (gMStage.containsVertex(nMSOriginId)) {
                    kShortestPath = shortestPathMap.get(node.getId() + "-" + traffic.getNodeDestinyId());
                    if (kShortestPath != null && kShortestPath.size() > 0) {
                        path = new KPath(kShortestPath, nMSOriginId + "-" + traffic.getNodeDestinyId());
                        gMStage.addEdge(nMSOriginId, traffic.getNodeDestinyId(), path);
                    } else if (node.getId().equals(traffic.getNodeDestinyId())) {
                        kShortestPath = new ArrayList<>();
                        ShortestPath shortestPath = new ShortestPath();
                        shortestPath.getNodes().add(node.getId());
                        kShortestPath.add(shortestPath);
                        path = new KPath(kShortestPath, nMSOriginId + "-" + traffic.getNodeDestinyId());
                        gMStage.addEdge(changeId(node.getId(), numberStages), traffic.getNodeDestinyId(), path);
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
        String originNodeId;
        List<Path> pathNodeIds = new ArrayList<>();
        List<String> serverVnf = new ArrayList<>();

        double bandwidtCurrent;
        boolean validPlacement;
        try {
            Map<String, Node> nodesMapAux = loadNodesMapAux(nodesMap);
            Map<String, Link> linksMapAux = loadLinkMapAux(linksMap);
            originNodeId = traffic.getNodeOriginId();
            bandwidtCurrent = traffic.getBandwidth();

            validPlacement = recursion(originNodeId, traffic, bandwidtCurrent, 0, pathNodeIds, serverVnf, nodesMapAux, linksMapAux);

            if (validPlacement) {
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

    private boolean recursion(String originNodeId, Traffic traffic, double bandwidtCurrent, int indexVnf,
                              List<Path> pathNodeIds, List<String> serverVnf,
                              Map<String, Node> nodesMap, Map<String, Link> linksMap) throws Exception {
        Map<String, Node> nodesMapAux = loadNodesMapAux(nodesMap);
        Map<String, Link> linksMapAux = loadLinkMapAux(linksMap);
        ShortestPath shortestPath;
        List<Cost> costs;
        String destinyNodeId;

        if (traffic.getSfc().getVnfs().size() == indexVnf) {
            Set<KPath> links = graphMultiStage.outgoingEdgesOf(originNodeId);
            destinyNodeId = graphMultiStage.getEdgeTarget(links.iterator().next());

            if (traffic.getNodeDestinyId().equals(destinyNodeId)) {
                for(ShortestPath shortestPathLast : links.iterator().next().getKShortestPath()) {
                    if (isResourceAvailableLink(originNodeId, destinyNodeId,
                            bandwidtCurrent, linksMapAux, nodesMapAux, shortestPathLast, traffic)) {
                        pathNodeIds.add(new Path(originNodeId, destinyNodeId, shortestPathLast));
                        updateGraphMap(nodesMapAux, linksMapAux);
                        return true;
                    } else
                        linksMapAux = loadLinkMapAux(linksMap);
                }
                return false;
            }
        }
        Vnf vnf = traffic.getSfc().getVnfs().get(indexVnf);
        //Normaliza los costos y guarda en un atributo
        costs = normalizeCosts(vnf, originNodeId, bandwidtCurrent, nodesMapAux, linksMapAux);

        //Se ordena de acuerdo al valor normalizado
        costs = costs.stream().sorted(Comparator.comparing(Cost::getCostNormalized)).collect(Collectors.toList());

        for (Cost destinyCosts : costs) {
            destinyNodeId = destinyCosts.getId();
            shortestPath = destinyCosts.getShortestPath();

            if (isResourceAvailableGraph(vnf, nodesMapAux, linksMapAux, shortestPath, bandwidtCurrent)) {
                bandwidtCurrent = vnfSharedMap.get(vnf.getId()).getBandwidthFactor() * bandwidtCurrent;
                pathNodeIds.add(new Path(originNodeId, destinyNodeId, shortestPath));
                serverVnf.add(destinyNodeId);
                originNodeId = destinyNodeId;

                if (recursion(originNodeId, traffic, bandwidtCurrent, indexVnf + 1, pathNodeIds, serverVnf, nodesMapAux, linksMapAux))
                    return true;
                else {
                    pathNodeIds.remove(indexVnf - 1);
                    serverVnf.remove(indexVnf - 1);
                }
            }
            nodesMapAux = loadNodesMapAux(nodesMap);
            linksMapAux = loadLinkMapAux(linksMap);
        }
        return false;
    }

    private VnfShared installVNF(Server server, Vnf vnf) throws Exception {
        int cpuToUse, ramToUse, storageToUse;
        VnfShared vnfToInstall;
        try {
            //Vnf a instalar en el servidor
            vnfToInstall = new VnfShared(vnfSharedMap.get(vnf.getId()));
            cpuToUse = server.getResourceCPUUsed() + vnfToInstall.getResourceCPU();
            ramToUse = server.getResourceRAMUsed() + vnfToInstall.getResourceRAM();
            storageToUse = server.getResourceStorageUsed() + vnfToInstall.getResourceStorage();

            if (cpuToUse <= server.getResourceCPU() && ramToUse <= server.getResourceRAM() &&
                    storageToUse <= server.getResourceStorage()) {
                server.setResourceCPUUsed(cpuToUse);
                server.setResourceRAMUsed(ramToUse);
                server.setResourceStorageUsed(storageToUse);
            } else
                return null;

            cpuToUse = vnfToInstall.getResourceCPUUsed() + vnf.getResourceCPU();
            ramToUse = vnfToInstall.getResourceRAMUsed() + vnf.getResourceRAM();
            if (cpuToUse <= vnfToInstall.getResourceCPU() && ramToUse <= vnfToInstall.getResourceRAM()) {
                vnfToInstall.setResourceRAMUsed(ramToUse);
                vnfToInstall.setResourceCPUUsed(cpuToUse);
                vnfToInstall.getVnfs().add(vnf);
                return vnfToInstall;
            } else
                return null;
        } catch (Exception e) {
            logger.error("Error al instalar VNF: " + e.getMessage());
            throw new Exception();
        }
    }

    private boolean isResourceAvailableLink(String nodeOriginId, String nodeDestinyId, double bandwidtCurrent,
                                            Map<String, Link> linksMapAux, Map<String, Node> nodesMapAux,
                                            ShortestPath shortestPath, Traffic traffic) throws Exception {
        Link link;Node node;
        double bandwidtUsed;
        try {
            if (!nodeDestinyId.equals(nodeOriginId)) {
                for (String linkId : shortestPath.getLinks()) {
                    link = linksMapAux.get(linkId);
                    bandwidtUsed = link.getBandwidthUsed() + bandwidtCurrent;
                    if (link.getBandwidth() < bandwidtUsed) {
                        traffic.setRejectLink(traffic.getRejectLink() + 1);
                        return false;
                    } else {
                        link.setBandwidthUsed(bandwidtUsed);
                        link.setTrafficAmount(link.getTrafficAmount() + 1);
                    }
                }
                if (shortestPath.getLinks().size() != 0) {
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
        int cpuToUse, ramToUse, storageToUse;
        VnfShared vnfToInstall;
        List<VnfShared> vnfsShared;
        try {
            vnfsShared = server.getVnfs().get(vnf.getId());
            if (vnfsShared != null) {
                for (VnfShared vnfShared : vnfsShared) {
                    cpuToUse = vnfShared.getResourceCPUUsed() + vnf.getResourceCPU();
                    ramToUse = vnfShared.getResourceRAMUsed() + vnf.getResourceRAM();

                    if (cpuToUse <= vnfShared.getResourceCPU() && ramToUse <= vnfShared.getResourceRAM())
                        return true;
                }
            }
            vnfToInstall = vnfSharedMap.get(vnf.getId());
            cpuToUse = server.getResourceCPUUsed() + vnfToInstall.getResourceCPU();
            ramToUse = server.getResourceRAMUsed() + vnfToInstall.getResourceRAM();
            storageToUse = server.getResourceStorageUsed() + vnfToInstall.getResourceStorage();

            return cpuToUse <= server.getResourceCPU() && ramToUse <= server.getResourceRAM() &&
                    storageToUse <= server.getResourceStorage();

        } catch (Exception e) {
            logger.error("Error en IsResourceAvailableServer: " + e.getMessage());
            throw new Exception();
        }
    }

    private String changeId(String originalNodeId, int stage) {
        return "s" + stage + originalNodeId;
    }

    private Map<String, Node> loadNodesMapAux(Map<String, Node> nodesMap) throws Exception {
        try {
            return nodesMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new Node(e.getValue())));

        } catch (Exception e) {
            logger.error("Error en loadNodesMapAux: " + e.getMessage());
            throw new Exception();
        }
    }

    private Map<String, Link> loadLinkMapAux(Map<String, Link> linksMap) throws Exception {
        try {
            return linksMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new Link(e.getValue())));

        } catch (Exception e) {
            logger.error("Error en loadLinkMapAux: " + e.getMessage());
            throw new Exception();
        }
    }

    private List<Traffic> loadTraffics(List<Traffic> traffics) throws Exception {
        List<Traffic> trafficsAux = new ArrayList<>();
        Traffic trafficAux;
        try{
            for(Traffic traffic : traffics){
                trafficAux = new Traffic(traffic);
                trafficsAux.add(trafficAux);
            }
            return trafficsAux;
        }catch (Exception e){
            logger.error("Error en loadTraffics: " + e.getMessage());
            throw new Exception();
        }
    }

    private void updateGraphMap(Map<String, Node> nodesMapAux, Map<String, Link> linksMapAux)
            throws Exception {
        try {
            nodesMap = new HashMap<>(nodesMapAux);
            linksMap = new HashMap<>(linksMapAux);
        } catch (Exception e) {
            logger.error("Error en updateGraphMap: " + e.getMessage());
            throw new Exception();
        }
    }

    // Calcula el costo de todas las funciones objetivos al instalar un VNF
    private Cost calculateCosts(Vnf vnf, ShortestPath shortestPath, double bandwidtCurrent, Map<String, Node> nodesMap, Map<String, Link> linksMap) throws Exception {
        ObjectiveFunctionService ofs = new ObjectiveFunctionService();

        Map<String, Node> nodesMapAux = loadNodesMapAux(nodesMap);
        Map<String, Link> linksMapAux = loadLinkMapAux(linksMap);

        boolean result = isResourceAvailableGraph(vnf, nodesMapAux, linksMapAux, shortestPath, bandwidtCurrent);

        if(result)
            return ofs.costTotalFOs(nodesMapAux, linksMapAux);
        else
            return null;
    }

    private boolean isResourceAvailableGraph(Vnf vnf, Map<String, Node> nodesMapAux, Map<String, Link> linksMapAux,
                                             ShortestPath shortestPath, double bandwidtCurrent) throws Exception {
        VnfShared vnfToInstall;
        int cpuToUse, ramToUse;
        List<VnfShared> vnfsShared;
        double bandwidtUsed;
        Link link;
        try {
            String nodeOriginId = shortestPath.getNodes().get(0);
            String nodeDestinyId = shortestPath.getNodes().get(shortestPath.getNodes().size() - 1);
            Node node = nodesMapAux.get(nodeDestinyId);
            Server server = node.getServer();
            if (server != null) {
                //se verifica si el vnf ya esta instalado para poder reutilizar
                vnfsShared = server.getVnfs().get(vnf.getId());
                if (vnfsShared == null) {
                    //Vnf a instalar en el servidor por primera vez
                    vnfToInstall = installVNF(server, vnf);
                    if (vnfToInstall != null) {
                        vnfsShared = new ArrayList<>();
                        vnfsShared.add(vnfToInstall);
                        server.getVnfs().put(vnf.getId(), vnfsShared);
                    } else {
                        return false;
                    }
                } else {
                    //Buscar VNF compartido para reutilizar
                    for (VnfShared vnfShared : vnfsShared) {
                        cpuToUse = vnfShared.getResourceCPUUsed() + vnf.getResourceCPU();
                        ramToUse = vnfShared.getResourceRAMUsed() + vnf.getResourceRAM();
                        if (cpuToUse <= vnfShared.getResourceCPU() && ramToUse <= vnfShared.getResourceRAM()) {
                            vnfShared.setResourceRAMUsed(ramToUse);
                            vnfShared.setResourceCPUUsed(cpuToUse);
                            vnfShared.getVnfs().add(vnf);
                            return true;
                        }
                    }

                    //Instalar un nuevo VNF porque no existe espacio
                    vnfToInstall = installVNF(server, vnf);
                    if (vnfToInstall != null) {
                        vnfsShared.add(vnfToInstall);
                        server.getVnfs().put(vnf.getId(), vnfsShared);
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
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
                if (shortestPath.getLinks().size() != 0) {
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

    private List<Cost> normalizeCosts(Vnf vnf,
                                      String originNodeId, double bandwidtCurrent, Map<String, Node> nodesMap, Map<String, Link> linksMap) throws Exception {
        Set<KPath> links = graphMultiStage.outgoingEdgesOf(originNodeId);
        String destinyNodeId;
        ShortestPath shortestPath;
        List<ShortestPath> kShortestPath;
        List<Cost> costs = new ArrayList<>();
        for (KPath kPath : links) {
            destinyNodeId = graphMultiStage.getEdgeTarget(kPath);
            kShortestPath = kPath.getKShortestPath();
            for (int k = 0; k < kShortestPath.size(); k++) {
                shortestPath = kShortestPath.get(k);
                Cost resultCost = calculateCosts(vnf, shortestPath, bandwidtCurrent, nodesMap, linksMap);
                if(resultCost != null) {
                    resultCost.setId(destinyNodeId);
                    resultCost.setShortestPath(shortestPath);
                    costs.add(resultCost);
                }
            }
        }

        if(costs.size() > 0) {
            double maxEnergy = costs.stream().mapToDouble(Cost::getEnergy)
                    .max().orElseThrow(NoSuchElementException::new);
            double minEnergy = costs.stream().mapToDouble(Cost::getEnergy)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxBandwidth = costs.stream().mapToDouble(Cost::getBandwidth)
                    .max().orElseThrow(NoSuchElementException::new);
            double minBandwidth = costs.stream().mapToDouble(Cost::getBandwidth)
                    .min().orElseThrow(NoSuchElementException::new);

            int maxDelay = costs.stream().mapToInt(Cost::getDelay)
                    .max().orElseThrow(NoSuchElementException::new);
            int minDelay = costs.stream().mapToInt(Cost::getDelay)
                    .min().orElseThrow(NoSuchElementException::new);

            int maxDistance = costs.stream().mapToInt(Cost::getDistance)
                    .max().orElseThrow(NoSuchElementException::new);
            int minDistance = costs.stream().mapToInt(Cost::getDistance)
                    .min().orElseThrow(NoSuchElementException::new);

            int maxInstances = costs.stream().mapToInt(Cost::getNumberInstances)
                    .max().orElseThrow(NoSuchElementException::new);
            int minInstances = costs.stream().mapToInt(Cost::getNumberInstances)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxResources = costs.stream().mapToDouble(Cost::getResources)
                    .max().orElseThrow(NoSuchElementException::new);
            double minResources = costs.stream().mapToDouble(Cost::getResources)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxLicences = costs.stream().mapToDouble(Cost::getLicences)
                    .max().orElseThrow(NoSuchElementException::new);
            double minLicences = costs.stream().mapToDouble(Cost::getLicences)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxFragmentation = costs.stream().mapToDouble(Cost::getFragmentation)
                    .max().orElseThrow(NoSuchElementException::new);
            double minFragmentation = costs.stream().mapToDouble(Cost::getFragmentation)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxMaximunUseLink = costs.stream().mapToDouble(Cost::getMaximunUseLink)
                    .max().orElseThrow(NoSuchElementException::new);
            double minMaximunUseLink = costs.stream().mapToDouble(Cost::getMaximunUseLink)
                    .min().orElseThrow(NoSuchElementException::new);

            for (Cost cost : costs) {
                double normalizedEnergy = normalize(cost.getEnergy(), maxEnergy, minEnergy);
                double normalizedBandwidth = normalize(cost.getBandwidth(), maxBandwidth, minBandwidth);
                double normalizedDelay = normalize(cost.getDelay(), maxDelay, minDelay);
                double normalizedDistance = normalize(cost.getDistance(), maxDistance, minDistance);
                double normalizedInstances = normalize(cost.getNumberInstances(), maxInstances, minInstances);
                double normalizedResources = normalize(cost.getResources(), maxResources, minResources);
                double normalizedLicences = normalize(cost.getLicences(), maxLicences, minLicences);
                double normalizedFragmentation = normalize(cost.getFragmentation(), maxFragmentation, minFragmentation);
                double normalizedMaximunUseLink = normalize(cost.getMaximunUseLink(), maxMaximunUseLink, minMaximunUseLink);

                double costNormalized = normalizedEnergy + normalizedBandwidth + normalizedDelay +
                        normalizedDistance + normalizedInstances + normalizedResources + normalizedLicences +
                        normalizedFragmentation + normalizedMaximunUseLink;

                cost.setCostNormalized(costNormalized / 9);
            }
        }

        return costs;
    }

    private double normalize(double x, double max, double min) {
        if (max == min)
            return 0;
        return (x - min) / (max - min);
    }

}

