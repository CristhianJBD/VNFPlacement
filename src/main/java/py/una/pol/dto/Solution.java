package py.una.pol.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Solution {

    private List<String> energyCostList = new ArrayList<>();
    private List<String> forwardingTrafficCostList = new ArrayList<>();
    private List<String> bandwidthList = new ArrayList<>();
    private List<String> loadTrafficList = new ArrayList<>();
    private List<String> resourcesCostList = new ArrayList<>();
    private List<String> fragmentationList = new ArrayList<>();
    private List<String> sloCostList = new ArrayList<>();
    private List<String> allLinksCostList = new ArrayList<>();
    private List<String> licencesCostList = new ArrayList<>();
    private List<String> maxUseLinkList = new ArrayList<>();
    private List<Integer> hostSizeList = new ArrayList<>();
    private List<Integer> delayCostList = new ArrayList<>();
    private List<Integer> deployCostList = new ArrayList<>();
    private List<Integer> distanceList = new ArrayList<>();
    private List<Integer> hopsList = new ArrayList<>();
    private List<Integer> numberInstancesList = new ArrayList<>();
    private List<String> throughputList = new ArrayList<>();


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Solutions").append("\n");
        sb.append("Energy Cost=").append(energyCostList).append("\n");
        sb.append("Forwarding Traffic Cost=").append(forwardingTrafficCostList).append("\n");
        sb.append("Bandwidth=").append(bandwidthList).append("\n");
        sb.append("Delay Cost=").append(delayCostList).append("\n");
        sb.append("Load Traffic=").append(loadTrafficList).append("\n");
        sb.append("Deploy Cost=").append(deployCostList).append("\n");
        sb.append("Resources Cost=").append(resourcesCostList).append("\n");
        sb.append("Fragmentation=").append(fragmentationList).append("\n");
        sb.append("All Links Cost=").append(allLinksCostList).append("\n");
        sb.append("Max Use Link=").append(maxUseLinkList).append("\n");
        sb.append("Licences Cost=").append(licencesCostList).append("\n");
        sb.append("Slo Cost=").append(sloCostList).append("\n");
        sb.append("Distance=").append(distanceList).append("\n");
        sb.append("Hops=").append(hopsList).append("\n");
        sb.append("Host Size=").append(hostSizeList).append("\n");
        sb.append("Number Instances=").append(numberInstancesList).append("\n");
        sb.append("Throughput=").append(throughputList).append("\n");
        return sb.toString();
    }
}
