package py.una.pol.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShortestPath implements Serializable {
    private static final long serialVersionUID = 8889365184884886072L;

    private List<String> nodes = new ArrayList<>();
    private List<String> links = new ArrayList<>();
}
