package py.una.pol.dto;

import lombok.Data;

@Data
public class Cost {

    private String id;
    private double costNormalized;
    private ShortestPath shortestPath;
    private boolean free;

    private double energy;
    private int delay;
    private int distance;
    private double bandwidth;
    private int numberInstances;
    private double resources;
    private double licences;
    private double fragmentation;
    private double maximunUseLink;

    public Cost(Cost cost) {
        this.energy = cost.getEnergy();
        this.delay = cost.getDelay();
        this.distance = cost.getDistance();
        this.bandwidth = cost.getBandwidth();
        this.numberInstances = cost.getNumberInstances();
        this.resources = cost.getResources();
        this.licences = cost.getLicences();
        this.fragmentation = cost.getFragmentation();
        this.maximunUseLink = cost.getMaximunUseLink();
    }

    public Cost() {
    }
}
