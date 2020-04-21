package py.una.pol.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ObjectiveFunctionsSolutions {

    private List<Double> energyCostList = new ArrayList<>();
    private List<Double> forwardingTrafficCostList = new ArrayList<>();
    private List<Double> bandwidthList = new ArrayList<>();
    private List<Double> loadTrafficList = new ArrayList<>();
    private List<Double> resourcesCostList = new ArrayList<>();
    private List<Double> fragmentationList = new ArrayList<>();
    private List<Double> sloCostList = new ArrayList<>();
    private List<Double> allLinksCostList = new ArrayList<>();
    private List<Double> licencesCostList = new ArrayList<>();
    private List<Double> maxUseLinkList = new ArrayList<>();
    private List<Integer> hostSizeList = new ArrayList<>();
    private List<Integer> delayCostList = new ArrayList<>();
    private List<Integer> deployCostList = new ArrayList<>();
    private List<Integer> distanceList = new ArrayList<>();
    private List<Integer> hopsList = new ArrayList<>();
    private List<Integer> numberInstancesList = new ArrayList<>();
    private List<Integer> throughputList = new ArrayList<>();


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Solutions{").append("\n");
        sb.append("RnergyCostList=").append(energyCostList).append("\n");
        sb.append("ForwardingTrafficCostList=").append(forwardingTrafficCostList).append("\n");
        sb.append("BandwidthList=").append(bandwidthList).append("\n");
        sb.append("LoadTrafficList=").append(loadTrafficList).append("\n");
        sb.append("ResourcesCostList=").append(resourcesCostList).append("\n");
        sb.append("FragmentationList=").append(fragmentationList).append("\n");
        sb.append("SloCostList=").append(sloCostList).append("\n");
        sb.append("AllLinksCostList=").append(allLinksCostList).append("\n");
        sb.append("LicencesCostList=").append(licencesCostList).append("\n");
        sb.append("MaxUseLinkList=").append(maxUseLinkList).append("\n");
        sb.append("HostSizeList=").append(hostSizeList).append("\n");
        sb.append("DelayCostList=").append(delayCostList).append("\n");
        sb.append("DeployCostList=").append(deployCostList).append("\n");
        sb.append("DistanceList=").append(distanceList).append("\n");
        sb.append("HopsList=").append(hopsList).append("\n");
        sb.append("NumberInstancesList=").append(numberInstancesList).append("\n");
        sb.append("ThroughputList=").append(throughputList).append("\n");
        sb.append('}');
        return sb.toString();
    }
}
