package py.una.pol.dto;


import lombok.Data;

@Data
public class Path {
    private String id;

    private ShortestPath shortestPath;

    public Path(String id, ShortestPath shortestPath) {
        this.id = id;
        this.shortestPath = shortestPath;
    }
}
