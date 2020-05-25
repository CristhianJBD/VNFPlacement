package py.una.pol.service;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.RandomWalkIterator;
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
    private Graph<Node, Link> graph;
    private List<Node> nodes;
    private List<Link> links;
    private List<Vnf> vnfs;
    private DirectedGraph<String, Path> graphMultiStage;
    private DataGraphMap dataGraphMap;

    public boolean placement() {
        List<String> pathNodeIds;
        try {
            data.loadData();
            shortestPathMap = data.shortestPathMap;
            graph = data.graph;
            nodes = data.nodes;
            links = data.links;
            vnfs = data.vnfs;

            Traffic traffic = trafficService.generateRandomtraffic(nodes, vnfs);
            loadDataGraphMap(nodes, links);
            graphMultiStage = createGraphtMultiStage(traffic);
            if(graphMultiStage == null){
                logger.error("No existe solucion");
            }else {
                pathNodeIds = provisionTraffic(traffic);
                if(pathNodeIds == null){
                    logger.error("No existe solucion");
                }else {
                    updatedGraph();
                    getFinalPath(pathNodeIds);
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
            for (Node node : nodes) {
                //Ser verfifica si el nodo tiene servidor y si el origen tiene un camino al mismo
                if (node.getServer() != null && isResourceAvailable(node.getServer()) &&
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
                        shortestPath.getNodes().add(nMSDestiny.getId());
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
                            shortestPath.getNodes().add(nMSDestiny.getId());
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
                    path = new Path(shortestPath, nMSOrigin.getId() + "-" + traffic.getNodeDestiny());
                    gMStage.addEdge(nMSOrigin.getId(), traffic.getNodeDestiny().getId(), path);
                } else if (traffic.getNodeDestiny().getServer() != null && node.equals(traffic.getNodeDestiny())) {
                    nMSOrigin = changeId(node, numberStages);
                    shortestPath.getNodes().add(nMSOrigin.getId());
                    path = new Path(nMSOrigin.getId() + "-" + traffic.getNodeDestiny().getId());
                    gMStage.addEdge(changeId(node, numberStages).getId(), traffic.getNodeDestiny().getId(), path);
                }
            }

            return gMStage;
        } catch (Exception e) {
            logger.error("No se pudo crear el grafo multi-estados: " + e.getMessage());
            throw new Exception();
        }
    }

    private List<String> provisionTraffic(Traffic traffic) throws Exception {
        RandomWalkIterator<String, Path> randomWalkIterator;
        String randomNodeId, originNodeId;
        List<String> pathNodeIds = null;
        double bandwidtCurrent;
        boolean validPlacement = false;
        int retries = 0, indexVnf = 0;
        Vnf vnf;
        try {
            originNodeId = traffic.getNodeOrigin().getId();
            bandwidtCurrent = traffic.getBandwidth();

            while (!validPlacement || retries <= conf.getRetriesSolution()) {
                pathNodeIds = new ArrayList<>();
                pathNodeIds.add(originNodeId);
                retries = retries + 1;
                //De forma randomica se obtiene un nodo del grafo multi estados
                randomWalkIterator = new RandomWalkIterator<>(graphMultiStage, traffic.getNodeOrigin().getId());
                while (randomWalkIterator.hasNext()) {  //Hasta completar una ruta random
                    randomNodeId = randomWalkIterator.next();
                    //la ruta es valida si se llega hasta el nodo destino
                    if(randomNodeId.equals(traffic.getNodeDestiny().getId())){
                        validPlacement = true;
                        pathNodeIds.add(randomNodeId);
                        break;
                    }
                    vnf = traffic.getSfc().getVnfs().get(indexVnf);
                    if (!isResourceAvailable(originNodeId, randomNodeId, bandwidtCurrent,
                            traffic.getSfc().getVnfs().get(indexVnf))) {
                        clearDataGraph();
                        break;
                    }else {
                        bandwidtCurrent = vnf.getBandwidthFactor() * bandwidtCurrent;
                        pathNodeIds.add(randomNodeId);
                    }
                    indexVnf = indexVnf + 1;
                    originNodeId = randomNodeId;
                }
            }
            return pathNodeIds;
        } catch (Exception e) {
            logger.error("Error en el provisionTraffic:" + e.getMessage());
            throw new Exception();
        }
    }

    /* Cargar los nodos y enlaces del grafo en un Map para obtener el objeto antes de ser actualizado
 y despues de ser modificados  */
    private void loadDataGraphMap(List<Node> nodes, List<Link> links) {
        Map<String, NodeGraph> nodeGraphMap = new HashMap<>();
        Map<String, LinkGraph> linkGraphMap = new HashMap<>();
        NodeGraph nodeGraph;
        LinkGraph linkGraph;
        for (Node node : nodes) {
            nodeGraph = new NodeGraph(node);
            nodeGraphMap.put(node.getId(), nodeGraph);
        }

        for (Link link : links) {
            linkGraph = new LinkGraph(link);
            linkGraphMap.put(link.getId(), linkGraph);
        }

        dataGraphMap.setNodesGraph(nodeGraphMap);
        dataGraphMap.setLinksGraph(linkGraphMap);
    }

    private void updatedGraph() throws Exception {
        try{
            for (Map.Entry<String, NodeGraph> entry : dataGraphMap.getNodesGraph().entrySet()) {
                if(entry.getValue().isUpdated())
                    replaceVertexGraph(entry.getValue().getNode(), entry.getValue().getNodeUpdated());
            }
            for (Map.Entry<String, LinkGraph> entry : dataGraphMap.getLinksGraph().entrySet()) {
                if(entry.getValue().isUpdated())
                    replaceLinkGraph(entry.getValue().getLink(), entry.getValue().getLinkUpdated());
            }

        }catch (Exception e){
            logger.error("Error al actualizar el grafo: "+ e.getMessage());
            throw new Exception();
        }
    }

    private void replaceLinkGraph(Link link, Link linkUpdated){
        Node nodeOrigin = graph.getEdgeSource(link);
        Node nodeDestiny = graph.getEdgeTarget(link);
        graph.removeEdge(link);
        graph.addEdge(nodeOrigin,nodeDestiny, linkUpdated);
    }

    private void replaceVertexGraph(Node node, Node nodeUpdated) {
        graph.addVertex(nodeUpdated);
        for (Link edge : graph.edgesOf(node))
            graph.addEdge(nodeUpdated, graph.getEdgeTarget(edge), edge);
        graph.removeVertex(node);
    }

    private void clearDataGraph(){
        for (Map.Entry<String, NodeGraph> entry : dataGraphMap.getNodesGraph().entrySet()) {
            if(entry.getValue().isUpdated())
                entry.setValue(new NodeGraph(entry.getValue().getNode()));
        }
        for (Map.Entry<String, LinkGraph> entry : dataGraphMap.getLinksGraph().entrySet()) {
            if(entry.getValue().isUpdated())
                entry.setValue(new LinkGraph(entry.getValue().getLink()));
        }
    }

    private ResultPath getFinalPath(List<String> nodesId){
        ResultPath resultPath = new ResultPath();
        List<Node> nodes = new ArrayList<>();
        List<Link> links = new ArrayList<>();
        Map<String, NodeGraph> nodesGraph = dataGraphMap.getNodesGraph();

        for(int i = 0; i<nodesId.size()-1; i++){
            nodes.add(nodesGraph.get(nodesId.get(i)).getNodeUpdated());
            links.add(graph.getEdge(nodesGraph.get(nodesId.get(i)).getNodeUpdated(),
                    nodesGraph.get(nodesId.get(i+1)).getNodeUpdated()));
        }
        resultPath.setNodes(nodes);
        resultPath.setLinks(links);

        loadDataGraphMap(nodes, links);
        return resultPath;
    }

    private boolean isResourceAvailable(String nodeOriginId, String nodeDestinyId,
                                        double bandwidtCurrent, Vnf vnf) throws Exception {
        int cpuToUse, ramToUse, storageToUse, energyToUse;
        Path randomPath;
        LinkGraph linkGraph;
        Link linkUpdated;
        try {
            randomPath = graphMultiStage.getEdge(nodeOriginId, nodeDestinyId);
            if (!nodeDestinyId.equals(nodeOriginId)) {
                for (String linkId : randomPath.getShortestPath().getLinks()) {
                    linkGraph = dataGraphMap.getLinksGraph().get(linkId);
                    linkUpdated = linkGraph.getLinkUpdated();
                    if (linkUpdated.getBandwidth() < bandwidtCurrent) {
                        return false;
                    } else {
                        linkUpdated.setBandwidthUsed(linkUpdated.getBandwidthUsed() + bandwidtCurrent);
                        linkGraph.setUpdated(true);
                    }
                }
            }
            String nodeId;
            if(nodeDestinyId.equals(nodeOriginId))
                nodeId = randomPath.getShortestPath().getNodes().get(0);
            else
                nodeId = randomPath.getShortestPath().getNodes().get(randomPath.getShortestPath().getNodes().size() - 1);

            NodeGraph nodeGraph = dataGraphMap.getNodesGraph().get(nodeId);
            Node nodeUpdated = nodeGraph.getNodeUpdated();
            Server server = nodeUpdated.getServer();
            if (server != null) {
                //Se calculan los recursos que seran utilizados
                cpuToUse = server.getResourceCPUUsed() + vnf.getResourceCPU();
                ramToUse = server.getResourceRAMUsed() + vnf.getResourceRAM();
                storageToUse = server.getResourceStorageUsed() + vnf.getResourceStorage();
                energyToUse = server.getEnergyUsed() + vnf.getResourceCPU() * server.getEnergyPerCoreWatts();

                if (cpuToUse < server.getResourceCPU() && ramToUse < server.getResourceRAM() &&
                        storageToUse < server.getResourceStorage() && energyToUse < server.getEnergyPeakWatts()) {

                    //setear los recursos utilizados
                    server.setResourceCPUUsed(cpuToUse);
                    server.setResourceRAMUsed(ramToUse);
                    server.setResourceStorageUsed(storageToUse);
                    server.setEnergyUsed(energyToUse);
                    server.getVnf().add(vnf);
                    nodeGraph.setUpdated(true);

                    return true;
                } else
                    return false;
            }else
                return false;
        } catch (Exception e) {
            logger.error("Error en IsResourceAvailable: " + e.getMessage());
            throw new Exception();
        }
    }

    private boolean isResourceAvailable(Server server) throws Exception {
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

    private boolean isResourceAvailable(Link link) throws Exception {
        try {
            double bandwidth = (link.getBandwidthUsed() / link.getBandwidth()) * 100;

            if (bandwidth > conf.getPercentageUtilBandwidth())
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
        node.setId(stage + "-" + originalNode.getId());
        node.setEnergyCost(originalNode.getEnergyCost());
        node.setServer(originalNode.getServer());
        return node;
    }

}

