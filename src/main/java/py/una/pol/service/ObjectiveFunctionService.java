package py.una.pol.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import py.una.pol.dto.NFVdto.*;
import py.una.pol.dto.Path;
import py.una.pol.dto.Solutions;
import py.una.pol.util.Configurations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
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

    private final Configurations conf;

    Solutions solutions = new Solutions();

    public ObjectiveFunctionService(Configurations configuration) {
        this.conf = configuration;
    }

    public void solutionFOs(Map<String, Node> nodesMap, Map<String, Link> linksMap,
                                 List<Traffic> traffics, Map<String, VnfShared> vnfsShared) throws Exception {

        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        List<Server> servers = new ArrayList<>();

        List<Node> nodes = new ArrayList<>(nodesMap.values());
        List<Link> links = new ArrayList<>(linksMap.values());

        for(Node node : nodes)
            if(node.getServer()!=null && node.getServer().getVnfs().size() > 0)
                servers.add(node.getServer());

        solutions.getEnergyCostList().add(decimalFormat.format(
                calculateEnergyCost(nodes)));
        solutions.getSloCostList().add(decimalFormat.format(
                calculateSLOCost(linksMap, traffics, vnfsShared)));
        solutions.getHostSizeList().add(calculateHostSize(servers));
        solutions.getDelayCostList().add(calculateDelayTotal(servers, links));
        solutions.getDeployCostList().add(calculateDeployCost(servers));
        solutions.getDistanceList().add(calculateDistance(links));
        solutions.getHopsList().add(calculateHops(links));
        solutions.getBandwidthList().add(decimalFormat.format(calculateBandwidth(links)));
        solutions.getNumberInstancesList().add(calculateNumberIntances(servers));
        solutions.getLoadTrafficList().add(decimalFormat.format(calculateLoadTraffic(linksMap,traffics,vnfsShared)));
        solutions.getResourcesCostList().add(decimalFormat.format(calculateResources(servers)));
        solutions.getLicencesCostList().add(decimalFormat.format(calculateLicencesCost(servers)));
        solutions.getFragmentationList().add(decimalFormat.format(calculateResourceFragmentation(servers, links)));
        solutions.getAllLinksCostList().add(decimalFormat.format(calculateAllLinkCost(links)));
        solutions.getMaxUseLinkList().add(decimalFormat.format(calculateMaximunUseLink(links)));
        solutions.getThroughputList().add(decimalFormat.format(calculateThroughput(traffics)));
        solutions.getRejectLink().add(decimalFormat.format(rejectLink(traffics)));
        solutions.getRejectNode().add(decimalFormat.format(rejectNode(traffics)));
        solutions.getAttendVnfs().add(decimalFormat.format(attendVnfs(traffics)));
    }

    /*  Formula = Paper 469
    Costo de la Energia en Dolares = suma de los costos(dolares) de energia utilizados en los nodos mas
    la energia en watts utilizada en cada servidor por el costo de energia correspondiente al servidor
     */
    private double calculateEnergyCost(List<Node> nodes) throws Exception {
        double energyCost = 0;
        Server server;
        try {

            for (Node node : nodes){
                //Costo monetario de energia de los nodos que se encuentran
                // en la ruta por la cantidad de flujos que pasan por el nodo
                energyCost = energyCost + node.getEnergyCost() * node.getTrafficAmount();

                //Costo de energia consumida en los servidores donde se instalaron los VNFs
                server = node.getServer();
                if (server != null && server.getVnfs().size() > 0) {
                    double proportionCpu = (double) server.getResourceCPUUsed() / server.getResourceCPU();
                    energyCost = energyCost +
                            (server.getEnergyIdleWatts()
                                    + (server.getEnergyPeakWatts() - server.getEnergyIdleWatts()) * proportionCpu)
                                    * node.getEnergyCost();
                }
            }

            return energyCost;
        } catch (Exception e) {
            logger.error("Error al calcular la energia: " + e.getMessage());
            throw new Exception();
        }
    }

    /*
        Suma del costo de todos los enlaces, costo por unidad de Mbit por ancho de banda
     */
    private double calculateAllLinkCost(List<Link> links) throws Exception {
        double linksCost = 0;
        try {
            //suma del costo unitario * Ancho de banda utilizado
            for (Link link : links)
                linksCost = linksCost + (link.getBandwidthUsed() * link.getBandwidthCost());

            return linksCost;
        } catch (Exception e) {
            logger.error("Error al calcular el costo de todos los enlaces: " + e.getMessage());
            throw new Exception();
        }
    }

    private int calculateHostSize(List<Server> servers) throws Exception {
        try {
            return servers.size();
        } catch (Exception e) {
            logger.error("Error al calcular la cantidad de hosts utilizados: " + e.getMessage());
            throw new Exception();
        }
    }

    private int calculateDelayTotal(List<Server> servers, List<Link> links) throws Exception {
        int latency = 0;
        try {
            //Suma del delay de procesamiento de cada VNF instalado y compartido
            for (Server server : servers)
                for(List<VnfShared> vnfsShared : server.getVnfs().values())
                    for(VnfShared vnfShared : vnfsShared)
                        latency = latency + vnfShared.getDelay() * vnfShared.getVnfs().size();

            //Suma del delay de cada enlace de la ruta
            for (Link link : links)
                latency = latency + (link.getDelay() * link.getTrafficAmount());

            return latency;
        } catch (Exception e) {
            logger.error("Error al calcular el latencia/Retardo/Retraso total: " + e.getMessage());
            throw new Exception();
        }
    }

    private int calculateDeployCost(List<Server> servers) throws Exception {
        int deployCost = 0;
        try {
            //Suma del costo de deployar los VNFs en los servidores
            for (Server server : servers)
                for(List<VnfShared> vnfsShared : server.getVnfs().values())
                    for(VnfShared vnfShared : vnfsShared)
                        deployCost = deployCost + vnfShared.getDeploy() + server.getDeploy();

            return deployCost;
        } catch (Exception e) {
            logger.error("Error al calcular el costo de Intalacion o configuracion de los VNFs: " + e.getMessage());
            throw new Exception();
        }
    }

    private int calculateDistance(List<Link> links) throws Exception {
        int distance = 0;
        try {
            // suma de las distancias de los enlaces
            for (Link link : links)
                distance = distance + link.getDistance() * link.getTrafficAmount();

            return distance;
        } catch (Exception e) {
            logger.error("Error al calcular la distancia total: " + e.getMessage());
            throw new Exception();
        }
    }

    private int calculateHops(List<Link> links) throws Exception {
        int hops = 0;
        try {
            // suma de los saltos
            for (Link link : links)
                hops = hops + link.getTrafficAmount();

            return hops;
        } catch (Exception e) {
            logger.error("Error al calcular el numero de saltos: " + e.getMessage());
            throw new Exception();
        }
    }

    private double calculateLicencesCost(List<Server> servers) throws Exception {
        double licencesCost = 0;
        try {
            for (Server server : servers) {
                //Suma del costo de licencia de cada servidor
                licencesCost = server.getLicenceCost();
                for (List<VnfShared> vnfsShared : server.getVnfs().values())
                    //Suma del costo de licencia de cada VNF
                    for(VnfShared vnfShared : vnfsShared)
                        licencesCost = licencesCost + vnfShared.getLicenceCost();
            }
            return licencesCost;
        } catch (Exception e) {
            logger.error("Error al calcular el costo de licencia total: " + e.getMessage());
            throw new Exception();
        }
    }

    private double calculateSLOCost(Map<String, Link> linksMap, List<Traffic> traffics,
                                   Map<String, VnfShared> vnfsShared) throws Exception {
        double sloCost = 0;
        int delayTotal;
        try {

            //Costo por superar el maximo delay
            for(Traffic traffic : traffics){
                delayTotal = 0;
                if(traffic.isProcessed()) {
                    for (Path path : traffic.getResultPath().getPaths())
                        for (String linkId : path.getShortestPath().getLinks())
                            delayTotal = delayTotal + linksMap.get(linkId).getDelay();

                    for (Vnf vnf : traffic.getSfc().getVnfs())
                        delayTotal = delayTotal + vnfsShared.get(vnf.getId()).getDelay();

                    if (traffic.getDelayMaxSLA() < delayTotal)
                        sloCost = sloCost + traffic.getPenaltyCostSLO();
                }
            }

            return sloCost;
        } catch (Exception e) {
            logger.error("Error al calcular el costo de SLO: " + e.getMessage());
            throw new Exception();
        }
    }

    private double calculateResourceFragmentation(List<Server> servers, List<Link> links) throws Exception {
        double fragmentation = 0;
        try {
            //Costo de multa de cada recurso por el recurso que sobra de la capacidad total de cada Servidor
            for (Server server : servers) {
                fragmentation = fragmentation +
                        (server.getResourceCPU() - server.getResourceCPUUsed()) *
                                conf.getServerPenaltyCPUCost();
                fragmentation = fragmentation +
                        (server.getResourceRAM() - server.getResourceRAMUsed()) *
                                conf.getServerPenaltyRAMCost();
                fragmentation = fragmentation +
                        (server.getResourceStorage() - server.getResourceStorageUsed()) *
                                conf.getServerPenaltyStorageCost();
            }

            //Costo de multa por el ancho banda que sobra de la capacidad total de cada enlace
            for (Link link : links)
                fragmentation = fragmentation +
                        (link.getBandwidth() - link.getBandwidthUsed()) * conf.getLinkPenaltyBandwidthCost();

            return fragmentation;
        } catch (Exception e) {
            logger.error("Error al calcular la fragmentacion de recursos: " + e.getMessage());
            throw new Exception();
        }
    }

    private double calculateResources(List<Server> servers) throws Exception {
        double resourceCPUCost = 0, resourceRAMCost = 0, resourceStorageCost = 0;
        double resourceTotalCost;
        try {
            //Suma de los recursos utilizados de cada servidor
            for (Server server : servers) {
                    resourceCPUCost = resourceCPUCost + (server.getResourceCPUUsed() * server.getResourceCPUCost());
                    resourceRAMCost = resourceRAMCost + (server.getResourceRAMUsed() * server.getResourceRAMCost());
                    resourceStorageCost = resourceStorageCost + (server.getResourceStorageUsed() * server.getResourceStorageCost());
                }

            resourceTotalCost = resourceCPUCost + resourceRAMCost + resourceStorageCost;
            return resourceTotalCost;
        } catch (Exception e) {
            logger.error("Error al calcular el consumo total de Recursos: " + e.getMessage());
            throw new Exception();
        }
    }

    private double calculateBandwidth(List<Link> links) throws Exception {
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

    private double calculateMaximunUseLink(List<Link> links) throws Exception {
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

    private double calculateLoadTraffic(Map<String, Link> linksMap, List<Traffic> traffics, Map<String, VnfShared> vnfsShared) throws Exception {
        double loadTraffic = 0;
        int delay;
        double bandwidth;
        try {
            //formula del paper 197
            for(Traffic traffic: traffics)
                if(traffic.isProcessed()) {
                    List<Vnf> sfc = traffic.getSfc().getVnfs();
                    bandwidth = traffic.getBandwidth();
                    for (int i = 0; i < sfc.size(); i++) {
                        delay=0;
                        for(String linkId: traffic.getResultPath().getPaths().get(i + 1).getShortestPath().getLinks())
                            delay = delay + linksMap.get(linkId).getDelay();

                        bandwidth = bandwidth * vnfsShared.get(sfc.get(i).getId()).getBandwidthFactor();

                        loadTraffic = loadTraffic + (bandwidth * delay);
                    }
                }

            return loadTraffic;
        } catch (Exception e) {
            logger.error("Error al calcular el Trafico de Carga: " + e.getMessage());
            throw new Exception();
        }
    }

    //Depende de la implementacion (Reutilizar VNF entre varios flujos)
    private int calculateNumberIntances(List<Server> servers) throws Exception {
        int instances = 0;
        try {
            for (Server server : servers)
                instances = instances + server.getVnfs().size();

            return instances;
        } catch (Exception e) {
            logger.error("Error al calcular el numero de instacias: " + e.getMessage());
            throw new Exception();
        }
    }

    //Formula (Calculo de ancho de banda inicial antendidos sobre el total)
    private double calculateThroughput(List<Traffic> traffics) throws Exception {
        double successful = 0;
        double total = 0;
        try {
            //Suma del ancho de banda de cada enlace de la ruta
            for (Traffic traffic : traffics) {
                total = total + traffic.getBandwidth();
                if (traffic.isProcessed())
                    successful = successful + traffic.getBandwidth();
            }
            return -((successful / total) * 100);
        } catch (Exception e) {
            logger.error("Error al calcular el throughput: " + e.getMessage());
            throw new Exception();
        }
    }

    //Porcentaje de rechazos por sobrecarga de enlaces
    private double rejectLink(List<Traffic> traffics) throws Exception {
        double reject = 0;
        double total = 0;
        try {

            for (Traffic traffic : traffics) {
                total = total + 1;
                if (!traffic.isProcessed() && traffic.getRejectLink()>traffic.getRejectNode())
                    reject = reject + 1;
            }
            return (reject / total) * 100;
        } catch (Exception e) {
            logger.error("Error al calcular porcentaje de sobrecarga en los enlaces: " + e.getMessage());
            throw new Exception();
        }
    }

    //Porcentaje de rechazos por sobrecarga de nodos
    private double rejectNode(List<Traffic> traffics) throws Exception {
        double reject = 0;
        double total = 0;
        try {
            for (Traffic traffic : traffics) {
                total = total + 1;
                if (!traffic.isProcessed() && traffic.getRejectLink()<traffic.getRejectNode())
                    reject = reject + 1;
            }
            return (reject / total) * 100;
        } catch (Exception e) {
            logger.error("Error al calcular porcentaje de sobrecarga en los nodos: " + e.getMessage());
            throw new Exception();
        }
    }

    //Porcentaje de atendidos de los vnfs
    private double attendVnfs(List<Traffic> traffics) throws Exception {
        double attend = 0;
        double total = 0;
        try {
            for (Traffic traffic : traffics) {
                total = total + traffic.getSfc().getVnfs().size();
                if (traffic.isProcessed())
                    attend = attend + traffic.getSfc().getVnfs().size();
            }
            return -((attend / total) * 100);
        } catch (Exception e) {
            logger.error("Error al calcular la cantidad de vnfs atendidos: " + e.getMessage());
            throw new Exception();
        }
    }



    public void writeSolutions(Solutions solutions) throws Exception {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(System.getProperty("app.home") + conf.getSolutionsFileName()));
            objectOutputStream = new ObjectOutputStream(fileOutputStream);

            String header = ";" +
            "Energy Cost(Dolar)" + ";" +
            "Bandwidth(MB)" + ";" +
            "Delay Cost" + ";" +
            "Load Traffic" + ";" +
            "Deploy Cost" + ";" +
            "Resources Cost(Dolar)" + ";" +
            "Fragmentation(Dolar)" + ";" +
            "All Links Cost(Dolar)" + ";" +
            "Max Use Link(MB)" + ";" +
            "Licences Cost(Dolar)" + ";" +
            "Slo Cost(Dolar)" + ";" +
            "Distance(KM)" + ";" +
            "Hops" + ";" +
            "Host Size" + ";" +
            "Number Instances" + ";" +
            "Throughput(%)" + ";" +
            "Attend Vnfs(%)" + ";" +
            "Reject-Link" + ";" +
            "Reject-Node" + "\n";

            objectOutputStream.writeObject(header);

            for (int i = 0; i < conf.getNumberSolutions(); i++) {
                String sb = ";" +
                solutions.getEnergyCostList().get(i) + ";" +
                solutions.getBandwidthList().get(i) + ";" +
                solutions.getDelayCostList().get(i) + ";" +
                solutions.getLoadTrafficList().get(i) + ";" +
                solutions.getDeployCostList().get(i) + ";" +
                solutions.getResourcesCostList().get(i) + ";" +
                solutions.getFragmentationList().get(i) + ";" +
                solutions.getAllLinksCostList().get(i) + ";" +
                solutions.getMaxUseLinkList().get(i) + ";" +
                solutions.getLicencesCostList().get(i) + ";" +
                solutions.getSloCostList().get(i) + ";" +
                solutions.getDistanceList().get(i) + ";" +
                solutions.getHopsList().get(i) + ";" +
                solutions.getHostSizeList().get(i) + ";" +
                solutions.getNumberInstancesList().get(i) + ";" +
                (solutions.getThroughputList().get(i)) + ";" +
                solutions.getAttendVnfs().get(i) + ";" +
                solutions.getRejectLink().get(i) + ";" +
                solutions.getRejectNode().get(i) + "\n";

                objectOutputStream.writeObject(sb);
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


}
