package py.una.pol.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import py.una.pol.dto.Link;
import py.una.pol.dto.Node;
import py.una.pol.dto.Vnf;
import py.una.pol.util.Configurations;

import java.util.List;

@Service
public class ObjectiveFunctionService {
    Logger logger = Logger.getLogger(ObjectiveFunctionService.class);

    @Autowired
    private Configurations configuration;

    /*
    Costo de la Energia en Dolares = suma de los costos(dolares) de energia utilizados en los nodos mas
    la energia en watts utilizada en cada servidor por el costo de energia correspondiente al servidor
     */
    public Integer calculateEnergyCost(List<Node> nodes, Node originNode) throws Exception {
        int energyCost;
        try {
            //Costo de energia consumida en el Nodo origen
            energyCost = originNode.getEnergyCost();

            //Costo de energia consumida en el nodo mas energia consumida en el servidor donde se instalo el VNF
            for (Node node : nodes) {
                if (node.getServer() != null)
                    energyCost = energyCost + node.getEnergyCost() +
                            (node.getServer().getEnergyCost() * node.getServer().getEnergyUsed());

                    //Costo de energia consumida en el nodo donde no hay Servidor se encuentra en la ruta
                 else
                     energyCost = energyCost + node.getEnergyCost();
            }
            return energyCost;
        } catch (Exception e) {
            logger.error("Error al calcular la energia: " + e.getMessage());
            throw new Exception();
        }
    }

    /*
        Costo de Reenvio de trafico = suma del costo en dolar por el ancho de banda utilizado en cada enlace
        mas la energia total en dolares
     */
    public Integer calculateForwardingTrafficCost(List<Node> nodes, List<Link> links, Node originNode) throws Exception {
        int forwardingTrafficCost;
        try {
            //Energia Total en dolares
            forwardingTrafficCost = calculateEnergyCost(nodes, originNode);

            //Ancho de banda de cada enlace por el costo en dolares por ancho de banda de cada enlace
            for (Link link : links)
                forwardingTrafficCost = forwardingTrafficCost + (link.getBandwidthUsed() * link.getBandwidthCost());

            return forwardingTrafficCost;
        } catch (Exception e) {
            logger.error("Error al calcular la energia minima: " + e.getMessage());
            throw new Exception();
        }
    }

    public Integer calculateHostSize(List<Node> nodes) throws Exception {
        int hostSize = 0;
        try {
            //Numero de Servidores utilizados para instalar un VNF
            for (Node node : nodes)
                if (node.getServer() != null)
                    hostSize = hostSize + 1;

            return hostSize;
        } catch (Exception e) {
            logger.error("Error al calcular la cantidad de hosts utilizados: " + e.getMessage());
            throw new Exception();
        }
    }

    public Integer calculateDelayTotal(List<Node> nodes, List<Link> links) throws Exception {
        int latency = 0;
        try {
            //Suma del delay de procesamiento de cada VNF instalado
            for (Node node : nodes)
                if (node.getServer() != null)
                    for (Vnf vnf : node.getServer().getVnf())
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

    public Integer calculateDeployCost(List<Node> nodes) throws Exception {
        int deployCost = 0;
        try {
            //Suma del costo de deployar cada VNF
            for (Node node : nodes)
                if (node.getServer() != null)
                    for (Vnf vnf : node.getServer().getVnf())
                        deployCost = deployCost + vnf.getDeploy();

            return deployCost;
        } catch (Exception e) {
            logger.error("Error al calcular el costo de Intalacion o configuracion de los VNFs: " + e.getMessage());
            throw new Exception();
        }
    }

    public Integer calculateDistance(List<Link> links) throws Exception {
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

    public Integer calculateHops(List<Link> links) throws Exception {
        try {
            return links.size();
        } catch (Exception e) {
            logger.error("Error al calcular el numero de saltos: " + e.getMessage());
            throw new Exception();
        }
    }

    public Integer calculateLicencesCost(List<Node> nodes) throws Exception {
        int licencesCost = 0;
        try {
            for (Node node : nodes) {
                if (node.getServer() != null) {
                    //Suma del costo de licencia de cada VNF
                    for (Vnf vnf : node.getServer().getVnf())
                        licencesCost = licencesCost + vnf.getLicenceCost();

                    //Suma del costo de licencia de cada servidor
                    licencesCost = licencesCost + node.getServer().getLicenceCost();
                }
            }
            return licencesCost;
        } catch (Exception e) {
            logger.error("Error al calcular el costo de licencia total: " + e.getMessage());
            throw new Exception();
        }
    }

    public Integer calculateSLOCost(List<Node> nodes, List<Link> links, Integer sloCost, Integer delayMax) throws Exception {
        try {
            //Costo por superar el maximo delay
            if (delayMax > calculateDelayTotal(nodes, links)) {
                return sloCost;
            }else{
                return 0;
            }
        } catch (Exception e) {
            logger.error("Error al calcular el costo de SLO: " + e.getMessage());
            throw new Exception();
        }
    }

    public Integer calculateResourceFragmentation(List<Node> nodes, List<Link> links) throws Exception {
        int fragmentation = 0;
        try {
            //Costo de multa de cada recurso por el recurso que sobra de la capacidad total de cada Servidor
            for (Node node : nodes) {
                if (node.getServer() != null) {
                    fragmentation = fragmentation+
                            (node.getServer().getResourceCPU() - node.getServer().getResourceUsedCPU()) *
                                    configuration.getServerPenaltyCostCPU();
                    fragmentation = fragmentation+
                            (node.getServer().getResourceRAM() - node.getServer().getResourseUsedRAM()) *
                                    configuration.getServerPenaltyCostRAM();
                    fragmentation = fragmentation+
                            (node.getServer().getResourceStorage() - node.getServer().getResourseUsedStorage()) *
                                    configuration.getServerPenaltyCostStorage();
                }
            }

            //Costo de multa por el ancho banda que sobra de la capacidad total de cada enlace
            for (Link link : links)
                fragmentation = fragmentation +
                        (link.getBandwidth() - link.getBandwidthUsed()) * configuration.getLinkPenaltyCostBandwidth();

            return fragmentation;
        } catch (Exception e) {
            logger.error("Error al calcular la fragmentacion de recursos: " + e.getMessage());
            throw new Exception();
        }
    }

    public Integer calculateAllLinkCost(List<Link> links) throws Exception {
        int linksCost = 0;
        try {
            //suma del costo unitario * Ancho de banda requerido
            for (Link link : links)
                linksCost = linksCost + link.getBandwidth() * link.getBandwidthCost();

            return linksCost;
        } catch (Exception e) {
            logger.error("Error al calcular el costo de todos los enlaces: " + e.getMessage());
            throw new Exception();
        }
    }

    //se multiplica por el costo por unidad de ancho de band (por unidad de Mbit)?
    public Integer calculateBandwidth(List<Link> links) throws Exception {
        int bandwidth = 0;
        try {
            //Suma del ancho de banda de cada enlace de la ruta
            for (Link link : links)
                bandwidth = bandwidth + link.getBandwidth();

            return bandwidth;
        } catch (Exception e) {
            logger.error("Error al calcular el ancho de banda total: " + e.getMessage());
            throw new Exception();
        }
    }

    //no contempla los flujos que pasan para reutilizar los VNFs?
    public Integer calculateNumberIntances(List<Node> nodes) throws Exception {
        int instances = 0;
        try {
            for (Node node : nodes)
                if (node.getServer() != null)
                    instances = instances + node.getServer().getVnf().size();

            return instances;
        } catch (Exception e) {
            logger.error("Error al calcular el numero de instacias: " + e.getMessage());
            throw new Exception();
        }
    }

    //Verificar formula?
    public Integer calculateLoadTraffic(List<Node> nodes, List<Link> links) throws Exception {
        int delayTotal;
        int bandwidth = 0;
        int loadTraffic;
        try {
            //Delay total
            delayTotal = calculateDelayTotal(nodes, links);

            //Ancho de Banda total
            for (Link link : links)
                bandwidth = bandwidth + link.getBandwidth();

            loadTraffic = bandwidth * delayTotal;
            return loadTraffic;
        } catch (Exception e) {
            logger.error("Error al calcular el Trafico de Carga: " + e.getMessage());
            throw new Exception();
        }
    }

    //Se suman todos juntos los recursos?
    public Integer calculateResources(List<Node> nodes) throws Exception {
        int resourceCPU = 0;
        int resourceRAM = 0;
        int resourceStorage = 0;
        int resourceTotal;
        try {
            //Suma de los recursos utilizados de cada servidor
            for (Node node : nodes)
                if (node.getServer() != null) {
                    resourceCPU = resourceCPU + node.getServer().getResourceUsedCPU();
                    resourceRAM = resourceRAM + node.getServer().getResourseUsedRAM();
                    resourceStorage = resourceStorage + node.getServer().getResourseUsedStorage();
                }

            resourceTotal = resourceCPU + resourceRAM + resourceStorage;
            return resourceTotal;
        } catch (Exception e) {
            logger.error("Error al calcular el consumo total de Recursos: " + e.getMessage());
            throw new Exception();
        }
    }

    //Como calcular la maxima utlizacion del enlace?
    public Integer calculateMaximunUseLink(List<Link> links) throws Exception {
        int maximunUseLink = 0;
        try {
            for (Link link : links) {

            }
            return maximunUseLink;
        } catch (Exception e) {
            logger.error("Error al calcular la maxima utilizacion del enlace: " + e.getMessage());
            throw new Exception();
        }
    }


    //Seria lo mismo que calcular el ancho de banda?
    public Integer calculateThroughput(List<Link> links) throws Exception {
        int throughput = 0;
        try {
            for (Link link : links) {

            }
            return throughput;
        } catch (Exception e) {
            logger.error("Error al calcular el throughput: " + e.getMessage());
            throw new Exception();
        }
    }
}
