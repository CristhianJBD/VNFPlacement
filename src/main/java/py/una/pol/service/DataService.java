package py.una.pol.service;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.SimpleGraph;
import py.una.pol.dto.ShortestPath;
import py.una.pol.dto.NFVdto.*;
import py.una.pol.util.Configurations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class DataService {
    static Logger logger = Logger.getLogger(DataService.class);

    private static List<String> linksString;
    public static final Graph<Node, Link> graph = new SimpleGraph<>(Link.class);
    public static Map<String, List<ShortestPath>> shortestPathMap = new HashMap<>();
    public static Map<String, Node> nodes = new HashMap<>();
    public static Map<String, Node> nodesMap = new HashMap<>();
    public static Map<String, Link> linksMap = new HashMap<>();
    public static Map<String, VnfShared> vnfsShared = new HashMap<>();
    public static List<Vnf> vnfs = new ArrayList<>();
    public static Map<String, Server> servers = new HashMap<>();

    public DataService() throws Exception {
        loadData();
    }

    public static void loadData() throws Exception {
        try {
            logger.info("Valores iniciales: ");
            loadVnfsShared();
            loadVnfs();
            loadServers();
            loadNodes();
            loadLinks();
            loadGraph();
            kShortestPath();

        } catch (Exception e) {
            logger.error("Error al cargar los datos: " + e.getMessage());
            throw new Exception();
        }
    }


    private static void loadVnfsShared() throws Exception {
        BufferedReader reader = null;
        VnfShared vnfShared;
        String vnfLine;
        String[] vnfSplit;

        logger.info("VNFs: ");
        try {
            reader = new BufferedReader(new FileReader(System.getProperty("app.home") + Configurations.vnfsShareFileName));

            int i = 1;
            reader.readLine();
            while ((vnfLine = reader.readLine()) != null) {
                vnfSplit = vnfLine.split(" ");

                vnfShared = new VnfShared();
                vnfShared.setId(vnfSplit[0]);
                vnfShared.setDelay(Integer.parseInt(vnfSplit[1]));
                vnfShared.setDeploy(Integer.parseInt(vnfSplit[2]));
                vnfShared.setLicenceCost(Integer.parseInt(vnfSplit[3]));
                vnfShared.setBandwidthFactor(Double.parseDouble(vnfSplit[4]));
                vnfShared.setResourceCPU(Integer.parseInt(vnfSplit[5]));
                vnfShared.setResourceRAM(Integer.parseInt(vnfSplit[6]));
                vnfShared.setResourceStorage(Integer.parseInt(vnfSplit[7]));

                logger.info(i++ + " " + vnfShared.toString());
                vnfsShared.put(vnfShared.getId(), vnfShared);
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los VNFs: " + e.getMessage());
            throw new Exception();
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los VNFs Shared:" + e.getMessage());
            throw new Exception();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    private static void loadVnfs() throws Exception {
        BufferedReader reader = null;
        Vnf vnf;
        String vnfLine;
        String[] vnfSplit;
        logger.info("VNFs: ");
        try {
            reader = new BufferedReader(new FileReader(System.getProperty("app.home") + Configurations.vnfsSfcFileName));

            int i = 1;
            reader.readLine();
            while ((vnfLine = reader.readLine()) != null) {
                vnfSplit = vnfLine.split(" ");

                vnf = new Vnf();
                vnf.setId(vnfSplit[0]);
                vnf.setType(vnfSplit[1]);
                vnf.setResourceCPU(Integer.parseInt(vnfSplit[2]));
                vnf.setResourceRAM(Integer.parseInt(vnfSplit[3]));

                logger.info(i++ + " " + vnf.toString());
                vnfs.add(vnf);
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los VNFs: " + e.getMessage());
            throw new Exception();
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los VNFs:" + e.getMessage());
            throw new Exception();
        } finally {
            if (reader != null)
                reader.close();
        }
    }


    private static void loadServers() throws Exception {
        BufferedReader reader = null;
        Server server;
        String serverLine;
        String[] serverSplit;
        logger.info("Servidores: ");
        try {
            reader = new BufferedReader(new FileReader(System.getProperty("app.home") +
                    Configurations.networkPackage + Configurations.serversFileName));

            int i = 1;
            reader.readLine();
            while ((serverLine = reader.readLine()) != null) {
                serverSplit = serverLine.split(" ");

                server = new Server();
                server.setId(serverSplit[0].trim());
                server.setLicenceCost(Integer.parseInt(serverSplit[1]));
                server.setDeploy(Integer.parseInt(serverSplit[2]));
                server.setResourceCPU(Integer.parseInt(serverSplit[3]));
                server.setResourceRAM(Integer.parseInt(serverSplit[4]));
                server.setResourceStorage(Integer.parseInt(serverSplit[5]));
                server.setResourceCPUCost(Double.parseDouble(serverSplit[6]));
                server.setResourceRAMCost(Double.parseDouble(serverSplit[7]));
                server.setResourceStorageCost(Double.parseDouble(serverSplit[8]));
                server.setEnergyIdleWatts(Integer.parseInt(serverSplit[9]));
                server.setEnergyPeakWatts(Integer.parseInt(serverSplit[10]));

                logger.info(i++ + " " + server.toString());
                servers.put(server.getId(), server);
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los Servidores: " + e.getMessage());
            throw new Exception();
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los Servidores: " + e.getMessage());
            throw new Exception();
        } finally {
            if (reader != null)
                reader.close();
        }
    }


    private static void loadNodes() throws Exception {
        BufferedReader reader = null;
        Node node;
        String nodeString;
        String[] splitNode;
        logger.info("Nodos: ");
        try {
            reader = new BufferedReader(new FileReader(System.getProperty("app.home") +
                    Configurations.networkPackage + Configurations.nodesFileName));

            int i = 1;
            reader.readLine();
            while ((nodeString = reader.readLine()) != null) {
                splitNode = nodeString.split(" ");

                node = new Node();
                node.setId(splitNode[0].trim());
                node.setEnergyCost(Double.parseDouble(splitNode[1].trim()));
                if(splitNode.length == 3)
                    node.setServer(servers.get(splitNode[2].trim()));

                logger.info(i++ + " " + node.toString());
                nodesMap.put(node.getId(), node);
                nodes.put(node.getId(), node);
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los Nodos: " + e.getMessage());
            throw new Exception();
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los Nodos: " + e.getMessage());
            throw new Exception();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    private static void loadLinks() throws Exception {
        BufferedReader reader = null;
        String linkLine;
        try {
            reader = new BufferedReader(new FileReader(System.getProperty("app.home") +
                    Configurations.networkPackage + Configurations.linksFileName));

            reader.readLine();
            linksString = new ArrayList<>();
            while ((linkLine = reader.readLine()) != null)
                linksString.add(linkLine.trim());

        } catch (Exception e) {
            logger.error("Error al cargar las matrices: " + e.getMessage());
            throw new Exception();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    private static void loadGraph() throws Exception {
        Link link;
        try {
            for (Node node : nodes.values())
                graph.addVertex(node);

            logger.info("Enlaces: ");
            int i = 1;
            for (String linkString : linksString) {
                String[] linkSplit = linkString.split(" ");

                link = new Link();
                link.setId(linkSplit[0] + "-" + linkSplit[1] + "/" + linkSplit[1] + "-" + linkSplit[0]);
                link.setDelay(Integer.parseInt(linkSplit[2]));
                link.setDistance(Integer.parseInt(linkSplit[3]));
                link.setBandwidth(Double.parseDouble(linkSplit[4]));
                link.setBandwidthCost(Double.parseDouble(linkSplit[5]));

                linksMap.put(link.getId(), link);
                logger.info(i++ + " " + link.toString());
                graph.addEdge(nodes.get(linkSplit[0]), nodes.get(linkSplit[1]), link);
            }
            logger.info("Grafo: ");
            logger.info(graph.toString());
        } catch (Exception e) {
            logger.error("Error al cargar el Grafo: " + e.getMessage());
            throw new Exception();
        }
    }

    //Obtiene los k caminos mas cortos(por cantidad de saltos), de cada par de nodos
    private static void kShortestPath() throws Exception {
        try {
            KShortestPaths<Node, Link> pathInspector =
                    new KShortestPaths<>(graph, Configurations.k, Integer.MAX_VALUE);
            List<GraphPath<Node, Link>> paths;
            List<ShortestPath> kShortestPath;
            String nodeString, linkString;
            for (Node origin : nodes.values())
                for (Node destiny : nodes.values()) {
                    if (!origin.equals(destiny)) {
                        kShortestPath = new ArrayList<>();
                        //Obtiene los k caminos mas cortos entre dos pares de nodos
                        paths = pathInspector.getPaths(origin, destiny);
                        for (GraphPath<Node, Link> path : paths) {
                            ShortestPath shortestPath = new ShortestPath();
                            // Guarda el ID de la lista de nodos de los k caminos mas cortos
                            for (Node node : path.getVertexList()) {
                                nodeString = node.getId();
                                shortestPath.getNodes().add(nodeString);
                            }
                            // Guarda el ID de la lista de enlaces de los k caminos mas cortos
                            for (Link link : path.getEdgeList()) {
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
