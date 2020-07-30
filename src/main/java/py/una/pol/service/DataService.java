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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

@Service()
public class DataService {
    Logger logger = Logger.getLogger(DataService.class);

    private final Configurations conf;

    private List<String> linksString;
    public final Graph<Node, Link> graph = new SimpleGraph<>(Link.class);
    public Map<String, List<ShortestPath>> shortestPathMap = new HashMap<>();
    public Map<String, Node> nodes = new HashMap<>();
    public Map<String, Node> nodesMap = new HashMap<>();
    public Map<String, Link> linksMap = new HashMap<>();
    public Map<String, VnfShared> vnfsShared = new HashMap<>();
    public List<Vnf> vnfs = new ArrayList<>();
    public Map<String, Server> servers = new HashMap<>();

    public DataService(Configurations conf) {
        this.conf = conf;
    }

    public void loadData() throws Exception {
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


    private void loadVnfsShared() throws Exception {
        BufferedReader reader = null;
        VnfShared vnfShared;
        String vnfLine;
        String[] vnfSplit;

        logger.info("VNFs: ");
        try {
            reader = new BufferedReader(new FileReader(System.getProperty("app.home") + conf.getVnfsShareFileName()));

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

                logger.info(vnfShared.toString());
                vnfsShared.put(vnfShared.getId(), vnfShared);
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

    private void loadVnfs() throws Exception {
        BufferedReader reader = null;
        Vnf vnf;
        String vnfLine;
        String[] vnfSplit;
        logger.info("VNFs: ");
        try {
            reader = new BufferedReader(new FileReader(System.getProperty("app.home") + conf.getVnfsSfcFileName()));

            reader.readLine();
            while ((vnfLine = reader.readLine()) != null) {
                vnfSplit = vnfLine.split(" ");

                vnf = new Vnf();
                vnf.setId(vnfSplit[0]);
                vnf.setType(vnfSplit[1]);
                vnf.setResourceCPU(Integer.parseInt(vnfSplit[2]));
                vnf.setResourceRAM(Integer.parseInt(vnfSplit[3]));

                logger.info(vnf.toString());
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


    private void loadServers() throws Exception {
        BufferedReader reader = null;
        Server server;
        String serverLine;
        String[] serverSplit;
        logger.info("Servidores: ");
        try {
            reader = new BufferedReader(new FileReader(System.getProperty("app.home") + conf.getServersFileName()));

            reader.readLine();
            while ((serverLine = reader.readLine()) != null) {
                serverSplit = serverLine.split(" ");

                server = new Server();
                server.setId(serverSplit[0].trim());
                server.setLicenceCost(Integer.parseInt(serverSplit[1]));
                server.setEnergyCost(Double.parseDouble(serverSplit[2]));
                server.setDeploy(Integer.parseInt(serverSplit[3]));
                server.setResourceCPU(Integer.parseInt(serverSplit[4]));
                server.setResourceRAM(Integer.parseInt(serverSplit[5]));
                server.setResourceStorage(Integer.parseInt(serverSplit[6]));
                server.setResourceCPUCost(Double.parseDouble(serverSplit[7]));
                server.setResourceRAMCost(Double.parseDouble(serverSplit[8]));
                server.setResourceStorageCost(Double.parseDouble(serverSplit[9]));
                server.setEnergyIdleWatts(Integer.parseInt(serverSplit[10]));
                server.setEnergyPeakWatts(Integer.parseInt(serverSplit[11]));

                logger.info(server.toString());
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


    private void loadNodes() throws Exception {
        BufferedReader reader = null;
        Node node;
        String nodeString;
        String[] splitNode;
        logger.info("Nodos: ");
        try {
            reader = new BufferedReader(new FileReader(System.getProperty("app.home") + conf.getNodesFileName()));

            reader.readLine();
            while ((nodeString = reader.readLine()) != null) {
                splitNode = nodeString.split(" ");

                node = new Node();
                node.setId(splitNode[0].trim());
                node.setEnergyCost(Double.parseDouble(splitNode[1].trim()));
                node.setServer(servers.get(splitNode[2].trim()));

                logger.info(node.toString());
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

    private void loadLinks() throws Exception {
        BufferedReader reader = null;
        String linkLine;
        try {
            reader = new BufferedReader(new FileReader(System.getProperty("app.home") + conf.getLinksFileName()));

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

    private void loadGraph() throws Exception {
        Link link;
        try {
            for (Node node : nodes.values())
                graph.addVertex(node);

            logger.info("Enlaces: ");
            for (String linkString : linksString) {
                String[] linkSplit = linkString.split(" ");

                link = new Link();
                link.setId(linkSplit[0] + "-" + linkSplit[1] + "/" + linkSplit[1] + "-" + linkSplit[0]);
                link.setDelay(Integer.parseInt(linkSplit[2]));
                link.setDistance(Integer.parseInt(linkSplit[3]));
                link.setBandwidth(Double.parseDouble(linkSplit[4]));
                link.setBandwidthCost(Double.parseDouble(linkSplit[5]));

                linksMap.put(link.getId(), link);
                logger.info(link.toString());
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
    private void kShortestPath() throws Exception {
        try {
            KShortestPaths<Node, Link> pathInspector =
                    new KShortestPaths<>(graph, conf.getK(), Integer.MAX_VALUE);
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

    public double getDelayMax(SFC sfc, String originId, String destinyId) throws Exception {
        try {
            List<GraphPath<Node, Link>> paths;
            double delayMin = 0;

            KShortestPaths<Node, Link> pathInspector =
                    new KShortestPaths<>(graph, 1, Integer.MAX_VALUE);

            paths = pathInspector.getPaths(nodesMap.get(originId), nodesMap.get(destinyId));

            for (Vnf vnf : sfc.getVnfs())
                delayMin = delayMin + vnfsShared.get(vnf.getId()).getDelay();

            if (paths != null && paths.size() > 0)
                for (Link link : paths.get(0).getEdgeList())
                    delayMin = delayMin + link.getDelay();

            return delayMin + (delayMin * (conf.getTrafficDelayMax() / 100));
        }catch (Exception e){
            logger.error("Error al calcular el delay maximo: " + e.getMessage());
            throw new Exception();
        }
    }
}
