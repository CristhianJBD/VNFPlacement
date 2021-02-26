package py.una.pol;

import py.una.pol.service.DataService;
import py.una.pol.service.TrafficService;
import py.una.pol.util.Configurations;

public class Traffic {

    public static void main(String[] args) throws Exception {

        Configurations.loadProperties();
        DataService.loadData();
        TrafficService trafficService = new TrafficService();

        trafficService.generateRandomtraffic(DataService.nodesMap, DataService.vnfs);

    }

}
