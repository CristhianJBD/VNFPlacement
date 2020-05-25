package py.una.pol.dto;

import lombok.Data;


@Data
public class Path {
   String id;
   ShortestPath shortestPath;

   public Path( ShortestPath shortestPath, String id) {
      this.id = id;
      this.shortestPath = shortestPath;
   }

   public Path(String id) {
      this.id = id;
   }
}
