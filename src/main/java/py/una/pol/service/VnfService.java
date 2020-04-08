package py.una.pol.service;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.traverse.RandomWalkIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import py.una.pol.dto.*;
import py.una.pol.util.Configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Component
public class VnfService {
    Logger logger = Logger.getLogger(VnfService.class);

    @Autowired
    private DataService data;
    @Autowired
    private Configurations configuration;

    public boolean placement() {
        ResultRandomPath resultRandomPath;
        Traffic traffic;
        Integer energyCost, forwardingTrafficCost, hostSize, delayCost, deployCost, distance, hops, bandwidth, numberInstances,
                loadTraffic, resourcesCost, maxUseLink, sloCost, throughput, licencesCost, fragmentation, allLinksCost;
        try {
            data.loadData();
            Graph<Node, Link> graph = data.graph;

            traffic = generateRandomtraffic();
            resultRandomPath = createRandomPath(graph, traffic);
            List<Node> nodes = resultRandomPath.getRandomNodes();
            List<Link> links = resultRandomPath.getRandomLinks();

            energyCost = calculateEnergyCost(nodes, traffic.getNodeOrigin());
            forwardingTrafficCost = calculateForwardingTrafficCost(nodes, links, traffic.getNodeOrigin());
            hostSize = calculateHostSize(nodes);
            delayCost = calculateDelayTotal(nodes, links);
            deployCost = calculateDeployCost(nodes);
            distance = calculateDistance(links);
            hops = calculateHops(links);
            bandwidth = calculateBandwidth(links);
            numberInstances = calculateNumberIntances(nodes);
            loadTraffic = calculateLoadTraffic(nodes, links);
            resourcesCost = calculateResources(nodes);
            maxUseLink = calculateMaximunUseLink(links);
            sloCost = calculateSLOCost(nodes,links, traffic.getPenaltyCostSLO(), traffic.getDelayMaxSLA());
            throughput = calculateThroughput(links);
            licencesCost = calculateLicencesCost(nodes);
            fragmentation = calculateResourceFragmentation(nodes,links);
            allLinksCost = calculateAllLinkCost(links);

        } catch (Exception e) {
            logger.error("Error VNF placement:" + e.getMessage());
        }
        return true;
    }


    private ResultRandomPath createRandomPath(Graph<Node, Link> graph, Traffic traffic) throws Exception {
        ResultRandomPath resultRandomPath = new ResultRandomPath();
        List<Node> randomNodes = null;
        List<Link>  randomLinks = null;
        Node randomNode;
        Link link;
        boolean validPath = false;
        boolean validResources, reuseServer;
        int indexSfc;
        Random random = new Random();
        Integer bandwidtCurrent = traffic.getBandwidth();

        try {
            while (!validPath) {  // hasta encontrar una solucion factible
                Node nodeOrigen = traffic.getNodeOrigin();
                RandomWalkIterator randomWalkIterator = new RandomWalkIterator<>(graph, nodeOrigen);
                randomNodes = new ArrayList<>();
                randomLinks = new ArrayList<>();
                indexSfc = 0; // indice de vnfs del sfc
                while (randomWalkIterator.hasNext()) {  //Hasta completar una ruta random
                    randomNode = (Node) randomWalkIterator.next();    //siguiente nodo random para la ruta

                    if (randomNode.getId().equals(nodeOrigen.getId())) {
                        break;  //salir cuando se repite el ultimo nodo
                    }

                    link = data.graph.getEdge(nodeOrigen, randomNode);  //enlace entre los nodos
                    if (link.getBandwidth() > bandwidtCurrent)   //Se verifica si el enlace tiene capacidad para el ancho de banda
                        break;
                    else {
                        link.setBandwidthUsed(bandwidtCurrent);
                        randomLinks.add(link);
                    }

                    reuseServer = true;
                    validResources = true;
                    while (validResources && reuseServer && !validPath) {
                        Server server = randomNode.getServer();
                        if (server != null) {
                            Vnf vnf = traffic.getSfc().getVnfs().get(indexSfc);

                            Integer cpuToUse = server.getResourceUsedCPU() + vnf.getResourceCPU();
                            Integer ramToUse = server.getResourseUsedRAM() + vnf.getResourceRAM();
                            Integer storageToUse = server.getResourseUsedStorage() + vnf.getResourceStorage();
                            Integer energyToUse = server.getEnergyUsed() + vnf.getResourceCPU() * server.getEnergyPerCoreWatts();

                            //Verificar la capacidad de cada recurso del Servicdor
                            if (cpuToUse < server.getResourceCPU() && ramToUse < server.getResourceRAM() &&
                                    storageToUse < server.getResourceStorage() && energyToUse < server.getEnergyPeakWatts()) {

                                randomNode.getServer().setResourceUsedCPU(cpuToUse);
                                randomNode.getServer().setResourseUsedRAM(ramToUse);
                                randomNode.getServer().setResourseUsedStorage(storageToUse);
                                randomNode.getServer().setEnergyUsed(energyToUse);
                                randomNode.getServer().getVnf().add(vnf);

                                bandwidtCurrent = vnf.getBandwidthFactor() * bandwidtCurrent;
                                indexSfc++;
                            } else
                                validResources = false;   // cuando el servidor no cumple la capacidad de algun recurso

                            if (indexSfc == traffic.getSfc().getVnfs().size() - 1) { // se completaron los VNFs por lo tanto es valido el camino
                                validPath = true;
                            }
                            reuseServer = random.nextBoolean();  //random para reutilizar el Servidor
                        } else
                            reuseServer = false;  // No existe servidor por lo tanto no se puede utilizar el nodo

                    }
                    randomNodes.add(randomNode);
                    nodeOrigen = randomNode;
                }
                validPath = true;
            }
            resultRandomPath.setRandomLinks(randomLinks);
            resultRandomPath.setRandomNodes(randomNodes);
            return resultRandomPath;
        } catch (Exception e) {
            logger.error("Error al crear la ruta aleatoria para el traffico: " + traffic);
            throw new Exception();
        }
    }

    private Traffic generateRandomtraffic () throws Exception {
        try {
            Traffic traffic = new Traffic();
            traffic.setBandwidth(300);
            traffic.setDelayMaxSLA(3);
            traffic.setNodeDestiny(data.nodes.get(5));
            traffic.setNodeOrigin(data.nodes.get(0));
            traffic.setPenaltyCostSLO(4);

            SFC sfc = new SFC();
            sfc.getVnfs().add(data.vnfs.get(0));
            sfc.getVnfs().add(data.vnfs.get(2));
            sfc.getVnfs().add(data.vnfs.get(4));
            traffic.setSfc(sfc);

            return traffic;
        }catch (Exception e){
            logger.error("Error al crear el traffico Random:" + e.getMessage());
            throw new Exception();
        }

    }

    private Integer calculateEnergyCost(List<Node> nodes, Node originNode) throws Exception {
        int energyCost;
        try {
            energyCost =+ originNode.getEnergyCost();
            for (Node node : nodes){
                if(node.getServer()!=null){
                    energyCost =+ node.getServer().getEnergyCost() * node.getServer().getEnergyUsed();
                }else
                    energyCost =+ node.getEnergyCost();
            }
            return energyCost;
        }catch (Exception e){
            logger.error("Error al calcular la energia: " + e.getMessage());
            throw new Exception();
        }
    }


    private Integer calculateForwardingTrafficCost(List<Node> nodes, List<Link> links, Node originNode) throws Exception {
        int forwardingTrafficCost;
        try {
            forwardingTrafficCost =+ originNode.getEnergyCost();
            for (Link link: links){
                forwardingTrafficCost =+ link.getBandwidthUsed() * link.getBandwidthCost();
            }
            for(Node node: nodes){
                if (node.getServer()!= null) {
                    forwardingTrafficCost =+ node.getServer().getEnergyUsed() * node.getServer().getEnergyCost();
                }
                forwardingTrafficCost =+ node.getEnergyCost();
            }
            return forwardingTrafficCost;
        }catch (Exception e){
            logger.error("Error al calcular la energia minima: " + e.getMessage());
            throw new Exception();
        }
    }

    private Integer calculateHostSize(List<Node> nodes) throws Exception {
        int hostSize = 0;
        try {
            for (Node node : nodes){
                if(node.getServer()!=null){
                    hostSize =+ 1;
                }
            }
            return hostSize;
        }catch (Exception e){
            logger.error("Error al calcular la cantidad de hosts utilizados: " + e.getMessage());
            throw new Exception();
        }
    }

    private Integer calculateDelayTotal(List<Node> nodes, List<Link> links) throws Exception {
        int latency = 0;
        try {
            for (Node node : nodes){
                if(node.getServer()!=null){
                    for (Vnf vnf : node.getServer().getVnf())
                    latency =+ vnf.getDelay();
                }
            }
            for (Link link : links){
                latency =+ link.getDelay();
            }
            return latency;
        }catch (Exception e){
            logger.error("Error al calcular el Delay total: " + e.getMessage());
            throw new Exception();
        }
    }

    private Integer calculateDeployCost(List<Node> nodes) throws Exception {
        int deployHost = 0;
        try {
            for (Node node : nodes){
                if(node.getServer()!=null){
                    for (Vnf vnf : node.getServer().getVnf())
                        deployHost =+ vnf.getDeploy();
                    }
                }
            return deployHost;
        }catch (Exception e){
            logger.error("Error al calcular el costo de Intalacion o configuracion de los VNFs: " + e.getMessage());
            throw new Exception();
        }
    }

    //Confirmar si es la distancia total o por enlace?
    private Integer calculateDistance(List<Link> links) throws Exception {
        int distance = 0;
        try {
            for (Link link : links){
                distance =+ link.getDistance();
            }
            return distance;
        }catch (Exception e){
            logger.error("Error al calcular la distancia total: " + e.getMessage());
            throw new Exception();
        }
    }

    private Integer calculateHops(List<Link> links) throws Exception {
        try {
            return links.size();
        }catch (Exception e){
            logger.error("Error al calcular el numero de saltos: " + e.getMessage());
            throw new Exception();
        }
    }

    //se multiplica por el costo por unidad de ancho de banda?
    private Integer calculateBandwidth(List<Link> links) throws Exception {
        int bandwidth = 0;
        try {
            for (Link link : links){
                bandwidth =+ link.getBandwidth();
            }
            return bandwidth;
        }catch (Exception e){
            logger.error("Error al calcular el ancho de banda total: " + e.getMessage());
            throw new Exception();
        }
    }

    //analizar los flujos que pasan para reutilizar los VNFs
    private Integer calculateNumberIntances(List<Node> nodes) throws Exception {
        int instances = 0;
        try {
            for (Node node : nodes){
                if(node.getServer()!=null){
                    instances =+ node.getServer().getVnf().size();
                }
            }
            return instances;
        }catch (Exception e){
            logger.error("Error al calcular el numero de instacias: " + e.getMessage());
            throw new Exception();
        }
    }

    private Integer calculateLoadTraffic(List<Node> nodes, List<Link> links) throws Exception {
        int delayTotal;
        int bandwidth;
        try {
            delayTotal = calculateDelayTotal(nodes, links);
            bandwidth = calculateBandwidth(links);

            return bandwidth * delayTotal;
        }catch (Exception e){
            logger.error("Error al calcular el Trafico de Carga: " + e.getMessage());
            throw new Exception();
        }
    }

    private Integer calculateResources(List<Node> nodes) throws Exception {
        int resourceCPU = 0;
        int resourceRAM = 0;
        int resourceStorage = 0;
        try {
            for (Node node : nodes){
                if(node.getServer()!=null){
                    resourceCPU =+ node.getServer().getResourceUsedCPU();
                    resourceRAM =+ node.getServer().getResourseUsedRAM();
                    resourceStorage =+ node.getServer().getResourseUsedStorage();
                    }
                }

            return resourceCPU + resourceRAM + resourceStorage;
        }catch (Exception e){
            logger.error("Error al calcular el consumo total de Recursos: " + e.getMessage());
            throw new Exception();
        }
    }

    //Como calcular la maxima utlizacion del enlace?
    private Integer calculateMaximunUseLink(List<Link> links) throws Exception {
        int maximunUseLink = 0;
        try {
            for (Link link : links){

            }
            return maximunUseLink;
        }catch (Exception e){
            logger.error("Error al calcular la maxima utilizacion del enlace: " + e.getMessage());
            throw new Exception();
        }
    }

    private Integer calculateSLOCost(List<Node> nodes, List<Link> links, Integer sloCost, Integer delayMax) throws Exception {
        try {
            if(delayMax > calculateDelayTotal(nodes, links))
                return sloCost;
            else
                return 0;
        }catch (Exception e){
            logger.error("Error al calcular el costo de SLO: " + e.getMessage());
            throw new Exception();
        }
    }

    //Seria lo mismo que calcular el ancho de banda
    private Integer calculateThroughput(List<Link> links) throws Exception {
        int throughput = 0;
        try {
            for(Link link : links){

            }

            return throughput;
        }catch (Exception e){
            logger.error("Error al calcular el throughput: " + e.getMessage());
            throw new Exception();
        }
    }


    private Integer calculateLicencesCost(List<Node> nodes) throws Exception {
        int licencesCost = 0;
        try {
            for (Node node : nodes){
                if(node.getServer()!=null){
                    for (Vnf vnf : node.getServer().getVnf())
                        licencesCost =+ vnf.getLicenceCost();

                    licencesCost=+ node.getServer().getLicenceCost();
                }
            }
            return licencesCost;
        }catch (Exception e){
            logger.error("Error al calcular el costo de licencias total: " + e.getMessage());
            throw new Exception();
        }
    }

    private Integer calculateResourceFragmentation(List<Node> nodes, List<Link> links) throws Exception {
        int fragmentation = 0;
        try {
            for (Node node : nodes){
                if(node.getServer()!=null){
                    fragmentation =+ (node.getServer().getResourceCPU() - node.getServer().getResourceUsedCPU()) *
                            configuration.getServerPenaltyCostCPU();
                    fragmentation =+ (node.getServer().getResourceRAM() - node.getServer().getResourseUsedRAM()) *
                    configuration.getServerPenaltyCostRAM();
                    fragmentation =+ (node.getServer().getResourceStorage() - node.getServer().getResourseUsedStorage()) *
                            configuration.getServerPenaltyCostStorage();
                }
            }
            for (Link link : links){
                fragmentation =+ (link.getBandwidth() - link.getBandwidthUsed()) * configuration.getLinkPenaltyCostBandwidth();
            }
            return fragmentation;
        }catch (Exception e){
            logger.error("Error al calcular la fragmentacion de recursos: " + e.getMessage());
            throw new Exception();
        }
    }

    private Integer calculateAllLinkCost(List<Link> links) throws Exception {
        int linksCost = 0;
        try {
            for (Link link : links){
                linksCost =+ link.getBandwidth() * link.getBandwidthCost();
            }
            return linksCost;
        }catch (Exception e){
            logger.error("Error al calcular el costo de todos los enlaces: " + e.getMessage());
            throw new Exception();
        }
    }



}

