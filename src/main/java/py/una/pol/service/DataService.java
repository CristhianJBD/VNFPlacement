package py.una.pol.service;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.SimpleGraph;
import org.springframework.stereotype.Service;
import py.una.pol.dto.ShortestPath;
import py.una.pol.dto.NFVdto.*;
import py.una.pol.util.Configurations;
import py.una.pol.util.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

@Service()
public class DataService {
    Logger logger = Logger.getLogger(DataService.class);

    private final Configurations conf;

    private int[][] delay;
    private int[][] distance;
    private double[][] bandwidthCost;
    private double[][] bandwidth;
    private String[][] matrixNodes;
    private final Graph<Node, Link> graph = new SimpleGraph<>(Link.class);
    public Map<String, List<ShortestPath>> shortestPathMap = new HashMap<>();
    public List<Node> nodes = new ArrayList<>();
    public List<Link> links = new ArrayList<>();
    public Map<String, Node> nodesMap = new HashMap<>();
    public Map<String, Link> linksMap = new HashMap<>();
    public Map<String, VnfShared> vnfsShared = new HashMap<>();
    public List<Vnf> vnfs = new ArrayList<>();
    public List<Server> servers = new ArrayList<>();

    public DataService(Configurations conf) {
        this.conf = conf;
    }

    public void loadData() {
        try {
            logger.info("Valores iniciales: ");
            loadVnfsShared();
            loadVnfs();
            loadServers();
            loadNodes();
            readFile();
            loadGraph();
            kShortestPath();

        } catch (Exception e) {
            logger.error("Error al cargar los datos: " + e.getMessage());
        }
    }


    private void loadVnfsShared() throws Exception {
        VnfShared vnfShared;
        String separator = Constants.separatorData;
        logger.info("VNFs: ");
        try {
            String[] ids = conf.getVnfSharedId().split(separator);
            String[] delays = conf.getVnfSharedDelay().split(separator);
            String[] deploys = conf.getVnfSharedDeploy().split(separator);
            String[] cpus = conf.getVnfSharedResourceCPU().split(separator);
            String[] rams = conf.getVnfSharedResourceRAM().split(separator);
            String[] storages = conf.getVnfSharedResourceStorage().split(separator);
            String[] licences = conf.getVnfSharedLicenceCost().split(separator);
            String[] bandwidthFactor = conf.getVnfSharedBandwidthFactor().split(separator);

            for (int i = 0; i < conf.getVnfSharedSize(); i++) {
                vnfShared = new VnfShared();
                vnfShared.setId(ids[i]);
                vnfShared.setDelay(Integer.parseInt(delays[i]));
                vnfShared.setDeploy(Integer.parseInt(deploys[i]));
                vnfShared.setResourceCPU(Integer.parseInt(cpus[i]));
                vnfShared.setResourceRAM(Integer.parseInt(rams[i]));
                vnfShared.setResourceStorage(Integer.parseInt(storages[i]));
                vnfShared.setLicenceCost(Integer.parseInt(licences[i]));
                vnfShared.setBandwidthFactor(Double.parseDouble(bandwidthFactor[i]));

                logger.info(vnfShared.toString());
                vnfsShared.put(vnfShared.getId(), vnfShared);
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los VNFs: " + e.getMessage());
            throw new Exception();
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los VNFs:" + e.getMessage());
            throw new Exception();
        }
    }

    private void loadVnfs() throws Exception {
        Vnf vnf;
        String separator = Constants.separatorData;
        logger.info("VNFs: ");
        try {
            String[] ids = conf.getVnfId().split(separator);
            String[] cpus = conf.getVnfResourceCPU().split(separator);
            String[] rams = conf.getVnfResourceRAM().split(separator);

            for (int i = 0; i < conf.getVnfSize(); i++) {
                vnf = new Vnf();
                vnf.setId(ids[i]);
                vnf.setResourceCPU(Integer.parseInt(cpus[i]));
                vnf.setResourceRAM(Integer.parseInt(rams[i]));

                logger.info(vnf.toString());
                vnfs.add(vnf);
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los VNFs: " + e.getMessage());
            throw new Exception();
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los VNFs:" + e.getMessage());
            throw new Exception();
        }
    }


    private void loadServers() throws Exception {
        Server server;
        String separator = Constants.separatorData;
        logger.info("Servidores: ");
        try {
            String[] ids = conf.getServerId().split(separator);
            String[] licences = conf.getServerLicenceCost().split(separator);
            String[] deploys = conf.getServerDeploy().split(separator);
            String[] energyCosts = conf.getServerEnergyCost().split(separator);
            String[] cpus = conf.getServerResourceCPU().split(separator);
            String[] rams = conf.getServerResourceRAM().split(separator);
            String[] storages = conf.getServerResourceStorage().split(separator);
            String[] cpusCost = conf.getServerResourceCPUCost().split(separator);
            String[] ramsCost = conf.getServerResourceRAMCost().split(separator);
            String[] storagesCost = conf.getServerResourceStorageCost().split(separator);
            String[] energyPerCore = conf.getServerEnergyPerCoreWatts().split(separator);
            String[] energyPeaks = conf.getServerEnergyPeakWatts().split(separator);

            for (int i = 0; i < conf.getServerSize(); i++) {
                server = new Server();
                server.setId(ids[i]);
                server.setLicenceCost(Integer.parseInt(licences[i]));
                server.setDeploy(Integer.parseInt(deploys[i]));
                server.setEnergyCost(Double.parseDouble(energyCosts[i]));
                server.setResourceCPU(Integer.parseInt(cpus[i]));
                server.setResourceRAM(Integer.parseInt(rams[i]));
                server.setResourceStorage(Integer.parseInt(storages[i]));
                server.setResourceCPUCost(Double.parseDouble(cpusCost[i]));
                server.setResourceRAMCost(Double.parseDouble(ramsCost[i]));
                server.setResourceStorageCost(Double.parseDouble(storagesCost[i]));
                server.setEnergyPerCoreWatts(Integer.parseInt(energyPerCore[i]));
                server.setEnergyPeakWatts(Integer.parseInt(energyPeaks[i]));

                logger.info(server.toString());
                servers.add(server);
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los Servidores: " + e.getMessage());
            throw new Exception();
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los Servidores: " + e.getMessage());
            throw new Exception();
        }
    }


    private void loadNodes() throws Exception {
        Node node;
        String separator = Constants.separatorData;
        logger.info("Nodos: ");
        try {
            String[] ids = conf.getNodeId().split(separator);
            String[] energyCosts = conf.getNodeEnergyCost().split(separator);
            String[] nodeServer = conf.getNodeServer().split(separator);

            for (int i = 0; i < conf.getNodeSize(); i++) {
                node = new Node();
                node.setId(ids[i]);
                node.setEnergyCost(Double.parseDouble(energyCosts[i]));
                node.setServer(getServer(nodeServer[i]));

                logger.info(node.toString());
                nodesMap.put(node.getId(), node);
                nodes.add(node);
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los Nodos: " + e.getMessage());
            throw new Exception();
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los Nodos: " + e.getMessage());
            throw new Exception();
        }
    }


    private Server getServer(String serverId) throws Exception {
        if (serverId.equals(Constants.ZERO))
            return null;
        for (Server server : servers) {
            if (server.getId().equals(serverId)) {
                return server;
            }
        }

        throw new Exception("No se encontro el servidor con id: " + serverId + " en la lista de Servidores.");
    }


    private void readFile() throws Exception {
        logger.info("Matrices: ");
        try {
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(System.getProperty("app.home") + conf.getMatrixFileName()));

            int size = Integer.parseInt(reader.readLine());
            logger.info("Cantidad de nodos: " + size);

            logger.info(reader.readLine());
            matrixNodes = new String[size][size];
            for (int i = 0; i < size; i++) {
                String[] line = reader.readLine().split(" ");

                System.arraycopy(line, 0, matrixNodes[i], 0, size);
            }
            logger.info(Arrays.deepToString(matrixNodes));

            logger.info(reader.readLine());
            delay = new int[size][size];
            for (int i = 0; i < size; i++) {
                String[] line = reader.readLine().split(" ");

                for (int j = 0; j < size; j++) {
                    delay[i][j] = Integer.parseInt((line[j]));
                }
            }
            logger.info(Arrays.deepToString(delay));

            logger.info(reader.readLine());
            distance = new int[size][size];
            for (int i = 0; i < size; i++) {
                String[] line = reader.readLine().split(" ");

                for (int j = 0; j < size; j++) {
                    distance[i][j] = Integer.parseInt(line[j]);
                }
            }
            logger.info(Arrays.deepToString(distance));

            logger.info(reader.readLine());
            bandwidth = new double[size][size];
            for (int i = 0; i < size; i++) {
                String[] line = reader.readLine().split(" ");

                for (int j = 0; j < size; j++) {
                    bandwidth[i][j] = Double.parseDouble(line[j]);
                }
            }
            logger.info(Arrays.deepToString(bandwidth));

            logger.info(reader.readLine());
            bandwidthCost = new double[size][size];
            for (int i = 0; i < size; i++) {
                String[] line = reader.readLine().split(" ");

                for (int j = 0; j < size; j++) {
                    bandwidthCost[i][j] = Double.parseDouble(line[j]);
                }
            }
            logger.info(Arrays.deepToString(bandwidthCost));
        } catch (Exception e) {
            logger.error("Error al cargar las matrices: " + e.getMessage());
            throw new Exception();
        }
    }

    private void loadGraph() throws Exception {
        Link link;
        try {
            for (int i = 0; i < conf.getNodeSize(); i++) {
                graph.addVertex(nodes.get(i));
            }
            logger.info("Enlaces: ");
            for (int i = 0; i < conf.getNodeSize(); i++) {
                for (int j = 0; j < conf.getNodeSize(); j++) {
                    if (!matrixNodes[i][j].equals(Constants.ZERO)) {
                        link = new Link();
                        link.setId(nodes.get(i).getId() + "-" + nodes.get(j).getId() +
                                "/" + nodes.get(j).getId() + "-" + nodes.get(i).getId());
                        link.setBandwidth(bandwidth[i][j]);
                        link.setBandwidthCost(bandwidthCost[i][j]);
                        link.setDelay(delay[i][j]);
                        link.setDistance(distance[i][j]);

                        linksMap.put(link.getId(), link);
                        links.add(link);
                        logger.info(link.toString());

                        graph.addEdge(nodes.get(i), nodes.get(j), link);
                    }
                }
            }
            logger.info("Grafo: ");
            logger.info(graph.toString());
        } catch (Exception e) {
            logger.error("Error al cargar el Grafo: " + e.getMessage());
            throw new Exception();
        }
    }

    //Obtiene los k caminos mas cortos(por cantidad de saltos), de cada par de nodos
    private void kShortestPath() throws Exception {
        try {
            KShortestPaths<Node, Link> pathInspector =
                    new KShortestPaths<>(graph, conf.getK(), Integer.MAX_VALUE);
            List<GraphPath<Node, Link>> paths;
            List<ShortestPath> kShortestPath;
            String nodeString, linkString;
            for (Node origin : nodes)
                for (Node destiny : nodes) {
                    if (!origin.equals(destiny)) {
                        kShortestPath = new ArrayList<>();
                        //Obtiene los k caminos mas cortos entre dos pares de nodos
                        paths = pathInspector.getPaths(origin, destiny);
                        for(GraphPath<Node, Link> path : paths){
                            ShortestPath shortestPath = new ShortestPath();
                            // Guarda el ID de la lista de nodos de los k caminos mas cortos
                            for(Node node : path.getVertexList()){
                                nodeString = node.getId();
                                shortestPath.getNodes().add(nodeString);
                            }
                            // Guarda el ID de la lista de enlaces de los k caminos mas cortos
                            for(Link link : path.getEdgeList()){
                                linkString = link.getId();
                                shortestPath.getLinks().add(linkString);
                            }
                            kShortestPath.add(shortestPath);
                        }
                        //Se guarda en un Map los k caminos mas cortos de cada para de nodos del grafo
                        // donde el key es el origenId - destinoId
                        shortestPathMap.put(origin.getId() + "-" + destiny.getId(), kShortestPath);
                    }
                }

        } catch (Exception e) {
            logger.error("Error al cargar kShortestPath: " + e.getMessage());
            throw new Exception();
        }
    }
}
