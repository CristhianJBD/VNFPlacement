package py.una.pol.dto;

import lombok.Data;

import java.util.List;


@Data
public class KPath {
   private String id;
   private List<ShortestPath> kShortestPath;

   public KPath(List<ShortestPath> kShortestPath, String id) {
      this.id = id;
      this.kShortestPath = kShortestPath;
   }

   public KPath(String id) {
      this.id = id;
   }
}
