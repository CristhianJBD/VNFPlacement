package py.una.pol.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ShortestPath {
    List<String> nodes = new ArrayList<>();
    List<String> links = new ArrayList<>();
}
