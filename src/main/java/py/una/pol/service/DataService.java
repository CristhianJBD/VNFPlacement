package py.una.pol.service;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import py.una.pol.dto.NFVdto.Link;
import py.una.pol.dto.NFVdto.Node;
import py.una.pol.dto.NFVdto.Server;
import py.una.pol.dto.NFVdto.Vnf;
import py.una.pol.util.Configurations;
import py.una.pol.util.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component()
public class DataService {
    Logger logger = Logger.getLogger(DataService.class);

    @Autowired
    private Configurations configuration;

    public List<Vnf> vnfs = new ArrayList<>();
    public List<Server> servers = new ArrayList<>();
    public List<Node> nodes = new ArrayList<>();
    public List<Link> links = new ArrayList<>();
    private int[][] delay;
    private int[][] distance;
    private double[][] bandwidthCost;
    private double[][] bandwidth;
    private String[][] matrixNodes;
    DirectedGraph<Node, Link> graph = new DefaultDirectedGraph<>(Link.class);

    public void loadData() {
        try {
            logger.info("Valores iniciales: ");
            loadVnfs();
            loadServers();
            loadNodes();
            readFile();
            loadGraph();

        }catch (Exception e){
            logger.error("Error al cargar los datos: " + e.getMessage());
        }
    }


    private void loadVnfs() throws Exception {
        Vnf vnf;
        String separator = configuration.getSeparator();
        logger.info("VNFs: ");
        try {
            String[] ids = configuration.getVnfId().split(separator);
            String[] delays = configuration.getVnfDelay().split(separator);
            String[] deploys = configuration.getVnfDeploy().split(separator);
            String[] cpus = configuration.getVnfResourceCPU().split(separator);
            String[] rams = configuration.getVnfResourceRAM().split(separator);
            String[] storages = configuration.getVnfResourceStorage().split(separator);
            String[] licences = configuration.getVnfLicenceCost().split(separator);
            String[] bandwidthFactor = configuration.getVnfBandwidthFactor().split(separator);

            for (int i = 0; i < configuration.getVnfSize(); i++) {
                vnf = new Vnf();
                vnf.setId(ids[i]);
                vnf.setDelay(Integer.valueOf(delays[i]));
                vnf.setDeploy(Integer.valueOf(deploys[i]));
                vnf.setResourceCPU(Integer.valueOf(cpus[i]));
                vnf.setResourceRAM(Integer.valueOf(rams[i]));
                vnf.setResourceStorage(Integer.valueOf(storages[i]));
                vnf.setLicenceCost(Integer.valueOf(licences[i]));
                vnf.setBandwidthFactor(Double.valueOf(bandwidthFactor[i]));

                logger.info(vnf.toString());
                vnfs.add(vnf);
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los VNFs: "+ e.getMessage());
            throw new Exception();
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los VNFs:" + e.getMessage());
            throw new Exception();
        }
    }


    private void loadServers() throws Exception {
        Server server;
        String separator = configuration.getSeparator();
        logger.info("Servidores: ");
        try {
            String[] ids = configuration.getServerId().split(separator);
            String[] licences = configuration.getServerLicenceCost().split(separator);
            String[] energyCosts = configuration.getServerEnergyCost().split(separator);
            String[] cpus = configuration.getServerResourceCPU().split(separator);
            String[] rams = configuration.getServerResourceRAM().split(separator);
            String[] storages = configuration.getServerResourceStorage().split(separator);
            String[] cpusCost = configuration.getServerResourceCPUCost().split(separator);
            String[] ramsCost = configuration.getServerResourceRAMCost().split(separator);
            String[] storagesCost = configuration.getServerResourceStorageCost().split(separator);
            String[] energyPerCore = configuration.getServerEnergyPerCoreWatts().split(separator);
            String[] energyPeaks = configuration.getServerEnergyPeakWatts().split(separator);

            for (int i = 0; i < configuration.getServerSize(); i++) {
                server = new Server();
                server.setId(ids[i]);
                server.setLicenceCost(Integer.valueOf(licences[i]));
                server.setEnergyCost(Double.valueOf(energyCosts[i]));
                server.setResourceCPU(Integer.valueOf(cpus[i]));
                server.setResourceRAM(Integer.valueOf(rams[i]));
                server.setResourceStorage(Integer.valueOf(storages[i]));
                server.setResourceCPUCost(Double.valueOf(cpusCost[i]));
                server.setResourceRAMCost(Double.valueOf(ramsCost[i]));
                server.setResourceStorageCost(Double.valueOf(storagesCost[i]));
                server.setEnergyPerCoreWatts(Integer.valueOf(energyPerCore[i]));
                server.setEnergyPeakWatts(Integer.valueOf(energyPeaks[i]));

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
        String separator = configuration.getSeparator();
        logger.info("Nodos: ");
        try {
            String[] ids = configuration.getNodeId().split(separator);
            String[] energyCosts = configuration.getNodeEnergyCost().split(separator);
            String[] nodeServer = configuration.getNodeServer().split(separator);

            for (int i = 0; i < configuration.getNodeSize(); i++) {
                node = new Node();
                node.setId(ids[i]);
                node.setEnergyCost(Double.valueOf(energyCosts[i]));
                node.setServer(getServer(nodeServer[i]));

                logger.info(node.toString());
                nodes.add(node);
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los Nodos: " + e.getMessage());
            throw new Exception();
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los Nodos: " +  e.getMessage());
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
            reader = new BufferedReader(new FileReader(System.getProperty("app.home") + configuration.getMatrixName()));

            int size = Integer.parseInt(reader.readLine());
            logger.info("Cantidad de nodos: " + size);

            logger.info(reader.readLine());
            matrixNodes = new String[size][size];
            for (int i = 0; i < size; i++) {
                String[] line = reader.readLine().split(" ");

                for (int j = 0; j < size; j++) {
                    matrixNodes[i][j] = line[j];
                }
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
                    bandwidth[i][j] = Double.valueOf(line[j]);
                }
            }
            logger.info(Arrays.deepToString(bandwidth));

            logger.info(reader.readLine());
            bandwidthCost = new double[size][size];
            for (int i = 0; i < size; i++) {
                String[] line = reader.readLine().split(" ");

                for (int j = 0; j < size; j++) {
                    bandwidthCost[i][j] = Double.valueOf(line[j]);
                }
            }
            logger.info(Arrays.deepToString(bandwidthCost));
        }catch (Exception e) {
            logger.error("Error al cargar las matrices: " + e.getMessage());
            throw new Exception();
        }
    }

    private void loadGraph() throws Exception {
        Link link;
        try {
            for (int i = 0; i < configuration.getNodeSize(); i++) {
                graph.addVertex(nodes.get(i));
            }
            logger.info("Enlaces: ");
            for (int i = 0; i < configuration.getNodeSize(); i++) {
                for (int j = 0; j < configuration.getNodeSize(); j++) {
                    if (!matrixNodes[i][j].equals(Constants.ZERO)) {
                        link = new Link();
                        link.setId(nodes.get(i).getId()+"-"+nodes.get(j).getId());
                        link.setBandwidth(bandwidth[i][j]);
                        link.setBandwidthCost(bandwidthCost[i][j]);
                        link.setDelay(delay[i][j]);
                        link.setDistance(distance[i][j]);

                        links.add(link);
                        logger.info(link.toString());

                        graph.addEdge(nodes.get(i),nodes.get(j),link);
                    }
                }
            }
            logger.info("Grafo: ");
            logger.info(graph.toString());
        }catch (Exception e){
            logger.error("Error al cargar el Grafo: " + e.getMessage());
            throw new Exception();
        }
    }
}
