package py.una.pol.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import py.una.pol.dto.NFVdto.*;
import py.una.pol.dto.Solution;
import py.una.pol.dto.Path;
import py.una.pol.dto.ResultPath;
import py.una.pol.util.Configurations;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ObjectiveFunctionService {
    Logger logger = Logger.getLogger(ObjectiveFunctionService.class);

    @Autowired
    private Configurations configuration;

    Solution solutions = new Solution();

    public Solution solutionFOs(Traffic traffic, ResultPath resultPath,
                                Map<String, Node> nodesMap, Map<String, Link> linksMap)
            throws Exception {
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        List<Node> nodes = new ArrayList<>();
        List<Link> links = new ArrayList<>();
        List<Server> serversSFC = new ArrayList<>();
        List<Server> servers;

        for (Path path : resultPath.getPaths()) {
            for (String nodeId : path.getShortestPath().getNodes())
                nodes.add(nodesMap.get(nodeId));
        }

        for (Path path : resultPath.getPaths()) {
            for (String linkId : path.getShortestPath().getLinks())
                links.add(linksMap.get(linkId));
        }

        for (String nodeId : resultPath.getServerVnf()) {
            serversSFC.add(nodesMap.get(nodeId).getServer());
        }
        servers = serversSFC.stream().distinct().collect(Collectors.toList());

        solutions.getEnergyCostList().add(decimalFormat.format(
                calculateEnergyCost(nodes, serversSFC, traffic.getSfc())));
        solutions.getForwardingTrafficCostList().add(decimalFormat.format(
                calculateForwardingTrafficCost(nodes, links)));
        solutions.getSloCostList().add(decimalFormat.format(
                calculateSLOCost(traffic.getSfc(), links, traffic.getPenaltyCostSLO(), traffic.getDelayMaxSLA())));
        solutions.getHostSizeList().add(calculateHostSize(servers));
        solutions.getDelayCostList().add(calculateDelayTotal(traffic.getSfc(), links));
        solutions.getDeployCostList().add(calculateDeployCost(serversSFC, traffic.getSfc()));
        solutions.getDistanceList().add(calculateDistance(links));
        solutions.getHopsList().add(calculateHops(links));
        solutions.getBandwidthList().add(decimalFormat.format(calculateBandwidth(links)));
        solutions.getNumberInstancesList().add(calculateNumberIntances(traffic.getSfc(), servers));
        solutions.getLoadTrafficList().add(decimalFormat.format(calculateLoadTraffic(traffic.getSfc(), links)));
        solutions.getResourcesCostList().add(decimalFormat.format(calculateResources(serversSFC, traffic.getSfc())));
        solutions.getLicencesCostList().add(decimalFormat.format(calculateLicencesCost(servers, traffic.getSfc())));
        solutions.getFragmentationList().add(decimalFormat.format(calculateResourceFragmentation(servers, links)));
        solutions.getAllLinksCostList().add(decimalFormat.format(calculateAllLinkCost(links)));
        solutions.getMaxUseLinkList().add(decimalFormat.format(calculateMaximunUseLink(links)));
        solutions.getThroughputList().add(decimalFormat.format(calculateThroughput(links)));

        return solutions;
    }

    /*
    Costo de la Energia en Dolares = suma de los costos(dolares) de energia utilizados en los nodos mas
    la energia en watts utilizada en cada servidor por el costo de energia correspondiente al servidor
     */
    public double calculateEnergyCost(List<Node> nodes, List<Server> servers, SFC sfc) throws Exception {
        double energyCost = 0;
        Server server;
        try {
            //Costo de energia consumida en el nodo mas energia consumida en el servidor donde se instalo el VNF
            for (int i = 0; i < sfc.getVnfs().size(); i++) {
                energyCost = energyCost +
                        (servers.get(i).getEnergyPerCoreWatts() * sfc.getVnfs().get(i).getResourceCPU());
            }

            //Costo de energia consumida en el nodo donde no hay Servidor se encuentra en la ruta
            for (Node node : nodes)
                energyCost = energyCost + node.getEnergyCost();

            return energyCost;
        } catch (Exception e) {
            logger.error("Error al calcular la energia: " + e.getMessage());
            throw new Exception();
        }
    }

    /*
        Costo de Reenvio de trafico = suma del costo en dolar por el ancho de banda utilizado en cada enlace
        mas la energia consumida en los nodos en dolares
     */
    public double calculateForwardingTrafficCost(List<Node> nodes, List<Link> links) throws Exception {
        double forwardingTrafficCost = 0;
        try {
            //Costo de energia consumida en los nodos en dolares
            for (Node node : nodes)
                if (node.getServer() != null)
                    forwardingTrafficCost = forwardingTrafficCost + node.getEnergyCost();

            //Ancho de banda de cada enlace por el costo en dolares por ancho de banda de cada enlace
            for (Link link : links)
                forwardingTrafficCost = forwardingTrafficCost + (link.getBandwidthUsed() * link.getBandwidthCost());

            return forwardingTrafficCost;
        } catch (Exception e) {
            logger.error("Error al calcular la energia minima: " + e.getMessage());
            throw new Exception();
        }
    }

    public int calculateHostSize(List<Server> servers) throws Exception {
        try {
            return servers.size();
        } catch (Exception e) {
            logger.error("Error al calcular la cantidad de hosts utilizados: " + e.getMessage());
            throw new Exception();
        }
    }

    public int calculateDelayTotal(SFC sfc, List<Link> links) throws Exception {
        int latency = 0;
        try {
            //Suma del delay de procesamiento de cada VNF instalado
            for (Vnf vnf : sfc.getVnfs())
                latency = latency + vnf.getDelay();

            //Suma del delay de cada enlace de la ruta
            for (Link link : links)
                latency = latency + link.getDelay();

            return latency;
        } catch (Exception e) {
            logger.error("Error al calcular el latencia/Retardo/Retraso total: " + e.getMessage());
            throw new Exception();
        }
    }

    public int calculateDeployCost(List<Server> servers, SFC sfc) throws Exception {
        int deployCost = 0;
        try {
            //Suma del costo de deployar los VNFs en los servidores
            for (int i = 0; i < sfc.getVnfs().size(); i++) {
                deployCost = deployCost +
                        (servers.get(i).getDeploy() + sfc.getVnfs().get(i).getDeploy());
            }
            return deployCost;
        } catch (Exception e) {
            logger.error("Error al calcular el costo de Intalacion o configuracion de los VNFs: " + e.getMessage());
            throw new Exception();
        }
    }

    public int calculateDistance(List<Link> links) throws Exception {
        int distance = 0;
        try {
            // suma de las distancias de los enlaces
            for (Link link : links)
                distance = distance + link.getDistance();

            return distance;
        } catch (Exception e) {
            logger.error("Error al calcular la distancia total: " + e.getMessage());
            throw new Exception();
        }
    }

    public int calculateHops(List<Link> links) throws Exception {
        try {
            return links.size();
        } catch (Exception e) {
            logger.error("Error al calcular el numero de saltos: " + e.getMessage());
            throw new Exception();
        }
    }

    public double calculateLicencesCost(List<Server> servers, SFC sfc) throws Exception {
        double licencesCost = 0;
        try {
            //Suma del costo de licencia de cada VNF
            for (Vnf vnf : sfc.getVnfs())
                licencesCost = licencesCost + vnf.getLicenceCost();

            //Suma del costo de licencia de cada servidor
            for (Server server : servers)
                licencesCost = server.getLicenceCost();

            return licencesCost;
        } catch (Exception e) {
            logger.error("Error al calcular el costo de licencia total: " + e.getMessage());
            throw new Exception();
        }
    }

    public double calculateSLOCost(SFC sfc, List<Link> links, double sloCost, Integer delayMax) throws Exception {
        try {
            //Costo por superar el maximo delay
            if (delayMax < calculateDelayTotal(sfc, links)) {
                return sloCost;
            } else {
                return 0;
            }
        } catch (Exception e) {
            logger.error("Error al calcular el costo de SLO: " + e.getMessage());
            throw new Exception();
        }
    }

    public double calculateResourceFragmentation(List<Server> servers, List<Link> links) throws Exception {
        double fragmentation = 0;
        try {
            //Costo de multa de cada recurso por el recurso que sobra de la capacidad total de cada Servidor
            for (Server server : servers) {
                fragmentation = fragmentation +
                        (server.getResourceCPU() - server.getResourceCPUUsed()) *
                                configuration.getServerPenaltyCPUCost();
                fragmentation = fragmentation +
                        (server.getResourceRAM() - server.getResourceRAMUsed()) *
                                configuration.getServerPenaltyRAMCost();
                fragmentation = fragmentation +
                        (server.getResourceStorage() - server.getResourceStorageUsed()) *
                                configuration.getServerPenaltyStorageCost();
            }

            //Costo de multa por el ancho banda que sobra de la capacidad total de cada enlace
            for (Link link : links)
                fragmentation = fragmentation +
                        (link.getBandwidth() - link.getBandwidthUsed()) * configuration.getLinkPenaltyBandwidthCost();

            return fragmentation;
        } catch (Exception e) {
            logger.error("Error al calcular la fragmentacion de recursos: " + e.getMessage());
            throw new Exception();
        }
    }

    public double calculateResources(List<Server> servers, SFC sfc) throws Exception {
        double resourceCPUCost = 0, resourceRAMCost = 0, resourceStorageCost = 0;
        double resourceTotalCost;
        try {
            //Suma de los recursos utilizados de cada servidor
            for (int i = 0; i < sfc.getVnfs().size(); i++) {
                    resourceCPUCost = resourceCPUCost +
                            (sfc.getVnfs().get(i).getResourceCPU() * servers.get(i).getResourceCPUCost());
                    resourceRAMCost = resourceRAMCost +
                            (sfc.getVnfs().get(i).getResourceRAM() * servers.get(i).getResourceRAMCost());
                    resourceStorageCost = resourceStorageCost +
                            (sfc.getVnfs().get(i).getResourceStorage() * servers.get(i).getResourceStorageCost());
                }

            resourceTotalCost = resourceCPUCost + resourceRAMCost + resourceStorageCost;
            return resourceTotalCost;
        } catch (Exception e) {
            logger.error("Error al calcular el consumo total de Recursos: " + e.getMessage());
            throw new Exception();
        }
    }

    public double calculateBandwidth(List<Link> links) throws Exception {
        double bandwidth = 0;
        try {
            //Suma del ancho de banda de cada enlace de la ruta
            for (Link link : links)
                bandwidth = bandwidth + link.getBandwidthUsed();

            return bandwidth;
        } catch (Exception e) {
            logger.error("Error al calcular el ancho de banda total: " + e.getMessage());
            throw new Exception();
        }
    }

    //Verificar formula?
    public double calculateMaximunUseLink(List<Link> links) throws Exception {
        double maximunUseLink;
        List<Double> bandwidths = new ArrayList<>();
        try {
            for (Link link : links)
                bandwidths.add(link.getBandwidthUsed());

            List<Double> sortedList = bandwidths.stream()
                    .sorted(Comparator.reverseOrder()).collect(Collectors.toList());

            //Máx ancho de banda de entre todos los enlaces.
            maximunUseLink = sortedList.get(0);
            return maximunUseLink;
        } catch (Exception e) {
            logger.error("Error al calcular la maxima utilizacion del enlace: " + e.getMessage());
            throw new Exception();
        }
    }

    //Verificar formula?
    public double calculateAllLinkCost(List<Link> links) throws Exception {
        double linksCost = 0;
        try {
            //suma del costo unitario * Ancho de banda requerido
            for (Link link : links)
                linksCost = linksCost + link.getBandwidthUsed() * link.getBandwidthCost();

            return linksCost;
        } catch (Exception e) {
            logger.error("Error al calcular el costo de todos los enlaces: " + e.getMessage());
            throw new Exception();
        }
    }

    //Verificar formula?
    public double calculateLoadTraffic(SFC sfc, List<Link> links) throws Exception {
        int delayTotal;
        double bandwidth = 0;
        double loadTraffic;
        try {
            //Delay total
            delayTotal = calculateDelayTotal(sfc, links);

            //Ancho de Banda total
            for (Link link : links)
                bandwidth = bandwidth + link.getBandwidthUsed();

            //anchi de banda total por delay total
            loadTraffic = bandwidth * delayTotal;
            return loadTraffic;
        } catch (Exception e) {
            logger.error("Error al calcular el Trafico de Carga: " + e.getMessage());
            throw new Exception();
        }
    }

    //No tiene sentido, No se reutilizan VNF entre los traficos
    public int calculateNumberIntances(SFC sfc, List<Server> servers) throws Exception {
        int instances = 0;
        try {
            return sfc.getVnfs().size();
        } catch (Exception e) {
            logger.error("Error al calcular el numero de instacias: " + e.getMessage());
            throw new Exception();
        }
    }

    //Formula?
    public double calculateThroughput(List<Link> links) throws Exception {
        double throughput = 0;
        try {
            //Suma del ancho de banda de cada enlace de la ruta
            for (Link link : links)
                throughput = throughput + link.getBandwidthUsed();

            return throughput;
        } catch (Exception e) {
            logger.error("Error al calcular el throughput: " + e.getMessage());
            throw new Exception();
        }
    }
}
