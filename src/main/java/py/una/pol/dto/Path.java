package py.una.pol.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class Path implements Serializable {
    private static final long serialVersionUID = 3054912281173878521L;

    private String id;

    private ShortestPath shortestPath;

    public Path(String id, ShortestPath shortestPath) {
        this.id = id;
        this.shortestPath = shortestPath;
    }

    public Path(String origin, String destiny, ShortestPath shortestPath) {
        this.id = origin + "-" + destiny;
        this.shortestPath = shortestPath;
    }
}
