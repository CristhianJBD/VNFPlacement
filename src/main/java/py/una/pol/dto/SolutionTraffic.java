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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Solution{");
        sb.append("energyCost=").append(energyCost);
        sb.append(", bandwidth=").append(bandwidth);
        sb.append(", loadTraffic=").append(loadTraffic);
        sb.append(", resourcesCost=").append(resourcesCost);
        sb.append(", fragmentation=").append(fragmentation);
        sb.append(", sloCost=").append(sloCost);
        sb.append(", licencesCost=").append(licencesCost);
        sb.append(", maxUseLink=").append(maxUseLink);
        sb.append(", delayCost=").append(delayCost);
        sb.append(", distance=").append(distance);
        sb.append(", numberInstances=").append(numberInstances);
        sb.append(", throughput=").append(throughput);
        sb.append('}');
        return sb.toString();
    }
}
