package py.una.pol.dto;

import py.una.pol.dto.NFVdto.Link;
import py.una.pol.dto.NFVdto.Node;

import java.io.Serializable;
import java.util.Map;

public class ResultGraphMap implements Serializable {

    private static final long serialVersionUID = -8626797840944687981L;

    private Map<String, Node> nodesMap;
    private Map<String, Link> linksMap;

    public Map<String, Node> getNodesMap() {
        return nodesMap;
    }

    public void setNodesMap(Map<String, Node> nodesMap) {
        this.nodesMap = nodesMap;
    }

    public Map<String, Link> getLinksMap() {
        return linksMap;
    }

    public void setLinksMap(Map<String, Link> linksMap) {
        this.linksMap = linksMap;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResultGraphMap{");
        sb.append("nodesMap=").append(nodesMap);
        sb.append(", linksMap=").append(linksMap);
        sb.append('}');
        return sb.toString();
    }
}
