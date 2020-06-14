package py.una.pol.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ShortestPath {
    private List<String> nodes = new ArrayList<>();
    private List<String> links = new ArrayList<>();
}
