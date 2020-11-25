package py.una.pol.dto;

import lombok.Data;

@Data
public class SolutionTraffic {

    private Double energyCost;
    private Double bandwidth;
    private Double loadTraffic;
    private Double resourcesCost;
    private Double fragmentation;
    private Double sloCost;
    private Double licencesCost;
    private Double maxUseLink;
    private Integer delayCost;
    private Integer distance;
    private Integer numberInstances;
    private Double throughput;


}
