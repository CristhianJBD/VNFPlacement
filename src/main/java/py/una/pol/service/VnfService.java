package py.una.pol.service;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.traverse.RandomWalkIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import py.una.pol.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Service
public class VnfService {
    Logger logger = Logger.getLogger(VnfService.class);

    @Autowired
    private DataService data;
    @Autowired
    private ObjectiveFunctionService fo;

    public boolean placement() {
        ResultRandomPath resultRandomPath;
        Traffic traffic;
        int  hostSize, delayCost, deployCost, distance, hops, numberInstances,maxUseLink, throughput;
        double energyCost, forwardingTrafficCost, bandwidth, loadTraffic, resourcesCost,
                fragmentation, sloCost, allLinksCost, licencesCost;

        try {
            data.loadData();
            DirectedGraph<Node, Link> graph = data.graph;
            traffic = generateRandomtraffic(data.nodes, data.vnfs);

            resultRandomPath = createRandomPath(graph, traffic);
            List<Node> nodes = resultRandomPath.getRandomNodes();
            List<Link> links = resultRandomPath.getRandomLinks();

            logger.info(" ");

            energyCost = fo.calculateEnergyCost(nodes, traffic.getNodeOrigin());
            logger.info("Costo de Energia total: " + energyCost);

            forwardingTrafficCost = fo.calculateForwardingTrafficCost(nodes, links, traffic.getNodeOrigin());
            logger.info("Costo de Reenvio de Trafico: " + forwardingTrafficCost);

            hostSize = fo.calculateHostSize(nodes);
            logger.info("Cantidad de Hosts o Servidores utilizados: " + hostSize);

            delayCost = fo.calculateDelayTotal(nodes, links);
            logger.info("Costo de latencia/Retardo/Retraso total: " + delayCost);

            deployCost = fo.calculateDeployCost(nodes);
            logger.info("Costo de Instalacion o Configuracion total: " + deployCost);

            distance = fo.calculateDistance(links);
            logger.info("Distancia total: " + distance);

            hops = fo.calculateHops(links);
            logger.info("Numero de Saltos/Numero de enlaces de la ruta: " + hops);

            bandwidth = fo.calculateBandwidth(links);
            logger.info("Ancho de banda total: " + bandwidth);

            numberInstances = fo.calculateNumberIntances(nodes);
            logger.info("Numero de instancias: " + numberInstances);

            loadTraffic = fo.calculateLoadTraffic(nodes, links);
            logger.info("Trafico de Carga: " + loadTraffic);

            resourcesCost = fo.calculateResources(nodes);
            logger.info("Recursos total:" + resourcesCost);

            sloCost = fo.calculateSLOCost(nodes,links, traffic.getPenaltyCostSLO(), traffic.getDelayMaxSLA());
            logger.info("Costo por SLO: " + sloCost);

            licencesCost = fo.calculateLicencesCost(nodes);
            logger.info("Costo de licencia total: " + licencesCost);

            fragmentation = fo.calculateResourceFragmentation(nodes,links);
            logger.info("Costo de Fragmentacion: "+ fragmentation);

            allLinksCost = fo.calculateAllLinkCost(links);
            logger.info("Costo de todos los enlaces: " + allLinksCost);

            maxUseLink = fo.calculateMaximunUseLink(links);
            logger.info("Maxima uitlizacion del enlace: " + maxUseLink);

            throughput = fo.calculateThroughput(links);
            logger.info("Throughput: " + throughput);

        } catch (Exception e) {
            logger.error("Error VNF placement: " + e.getMessage());
        }
        return true;
    }


    private ResultRandomPath createRandomPath(Graph<Node, Link> graph, Traffic traffic) throws Exception {
        ResultRandomPath resultRandomPath = new ResultRandomPath();
        List<Node> randomNodes = null;
        List<Link> randomLinks = null;
        Node randomNode;
        Link linkToCopy,link;
        boolean validPath = false, validResources, reuseServer;
        Random random = new Random();
        double bandwidtCurrent;
        int indexSfc, cpuToUse, ramToUse, storageToUse, energyToUse;;
        try {
            while (!validPath) {  // hasta encontrar una solucion factible
                Node nodeOrigen = traffic.getNodeOrigin();
                RandomWalkIterator randomWalkIterator = new RandomWalkIterator<>(graph, nodeOrigen);
                bandwidtCurrent = traffic.getBandwidth();
                randomNodes = new ArrayList<>();
                randomLinks = new ArrayList<>();
                indexSfc = 0; // indice de vnfs del sfc
                while (randomWalkIterator.hasNext()) {  //Hasta completar una ruta random
                    randomNode = (Node) randomWalkIterator.next();    //siguiente nodo random para la ruta

                    if (randomNode.getId().equals(nodeOrigen.getId())) {
                        break;  //salir cuando se repite el ultimo nodo
                    }

                    linkToCopy = graph.getEdge(nodeOrigen, randomNode);  //enlace entre los nodos
                    if (linkToCopy.getBandwidth() < bandwidtCurrent) {  //Se verifica si el enlace tiene capacidad para el ancho de banda
                        validPath = false;
                        break;
                    } else {
                        link = setLink(linkToCopy);
                        link.setBandwidthUsed(link.getBandwidthUsed() + bandwidtCurrent); //Setea el ancho de banda al link utilizado
                        randomLinks.add(link);
                    }

                    reuseServer = true;
                    validResources = true;
                    Node node = setNode(randomNode);   // se setea el nodo aleatorio a otra instanacia de Nodo para que no remplace en el grafo
                    Server server = node.getServer();  // Servidor del nodo elegido de forma aleatoria
                    while (validResources && reuseServer && !validPath) {
                        if (server != null) {
                            Vnf vnf = traffic.getSfc().getVnfs().get(indexSfc);       //Vnf del SFC del trafico

                            //Se calculan los recursos que seran utilizados
                            cpuToUse = server.getResourceCPUUsed() + vnf.getResourceCPU();
                            ramToUse = server.getResourceRAMUsed() + vnf.getResourceRAM();
                            storageToUse = server.getResourceStorageUsed() + vnf.getResourceStorage();
                            energyToUse = server.getEnergyUsed() + vnf.getResourceCPU() * server.getEnergyPerCoreWatts();

                            //Verificar la capacidad de cada recurso del Servicdor
                            if (cpuToUse < server.getResourceCPU() && ramToUse < server.getResourceRAM() &&
                                    storageToUse < server.getResourceStorage() && energyToUse < server.getEnergyPeakWatts()) {

                                //setear los recursos utilizados
                                node.getServer().setResourceCPUUsed(cpuToUse);
                                node.getServer().setResourceRAMUsed(ramToUse);
                                node.getServer().setResourceStorageUsed(storageToUse);
                                node.getServer().setEnergyUsed(energyToUse);
                                node.getServer().getVnf().add(vnf);
                                bandwidtCurrent = vnf.getBandwidthFactor() * bandwidtCurrent;

                                indexSfc++;
                            } else
                                validResources = false;   // cuando el servidor no cumple la capacidad de algun recurso

                            if (indexSfc == traffic.getSfc().getVnfs().size()) { // se completaron los VNFs por lo tanto es valido el camino
                                validPath = true;
                            }
                            reuseServer = random.nextBoolean();  //random para reutilizar el Servidor
                        } else
                            reuseServer = false;  // No existe servidor por lo tanto no se puede utilizar el nodo

                    }
                    randomNodes.add(node);
                    nodeOrigen = randomNode;
                }
            }
            resultRandomPath.setRandomLinks(randomLinks);
            resultRandomPath.setRandomNodes(randomNodes);
            return resultRandomPath;
        } catch (Exception e) {
            logger.error("Error al crear la ruta aleatoria para el traffico: " + traffic);
            logger.error(e);
            throw new Exception();
        }
    }

    private Traffic generateRandomtraffic(List<Node> nodes, List<Vnf> vnfs) throws Exception {
        try {
            Traffic traffic = new Traffic();
            traffic.setBandwidth(300);
            traffic.setDelayMaxSLA(3);
            traffic.setNodeDestiny(nodes.get(5));
            traffic.setNodeOrigin(nodes.get(0));
            traffic.setPenaltyCostSLO(4);

            SFC sfc = new SFC();
            sfc.getVnfs().add(vnfs.get(0));
            sfc.getVnfs().add(vnfs.get(2));
            sfc.getVnfs().add(vnfs.get(4));
            traffic.setSfc(sfc);

            return traffic;
        } catch (Exception e) {
            logger.error("Error al crear el traffico Random:" + e.getMessage());
            throw new Exception();
        }
    }

    private Link setLink(Link linkToCopy) {
        Link link = new Link();

        link.setId(linkToCopy.getId());
        link.setDistance(linkToCopy.getDistance());
        link.setDelay(linkToCopy.getDelay());
        link.setBandwidthCost(linkToCopy.getBandwidthCost());
        link.setBandwidth(linkToCopy.getBandwidth());
        link.setBandwidthUsed(link.getBandwidthUsed());

        return link;
    }

    private Node setNode(Node nodeToCopy) {
        Server serverToCopy = nodeToCopy.getServer();
        Node node = new Node();

        if (serverToCopy != null) {
            Server server = new Server();
            server.setId(serverToCopy.getId());
            server.setEnergyPerCoreWatts(serverToCopy.getEnergyPerCoreWatts());
            server.setEnergyCost(serverToCopy.getEnergyCost());
            server.setEnergyPeakWatts(serverToCopy.getEnergyPeakWatts());
            server.setEnergyUsed(serverToCopy.getEnergyUsed());
            server.setResourceCPU(serverToCopy.getResourceCPU());
            server.setResourceRAM(serverToCopy.getResourceRAM());
            server.setResourceStorage(serverToCopy.getResourceStorage());
            server.setResourceCPUUsed(serverToCopy.getResourceCPUUsed());
            server.setResourceRAMUsed(serverToCopy.getResourceRAMUsed());
            server.setResourceStorageUsed(serverToCopy.getResourceStorageUsed());
            server.setLicenceCost(serverToCopy.getLicenceCost());
            server.setResourceCPUCost(serverToCopy.getResourceCPUCost());
            server.setResourceRAMCost(serverToCopy.getResourceRAMCost());
            server.setResourceStorageCost(serverToCopy.getResourceStorageCost());
            node.setServer(server);
        }

        node.setId(nodeToCopy.getId());
        node.setEnergyCost(nodeToCopy.getEnergyCost());
        return node;
    }

}

