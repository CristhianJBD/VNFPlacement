package py.una.pol.service;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.traverse.RandomWalkIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import py.una.pol.dto.NFVdto.*;
import py.una.pol.dto.ObjectiveFunctionsSolutions;
import py.una.pol.dto.ResultPath;
import py.una.pol.dto.ResultUseServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class RandomPathService {
    Logger logger = Logger.getLogger(RandomPathService.class);

    @Autowired
    private RandomPathService randomPathService;
    @Autowired
    private ObjectiveFunctionService fo;

    public void randomPathSolution(Graph<Node, Link> graph,
                                   Traffic traffic) throws Exception {
        int hostSize, delayCost, deployCost, distance, hops, numberInstances, throughput;
        double energyCost, forwardingTrafficCost, bandwidth, loadTraffic, resourcesCost,
                fragmentation, sloCost, allLinksCost, licencesCost, maxUseLink;

        ResultPath resultRandomPath = randomPathService.createRandomPath(graph, traffic);
        List<Node> nodes = resultRandomPath.getNodes();
        List<Link> links = resultRandomPath.getLinks();

        logger.info("Valores de la Solucion");
        logger.info("Trafico: ");
        logger.info(traffic);

        logger.info("Nodos de las Solucion: ");
        for (Node nodePrint : nodes)
            logger.info(nodePrint);

        logger.info("Enlaces de la Solucion: ");
        for (Link linkPrint : links)
            logger.info(linkPrint);

        logger.info("Funciones Objetivos con sus resultados: ");

        energyCost = fo.calculateEnergyCost(nodes);
        logger.info("Costo de Energia total: " + energyCost);

        forwardingTrafficCost = fo.calculateForwardingTrafficCost(nodes, links);
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

        sloCost = fo.calculateSLOCost(nodes, links, traffic.getPenaltyCostSLO(), traffic.getDelayMaxSLA());
        logger.info("Costo por SLO: " + sloCost);

        licencesCost = fo.calculateLicencesCost(nodes);
        logger.info("Costo de licencia total: " + licencesCost);

        fragmentation = fo.calculateResourceFragmentation(nodes, links);
        logger.info("Costo de Fragmentacion: " + fragmentation);

        allLinksCost = fo.calculateAllLinkCost(links);
        logger.info("Costo de todos los enlaces: " + allLinksCost);

        maxUseLink = fo.calculateMaximunUseLink(links);
        logger.info("Maxima uitlizacion del enlace: " + maxUseLink);

        throughput = fo.calculateThroughput(links);
        logger.info("Throughput: " + throughput);

    }

    public void randomPathSolutions(Graph<Node, Link> graph, List<Traffic> traffics) throws Exception {
        ObjectiveFunctionsSolutions solutions = new ObjectiveFunctionsSolutions();
        ResultPath resultRandomPath;

        logger.info("Soluciones: ");
        for (Traffic traffic : traffics) {
            resultRandomPath = createRandomPath(graph, traffic);
            List<Node> nodes = resultRandomPath.getNodes();
            List<Link> links = resultRandomPath.getLinks();

            solutions.getEnergyCostList().add(fo.calculateEnergyCost(nodes));
            solutions.getForwardingTrafficCostList().add(fo.calculateForwardingTrafficCost(nodes, links));
            solutions.getHostSizeList().add(fo.calculateHostSize(nodes));
            solutions.getDelayCostList().add(fo.calculateDelayTotal(nodes, links));
            solutions.getDeployCostList().add(fo.calculateDeployCost(nodes));
            solutions.getDistanceList().add(fo.calculateDistance(links));
            solutions.getHopsList().add(fo.calculateHops(links));
            solutions.getBandwidthList().add(fo.calculateBandwidth(links));
            solutions.getNumberInstancesList().add(fo.calculateNumberIntances(nodes));
            solutions.getLoadTrafficList().add(fo.calculateLoadTraffic(nodes, links));
            solutions.getResourcesCostList().add(fo.calculateResources(nodes));
            solutions.getSloCostList().add(fo.calculateSLOCost(nodes, links, traffic.getPenaltyCostSLO(), traffic.getDelayMaxSLA()));
            solutions.getLicencesCostList().add(fo.calculateLicencesCost(nodes));
            solutions.getFragmentationList().add(fo.calculateResourceFragmentation(nodes, links));
            solutions.getAllLinksCostList().add(fo.calculateAllLinkCost(links));
            solutions.getMaxUseLinkList().add(fo.calculateMaximunUseLink(links));
            solutions.getThroughputList().add(fo.calculateThroughput(links));
        }

        logger.info(solutions.toString());

    }

    private ResultPath createRandomPath(Graph<Node, Link> graph, Traffic traffic) throws Exception {
        ResultPath resultRandomPath = new ResultPath();
        Random random = new Random();
        RandomWalkIterator<Node, Link> randomWalkIterator;
        ResultUseServer resultUseServer;
        List<Node> randomNodes = null;
        List<Link> randomLinks = null;
        Node randomNode, nodeOrigen;
        Link linkToCopy, link;
        boolean validPath = false;
        double bandwidtCurrent;
        int indexSfc;
        try {
            while (!validPath) {    // hasta encontrar una solucion factible
                randomNodes = new ArrayList<>();
                randomLinks = new ArrayList<>();
                nodeOrigen = traffic.getNodeOrigin();
                bandwidtCurrent = traffic.getBandwidth();
                indexSfc = 0;       // indice de vnfs del sfc

                Node node = setNode(nodeOrigen);
                if (random.nextBoolean()) {    // random para utilizar o no el nodo
                    resultUseServer = useServer(nodeOrigen, bandwidtCurrent, traffic.getSfc().getVnfs(), indexSfc);

                    node = resultUseServer.getNode();
                    indexSfc = resultUseServer.getIndexSfc();
                    bandwidtCurrent = resultUseServer.getBandwidtCurrent();
                    validPath = resultUseServer.isValidPath();
                }
                randomNodes.add(node);

                randomWalkIterator = new RandomWalkIterator<>(graph, nodeOrigen);
                while (randomWalkIterator.hasNext()) {  //Hasta completar una ruta random
                    randomNode = randomWalkIterator.next();    //siguiente nodo random para la ruta

                    if (randomNode.getId().equals(nodeOrigen.getId()))
                        break;  //salir cuando se repite el ultimo nodo

                    linkToCopy = graph.getEdge(nodeOrigen, randomNode);  //enlace entre los nodos
                    if (linkToCopy.getBandwidth() < bandwidtCurrent) {  //Se verifica si el enlace tiene capacidad para el ancho de banda
                        validPath = false;
                        break;
                    } else {
                        link = setLink(linkToCopy);
                        link.setBandwidthUsed(link.getBandwidthUsed() + bandwidtCurrent); //Setea el ancho de banda al link utilizado
                        randomLinks.add(link);
                    }

                    node = setNode(randomNode);   // se setea el nodo aleatorio a otra instanacia de Nodo para que no remplace en el grafo
                    if (!validPath && random.nextBoolean()) {    // random para utilizar o no el nodo
                        resultUseServer = useServer(node, bandwidtCurrent, traffic.getSfc().getVnfs(), indexSfc);

                        node = resultUseServer.getNode();
                        indexSfc = resultUseServer.getIndexSfc();
                        bandwidtCurrent = resultUseServer.getBandwidtCurrent();
                        validPath = resultUseServer.isValidPath();
                    }

                    randomNodes.add(node);
                    nodeOrigen = randomNode;
                }
            }
            resultRandomPath.setLinks(randomLinks);
            resultRandomPath.setNodes(randomNodes);
            return resultRandomPath;
        } catch (Exception e) {
            logger.error("Error al crear la ruta aleatoria para el traffico: " + traffic);
            logger.error(e);
            throw new Exception();
        }
    }

    private ResultUseServer useServer(Node node, double bandwidtCurrent, List<Vnf> sfc, int indexSfc) {
        Random random = new Random();
        Server server = node.getServer();    // Servidor del nodo elegido de forma aleatoria
        boolean reuseServer = random.nextBoolean();             //random para utilizar o no el servidor
        boolean validResources = true, validPath = false;
        int cpuToUse, ramToUse, storageToUse, energyToUse;
        ResultUseServer resultUseServer = new ResultUseServer();

        while (validResources && reuseServer && !validPath) {
            if (server != null) {
                Vnf vnf = sfc.get(indexSfc);       //Vnf del SFC del trafico

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

                if (indexSfc == sfc.size()) { // se completaron los VNFs por lo tanto es valido el camino
                    validPath = true;
                }
                reuseServer = random.nextBoolean();  //random para reutilizar el Servidor
            } else
                reuseServer = false;  // No existe servidor por lo tanto no se puede utilizar el nodo

        }

        resultUseServer.setBandwidtCurrent(bandwidtCurrent);
        resultUseServer.setIndexSfc(indexSfc);
        resultUseServer.setNode(node);
        resultUseServer.setValidPath(validPath);
        return resultUseServer;

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
