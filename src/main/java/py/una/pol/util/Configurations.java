package py.una.pol.util;

import org.apache.log4j.Logger;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configurations {
    static Logger logger = Logger.getLogger(Configurations.class);

    //Parametros de Sistema
    public static String networkPackage;
    public static String linksFileName;
    public static String nodesFileName;
    public static String serversFileName;
    public static String vnfsSfcFileName;
    public static String vnfsShareFileName;
    public static String trafficsFileName;
    public static String solutionsFileName;
    public static Integer numberSolutions;
    public static Integer numberTraffic;
    public static Integer k;
    public static Integer retriesSolution;

    public static double serverPenaltyCPUCost;
    public static double serverPenaltyRAMCost;
    public static double serverPenaltyStorageCost;
    public static double linkPenaltyBandwidthCost;

    //Trafico
    public static boolean trafficsReadFile;
    public static boolean trafficsRandom;
    public static int trafficBandwidthMin;
    public static int trafficBandwidthMax;
    public static double trafficDelayMax;
    public static int trafficPenaltySloMin;
    public static int trafficPenaltySloMax;
    public static int trafficSfcMin;
    public static int trafficSfcMax;

    public Configurations() throws Exception {
        loadProperties();
    }

    public static void loadProperties() throws Exception {
        try (InputStream input = new FileInputStream(System.getProperty("app.home") + "/vnf_placement.properties")) {

            Properties prop = new Properties();

            prop.load(input);

            //Parametros de Sistema
            networkPackage = prop.getProperty("network.package");
            linksFileName = prop.getProperty("file.name.links");
            nodesFileName = prop.getProperty("file.name.nodes");
            serversFileName = prop.getProperty("file.name.servers");
            vnfsSfcFileName = prop.getProperty("file.name.vnfs.sfc");
            vnfsShareFileName = prop.getProperty("file.name.vnfs.share");
            trafficsFileName = prop.getProperty("file.name.traffics");
            solutionsFileName = prop.getProperty("file.name.solution");
            numberSolutions = Integer.valueOf(prop.getProperty("number.solution"));
            numberTraffic = Integer.valueOf(prop.getProperty("number.traffic"));
            k = Integer.valueOf(prop.getProperty("k.shortest"));
            retriesSolution = Integer.valueOf(prop.getProperty("retries.solution"));

            serverPenaltyCPUCost = Double.parseDouble(prop.getProperty("server.penalty.cpu.cost"));
            serverPenaltyRAMCost = Double.parseDouble(prop.getProperty("server.penalty.ram.cost"));
            serverPenaltyStorageCost = Double.parseDouble(prop.getProperty("server.penalty.storage.cost"));
            linkPenaltyBandwidthCost = Double.parseDouble(prop.getProperty("link.penalty.bandwidth.cost"));

            //Trafico
            trafficsReadFile = Boolean.parseBoolean(prop.getProperty("traffics.read.file"));
            trafficsRandom = Boolean.parseBoolean(prop.getProperty("traffics.random"));
            trafficBandwidthMin = Integer.parseInt(prop.getProperty("traffic.bandwidth.min"));
            trafficBandwidthMax = Integer.parseInt(prop.getProperty("traffic.bandwidth.max"));
            trafficDelayMax = Double.parseDouble(prop.getProperty("traffic.percentage.delay.max"));
            trafficPenaltySloMin = Integer.parseInt(prop.getProperty("traffic.penalty.slo.min"));
            trafficPenaltySloMax = Integer.parseInt(prop.getProperty("traffic.penalty.slo.max"));
            trafficSfcMin = Integer.parseInt(prop.getProperty("traffic.sfc.min"));
            trafficSfcMax = Integer.parseInt(prop.getProperty("traffic.sfc.max"));

        } catch (IOException ex) {
            logger.error("Error al cargar los datos del properties:" + ex.getMessage());
            throw new Exception();
        }
    }




}
